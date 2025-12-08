package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementHelper;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 热能元素属性
 */
public class HeatAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("e9f1a9b9-1c9a-1a2b-5b8a-5c6a9a0a7b");
    
    public HeatAttribute() {
        super(ElementType.HEAT, 0.0, AttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.heat.desc", formatValue(0));
    }
    
    @Override
    public AttributeModifier createModifier(ItemStack stack, double value) {
        return new AttributeModifier(MODIFIER_UUID, "Heat element modifier", value, getOperation());
    }
    
    @Override
    public boolean canApplyTo(ItemStack stack) {
        return ElementHelper.canApplyElements(stack);
    }
}