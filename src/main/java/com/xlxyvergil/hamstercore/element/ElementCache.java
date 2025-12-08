package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 自定义元素缓存系统，用于替代Placebo的CachedObject机制
 * 提供线程安全的缓存功能，避免重复计算元素属性
 */
public class ElementCache<T> {
    // 使用WeakHashMap防止内存泄漏，当ItemStack被垃圾回收时自动清理
    private final Map<ItemStack, LazyOptional<T>> cache = new WeakHashMap<>();
    // 使用ConcurrentHashMap保证线程安全
    private final Map<ItemStack, LazyOptional<T>> concurrentCache = new ConcurrentHashMap<>();
    
    private final Function<ItemStack, T> computer;
    
    public ElementCache(Function<ItemStack, T> computer) {
        this.computer = computer;
    }
    
    /**
     * 获取指定物品栈的缓存值
     * @param stack 物品栈
     * @return 缓存的值
     */
    public LazyOptional<T> get(ItemStack stack) {
        // 尝试从WeakHashMap获取
        LazyOptional<T> cached = cache.get(stack);
        if (cached != null && cached.isPresent()) {
            return cached;
        }
        
        // 如果没有缓存或已失效，则重新计算
        T computed = computer.apply(stack);
        LazyOptional<T> lazyComputed = LazyOptional.of(() -> computed);
        
        // 更新缓存
        cache.put(stack, lazyComputed);
        concurrentCache.put(stack, lazyComputed);
        
        return lazyComputed;
    }
    
    /**
     * 清除指定物品栈的缓存
     * @param stack 物品栈
     */
    public void invalidate(ItemStack stack) {
        cache.remove(stack);
        concurrentCache.remove(stack);
    }
    
    /**
     * 清除所有缓存
     */
    public void invalidateAll() {
        cache.clear();
        concurrentCache.clear();
    }
}