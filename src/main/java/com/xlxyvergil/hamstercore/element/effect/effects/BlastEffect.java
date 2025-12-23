package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.BlastManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectInstance;
import com.xlxyvergil.hamstercore.handler.ElementTriggerHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 爆炸元素效果
 * 用于管理延迟范围伤害的状态效果
 */
public class BlastEffect extends ElementEffect {
    
    // 爆炸效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public BlastEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFD700); // 金色
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 不在applyEffectTick中处理伤害，只在添加效果时添加到BlastManager
        return false;
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        
        // 检查是否正在处理DoT伤害，防止连锁反应
        if (ElementTriggerHandler.isProcessingDotDamage()) {
            return;
        }
        
        // 当添加爆炸效果时，将其添加到BlastManager进行范围伤害管理
        // 从ElementEffectInstance获取原始伤害值
        ElementEffectInstance elementEffectInstance = getElementEffectInstance(entity);
        float baseDamage = elementEffectInstance != null ? elementEffectInstance.getFinalDamage() : 1.0F;
        DamageSource damageSource = elementEffectInstance != null ? elementEffectInstance.getDamageSource() : entity.damageSources().generic();
        
        // 计算爆炸伤害：基础伤害 * 30% * (1 + 等级/10)
        float blastDamage = baseDamage * 0.30F * (1.0F + amplifier * 0.1F);
        
        // 添加到爆炸管理器，等待1.5秒后爆炸
        BlastManager.addBlast(entity, blastDamage, amplifier, damageSource);
    }
}