package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.util.WeaponApplicableItemsChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Murmur派系元素属性
 */
public class MurmurAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("d3127b43-6182-9f32-ef62-606f67d14e5f");
    
    public MurmurAttribute() {
        super(ElementType.MURMUR, 0.0, AttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.murmur.desc", formatValue(0));
    }
    
    @Override
    public AttributeModifier createModifier(ItemStack stack, double value) {
        return new AttributeModifier(MODIFIER_UUID, "Murmur faction element modifier", value, getOperation());
    }
    
    @Override
    public boolean canApplyTo(ItemStack stack) {
        return WeaponApplicableItemsChecker.canApplyElements(stack);
    }
}