package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import com.xlxyvergil.hamstercore.util.WeaponApplicableItemsChecker;

import java.util.UUID;

/**
 * 毒气元素属性(火焰 + 毒素)
 */
public class GasAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("e9f1a9b9-1c9a-1a2b-5b8a-5c6a9a0a7a");
    
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
        // 使用WeaponApplicableItemsChecker统一的属性检查逻辑
        return WeaponApplicableItemsChecker.canApplyElements(stack);
    }
}