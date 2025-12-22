package com.xlxyvergil.hamstercore.element.effect;

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
        private final int amplifier;
        private final int duration; // 持续时间（8秒 = 160 ticks）
        private int ticksRemaining;
        private final UUID modifierUUID; // 属性修饰符UUID
        private final AttributeModifier modifier; // 属性修饰符
        
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
                -reductionPercentage,
                AttributeModifier.Operation.MULTIPLY_BASE
            );
        }
        
        /**
         * 计算腐蚀百分比
         * 第1层26%，后续每层6%，最大80%
         */
        private double calculateReductionPercentage(int level) {
            if (level <= 0) return 0.0;
            double reduction = 0.26 + (level - 1) * 0.06;
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
        
        // 如果已有效果且叠加层数未达到最大值
        if (corrosives.size() < CorrosiveEffect.MAX_LEVEL) {
            CorrosiveEntry entry = new CorrosiveEntry(amplifier);
            corrosives.add(entry);
            
            // 应用属性修饰符
            if (entity.getAttribute(Attributes.ARMOR) != null) {
                entity.getAttribute(Attributes.ARMOR).addPermanentModifier(entry.getModifier());
            }
        }
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
}