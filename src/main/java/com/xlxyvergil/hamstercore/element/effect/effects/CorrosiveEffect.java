package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

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
     */
    public void applyEffect(LivingEntity entity, int amplifier) {
        // 实现护甲削减效果
        // 可以通过AttributeModifier减少实体的护甲值
    }
}