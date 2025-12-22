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
        private int tickCounter; // 计数器，用于控制伤害频率
        private final net.minecraft.world.damagesource.DamageSource damageSource; // 原始伤害源
        
        public DoTEntry(ElementType elementType, float damagePerTick, int ticksRemaining, int amplifier, net.minecraft.world.damagesource.DamageSource damageSource) {
            this.elementType = elementType;
            this.damagePerTick = damagePerTick;
            this.ticksRemaining = ticksRemaining;
            this.amplifier = amplifier;
            this.tickCounter = 0;
            this.damageSource = damageSource;
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
        
        public int getTickCounter() {
            return tickCounter;
        }
        
        public void incrementTickCounter() {
            this.tickCounter++;
        }
        
        public void resetTickCounter() {
            this.tickCounter = 0;
        }
        
        public net.minecraft.world.damagesource.DamageSource getDamageSource() {
            return damageSource;
        }
    }
    
    /**
     * 为实体添加DoT效果
     * @param entity 实体
     * @param elementType 元素类型
     * @param damagePerTick 每tick伤害
     * @param duration 持续时间（tick）
     * @param amplifier 效果等级
     * @param damageSource 原始伤害源
     */
    public static void addDoT(LivingEntity entity, ElementType elementType, float damagePerTick, int duration, int amplifier, net.minecraft.world.damagesource.DamageSource damageSource) {
        DoTEntry entry = new DoTEntry(elementType, damagePerTick, duration, amplifier, damageSource);
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
                entry.incrementTickCounter();
                
                // 每20 ticks（1秒）应用一次伤害
                if (entry.getTickCounter() >= 20) {
                    // 应用伤害
                    applyDotDamage(entity, entry);
                    // 重置计数器
                    entry.resetTickCounter();
                }
                
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
        // 直接使用每tick伤害值，不需要额外计算
        float damage = entry.getDamagePerTick();
        
        // 使用原始伤害源进行DoT伤害
        // 这样DoT伤害会继承原始伤害的所有属性（如攻击者、伤害类型等）
        entity.hurt(entry.getDamageSource(), damage);
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