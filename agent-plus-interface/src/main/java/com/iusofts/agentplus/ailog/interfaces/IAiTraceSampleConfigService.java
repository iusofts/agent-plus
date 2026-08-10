package com.iusofts.agentplus.ailog.interfaces;

import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigAddReqVo;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigEditReqVo;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigListVo;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigPageReqVo;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigRemoveReqVo;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigResolveReqVo;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigStatusReqVo;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigVo;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;

import java.math.BigDecimal;

/**
 * AI Trace 采样率配置服务接口。
 *
 * <p>提供配置 CRUD 与运行时采样率解析能力:
 * <ul>
 *   <li>{@link #pageConfig(AiTraceSampleConfigPageReqVo)} —— 配置分页查询</li>
 *   <li>{@link #getById(Long)} —— 配置详情</li>
 *   <li>{@link #addConfig(AiTraceSampleConfigAddReqVo)} —— 新增</li>
 *   <li>{@link #updateConfig(AiTraceSampleConfigEditReqVo)} —— 修改</li>
 *   <li>{@link #deleteConfigByIds(AiTraceSampleConfigRemoveReqVo)} —— 批量软删除</li>
 *   <li>{@link #changeStatus(AiTraceSampleConfigStatusReqVo)} —— 启停</li>
 *   <li>{@link #resolveSampleRate(AiTraceSampleConfigResolveReqVo)} —— 运行时优先级解析(用户>组织>全局>yml)</li>
 * </ul>
 *
 * @author Ivan
 * @since 2026-08-10
 */
public interface IAiTraceSampleConfigService {

    /**
     * 分页查询采样率配置列表。
     */
    PageResult<AiTraceSampleConfigListVo> pageConfig(AiTraceSampleConfigPageReqVo reqVo);

    /**
     * 根据主键查询配置详情。
     */
    AiTraceSampleConfigVo getById(Long id);

    /**
     * 新增采样率配置。
     *
     * @throws com.iusofts.agentplus.basic.exception.SystemBusinessException 同一作用域下已存在未删除配置
     */
    void addConfig(AiTraceSampleConfigAddReqVo reqVo);

    /**
     * 修改采样率配置。
     *
     * @throws com.iusofts.agentplus.basic.exception.SystemBusinessException 同一作用域下已存在其他未删除配置
     */
    void updateConfig(AiTraceSampleConfigEditReqVo reqVo);

    /**
     * 批量软删除采样率配置。
     */
    void deleteConfigByIds(AiTraceSampleConfigRemoveReqVo reqVo);

    /**
     * 启停采样率配置(状态翻转)。
     */
    void changeStatus(AiTraceSampleConfigStatusReqVo reqVo);

    /**
     * 解析用户实际生效的采样率。
     *
     * <p>优先级:用户级 > 组织级 > 全局级 > yml 兜底(default-sample-rate)。
     * 命中记录若 status=0(禁用)则视同未配置,继续向下找。
     * 解析结果会写入内存缓存避免每次重复查库。</p>
     *
     * @return 采样率,取值 [0, 1]
     */
    BigDecimal resolveSampleRate(AiTraceSampleConfigResolveReqVo reqVo);

    /**
     * 是否存在用户级或组织级配置覆盖。
     *
     * <p>供 {@link com.iusofts.agentplus.ailog.sample.AiTraceSampleService}
     * 做 short-circuit 优化:当 yml 兜底为 1.0 且无任何 user/org 覆盖时,
     * 跳过 resolveSampleRate 直接放行所有 span,降低热路径开销。</p>
     *
     * @return true 表示存在 user(org)级配置,需要走 resolveSampleRate
     */
    boolean hasUserOrOrgOverride();

    /**
     * 清理运行时缓存,运维接口或修改配置后调用。
     */
    void refreshCache();
}
