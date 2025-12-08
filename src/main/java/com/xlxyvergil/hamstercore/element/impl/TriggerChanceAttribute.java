package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementHelper;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.util.UUID;

/**
 * 触发率属性
 */
public class TriggerChanceAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("e9f1a9b9-1c9a-1a2b-5b8a-5c6a9a0a8c");
    
    public TriggerChanceAttribute() {
        super(ElementType.TRIGGER_CHANCE, 0.1, AttributeModifier.Operation.ADDITION); // 默认10%触发率
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        // 触发率以百分比显示
        return Component.translatable("element.trigger_chance.desc", formatPercentage(getDefaultValue()));
    }
    
    @Override
    public AttributeModifier createModifier(ItemStack stack, double value) {
        return new AttributeModifier(MODIFIER_UUID, "Trigger chance modifier", value, getOperation());
    }
    
    @Override
    public boolean canApplyTo(ItemStack stack) {
        return ElementHelper.canApplyElements(stack);
    }
    
    /**
     * 格式化百分比显示
     */
    public String formatPercentage(double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value * 100) + "%";
    }
}