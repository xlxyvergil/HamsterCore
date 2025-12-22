package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.effect.DoTManager;
import com.xlxyvergil.hamstercore.element.effect.HeatManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
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
    
    /**
     * 应用火焰效果，实现护甲减少和火焰DoT效果
     * @param entity 实体
     * @param amplifier 效果等级（0-9对应等级1-10）
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    public void applyEffect(LivingEntity entity, int amplifier, float finalDamage, net.minecraft.world.damagesource.DamageSource damageSource) {
        // 1. 应用护甲减少50%的效果，持续6秒（覆盖机制）
        HeatManager.addHeatArmorReduction(entity, FLAME_DURATION);
        
        // 2. 启动火焰DoT效果
        startFlameDoT(entity, amplifier, finalDamage, damageSource);
    }
    
    /**
     * 启动火焰DoT效果
     * @param target 目标实体
     * @param amplifier 效果等级
     * @param finalDamage 最终伤害值
     * @param damageSource 伤害源
     */
    private void startFlameDoT(LivingEntity target, int amplifier, float finalDamage, net.minecraft.world.damagesource.DamageSource damageSource) {
        // 计算每秒伤害：最终伤害的50% * (amplifier + 1)
        // 注意：amplifier从0开始，所以等级1-10对应amplifier 0-9
        float damagePerSecond = finalDamage * 0.5f * (amplifier + 1);
        
        // 添加DoT效果到目标
        // DoTManager会在第1~6秒造成伤害，第0秒不造成伤害
        DoTManager.addDoT(target, ElementType.HEAT, damagePerSecond, FLAME_DURATION, amplifier, damageSource);
    }
}