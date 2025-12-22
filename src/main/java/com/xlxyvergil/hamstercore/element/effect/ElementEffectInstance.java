package com.xlxyvergil.hamstercore.element.effect;

import net.minecraft.world.effect.MobEffectInstance;

/**
 * 元素效果实例类
 * 继承自MobEffectInstance，管理效果的具体参数和状态
 */
public class ElementEffectInstance extends MobEffectInstance {
    
    public ElementEffectInstance(ElementEffect effect) {
        super(effect);
    }
    
    public ElementEffectInstance(ElementEffect effect, int duration) {
        super(effect, duration);
    }
    
    public ElementEffectInstance(ElementEffect effect, int duration, int amplifier) {
        super(effect, duration, amplifier);
    }
    
    public ElementEffectInstance(ElementEffect effect, int duration, int amplifier, boolean ambient, boolean showParticles) {
        super(effect, duration, amplifier, ambient, showParticles);
    }
    
    public ElementEffectInstance(ElementEffect effect, int duration, int amplifier, boolean ambient, boolean showParticles, boolean showIcon) {
        super(effect, duration, amplifier, ambient, showParticles, showIcon);
    }
    
    // 管理效果的具体参数和状态的方法可以在这里添加
}