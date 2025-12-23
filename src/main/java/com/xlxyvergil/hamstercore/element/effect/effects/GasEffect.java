package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.GasManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.handler.ElementTriggerHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectInstance;

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
        // 不在applyEffectTick中处理伤害，只在添加效果时添加到GasManager
        return false;
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        
        // 当添加毒气效果时，创建毒气云并添加到GasManager进行范围伤害管理
        // 从ElementEffectInstance获取原始伤害值
        ElementEffectInstance elementEffectInstance = getElementEffectInstance(entity);
        float baseDamage = elementEffectInstance != null ? elementEffectInstance.getFinalDamage() : 1.0F;
        DamageSource damageSource = elementEffectInstance != null ? elementEffectInstance.getDamageSource() : entity.damageSources().generic();
        
        // 添加到毒气管理器，创建毒气云
        GasManager.addGasCloud(entity, amplifier, damageSource);
    }
}