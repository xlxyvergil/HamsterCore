package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.Lazy;

import java.util.List;
import java.util.WeakHashMap;

/**
 * 元素缓存类，用于提高元素数据的访问性能
 */
public class ElementCache {
    
    // 使用弱引用避免内存泄漏
    private static final WeakHashMap<ItemStack, Lazy<List<ElementInstance>>> ELEMENTS_CACHE = new WeakHashMap<>();
    private static final WeakHashMap<ItemStack, Lazy<List<ElementInstance>>> ACTIVE_ELEMENTS_CACHE = new WeakHashMap<>();
    
    /**
     * 获取元素列表的缓存值，如果缓存不存在则重新计算
     */
    public static List<ElementInstance> getCachedElements(ItemStack stack) {
        return ELEMENTS_CACHE.computeIfAbsent(stack, s -> 
            Lazy.of(() -> ElementHelper.getElements(s))
        ).get();
    }
    
    /**
     * 获取生效元素列表的缓存值，如果缓存不存在则重新计算
     */
    public static List<ElementInstance> getCachedActiveElements(ItemStack stack) {
        return ACTIVE_ELEMENTS_CACHE.computeIfAbsent(stack, s -> 
            Lazy.of(() -> ElementHelper.getActiveElements(s))
        ).get();
    }
    
    /**
     * 使指定物品的缓存失效
     */
    public static void invalidateCache(ItemStack stack) {
        ELEMENTS_CACHE.remove(stack);
        ACTIVE_ELEMENTS_CACHE.remove(stack);
    }
    
    /**
     * 清空所有缓存
     */
    public static void clearAllCaches() {
        ELEMENTS_CACHE.clear();
        ACTIVE_ELEMENTS_CACHE.clear();
    }
}