package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.GasManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

/**
 * 毒气元素效果
 * 用于管理AoE毒气DoT和毒云范围的状态效果
 */
public class GasEffect extends ElementEffect {
    
    // 毒气效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public GasEffect() {
        super(MobEffectCategory.HARMFUL, 0x9370DB); // 紫色
    }
    
    /**
     * 应用毒气效果，实现AoE毒气DoT和毒云范围效果
     * @param entity 实体
     * @param amplifier 效果等级
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    public void applyEffect(LivingEntity entity, int amplifier, float finalDamage, DamageSource damageSource) {
        // 创建毒气云，范围为3米 + 等级*0.3米（最大3米额外，总共6米）
        GasManager.addGasCloud(entity, finalDamage, amplifier, damageSource);
    }
}