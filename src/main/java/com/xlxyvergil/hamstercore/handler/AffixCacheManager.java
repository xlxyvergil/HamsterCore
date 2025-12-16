package com.xlxyvergil.hamstercore.handler;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * ElementCalculationCoordinator专用缓存管理器
 * 用于缓存元素计算协调器的计算结果，包括：
 * - 暴击率、暴击伤害、触发率等暴击相关统计
 * - 物理元素值
 * - 派系元素值
 * - 复合元素值
 */
public class AffixCacheManager {
    private static final Map<ItemStack, AffixCacheData> CACHE = new WeakHashMap<>();
    
    /**
     * 获取或创建ElementCalculationCoordinator所需的缓存数据
     */
    public static AffixCacheData getOrCreateCache(ItemStack stack) {
        return CACHE.computeIfAbsent(stack, s -> new AffixCacheData());
    }
    
    /**
     * 失效指定ItemStack的缓存
     * 在ElementCalculationCoordinator计算结果过期时调用
     */
    public static void invalidateCache(ItemStack stack) {
        CACHE.remove(stack);
    }
    
    /**
     * 清空所有缓存数据
     */
    public static void clearAllCache() {
        CACHE.clear();
    }
    
    /**
     * ElementCalculationCoordinator缓存数据类
     * 存储协调器的计算结果
     */
    public static class AffixCacheData {
        // 暴击相关统计：暴击率、暴击伤害、触发率等
        private Map<String, Double> criticalStats = new HashMap<>();
        
        // 物理元素值
        private Map<String, Double> physicalElements = new HashMap<>();
        
        // 派系元素值
        private Map<String, Double> factionElements = new HashMap<>();
        
        // 复合元素值
        private Map<String, Double> combinedElements = new HashMap<>();
        
        public Map<String, Double> getCriticalStats() {
            return criticalStats;
        }
        
        public void setCriticalStats(Map<String, Double> criticalStats) {
            this.criticalStats = criticalStats;
        }
        
        public Map<String, Double> getPhysicalElements() {
            return physicalElements;
        }
        
        public void setPhysicalElements(Map<String, Double> physicalElements) {
            this.physicalElements = physicalElements;
        }
        
        public Map<String, Double> getFactionElements() {
            return factionElements;
        }
        
        public void setFactionElements(Map<String, Double> factionElements) {
            this.factionElements = factionElements;
        }
        
        public Map<String, Double> getCombinedElements() {
            return combinedElements;
        }
        
        public void setCombinedElements(Map<String, Double> combinedElements) {
            this.combinedElements = combinedElements;
        }
        
        /**
         * 清空缓存数据
         */
        public void clear() {
            criticalStats.clear();
            physicalElements.clear();
            factionElements.clear();
            combinedElements.clear();
        }
    }
}