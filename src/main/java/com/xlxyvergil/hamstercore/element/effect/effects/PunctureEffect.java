package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 穿刺元素效果
 * 用于管理伤害输出减少的状态效果
 */
public class PunctureEffect extends ElementEffect {
    
    // 穿刺效果的最大等级
    public static final int MAX_LEVEL = 5;
    
    public PunctureEffect() {
        super(MobEffectCategory.HARMFUL, 0xD8BFD8); // 蓟色（淡紫色）
    }
    
    /**
     * 应用穿刺效果，实现伤害输出减少效果
     * @param entity 实体
     * @param amplifier 效果等级
     */
    public void applyEffect(LivingEntity entity, int amplifier) {
        // 实现伤害输出减少效果
        // 这里可以添加具体的伤害减少逻辑
    }
}