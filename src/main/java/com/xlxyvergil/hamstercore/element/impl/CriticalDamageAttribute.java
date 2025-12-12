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
 * 暴击伤害属性
 */
public class CriticalDamageAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("2E796F5B-8D05-5G4F-C0F6-AF2E0D0F9G8B");
    
    // 静态初始化块，在类加载时自动注册
    static {
        ElementRegistry.register(new CriticalDamageAttribute());
    }
    
    public CriticalDamageAttribute() {
        super(ElementType.CRITICAL_DAMAGE, 0.0, AttributeModifier.Operation.MULTIPLY_BASE);
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        return Component.translatable("element.critical_damage.desc", formatValue(0));
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
