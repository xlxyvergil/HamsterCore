package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.BlastManager;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

/**
 * 爆炸元素效果
 * 用于管理延迟范围伤害的状态效果
 */
public class BlastEffect extends ElementEffect {
    
    // 爆炸效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public BlastEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF4500); // 橙红色
    }
    
    /**
     * 应用爆炸效果，实现延迟范围伤害效果
     * @param entity 实体
     * @param amplifier 效果等级
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    public void applyEffect(LivingEntity entity, int amplifier, float finalDamage, DamageSource damageSource) {
        // 计算爆炸伤害
        float blastDamage = finalDamage * 0.3f * (amplifier + 1);
        // 添加到爆炸管理器
        BlastManager.addBlast(entity, blastDamage, amplifier, damageSource);
    }
}