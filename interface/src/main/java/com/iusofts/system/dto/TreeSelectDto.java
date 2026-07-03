package com.iusofts.system.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.iusofts.basic.utils.StringUtils;
import com.iusofts.basic.constants.UserConstants;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Treeselect树结构实体类
 * 
 * @author 
 */
public class TreeSelectDto implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 节点ID */
    private Long id;

    /** 节点名称 */
    private String label;

    /** 节点禁用 */
    private boolean disabled = false;

    /** 子节点 */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<TreeSelectDto> children;

    public TreeSelectDto()
    {

    }

    public TreeSelectDto(SysDeptDto dept)
    {
        this.id = dept.getDeptId();
        this.label = dept.getDeptName();
        this.disabled = StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus());
        this.children = dept.getChildren().stream().map(TreeSelectDto::new).collect(Collectors.toList());
    }
    
    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public boolean isDisabled()
    {
        return disabled;
    }

    public void setDisabled(boolean disabled)
    {
        this.disabled = disabled;
    }

    public List<TreeSelectDto> getChildren()
    {
        return children;
    }

    public void setChildren(List<TreeSelectDto> children)
    {
        this.children = children;
    }
}
