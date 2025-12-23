package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectInstance;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectRegistry;
import com.xlxyvergil.hamstercore.handler.ElementTriggerHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * 电击元素效果
 * 用于管理电击DoT和眩晕的状态效果
 */
public class ElectricityEffect extends ElementEffect {
    
    // 电击效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    // 眩晕持续时间（tick）：3秒 = 60 ticks
    private static final int STUN_DURATION = 60;
    
    // 电击DoT持续时间（tick）：6秒 = 120 ticks
    private static final int SHOCK_DURATION = 120;
    
    // 影响范围（米）：5米
    private static final double AOE_RANGE = 5.0;
    
    // 基础伤害值（每秒）
    private static final float BASE_DAMAGE_PER_SECOND = 5.0f;
    
    public ElectricityEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFF00); // 黄色
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每40 ticks（2秒）触发一次DoT伤害
        return duration % 40 == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 实现电击DoT效果，每2秒造成一次伤害
        // 获取ElementEffectInstance以访问原始伤害值
        ElementEffectInstance elementEffectInstance = getElementEffectInstance(entity);
        float baseDamage = elementEffectInstance != null ? elementEffectInstance.getFinalDamage() : 1.0F;
        
        // 计算DoT伤害：基础伤害 * 20% * (1 + 等级/10)
        float dotDamage = baseDamage * 0.20F * (1.0F + amplifier * 0.1F);
        
        // 设置正在处理DoT伤害的标志，防止DoT伤害触发新的元素效果
        ElementTriggerHandler.setProcessingDotDamage(true);
        try {
            // 使用魔法伤害源确保不会触发新的元素效果
            net.minecraft.world.damagesource.DamageSource damageSource = entity.damageSources().magic();
            entity.hurt(damageSource, dotDamage);
        } finally {
            // 确保在伤害处理完成后重置标志
            ElementTriggerHandler.setProcessingDotDamage(false);
        }
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        
        // 检查是否正在处理DoT伤害，如果是则不创建新的电云，防止无限循环
        if (ElementTriggerHandler.isProcessingDotDamage()) {
            return;
        }
        
        // 对主目标应用眩晕效果3秒
        applyStun(entity);
        
        // 为主目标添加电云效果进行AoE传播
        // 从ElementEffectInstance获取原始伤害值和伤害源
        ElementEffectInstance elementEffectInstance = getElementEffectInstance(entity);
        float baseDamage = elementEffectInstance != null ? elementEffectInstance.getFinalDamage() : 1.0F;
        net.minecraft.world.damagesource.DamageSource damageSource = elementEffectInstance != null ? elementEffectInstance.getDamageSource() : entity.damageSources().generic();
        
        // 添加电云效果进行AoE传播，持续6秒
        ElementEffectInstance cloudInstance = 
            new ElementEffectInstance(
                (ElementEffect) ElementEffectRegistry.ELECTRIC_CLOUD.get(), 120, amplifier, baseDamage, damageSource);
        entity.addEffect(cloudInstance);
    }
    
    /**
     * 应用眩晕效果
     * @param entity 目标实体
     */
    private void applyStun(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel) {
            // 应用移动减速效果实现眩晕
            entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                STUN_DURATION,
                255, // 最高等级，完全禁止移动
                false, // ambient
                true, // show particles
                true // show icon
            ));
            
            // 添加发光效果表示电击状态
            entity.addEffect(new MobEffectInstance(
                MobEffects.GLOWING,
                STUN_DURATION,
                0,
                false,
                true,
                false
            ));
            
            // 生成电击粒子效果
            ((ServerLevel) entity.level()).sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                20,
                0.5, 0.5, 0.5,
                0.1
            );
        }
    }
    
    /**
     * 对范围内敌人施加电击效果
     * @param center 中心实体
     * @param amplifier 效果等级
     */
    private void applyAoEShock(LivingEntity center, int amplifier, float baseDamage, net.minecraft.world.damagesource.DamageSource damageSource) {
        if (center.level() instanceof ServerLevel serverLevel) {
            // 获取周围5米内的所有生物（不包括中心实体）
            net.minecraft.world.phys.AABB aabb = new net.minecraft.world.phys.AABB(
                center.getX() - AOE_RANGE, center.getY() - AOE_RANGE, center.getZ() - AOE_RANGE,
                center.getX() + AOE_RANGE, center.getY() + AOE_RANGE, center.getZ() + AOE_RANGE);
            
            java.util.List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, aabb,
                entity -> entity != center && entity.isAlive());
            
            // 对每个周围的敌人施加电击DoT（但不施加眩晕，排除玩家）
            for (LivingEntity entity : nearbyEntities) {
                // 排除玩家实体
                if (entity instanceof Player) {
                    continue;
                }
                
                // 应用电击状态效果，持续时间与中心实体相同
                // 使用ElementEffectInstance以支持范围效果
                ElementEffectInstance effectInstance = 
                    new ElementEffectInstance(
                        this, // 使用当前电击效果实例
                        SHOCK_DURATION, // 持续时间
                        amplifier, // 效果等级
                        baseDamage, // 基础伤害
                        damageSource // 伤害源
                    );
                entity.addEffect(effectInstance);
                
                // 生成电击粒子效果
                serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    10,
                    0.3, 0.3, 0.3,
                    0.05
                );
            }
        }
    }
}