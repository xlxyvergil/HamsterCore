package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * 触发几率属性
 */
public class TriggerChanceAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("3F8A7G6C-9E16-6H5G-D1G7-BG3F1E1G0H9C");
    
    // 静态初始化块，在类加载时自动注册
    static {
        ElementRegistry.register(new TriggerChanceAttribute());
    }
    
    public TriggerChanceAttribute() {
        super(ElementType.TRIGGER_CHANCE, 0.0, AttributeModifier.Operation.MULTIPLY_BASE);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.trigger_chance.desc", formatValue(0));
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
}
