package com.iusofts.agentplus.ailog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iusofts.agentplus.ailog.config.TraceSampleProperties;
import com.iusofts.agentplus.ailog.entity.AiTraceSampleConfig;
import com.iusofts.agentplus.ailog.interfaces.IAiTraceSampleConfigService;
import com.iusofts.agentplus.ailog.mapper.AiTraceSampleConfigMapper;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigListVo;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigPageReqVo;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigVo;
import com.iusofts.agentplus.basic.exception.SystemBusinessException;
import com.iusofts.agentplus.basic.utils.ModelMapperUtil;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import jakarta.annotation.PostConstruct;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI Trace 采样率配置 Service 实现。
 *
 * <p>负责配置 CRUD 与运行时采样率解析:
 * <ol>
 *   <li>配置写入 DB 时同步更新内存缓存</li>
 *   <li>解析时按 优先级链 用户级 → 组织级 → 全局级 → yml 兜底(default-sample-rate)
 *       命中且 status=1 才返回,否则继续向下找</li>
 * </ol>
 *
 * <p>缓存设计:每个作用域维护最近一次 DB 命中值,避免每个 span 都查库。
 * 缓存失效受 {@code agent-plus.trace.sample.cache-ttl-seconds} 控制,
 * 设为 0 时退化为每次解析都查库。</p>
 *
 * @author Ivan
 * @since 2026-08-10
 */
@Service
@EnableConfigurationProperties(TraceSampleProperties.class)
public class AiTraceSampleConfigServiceImpl implements IAiTraceSampleConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiTraceSampleConfigServiceImpl.class);

    @Autowired
    private AiTraceSampleConfigMapper mapper;

    @Autowired
    private TraceSampleProperties props;

    /**
     * Redisson 客户端。{@link ObjectProvider} 包装,允许集群下未部署 Redis 时降级(本地缓存照常工作)。
     */
    @Autowired
    private ObjectProvider<RedissonClient> redissonClientProvider;

    /** 集群缓存失效广播通道,所有实例订阅并刷新本地缓存。 */
    private static final String INVALIDATE_TOPIC = "ai:trace:sample-config:invalidate";

    /**
     * 作用域 → 缓存条目,key 形如 {@code 3:10086} (type:targetId)。
     */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 是否存在 user(org)级配置覆盖。由 {@link #refreshHasUserOrOrgOverride()}
     * 在 cache 变更后维护,供 {@code AiTraceSampleService} 做 short-circuit 优化。
     */
    private volatile boolean hasUserOrOrgOverride = false;

    // ============================================================
    //  启动初始化
    // ============================================================

    @PostConstruct
    public void init() {
        try {
            loadAllEnabledToCache();
            LOGGER.info("AI Trace 采样率配置缓存初始化完成, 共 {} 条", cache.size());
        } catch (Exception e) {
            // 启动期 DB 不可用不应阻断应用,记日志后继续
            LOGGER.warn("AI Trace 采样率配置初始化失败, 运行时将降级为 yml 兜底", e);
        }
        subscribeInvalidateTopic();
    }

    /**
     * 启动时把全部启用配置拉入缓存,避免首请求穿透。
     */
    private void loadAllEnabledToCache() {
        List<AiTraceSampleConfig> all = mapper.selectList(
                new LambdaQueryWrapper<AiTraceSampleConfig>()
                        .eq(AiTraceSampleConfig::getDeleteFlag, 0)
                        .eq(AiTraceSampleConfig::getStatus, 1));
        for (AiTraceSampleConfig c : all) {
            putCache(c);
        }
    }

    // ============================================================
    //  CRUD
    // ============================================================

    @Override
    public PageResult<AiTraceSampleConfigListVo> pageConfig(AiTraceSampleConfigPageReqVo reqVo) {
        PageResult<AiTraceSampleConfigListVo> result = new PageResult<>();
        Page<AiTraceSampleConfig> page = new Page<>(reqVo.getCurrentPage(), reqVo.getPageSize());

        LambdaQueryWrapper<AiTraceSampleConfig> qw = new LambdaQueryWrapper<AiTraceSampleConfig>()
                .eq(AiTraceSampleConfig::getDeleteFlag, 0)
                .orderByDesc(AiTraceSampleConfig::getCreateTime);

        if (reqVo.getConfigType() != null) {
            qw.eq(AiTraceSampleConfig::getConfigType, reqVo.getConfigType());
        }
        if (reqVo.getTargetId() != null) {
            qw.eq(AiTraceSampleConfig::getTargetId, reqVo.getTargetId());
        }
        if (reqVo.getStatus() != null) {
            qw.eq(AiTraceSampleConfig::getStatus, reqVo.getStatus());
        }
        if (StringUtils.hasText(reqVo.getRemark())) {
            qw.like(AiTraceSampleConfig::getRemark, reqVo.getRemark());
        }

        Page<AiTraceSampleConfig> pageResult = mapper.selectPage(page, qw);

        List<AiTraceSampleConfigListVo> dataList = pageResult.getRecords().stream()
                .map(this::toListVo)
                .toList();
        result.setDataList(dataList);
        result.setTotalCount(pageResult.getTotal());
        return result;
    }

    @Override
    public AiTraceSampleConfigVo getById(Long id) {
        AiTraceSampleConfig entity = mapper.selectById(id);
        if (entity == null || entity.getDeleteFlag() != null && entity.getDeleteFlag() == 1) {
            throw new SystemBusinessException("采样率配置不存在或已删除");
        }
        return ModelMapperUtil.strictMap(entity, AiTraceSampleConfigVo.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addConfig(AiTraceSampleConfigVo vo) {
        validateScope(vo.getConfigType(), vo.getTargetId(), null);

        AiTraceSampleConfig entity = new AiTraceSampleConfig();
        entity.setConfigType(vo.getConfigType());
        entity.setTargetId(resolveTargetId(vo.getConfigType(), vo.getTargetId()));
        entity.setSampleRate(vo.getSampleRate());
        entity.setStatus(vo.getStatus() == null ? 1 : vo.getStatus());
        entity.setRemark(vo.getRemark());
        entity.setDeleteFlag(0);
        Long uid = vo.getCurrentUserId() == null ? 0L : vo.getCurrentUserId();
        entity.setCreateBy(uid);
        entity.setUpdateBy(uid);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());

        int row = mapper.insert(entity);
        if (row > 0 && entity.getStatus() != null && entity.getStatus() == 1) {
            putCache(entity);
        }
        // 广播集群失效,其他实例刷新本地缓存
        publishInvalidate();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(AiTraceSampleConfigVo vo) {
        if (vo.getId() == null) {
            throw new SystemBusinessException("主键ID不能为空");
        }
        AiTraceSampleConfig exist = mapper.selectById(vo.getId());
        if (exist == null || exist.getDeleteFlag() != null && exist.getDeleteFlag() == 1) {
            throw new SystemBusinessException("采样率配置不存在或已删除");
        }

        validateScope(vo.getConfigType(), vo.getTargetId(), vo.getId());

        exist.setConfigType(vo.getConfigType());
        exist.setTargetId(resolveTargetId(vo.getConfigType(), vo.getTargetId()));
        exist.setSampleRate(vo.getSampleRate());
        exist.setStatus(vo.getStatus() == null ? exist.getStatus() : vo.getStatus());
        exist.setRemark(vo.getRemark());
        exist.setUpdateBy(vo.getCurrentUserId() == null ? 0L : vo.getCurrentUserId());
        exist.setUpdateTime(LocalDateTime.now());

        int row = mapper.updateById(exist);

        // 缓存同步:失效旧作用域,写入新作用域
        evictCache(exist.getConfigType(), exist.getTargetId());
        if (row > 0 && exist.getStatus() != null && exist.getStatus() == 1) {
            putCache(exist);
        }
        // 广播集群失效
        publishInvalidate();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfigByIds(Long[] ids, Long operatorId) {
        if (ids == null || ids.length == 0) {
            return;
        }
        Long uid = operatorId == null ? 0L : operatorId;
        boolean changed = false;
        for (Long id : ids) {
            AiTraceSampleConfig entity = mapper.selectById(id);
            if (entity == null || entity.getDeleteFlag() != null && entity.getDeleteFlag() == 1) {
                continue;
            }
            entity.setDeleteFlag(1);
            entity.setUpdateBy(uid);
            entity.setUpdateTime(LocalDateTime.now());
            mapper.updateById(entity);
            evictCache(entity.getConfigType(), entity.getTargetId());
            changed = true;
        }
        if (changed) {
            publishInvalidate();
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(Long id, Integer status, Long operatorId) {
        if (id == null || status == null) {
            throw new SystemBusinessException("主键ID和状态不能为空");
        }
        if (status != 0 && status != 1) {
            throw new SystemBusinessException("状态值非法,仅支持 0/1");
        }
        AiTraceSampleConfig entity = mapper.selectById(id);
        if (entity == null || entity.getDeleteFlag() != null && entity.getDeleteFlag() == 1) {
            throw new SystemBusinessException("采样率配置不存在或已删除");
        }
        Long uid = operatorId == null ? 0L : operatorId;
        entity.setStatus(status);
        entity.setUpdateBy(uid);
        entity.setUpdateTime(LocalDateTime.now());
        mapper.updateById(entity);

        evictCache(entity.getConfigType(), entity.getTargetId());
        if (status == 1) {
            putCache(entity);
        }
        // 广播集群失效
        publishInvalidate();
    }

    @Override
    public void refreshCache() {
        cache.clear();
        loadAllEnabledToCache();
    }

    // ============================================================
    //  运行时优先级解析
    // ============================================================

    @Override
    public BigDecimal resolveSampleRate(Long userId, Long orgId) {
        // 1) 用户级
        if (userId != null) {
            BigDecimal rate = lookupEnabled(AiTraceSampleConfig.TYPE_USER, userId);
            if (rate != null) {
                return rate;
            }
        }
        // 2) 组织级
        if (orgId != null) {
            BigDecimal rate = lookupEnabled(AiTraceSampleConfig.TYPE_ORG, orgId);
            if (rate != null) {
                return rate;
            }
        }
        // 3) 全局级
        BigDecimal rate = lookupEnabled(AiTraceSampleConfig.TYPE_GLOBAL, AiTraceSampleConfig.GLOBAL_TARGET_ID);
        if (rate != null) {
            return rate;
        }
        // 4) yml 兜底
        return clamp(props.getDefaultSampleRate());
    }

    /**
     * 查询指定作用域的启用配置,缓存命中时直接返回;否则查库回写缓存。
     *
     * @return 启用态采样率,未命中返回 null
     */
    private BigDecimal lookupEnabled(int type, long targetId) {
        if (props.getCacheTtlSeconds() > 0) {
            String key = cacheKey(type, targetId);
            CacheEntry entry = cache.get(key);
            long now = System.currentTimeMillis();
            if (entry != null && now - entry.loadedAt < props.getCacheTtlSeconds() * 1000L) {
                return entry.rate;
            }
        }
        AiTraceSampleConfig entity = mapper.selectOne(
                new LambdaQueryWrapper<AiTraceSampleConfig>()
                        .eq(AiTraceSampleConfig::getConfigType, type)
                        .eq(AiTraceSampleConfig::getTargetId, targetId)
                        .eq(AiTraceSampleConfig::getDeleteFlag, 0)
                        .eq(AiTraceSampleConfig::getStatus, 1)
                        .last("LIMIT 1"));
        if (entity == null) {
            // 未命中时不写缓存,允许后续重新查;负缓存由 TTL 自然过期
            return null;
        }
        putCache(entity);
        return entity.getSampleRate();
    }

    // ============================================================
    //  缓存 / 工具
    // ============================================================

    private void putCache(AiTraceSampleConfig c) {
        if (c == null || c.getConfigType() == null || c.getTargetId() == null) {
            return;
        }
        cache.put(cacheKey(c.getConfigType(), c.getTargetId()),
                new CacheEntry(c.getSampleRate(), System.currentTimeMillis()));
        refreshHasUserOrOrgOverride();
    }

    private void evictCache(int type, long targetId) {
        cache.remove(cacheKey(type, targetId));
        refreshHasUserOrOrgOverride();
    }

    /**
     * 重新计算 {@link #hasUserOrOrgOverride}:扫描 cache,若存在
     * org(2:)/user(3:) 类条目则置 true。O(n) 但 cache 极小(启用配置总数),
     * 每次 cache 变更后调用一次,可接受。
     */
    private void refreshHasUserOrOrgOverride() {
        boolean found = false;
        for (String key : cache.keySet()) {
            if (key.startsWith(AiTraceSampleConfig.TYPE_ORG + ":")
                    || key.startsWith(AiTraceSampleConfig.TYPE_USER + ":")) {
                found = true;
                break;
            }
        }
        this.hasUserOrOrgOverride = found;
    }

    @Override
    public boolean hasUserOrOrgOverride() {
        return hasUserOrOrgOverride;
    }

    /**
     * 向 Redis Pub/Sub 广播失效消息,通知集群其他实例刷新本地缓存。
     * Redisson 不可用时静默降级(本地缓存已同步,集群不一致由 TTL 自然收敛)。
     */
    private void publishInvalidate() {
        RedissonClient client = redissonClientProvider.getIfAvailable();
        if (client == null) {
            return;
        }
        try {
            client.getTopic(INVALIDATE_TOPIC).publish("invalidate");
        } catch (Exception e) {
            LOGGER.warn("AI Trace 采样率配置集群失效广播失败, 集群节点可能短暂不一致", e);
        }
    }

    /**
     * 启动时订阅失效广播,收到消息后调用 {@link #refreshCache()} 刷新本地缓存。
     * Redisson 不可用时跳过订阅(单实例或 Redis 暂未就绪场景)。
     */
    private void subscribeInvalidateTopic() {
        RedissonClient client = redissonClientProvider.getIfAvailable();
        if (client == null) {
            LOGGER.info("Redisson 不可用, 集群缓存失效广播已降级为 TTL 自然收敛");
            return;
        }
        try {
            client.getTopic(INVALIDATE_TOPIC).addListener(String.class, (channel, msg) -> {
                try {
                    refreshCache();
                    LOGGER.debug("收到 AI Trace 采样率配置失效广播, 本地缓存已刷新");
                } catch (Exception e) {
                    LOGGER.warn("AI Trace 采样率配置本地缓存刷新失败", e);
                }
            });
            LOGGER.info("已订阅 AI Trace 采样率配置失效广播: {}", INVALIDATE_TOPIC);
        } catch (Exception e) {
            LOGGER.warn("订阅 AI Trace 采样率配置失效广播失败, 集群缓存可能短暂不一致", e);
        }
    }

    private static String cacheKey(int type, long targetId) {
        return type + ":" + targetId;
    }

    /**
     * 同一作用域下未删除配置必须唯一;存在则抛业务异常。
     */
    private void validateScope(Integer configType, Long targetId, Long excludeId) {
        if (configType == null) {
            throw new SystemBusinessException("配置类型不能为空");
        }
        long resolvedTarget = resolveTargetId(configType, targetId);
        Long count = mapper.selectCount(
                new LambdaQueryWrapper<AiTraceSampleConfig>()
                        .eq(AiTraceSampleConfig::getConfigType, configType)
                        .eq(AiTraceSampleConfig::getTargetId, resolvedTarget)
                        .eq(AiTraceSampleConfig::getDeleteFlag, 0)
                        .ne(excludeId != null, AiTraceSampleConfig::getId, excludeId));
        if (count != null && count > 0) {
            throw new SystemBusinessException("该作用域下已存在采样率配置,请勿重复添加");
        }
    }

    /**
     * 全局类型 targetId 缺省时填 0,组织/用户类型必须有值。
     */
    private long resolveTargetId(Integer configType, Long targetId) {
        if (configType == null) {
            return 0L;
        }
        if (configType == AiTraceSampleConfig.TYPE_GLOBAL) {
            return AiTraceSampleConfig.GLOBAL_TARGET_ID;
        }
        if (targetId == null) {
            throw new SystemBusinessException("组织/用户级配置必须指定 targetId");
        }
        return targetId;
    }

    private static BigDecimal clamp(BigDecimal v) {
        if (v == null) {
            return BigDecimal.ONE;
        }
        if (v.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        if (v.compareTo(BigDecimal.ONE) > 0) {
            return BigDecimal.ONE;
        }
        return v;
    }

    private AiTraceSampleConfigListVo toListVo(AiTraceSampleConfig entity) {
        AiTraceSampleConfigListVo vo = new AiTraceSampleConfigListVo();
        vo.setId(entity.getId());
        vo.setConfigType(entity.getConfigType());
        vo.setConfigTypeDesc(configTypeDesc(entity.getConfigType()));
        vo.setTargetId(entity.getTargetId());
        vo.setSampleRate(entity.getSampleRate());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        vo.setCreateBy(entity.getCreateBy() == null ? null : String.valueOf(entity.getCreateBy()));
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateBy(entity.getUpdateBy() == null ? null : String.valueOf(entity.getUpdateBy()));
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }

    private static String configTypeDesc(Integer t) {
        if (t == null) {
            return "";
        }
        return switch (t) {
            case AiTraceSampleConfig.TYPE_GLOBAL -> "全局";
            case AiTraceSampleConfig.TYPE_ORG -> "组织";
            case AiTraceSampleConfig.TYPE_USER -> "用户";
            default -> "未知";
        };
    }

    /**
     * 缓存条目:采样率 + 加载时间戳。
     */
    private static final class CacheEntry {
        final BigDecimal rate;
        final long loadedAt;

        CacheEntry(BigDecimal rate, long loadedAt) {
            this.rate = rate;
            this.loadedAt = loadedAt;
        }
    }
}
