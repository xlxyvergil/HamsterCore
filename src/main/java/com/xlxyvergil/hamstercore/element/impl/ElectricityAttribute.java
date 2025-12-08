package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 电击元素属性
 */
public class ElectricityAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("e9f1a9b9-1c9a-1a2b-5b8a-5c6a9a0a6f");
    
    public ElectricityAttribute() {
        super(ElementType.ELECTRICITY, 0.0, AttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.electricity.desc", formatValue(0));
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
        // 使用ElementHelper统一的属性检查逻辑
        return com.xlxyvergil.hamstercore.element.ElementHelper.canApplyElements(stack);
    }
}