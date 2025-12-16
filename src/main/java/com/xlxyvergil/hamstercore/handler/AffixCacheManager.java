package com.xlxyvergil.hamstercore.handler;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class AffixCacheManager {
    private static final Map<ItemStack, AffixCacheData> CACHE = new WeakHashMap<>();
    
    public static AffixCacheData getOrCreateCache(ItemStack stack) {
        return CACHE.computeIfAbsent(stack, s -> new AffixCacheData());
    }
    
    public static void invalidateCache(ItemStack stack) {
        CACHE.remove(stack);
    }
    
    public static class AffixCacheData {
        private Map<String, Double> criticalStats = new HashMap<>();
        private Map<String, Double> physicalElements = new HashMap<>();
        private Map<String, Double> factionElements = new HashMap<>();
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
        
        // 清空缓存方法
        public void clear() {
            criticalStats.clear();
            physicalElements.clear();
            factionElements.clear();
            combinedElements.clear();
        }
    }
}