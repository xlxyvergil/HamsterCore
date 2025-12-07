package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 暴击伤害属性
 */
public class CriticalDamageAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("c6d8a6b6-8a6c-8a9b-2a5d-2c3a8a3c3a");
    
    public CriticalDamageAttribute() {
        super(ElementType.CRITICAL_DAMAGE, 1.5, AttributeModifier.Operation.MULTIPLY_BASE); // 默认1.5倍暴击伤害
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.critical_damage.desc", formatValue(defaultValue));
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
        // 暴击伤害以倍数显示
        return String.format("%.1fx", value);
    }
}