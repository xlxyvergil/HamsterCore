package com.xlxyvergil.hamstercore.element.effect;

import com.xlxyvergil.hamstercore.content.capability.EntityCapabilityAttacher;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import com.xlxyvergil.hamstercore.util.AttributeHelper;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 属性修饰符工具类
 * 用于处理元素效果相关的属性修饰符操作
 */
public class AttributeModifierUtil {
    
    // 存储实体身上元素效果的修饰符UUID，以便后续移除
    private static final Map<LivingEntity, Map<String, UUID>> entityModifierMap = new HashMap<>();
    
    /**
     * 添加护甲减少修饰符
     * @param entity 实体
     * @param percentage 减少百分比 (0.0-1.0)
     * @param duration 持续时间（tick）
     * @param modifierKey 修饰符键名，用于标识修饰符类型
     */
    public static void addArmorReductionModifier(LivingEntity entity, double percentage, int duration, String modifierKey) {
        // 生成唯一的UUID
        UUID modifierUUID = UUID.randomUUID();
        
        // 创建修饰符
        AttributeModifier modifier = new AttributeModifier(
            modifierUUID,
            "Element Armor Reduction",
            -percentage,
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        
        // 添加修饰符到实体的护甲属性上
        if (entity.getAttribute(Attributes.ARMOR) != null) {
            // 先检查并移除已存在的同类型修饰符
            removeArmorReductionModifier(entity, modifierKey);
            
            entity.getAttribute(Attributes.ARMOR).addPermanentModifier(modifier);
            
            // 记录修饰符UUID
            entityModifierMap.computeIfAbsent(entity, k -> new HashMap<>()).put(modifierKey, modifierUUID);
            
            // 安排在指定时间后移除修饰符
            scheduleModifierRemoval(entity, modifierKey, duration);
            
            // 刷新Capability数据，保持当前值不变
            refreshCapabilityData(entity);
        }
    }
    
    /**
     * 移除护甲减少修饰符
     * @param entity 实体
     * @param modifierKey 修饰符键名
     */
    public static void removeArmorReductionModifier(LivingEntity entity, String modifierKey) {
        if (entity.getAttribute(Attributes.ARMOR) != null) {
            Map<String, UUID> modifiers = entityModifierMap.get(entity);
            if (modifiers != null) {
                UUID modifierUUID = modifiers.get(modifierKey);
                if (modifierUUID != null) {
                    entity.getAttribute(Attributes.ARMOR).removeModifier(modifierUUID);
                    modifiers.remove(modifierKey);
                    
                    // 如果该实体没有其他修饰符了，清理map
                    if (modifiers.isEmpty()) {
                        entityModifierMap.remove(entity);
                    }
                }
            }
        }
        
        // 刷新Capability数据，保持当前值不变
        refreshCapabilityData(entity);
    }
    
    /**
     * 安排在指定时间后移除修饰符
     * @param entity 实体
     * @param modifierKey 修饰符键名
     * @param duration 持续时间（tick）
     */
    private static void scheduleModifierRemoval(LivingEntity entity, String modifierKey, int duration) {
        // 使用一个简单的计时器来安排修饰符移除
        // 在实际应用中，可能需要使用Minecraft的调度系统
        new Thread(() -> {
            try {
                Thread.sleep(duration * 50L); // 每tick约为50毫秒
                removeArmorReductionModifier(entity, modifierKey);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    /**
     * 安全地更新实体的Capability数据，保持当前护盾值和血量不变
     * @param entity 实体
     */
    public static void refreshCapabilityData(LivingEntity entity) {
        // 仅更新Attribute相关的Capability，保持当前值不变
        entity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(armorCap -> {
            armorCap.setArmor(AttributeHelper.getArmor(entity));
        });
        
        // 先获取当前护盾值
        float currentShieldValue = entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY)
            .map(shieldCap -> shieldCap.getCurrentShield())
            .orElse(0.0f);
        
        // 然后更新护盾相关参数，同时保持当前护盾值
        entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(shieldCap -> {
            // 只更新不涉及当前护盾值的参数
            shieldCap.setMaxShield((float) AttributeHelper.getShield(entity));
            shieldCap.setRegenRate((float) AttributeHelper.getRegenRate(entity));
            shieldCap.setRegenDelay((int) AttributeHelper.getRegenDelay(entity));
            shieldCap.setRegenDelayDepleted((int) AttributeHelper.getDepletedRegenDelay(entity));
            shieldCap.setImmunityTime((int) AttributeHelper.getImmunityTime(entity));
            
            // 恢复之前的当前护盾值，防止实体回血
            shieldCap.setCurrentShield(currentShieldValue);
        });
        
        // 同步更新到客户端
        EntityCapabilityAttacher.syncEntityCapabilitiesToClients(entity);
    }
}