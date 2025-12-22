package com.xlxyvergil.hamstercore.element.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 元素效果基类
 * 继承自MobEffect，定义元素效果的通用行为
 */
public class ElementEffect extends MobEffect {
    
    protected ElementEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
    
    // 元素效果的通用行为可以在这里定义
    
    /**
     * 获取实体上该效果的ElementEffectInstance，以便访问自定义数据
     * @param entity 实体
     * @return ElementEffectInstance实例，如果不存在则返回null
     */
    public ElementEffectInstance getElementEffectInstance(LivingEntity entity) {
        if (entity.hasEffect(this)) {
            net.minecraft.world.effect.MobEffectInstance effectInstance = entity.getEffect(this);
            if (effectInstance instanceof ElementEffectInstance) {
                return (ElementEffectInstance) effectInstance;
            }
        }
        return null;
    }
}