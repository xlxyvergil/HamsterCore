package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 毒气元素属性 (火焰 + 毒素)
 */
public class GasAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("a1b3c1d1-3a1b-3a4d-7d0a-7b8a2b3d8b");
    
    public GasAttribute() {
        super(ElementType.GAS, 0.0, AttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.gas.desc", formatValue(0));
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