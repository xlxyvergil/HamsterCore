package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.CorrosiveManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

/**
 * 腐蚀元素效果
 * 用于管理护甲削减的状态效果
 */
public class CorrosiveEffect extends ElementEffect {
    
    // 腐蚀效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public CorrosiveEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B4513); // 褐色
    }
    
    /**
     * 应用腐蚀效果，实现护甲削减效果
     * @param entity 实体
     * @param amplifier 效果等级
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    public void applyEffect(LivingEntity entity, int amplifier, float finalDamage, DamageSource damageSource) {
        // 添加腐蚀效果到管理器
        CorrosiveManager.addCorrosive(entity, amplifier);
    }
}