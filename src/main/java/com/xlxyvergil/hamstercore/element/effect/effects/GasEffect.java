package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectRegistry;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectInstance;
import com.xlxyvergil.hamstercore.handler.ElementTriggerHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 毒气元素效果
 * 用于管理AoE毒气DoT的状态效果
 */
public class GasEffect extends ElementEffect {
    
    // 毒气效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    // 毒气效果持续时间（tick）：6秒 = 120 ticks
    private static final int GAS_DURATION = 120;
    
    public GasEffect() {
        super(MobEffectCategory.HARMFUL, 0x9370DB); // 紫色
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每40 ticks（2秒）触发一次DoT伤害
        return duration % 40 == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 实现毒气DoT效果，每2秒造成一次伤害
        // 获取ElementEffectInstance以访问原始伤害值
        ElementEffectInstance elementEffectInstance = getElementEffectInstance(entity);
        float baseDamage = elementEffectInstance != null ? elementEffectInstance.getFinalDamage() : 1.0F;
        
        // 计算DoT伤害：基础伤害 * 30% * (1 + 等级/10) - 提高伤害
        float dotDamage = baseDamage * 0.30F * (1.0F + amplifier * 0.1F);
        
        // 设置正在处理DoT伤害的标志，防止DoT伤害触发新的元素效果
        ElementTriggerHandler.setProcessingDotDamage(true);
        try {
            // 使用魔法伤害源确保不会触发新的元素效果
            DamageSource damageSource = entity.damageSources().magic();
            entity.hurt(damageSource, dotDamage);
        } finally {
            // 确保在伤害处理完成后重置标志
            ElementTriggerHandler.setProcessingDotDamage(false);
        }
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        
        // 检查是否正在处理DoT伤害，如果是则不创建新的毒云，防止无限循环
        if (ElementTriggerHandler.isProcessingDotDamage()) {
            return;
        }
        
        // 当添加毒气效果时，为主目标添加毒云效果进行AoE传播
        // 从ElementEffectInstance获取原始伤害值
        ElementEffectInstance elementEffectInstance = getElementEffectInstance(entity);
        float baseDamage = elementEffectInstance != null ? elementEffectInstance.getFinalDamage() : 1.0F;
        DamageSource damageSource = elementEffectInstance != null ? elementEffectInstance.getDamageSource() : entity.damageSources().generic();
        
        // 添加毒云效果进行AoE传播，持续6秒
        ElementEffectInstance cloudInstance = 
            new ElementEffectInstance(
                (ElementEffect) ElementEffectRegistry.GAS_CLOUD.get(), 120, amplifier, baseDamage, damageSource);
        entity.addEffect(cloudInstance);
    }
}