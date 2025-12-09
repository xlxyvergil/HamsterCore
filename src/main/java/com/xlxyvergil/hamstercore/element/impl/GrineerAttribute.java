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
 * Grineer派系元素属性
 */
public class GrineerAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("8e6d26fe-1c3d-4a8d-9a1d-1b1a128cf90a");
    
    public GrineerAttribute() {
        super(ElementType.GRINEER, 0.0, AttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.grineer.desc", formatValue(0));
    }
    
    @Override
    public AttributeModifier createModifier(ItemStack stack, double value) {
        return new AttributeModifier(MODIFIER_UUID, "Grineer faction element modifier", value, getOperation());
    }
    
    @Override
    public boolean canApplyTo(ItemStack stack) {
        return WeaponApplicableItemsChecker.canApplyElements(stack);
    }
}