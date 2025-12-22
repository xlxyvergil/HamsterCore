package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.DoTManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;

/**
 * 毒素元素效果
 * 用于管理可绕过护盾的毒素DoT的状态效果
 */
public class ToxinEffect extends ElementEffect {
    
    // 毒素效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public ToxinEffect() {
        super(MobEffectCategory.HARMFUL, 0x00FF00); // 绿色
    }
    
    /**
     * 应用毒素效果，实现可绕过护盾的毒素DoT效果
     * @param entity 实体
     * @param amplifier 效果等级
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    public void applyEffect(LivingEntity entity, int amplifier, float finalDamage, DamageSource damageSource) {
        // 实现毒素DoT效果，持续6秒(120ticks)，每秒造成一次伤害
        // 伤害数值为最终伤害的50%乘以当前触发等级
        float dotDamage = finalDamage * 0.5f * (amplifier + 1);
        DoTManager.addDoT(entity, com.xlxyvergil.hamstercore.element.ElementType.TOXIN, dotDamage, 120, amplifier, damageSource);
    }
}