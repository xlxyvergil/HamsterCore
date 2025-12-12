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
 * 暴击几率属性
 */
public class CriticalChanceAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("1D685E4A-7C94-4F3E-B9E5-9E1D9C9E8F7A");
    
    // 静态初始化块，在类加载时自动注册
    static {
        ElementRegistry.register(new CriticalChanceAttribute());
    }
    
    public CriticalChanceAttribute() {
        super(ElementType.CRITICAL_CHANCE, 0.0, AttributeModifier.Operation.MULTIPLY_BASE);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.critical_chance.desc", formatValue(0));
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
