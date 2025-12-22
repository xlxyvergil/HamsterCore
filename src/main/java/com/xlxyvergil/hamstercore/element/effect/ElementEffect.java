package com.xlxyvergil.hamstercore.element.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 元素效果基类
 * 继承自MobEffect，定义元素效果的通用行为
 */
public class ElementEffect extends MobEffect {
    
    protected ElementEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
    
    // 元素效果的通用行为可以在这里定义
}