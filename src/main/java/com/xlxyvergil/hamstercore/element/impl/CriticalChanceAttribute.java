package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 暴击率属性
 */
public class CriticalChanceAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("b5c7d5a5-7a5b-7a8d-1d4c-1a2b7a2a2b");
    
    public CriticalChanceAttribute() {
        super(ElementType.CRITICAL_CHANCE, 0.05, AttributeModifier.Operation.ADDITION); // 默认5%暴击率
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.critical_chance.desc", formatValue(defaultValue * 100));
    }
    
    @Override
    public AttributeModifier createModifier(ItemStack stack, double value) {
        return new AttributeModifier(
            MODIFIER_UUID,
            getIdentifier(),
            value,
            getOperation()
        );
    }
    
    @Override
    public boolean canApplyTo(ItemStack stack) {
        // 使用ElementHelper中统一的检查逻辑
        return com.xlxyvergil.hamstercore.element.ElementHelper.canApplyElementAttributes(stack);
    }
    
    @Override
    public String formatValue(double value) {
        // 暴击率以百分比显示
        return String.format("%.1f%%", value * 100);
    }
}