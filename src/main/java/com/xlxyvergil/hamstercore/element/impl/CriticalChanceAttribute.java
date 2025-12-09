package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.util.WeaponApplicableItemsChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.util.UUID;

/**
 * 暴击率属性
 */
public class CriticalChanceAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("e9f1a9b9-1c9a-1a2b-5b8a-5c6a9a0a6d");
    
    public CriticalChanceAttribute() {
        super(ElementType.CRITICAL_CHANCE, 0.05, AttributeModifier.Operation.ADDITION); // 默认5%暴击率
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        // 暴击率以百分比显示
        return Component.translatable("element.critical_chance.desc", formatPercentage(getDefaultValue()));
    }
    
    @Override
    public AttributeModifier createModifier(ItemStack stack, double value) {
        return new AttributeModifier(MODIFIER_UUID, "Critical chance modifier", value, getOperation());
    }
    
    @Override
    public boolean canApplyTo(ItemStack stack) {
        return WeaponApplicableItemsChecker.canApplyElements(stack);
    }
    
    /**
     * 格式化百分比显示
     */
    public String formatPercentage(double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value * 100) + "%";
    }
}