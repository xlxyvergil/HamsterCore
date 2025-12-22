package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 病毒元素效果
 * 用于管理受到生命值伤害增伤的状态效果
 */
public class ViralEffect extends ElementEffect {
    
    // 病毒效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public ViralEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF1493); // 深粉色
    }
}