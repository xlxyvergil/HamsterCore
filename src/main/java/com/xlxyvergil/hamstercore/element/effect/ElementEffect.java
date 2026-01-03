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
     * 获取实体上该效果存储的伤害值
     * @param entity 实体
     * @return 伤害值，如果不存在则返回0
     */
    public float getEffectDamage(LivingEntity entity) {
        if (entity.hasEffect(this)) {
            return ElementEffectDataHelper.getEffectDamage(entity, this);
        }
        return 0.0F;
    }

    /**
     * 检查实体是否有该效果的数据
     * @param entity 实体
     * @return 是否有数据
     */
    public boolean hasEffectData(LivingEntity entity) {
        return ElementEffectDataHelper.hasEffectData(entity, this);
    }
}