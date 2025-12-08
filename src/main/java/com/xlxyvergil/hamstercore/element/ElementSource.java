package com.xlxyvergil.hamstercore.element;

/**
 * 元素来源枚举
 * 用于标识元素的来源类型
 */
public enum ElementSource {
    /**
     * 直接设置 - 来自配置文件或直接添加的元素
     */
    DIRECT,
    
    /**
     * 计算得出 - 由基础元素组合计算得出的复合元素
     */
    COMPUTED,
    
    /**
     * 合并 - 由多个来源合并而成的元素
     */
    MERGED
}