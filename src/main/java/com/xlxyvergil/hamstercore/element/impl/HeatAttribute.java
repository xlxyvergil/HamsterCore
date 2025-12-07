package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 火焰元素属性
 */
public class HeatAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("c7e9d7a7-9f7a-9f0a-3a6b-3c4a7a8a4a");
    
    public HeatAttribute() {
        super(ElementType.HEAT, 0.0, AttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.heat.desc", formatValue(0));
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
}