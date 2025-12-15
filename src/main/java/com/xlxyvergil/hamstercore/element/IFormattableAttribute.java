package com.xlxyvergil.hamstercore.element;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;

/**
 * 可格式化属性接口
 * 参考Apotheosis的IFormattableAttribute实现
 * 用于属性值的格式化显示
 */
public interface IFormattableAttribute {

    /**
     * 将属性值格式化为文本组件
     * @param value 属性值
     * @param operation 操作类型
     * @param showAsPercent 是否显示为百分比
     * @return 格式化后的文本组件
     */
    Component formatValue(double value, Operation operation, boolean showAsPercent);

    /**
     * 检查属性是否应该显示为百分比
     * @return 是否显示为百分比
     */
    boolean isPercent();

    /**
     * 获取属性的单位文本
     * @return 单位文本组件
     */
    default Component getUnit() {
        return Component.empty();
    }
}
