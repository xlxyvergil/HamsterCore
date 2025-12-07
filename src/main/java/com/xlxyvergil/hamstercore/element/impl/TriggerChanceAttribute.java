package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 触发率属性
 */
public class TriggerChanceAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("b7c9d7a7-9e7f-9e0a-3a6c-3d4b9a4b4b");
    
    public TriggerChanceAttribute() {
        super(ElementType.TRIGGER_CHANCE, 0.1, AttributeModifier.Operation.ADDITION); // 默认10%触发率
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.trigger_chance.desc");
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
        // 触发率以百分比显示
        return String.format("%.1f%%", value * 100);
    }
}