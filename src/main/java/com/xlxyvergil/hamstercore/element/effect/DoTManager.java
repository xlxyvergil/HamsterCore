package com.xlxyvergil.hamstercore.element.effect;

import net.minecraft.world.entity.LivingEntity;
import com.xlxyvergil.hamstercore.element.ElementType;

import java.util.*;

/**
 * 持续伤害管理系统
 * 管理所有周期性伤害效果，支持叠加和独立计时
 */
public class DoTManager {
    
    // 存储实体身上的DoT效果
    private static final Map<LivingEntity, List<DoTEntry>> entityDoTs = new HashMap<>();
    
    /**
     * DoT效果条目类
     * 表示一个DoT效果条目
     */
    public static class DoTEntry {
        private final ElementType elementType;
        private final float damagePerTick;
        private int ticksRemaining;
        private final int amplifier;
        
        public DoTEntry(ElementType elementType, float damagePerTick, int ticksRemaining, int amplifier) {
            this.elementType = elementType;
            this.damagePerTick = damagePerTick;
            this.ticksRemaining = ticksRemaining;
            this.amplifier = amplifier;
        }
        
        public ElementType getElementType() {
            return elementType;
        }
        
        public float getDamagePerTick() {
            return damagePerTick;
        }
        
        public int getTicksRemaining() {
            return ticksRemaining;
        }
        
        public void decrementTicks() {
            this.ticksRemaining--;
        }
        
        public int getAmplifier() {
            return amplifier;
        }
    }
    
    /**
     * 为实体添加DoT效果
     * @param entity 实体
     * @param elementType 元素类型
     * @param damagePerTick 每tick伤害
     * @param duration 持续时间（tick）
     * @param amplifier 效果等级
     */
    public static void addDoT(LivingEntity entity, ElementType elementType, float damagePerTick, int duration, int amplifier) {
        DoTEntry entry = new DoTEntry(elementType, damagePerTick, duration, amplifier);
        entityDoTs.computeIfAbsent(entity, k -> new ArrayList<>()).add(entry);
    }
    
    /**
     * 更新实体身上的所有DoT效果
     * @param entity 实体
     */
    public static void updateDoTs(LivingEntity entity) {
        List<DoTEntry> dots = entityDoTs.get(entity);
        if (dots != null) {
            Iterator<DoTEntry> iterator = dots.iterator();
            while (iterator.hasNext()) {
                DoTEntry entry = iterator.next();
                entry.decrementTicks();
                
                // 应用伤害
                applyDotDamage(entity, entry);
                
                // 如果效果结束，移除它
                if (entry.getTicksRemaining() <= 0) {
                    iterator.remove();
                }
            }
            
            // 如果该实体没有任何DoT效果了，清理map
            if (dots.isEmpty()) {
                entityDoTs.remove(entity);
            }
        }
    }
    
    /**
     * 应用DoT伤害
     * @param entity 实体
     * @param entry DoT条目
     */
    private static void applyDotDamage(LivingEntity entity, DoTEntry entry) {
        // 这里应用自定义公式计算伤害
        float damage = calculateDotDamage(entry);
        // 应用伤害到实体
        entity.hurt(entity.damageSources().magic(), damage);
    }
    
    /**
     * 计算DoT伤害
     * @param entry DoT条目
     * @return 计算后的伤害值
     */
    private static float calculateDotDamage(DoTEntry entry) {
        // 使用自定义公式计算伤害
        // 这里只是一个示例，实际公式可能会更复杂
        return entry.getDamagePerTick() * (1.0f + entry.getAmplifier() * 0.1f);
    }
    
    /**
     * 移除实体身上的所有DoT效果
     * @param entity 实体
     */
    public static void clearDoTs(LivingEntity entity) {
        entityDoTs.remove(entity);
    }
    
    /**
     * 获取实体身上的DoT效果数量
     * @param entity 实体
     * @param elementType 元素类型
     * @return 该类型DoT效果的数量
     */
    public static int getDoTCount(LivingEntity entity, ElementType elementType) {
        List<DoTEntry> dots = entityDoTs.get(entity);
        if (dots != null) {
            return (int) dots.stream()
                    .filter(entry -> entry.getElementType() == elementType)
                    .count();
        }
        return 0;
    }
}