package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 磁力元素属性 (冰冻 + 电击)
 */
public class MagneticAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("a2b4c2d2-4a2b-4a5d-8d1a-8b9a4d9b9a");
    
    public MagneticAttribute() {
        super(ElementType.MAGNETIC, 0.0, AttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.magnetic.desc", formatValue(0));
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