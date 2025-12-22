package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 辐射元素效果
 * 用于管理敌我不分攻击友军的状态效果
 */
public class RadiationEffect extends ElementEffect {
    
    // 辐射效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public RadiationEffect() {
        super(MobEffectCategory.HARMFUL, 0x7CFC00); // 草坪绿
    }
    
    /**
     * 应用辐射效果，实现敌我不分攻击友军效果
     * @param entity 实体
     * @param amplifier 效果等级
     */
    public void applyEffect(LivingEntity entity, int amplifier) {
        // 实现敌我不分攻击友军效果
        // 可以通过修改实体的AI目标选择逻辑来实现敌我不分的效果
    }
}