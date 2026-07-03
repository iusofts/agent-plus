package com.iusofts.system.service;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.iusofts.basic.constants.CacheConstants;
import com.iusofts.basic.constants.UserConstants;
import com.iusofts.basic.exception.SystemBusinessException;
import com.iusofts.basic.page.PageResult;
import com.iusofts.basic.text.Convert;
import com.iusofts.basic.utils.StringUtils;
import com.iusofts.system.dao.SysConfigMapper;
import com.iusofts.system.dto.SysConfigDto;
import com.iusofts.system.entity.SysConfig;
import com.iusofts.system.interfaces.ISysConfigService;
import jakarta.annotation.PostConstruct;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 参数配置 服务层实现
 *
 * @author
 */
@DS("sys")
@Service
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {
    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 项目启动时，初始化参数到缓存
     */
    @PostConstruct
    public void init() {
        loadingConfigCache();
    }

    /**
     * 查询参数配置信息
     *
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    @Override
    public SysConfigDto selectConfigById(Long configId) {
        SysConfigDto config = new SysConfigDto();
        config.setConfigId(configId);
        return configMapper.selectConfig(config);
    }

    /**
     * 根据键名查询参数配置信息
     *
     * @param configKey 参数key
     * @return 参数键值
     */
    @Override
    public String selectConfigByKey(String configKey) {
        String configValue = getCache(getCacheKey(configKey));
        if (StringUtils.isNotEmpty(configValue)) {
            return configValue;
        }
        SysConfigDto config = new SysConfigDto();
        config.setConfigKey(configKey);
        SysConfigDto retConfig = configMapper.selectConfig(config);
        if (StringUtils.isNotNull(retConfig)) {
            setCache(getCacheKey(configKey), retConfig.getConfigValue());
            return retConfig.getConfigValue();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取验证码开关
     *
     * @return true开启，false关闭
     */
    @Override
    public boolean selectCaptchaEnabled() {
        String captchaEnabled = selectConfigByKey("sys.account.captchaEnabled");
        if (StringUtils.isEmpty(captchaEnabled)) {
            return true;
        }
        return Convert.toBool(captchaEnabled);
    }

    /**
     * 查询参数配置列表
     *
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    @Override
    public PageResult<SysConfigDto> selectConfigList(SysConfigDto config) {
        PageResult<SysConfigDto> pageResult = new PageResult<>();
        Page pageParam = new Page<>(config.getCurrentPage(), config.getPageSize());
        pageResult.setDataList(configMapper.selectConfigList(pageParam, config));
        pageResult.setTotalCount(pageParam.getTotal());
        return pageResult;
    }

    /**
     * 新增参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int insertConfig(SysConfigDto config) {
        int row = configMapper.insertConfig(config);
        if (row > 0) {
            setCache(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * 修改参数配置
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public int updateConfig(SysConfigDto config) {
        SysConfigDto temp = configMapper.selectConfigById(config.getConfigId());
        if (!StringUtils.equals(temp.getConfigKey(), config.getConfigKey())) {
            delCache(getCacheKey(temp.getConfigKey()));
        }

        int row = configMapper.updateConfig(config);
        if (row > 0) {
            setCache(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
        return row;
    }

    /**
     * 批量删除参数信息
     *
     * @param configIds 需要删除的参数ID
     */
    @Override
    public void deleteConfigByIds(Long[] configIds) {
        for (Long configId : configIds) {
            SysConfigDto config = selectConfigById(configId);
            if (StringUtils.equals(UserConstants.YES, config.getConfigType())) {
                throw new SystemBusinessException(String.format("内置参数【%1$s】不能删除 ", config.getConfigKey()));
            }
            configMapper.deleteConfigById(configId);
            delCache(getCacheKey(config.getConfigKey()));
        }
    }

    /**
     * 加载参数缓存数据
     */
    @Override
    public void loadingConfigCache() {
        Page pageParam = new Page<>(1, 999);
        List<SysConfigDto> configsList = configMapper.selectConfigList(pageParam, new SysConfigDto());
        for (SysConfigDto config : configsList) {
            setCache(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
    }

    /**
     * 清空参数缓存数据
     */
    @Override
    public void clearConfigCache() {
        for (String key : redissonClient.getKeys().getKeysByPattern(CacheConstants.SYS_CONFIG_KEY + "*")) {
            redissonClient.getKeys().delete(key);
        }
    }

    /**
     * 重置参数缓存数据
     */
    @Override
    public void resetConfigCache() {
        clearConfigCache();
        loadingConfigCache();
    }

    /**
     * 校验参数键名是否唯一
     *
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public boolean checkConfigKeyUnique(SysConfigDto config) {
        Long configId = StringUtils.isNull(config.getConfigId()) ? -1L : config.getConfigId();
        SysConfigDto info = configMapper.checkConfigKeyUnique(config.getConfigKey());
        if (StringUtils.isNotNull(info) && info.getConfigId().longValue() != configId.longValue()) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 设置cache key
     *
     * @param configKey 参数键
     * @return 缓存键key
     */
    private String getCacheKey(String configKey) {
        return CacheConstants.SYS_CONFIG_KEY + configKey;
    }

    private String getCache(String cacheKey) {
        RBucket<String> bucket = redissonClient.getBucket(cacheKey, StringCodec.INSTANCE);
        return bucket.get();
    }

    private void setCache(String cacheKey, String value) {
        RBucket<String> bucket = redissonClient.getBucket(cacheKey, StringCodec.INSTANCE);
        bucket.set(value);
    }

    private void delCache(String cacheKey) {
        redissonClient.getKeys().delete(cacheKey);
    }
}
