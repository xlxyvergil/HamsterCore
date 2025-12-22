package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * 冰冻元素效果
 * 用于管理减速状态效果
 */
public class ColdEffect extends ElementEffect {
    
    // 冰冻效果的最大等级
    public static final int MAX_LEVEL = 6;
    
    public ColdEffect() {
        super(MobEffectCategory.HARMFUL, 0x00FFFF); // 青色
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        // 实现减速效果，使用原版的缓慢效果
        // amplifier从0开始，所以等级1-6对应amplifier 0-5
        entity.addEffect(new MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN, 
            120, // 6秒 = 120 ticks
            amplifier, 
            false, 
            true, 
            true));
    }
}