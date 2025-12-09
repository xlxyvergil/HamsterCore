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
 * Orokin派系元素属性
 */
public class OrokinAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("b1905921-4f60-7d10-cd40-4e4d45bf2c3d");
    
    public OrokinAttribute() {
        super(ElementType.OROKIN, 0.0, AttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.orokin.desc", formatValue(0));
    }
    
    @Override
    public AttributeModifier createModifier(ItemStack stack, double value) {
        return new AttributeModifier(MODIFIER_UUID, "Orokin faction element modifier", value, getOperation());
    }
    
    @Override
    public boolean canApplyTo(ItemStack stack) {
        return WeaponApplicableItemsChecker.canApplyElements(stack);
    }
}