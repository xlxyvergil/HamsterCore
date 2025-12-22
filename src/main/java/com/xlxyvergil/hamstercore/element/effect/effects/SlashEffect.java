package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.DoTManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

/**
 * 切割元素效果
 * 用于管理出血DoT的状态效果
 */
public class SlashEffect extends ElementEffect {
    
    // 切割效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public SlashEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF0000); // 红色
    }
    

    
    /**
     * 应用切割效果，实现出血DoT效果
     * @param entity 实体
     * @param amplifier 效果等级
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    public void applyEffect(LivingEntity entity, int amplifier, float finalDamage,DamageSource damageSource) {
        // 实现出血DoT效果，持续6秒(120ticks)，每秒造成一次伤害
        // 伤害数值为最终伤害的35%乘以效果等级
        float dotDamage = finalDamage * 0.35f * (amplifier + 1);
        DoTManager.addDoT(entity, com.xlxyvergil.hamstercore.element.ElementType.SLASH, dotDamage, 120, amplifier, damageSource);
    }
}