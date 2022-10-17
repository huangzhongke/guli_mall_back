package com.hzk.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hzk.common.valid.AddGroup;
import com.hzk.common.valid.ListValue;
import com.hzk.common.valid.UpdateGroup;
import com.hzk.common.valid.UpdateStatusGroup;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;
import java.io.Serializable;

/**
 * 品牌
 *
 * @author kee
 * @email 1059308740@qq.com
 * @date 2022-08-11 11:13:31
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotNull(message = "修改必须提交品牌Id", groups = {UpdateGroup.class})
    @Null(message = "新增不能指定Id", groups = {AddGroup.class})
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */

    @NotBlank(message = "品牌名必须提交", groups = {UpdateGroup.class, AddGroup.class})
    private String name;
    /**
     * 品牌logo地址
     */
    @NotBlank(groups = {AddGroup.class})
    @URL(message = "必须是一个合法的Url地址", groups = {UpdateGroup.class, AddGroup.class})
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    @NotNull(groups = {AddGroup.class,UpdateStatusGroup.class})
    @ListValue(vals = {0, 1},groups = {AddGroup.class, UpdateStatusGroup.class},message = "只能提交指定的值")
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotEmpty(groups = {AddGroup.class})
    @Pattern(message = "首字母必须在a-zA-Z之间", regexp = "^[a-zA-Z]$", groups = {AddGroup.class, UpdateGroup.class})
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull(groups = {AddGroup.class})
    @Min(message = "排序值不能小于0", value = 0, groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;

}
