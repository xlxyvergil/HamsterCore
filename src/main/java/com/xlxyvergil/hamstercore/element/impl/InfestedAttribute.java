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
 * Infested派系元素属性
 */
public class InfestedAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("a08f4810-3e5f-6c0f-bc3f-3d3c34ae1b2e");
    
    // 静态初始化块，在类加载时自动注册
    static {
        ElementRegistry.register(new InfestedAttribute());
    }
    
    public InfestedAttribute() {
        super(ElementType.INFESTED, 0.0, AttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.infested.desc", formatValue(0));
    }
    
    @Override
    public AttributeModifier createModifier(ItemStack stack, double value) {
        return new AttributeModifier(MODIFIER_UUID, "Infested faction element modifier", value, getOperation());
    }
}