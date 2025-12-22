package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

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
     */
    public void applyEffect(LivingEntity entity, int amplifier) {
        // 实现可绕过护盾的毒素DoT效果
        // 可以通过直接减少生命值而非伤害的方式来绕过护盾
    }
}