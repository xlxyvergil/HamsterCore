package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

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
     */
    public void applyEffect(LivingEntity entity, int amplifier) {
        // 实现延迟范围伤害效果
        // 可以通过定时任务在一定延迟后造成范围伤害
    }
}