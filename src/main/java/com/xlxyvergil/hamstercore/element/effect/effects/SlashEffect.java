package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectManager;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

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
     */
    public void applyEffect(LivingEntity entity, int amplifier) {
        // 实现出血DoT效果
        // 这里可以添加具体的DoT逻辑
    }
}