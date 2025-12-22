package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 磁力元素效果
 * 用于管理护盾伤害、护盾再生失效和破盾后电击伤害的状态效果
 */
public class MagneticEffect extends ElementEffect {
    
    // 磁力效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public MagneticEffect() {
        super(MobEffectCategory.HARMFUL, 0x0000FF); // 蓝色
    }
    
    /**
     * 应用磁力效果，实现护盾伤害、护盾再生失效和破盾后电击伤害效果
     * @param entity 实体
     * @param amplifier 效果等级
     */
    public void applyEffect(LivingEntity entity, int amplifier) {
        // 实现护盾伤害、护盾再生失效和破盾后电击伤害效果
        // 可以通过修改实体的护盾相关属性来实现这些效果
    }
}