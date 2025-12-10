package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.util.WeaponApplicableItemsChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/**
 * Sentient派系元素属性
 */
public class SentientAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("c2016a32-5071-8e21-de51-5f5e56c03d4e");
    
    // 静态初始化块，在类加载时自动注册
    static {
        ElementRegistry.register(new SentientAttribute());
    }
    
    public SentientAttribute() {
        super(ElementType.SENTIENT, 0.0, AttributeModifier.Operation.ADDITION);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.sentient.desc", formatValue(0));
    }
    
    @Override
    public AttributeModifier createModifier(ItemStack stack, double value) {
        return new AttributeModifier(MODIFIER_UUID, "Sentient faction element modifier", value, getOperation());
    }
    
    @Override
    public boolean canApplyTo(ItemStack stack) {
        return WeaponApplicableItemsChecker.canApplyElements(stack);
    }
}