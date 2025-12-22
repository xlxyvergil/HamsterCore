package com.xlxyvergil.hamstercore.element.effect;

import net.minecraft.world.effect.MobEffectInstance;

/**
 * 元素效果实例类
 * 继承自MobEffectInstance，管理效果的具体参数和状态
 */
public class ElementEffectInstance extends MobEffectInstance {
    
    // 存储最终伤害值，用于DoT效果计算
    private float finalDamage = 0.0f;
    
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
    
    public ElementEffectInstance(ElementEffect effect, int duration, int amplifier, float finalDamage) {
        super(effect, duration, amplifier);
        this.finalDamage = finalDamage;
    }
    
    public float getFinalDamage() {
        return finalDamage;
    }
    
    public void setFinalDamage(float finalDamage) {
        this.finalDamage = finalDamage;
    }
    
    // 管理效果的具体参数和状态的方法可以在这里添加
}