package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.HeatManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.handler.ElementTriggerHandler;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 火焰元素效果
 * 用于管理护甲减少和火焰DoT的状态效果
 */
public class HeatEffect extends ElementEffect {
    
    // 火焰效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    // 火焰效果持续时间（tick）：6秒 = 120 ticks
    private static final int FLAME_DURATION = 120;
    
    public HeatEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF8C00); // 橙色
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每40 ticks（2秒）触发一次效果（参考Apotheosis的BleedingEffect实现）
        return duration % 40 == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 实现火焰DoT效果，每2秒造成一次伤害
        // 使用ElementEffectDataHelper获取存储的伤害值
        float baseDamage = this.getEffectDamage(entity);
        if (baseDamage <= 0.0F) {
            baseDamage = 1.0F;
        }

        // 计算DoT伤害：基础伤害 * (40% + 等级*10%)
        float dotDamage = baseDamage * (0.40F + amplifier * 0.1F);

        // 设置正在处理DoT伤害的标志，防止DoT伤害触发新的元素效果
        // 同时ElementDamageManager会检查这个标志，跳过暴击计算，避免双重暴击
        ElementTriggerHandler.setProcessingDotDamage(true);
        try {
            // 使用伤害源进行伤害，让伤害系统统一处理，但跳过暴击计算
            entity.hurt(entity.level().damageSources().mobAttack(entity.getLastAttacker()), dotDamage);
        } finally {
            // 确保在伤害处理完成后重置标志
            ElementTriggerHandler.setProcessingDotDamage(false);
        }
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        // 应用护甲减少50%的效果，持续6秒（覆盖机制）
        HeatManager.addHeatArmorReduction(entity, FLAME_DURATION);
    }
}