package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.CorrosiveManager;
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
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        // 添加腐蚀效果到管理器
        CorrosiveManager.addCorrosive(entity, amplifier);
    }
    
    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        // 移除腐蚀效果
        CorrosiveManager.clearCorrosives(entity);
    }
}