package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * AffixManager专用缓存管理器
 * 用于缓存AffixManager的临时数据和操作记录
 */
public class AffixManagerCache {
    // 使用WeakHashMap确保缓存不会导致内存泄漏
    private static final Map<ItemStack, AffixManagerCacheData> CACHE = new WeakHashMap<>();
    
    /**
     * 获取或创建AffixManager所需的缓存数据
     */
    public static AffixManagerCacheData getOrCreateCache(ItemStack stack) {
        return CACHE.computeIfAbsent(stack, s -> new AffixManagerCacheData());
    }
    
    /**
     * 失效指定ItemStack的缓存
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
     * AffixManager缓存数据类
     * 存储AffixManager的临时数据和操作记录
     */
    public static class AffixManagerCacheData {
        // 临时数据存储
        private Map<String, Object> tempData = new HashMap<>();
        
        // 上次操作记录
        private String lastOperation;
        
        /**
         * 获取临时数据
         */
        public <T> T getTempData(String key, Class<T> type) {
            Object value = tempData.get(key);
            return type.isInstance(value) ? type.cast(value) : null;
        }
        
        /**
         * 设置临时数据
         */
        public void setTempData(String key, Object value) {
            tempData.put(key, value);
        }
        
        /**
         * 移除临时数据
         */
        public void removeTempData(String key) {
            tempData.remove(key);
        }
        
        /**
         * 获取上次操作记录
         */
        public String getLastOperation() {
            return lastOperation;
        }
        
        /**
         * 设置上次操作记录
         */
        public void setLastOperation(String lastOperation) {
            this.lastOperation = lastOperation;
        }
        
        /**
         * 清空缓存数据
         */
        public void clear() {
            tempData.clear();
            lastOperation = null;
        }
    }
}