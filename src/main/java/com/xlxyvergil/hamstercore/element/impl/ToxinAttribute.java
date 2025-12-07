package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 毒素元素属性
 */
public class ToxinAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("d8f0e8a8-0b8c-0b1a-4a7b-4c5b8a9b5c");
    
    public ToxinAttribute() {
        super(ElementType.TOXIN, 0.0, AttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.toxin.desc", formatValue(0));
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