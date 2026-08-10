package com.iusofts.agentplus.ailog.interfaces;

import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigListVo;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigPageReqVo;
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
 *   <li>{@link #addConfig(AiTraceSampleConfigVo)} —— 新增</li>
 *   <li>{@link #updateConfig(AiTraceSampleConfigVo)} —— 修改</li>
 *   <li>{@link #deleteConfigByIds(Long[])} —— 软删除</li>
 *   <li>{@link #changeStatus(Long, Integer)} —— 启停</li>
 *   <li>{@link #resolveSampleRate(Long, Long)} —— 运行时优先级解析(用户>组织>全局>yml)</li>
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
    void addConfig(AiTraceSampleConfigVo vo);

    /**
     * 修改采样率配置。
     *
     * @throws com.iusofts.agentplus.basic.exception.SystemBusinessException 同一作用域下已存在其他未删除配置
     */
    void updateConfig(AiTraceSampleConfigVo vo);

    /**
     * 软删除采样率配置。
     *
     * @param ids         主键ID数组
     * @param operatorId  操作人ID(controller 从 session 注入)
     */
    void deleteConfigByIds(Long[] ids, Long operatorId);

    /**
     * 启停采样率配置(状态翻转)。
     *
     * @param id         主键ID
     * @param status     0:禁用 1:启用
     * @param operatorId 操作人ID(controller 从 session 注入)
     */
    void changeStatus(Long id, Integer status, Long operatorId);

    /**
     * 解析用户实际生效的采样率。
     *
     * <p>优先级:用户级 > 组织级 > 全局级 > yml 兜底(default-sample-rate)。
     * 命中记录若 status=0(禁用)则视同未配置,继续向下找。
     * 解析结果会写入内存缓存避免每次重复查库。</p>
     *
     * @param userId 用户ID,可为 null
     * @param orgId  组织ID,可为 null
     * @return 采样率,取值 [0, 1]
     */
    BigDecimal resolveSampleRate(Long userId, Long orgId);

    /**
     * 清理运行时缓存,运维接口或修改配置后调用。
     */
    void refreshCache();
}
