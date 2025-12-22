package com.xlxyvergil.hamstercore.element.effect;

import com.xlxyvergil.hamstercore.content.capability.EntityCapabilityAttacher;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import com.xlxyvergil.hamstercore.util.AttributeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import com.xlxyvergil.hamstercore.element.effect.effects.CorrosiveEffect;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 腐蚀效果管理器
 * 管理护甲削减效果，支持叠加和持续时间
 */
public class CorrosiveManager {
    
    // 存储实体身上的腐蚀效果
    private static final Map<LivingEntity, List<CorrosiveEntry>> entityCorrosives = new ConcurrentHashMap<>();
    
    /**
     * 腐蚀效果条目类
     * 表示一个腐蚀效果条目
     */
    public static class CorrosiveEntry {
        private int amplifier;  // 现在是可变的，以便支持叠加
        private final int duration; // 持续时间（8秒 = 160 ticks）
        private int ticksRemaining;
        private final UUID modifierUUID; // 属性修饰符UUID
        private AttributeModifier modifier; // 属性修饰符，现在是可变的以便更新
        
        public CorrosiveEntry(int amplifier) {
            this.amplifier = amplifier;
            this.duration = 160; // 8秒
            this.ticksRemaining = this.duration; // 修复：使用this.duration
            this.modifierUUID = UUID.randomUUID();
            
            // 计算腐蚀百分比：第1层26%，后续每层6%，最大80%
            double reductionPercentage = calculateReductionPercentage(amplifier);
            this.modifier = new AttributeModifier(
                modifierUUID,
                "Corrosive Armor Reduction",
                reductionPercentage,
                AttributeModifier.Operation.MULTIPLY_BASE
            );
        }
        
        /**
         * 更新效果等级，重新计算修饰符
         */
        public void updateAmplifier(int newAmplifier) {
            this.amplifier = newAmplifier;
            
            // 重新计算腐蚀百分比并创建新修饰符
            double reductionPercentage = calculateReductionPercentage(amplifier);
            this.modifier = new AttributeModifier(
                modifierUUID,
                "Corrosive Armor Reduction",
                reductionPercentage,
                AttributeModifier.Operation.MULTIPLY_BASE
            );
        }
        
        /**
         * 计算腐蚀百分比
         * 第1层26%，后续每层6%，最大80%
         */
        private double calculateReductionPercentage(int level) {
            if (level <= 0) return 0.0;
            double reduction = 1 - (0.26 + (level - 1) * 0.06);
            return Math.min(reduction, 0.80); // 最大80%
        }
        
        public int getAmplifier() {
            return amplifier;
        }
        
        public int getTicksRemaining() {
            return ticksRemaining;
        }
        
        public void decrementTicks() {
            this.ticksRemaining--;
        }
        
        public boolean isExpired() {
            return ticksRemaining <= 0;
        }
        
        public UUID getModifierUUID() {
            return modifierUUID;
        }
        
        public AttributeModifier getModifier() {
            return modifier;
        }
        
        public double getReductionPercentage() {
            return -modifier.getAmount();
        }
    }
    
    /**
     * 为实体添加腐蚀效果
     * @param entity 实体
     * @param amplifier 效果等级
     */
    public static void addCorrosive(LivingEntity entity, int amplifier) {
        // 检查是否已经存在腐蚀效果
        List<CorrosiveEntry> corrosives = entityCorrosives.computeIfAbsent(entity, k -> new ArrayList<>());
        
        // 如果已有效果，尝试叠加到现有效果而不是创建新的
        if (!corrosives.isEmpty()) {
            // 获取第一个腐蚀效果并更新其等级
            CorrosiveEntry existingEntry = corrosives.get(0);
            int newAmplifier = Math.min(9, existingEntry.getAmplifier() + 1); // amplifier从0开始，对应等级1-10
            
            // 移除旧的属性修饰符
            if (entity.getAttribute(Attributes.ARMOR) != null) {
                entity.getAttribute(Attributes.ARMOR).removeModifier(existingEntry.getModifierUUID());
            }
            
            // 更新效果等级
            existingEntry.updateAmplifier(newAmplifier);
            
            // 应用新的属性修饰符
            if (entity.getAttribute(Attributes.ARMOR) != null) {
                entity.getAttribute(Attributes.ARMOR).addPermanentModifier(existingEntry.getModifier());
            }
        } else {
            // 如果没有现有效果，则创建新的
            CorrosiveEntry entry = new CorrosiveEntry(amplifier);
            corrosives.add(entry);
            
            // 应用属性修饰符
            if (entity.getAttribute(Attributes.ARMOR) != null) {
                entity.getAttribute(Attributes.ARMOR).addPermanentModifier(entry.getModifier());
            }
        }
        
        // 安全地刷新Capability数据，保持当前护盾值和血量不变
        refreshCapabilityData(entity);
    }
    
    /**
     * 更新实体身上的所有腐蚀效果
     * @param entity 实体
     */
    public static void updateCorrosives(LivingEntity entity) {
        List<CorrosiveEntry> corrosives = entityCorrosives.get(entity);
        if (corrosives != null) {
            // 创建一个副本以避免并发修改
            List<CorrosiveEntry> corrosivesCopy = new ArrayList<>(corrosives);
            List<CorrosiveEntry> toRemove = new ArrayList<>();
            
            for (CorrosiveEntry entry : corrosivesCopy) {
                entry.decrementTicks();
                
                // 如果效果结束，移除它
                if (entry.isExpired()) {
                    // 移除属性修饰符
                    if (entity.getAttribute(Attributes.ARMOR) != null) {
                        entity.getAttribute(Attributes.ARMOR).removeModifier(entry.getModifierUUID());
                    }
                    toRemove.add(entry);
                }
            }
            
            // 从原始列表中移除需要移除的条目
            corrosives.removeAll(toRemove);
            
            // 如果该实体没有任何腐蚀效果了，清理map
            if (corrosives.isEmpty()) {
                entityCorrosives.remove(entity);
            }
            
            // 安全地刷新Capability数据，保持当前护盾值和血量不变
            refreshCapabilityData(entity);
        }
    }
    
    /**
     * 移除实体身上的所有腐蚀效果
     * @param entity 实体
     */
    public static void clearCorrosives(LivingEntity entity) {
        List<CorrosiveEntry> corrosives = entityCorrosives.get(entity);
        if (corrosives != null) {
            // 移除所有属性修饰符
            for (CorrosiveEntry entry : corrosives) {
                if (entity.getAttribute(Attributes.ARMOR) != null) {
                    entity.getAttribute(Attributes.ARMOR).removeModifier(entry.getModifierUUID());
                }
            }
            corrosives.clear();
            entityCorrosives.remove(entity);
            
            // 安全地刷新Capability数据，保持当前护盾值和血量不变
            refreshCapabilityData(entity);
        }
    }
    
    /**
     * 获取实体身上的腐蚀效果总减少百分比
     * @param entity 实体
     * @return 总减少百分比（0.0-1.0）
     */
    public static double getTotalReductionPercentage(LivingEntity entity) {
        List<CorrosiveEntry> corrosives = entityCorrosives.get(entity);
        if (corrosives != null && !corrosives.isEmpty()) {
            return corrosives.stream()
                    .mapToDouble(CorrosiveEntry::getReductionPercentage)
                    .sum();
        }
        return 0.0;
    }
    
    /**
     * 获取实体身上的腐蚀效果数量
     * @param entity 实体
     * @return 腐蚀效果的数量
     */
    public static int getCorrosiveCount(LivingEntity entity) {
        List<CorrosiveEntry> corrosives = entityCorrosives.get(entity);
        return corrosives != null ? corrosives.size() : 0;
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