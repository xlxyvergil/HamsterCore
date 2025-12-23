package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.UUID;

/**
 * 穿刺元素效果
 * 用于管理伤害输出减少的状态效果
 */
public class PunctureEffect extends ElementEffect {
    
    // 穿刺效果的最大等级
    public static final int MAX_LEVEL = 5;
    
    // 伤害输出减少属性修饰符UUID
    private static final UUID DAMAGE_OUTPUT_REDUCTION_UUID = UUID.fromString("cc226129-ffbd-451f-9808-ef6bd7ae2981");
    
    public PunctureEffect() {
        super(MobEffectCategory.HARMFUL, 0xD8BFD8); // 蓟色（淡紫色）
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        
        // 只有当实体具有攻击属性时才添加修饰符
        if (entity.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            // 减少伤害输出20% * (amplifier + 1)
            double damageReduction = -0.2 * (amplifier + 1);
            
            AttributeModifier modifier = new AttributeModifier(
                DAMAGE_OUTPUT_REDUCTION_UUID,
                "Puncture damage reduction",
                damageReduction,
                AttributeModifier.Operation.MULTIPLY_TOTAL
            );
            
            entity.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(modifier);
        }
    }
    
    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        
        // 只有当实体具有攻击属性时才移除修饰符
        if (entity.getAttribute(Attributes.ATTACK_DAMAGE) != null) {
            // 移除伤害输出减少修饰符
            entity.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(DAMAGE_OUTPUT_REDUCTION_UUID);
        }
    }
}