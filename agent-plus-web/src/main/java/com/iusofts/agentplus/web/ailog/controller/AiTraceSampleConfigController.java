package com.iusofts.agentplus.web.ailog.controller;

import com.iusofts.agentplus.ailog.interfaces.IAiTraceSampleConfigService;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigListVo;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigPageReqVo;
import com.iusofts.agentplus.ailog.vo.AiTraceSampleConfigVo;
import com.iusofts.agentplus.basic.web.annotation.OperationLogExclude;
import com.iusofts.agentplus.basic.web.vo.page.PageResult;
import com.iusofts.agentplus.web.common.controller.BApiController;
import com.iusofts.agentplus.web.common.util.SessionUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.iusofts.agentplus.basic.enums.OperationLogExcludeTypeEnums.RES;

/**
 * AI Trace 采样率配置前端控制器。
 *
 * <p>提供给前端管理 ai_trace_sample_config 表:
 * <ul>
 *   <li>分页查询、详情、新增、修改、软删除</li>
 *   <li>启停、刷新运行时缓存</li>
 *   <li>预览指定用户/组织命中的生效采样率(用于配置回显)</li>
 * </ul>
 *
 * @author Ivan
 * @since 2026-08-10
 */
@Tag(name = "AI Trace 采样率配置")
@RestController
@RequestMapping("/bapi/ai/trace/sample-config")
public class AiTraceSampleConfigController extends BApiController {

    @Resource
    private IAiTraceSampleConfigService sampleConfigService;

    @Operation(description = "分页查询采样率配置列表")
    @OperationLogExclude(type = RES)
    @PostMapping("/list")
    public PageResult<AiTraceSampleConfigListVo> list(@RequestBody AiTraceSampleConfigPageReqVo reqVo) {
        return sampleConfigService.pageConfig(reqVo);
    }

    @Operation(description = "根据主键查询配置详情")
    @OperationLogExclude(type = RES)
    @GetMapping("/{id}")
    public AiTraceSampleConfigVo getInfo(@PathVariable Long id) {
        return sampleConfigService.getById(id);
    }

    @Operation(description = "新增采样率配置")
    @PostMapping
    public void add(@Valid @RequestBody AiTraceSampleConfigVo vo) {
        vo.setCurrentUserId(currentUserId());
        sampleConfigService.addConfig(vo);
    }

    @Operation(description = "修改采样率配置")
    @PutMapping
    public void edit(@Valid @RequestBody AiTraceSampleConfigVo vo) {
        vo.setCurrentUserId(currentUserId());
        sampleConfigService.updateConfig(vo);
    }

    @Operation(description = "软删除采样率配置")
    @DeleteMapping("/{ids}")
    public void remove(@PathVariable Long[] ids) {
        sampleConfigService.deleteConfigByIds(ids, currentUserId());
    }

    @Operation(description = "启停采样率配置,status=0禁用/1启用")
    @PostMapping("/changeStatus")
    public void changeStatus(@RequestParam Long id, @Parameter(description = "0:禁用 1:启用") @RequestParam Integer status) {
        sampleConfigService.changeStatus(id, status, currentUserId());
    }

    @Operation(description = "刷新运行时缓存(配置变更后可手动触发)")
    @PostMapping("/refreshCache")
    public void refreshCache() {
        sampleConfigService.refreshCache();
    }

    @Operation(description = "预览指定用户/组织命中的生效采样率,优先级:用户>组织>全局>yml")
    @OperationLogExclude(type = RES)
    @GetMapping("/resolve")
    public java.math.BigDecimal resolve(@RequestParam(required = false) Long userId,
                                        @RequestParam(required = false) Long orgId) {
        return sampleConfigService.resolveSampleRate(userId, orgId);
    }

    private Long currentUserId() {
        try {
            return SessionUtil.getUserId();
        } catch (Exception e) {
            return 0L;
        }
    }
}
