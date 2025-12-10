package com.xlxyvergil.hamstercore.element.impl;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.util.WeaponApplicableItemsChecker;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.text.DecimalFormat;
import java.util.UUID;

/**
 * 暴击伤害属性
 */
public class CriticalDamageAttribute extends ElementAttribute {
    
    private static final UUID MODIFIER_UUID = UUID.fromString("e9f1a9b9-1c9a-1a2b-5b8a-5c6a9a0a6e");
    
    // 静态初始化块，在类加载时自动注册
    static {
        ElementRegistry.register(new CriticalDamageAttribute());
    }
    
    public CriticalDamageAttribute() {
        super(ElementType.CRITICAL_DAMAGE, 1.5, AttributeModifier.Operation.MULTIPLY_BASE); // 默认1.5倍暴击伤害
    }
    
    @Override
    public MutableComponent getDescription(ItemStack stack) {
        // 暴击伤害以倍数显示
        return Component.translatable("element.critical_damage.desc", formatMultiplier(getDefaultValue()));
    }
    
    @Override
    public AttributeModifier createModifier(ItemStack stack, double value) {
        return new AttributeModifier(MODIFIER_UUID, "Critical damage modifier", value, getOperation());
    }
    
    @Override
    public boolean canApplyTo(ItemStack stack) {
        return WeaponApplicableItemsChecker.canApplyElements(stack);
    }
    
    /**
     * 格式化倍数显示
     */
    public String formatMultiplier(double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value) + "x";
    }
}