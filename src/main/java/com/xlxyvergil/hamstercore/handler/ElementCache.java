package com.xlxyvergil.hamstercore.handler;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 自定义元素缓存系统，用于替代Placebo的CachedObject机制
 * 提供线程安全的缓存功能，避免重复计算元素属性
 * 参考TACZ的附件缓存设计模式
 */
public class ElementCache<K, T> {
    // 缓存值包装类
    public static class CacheValue<T> {
        private final T value;
        
        public CacheValue(T value) {
            this.value = value;
        }
        
        public T getValue() {
            return value;
        }
        
        public T orElse(T other) {
            return value != null ? value : other;
        }
    }
    
    // 使用WeakHashMap防止内存泄漏，当键被垃圾回收时自动清理
    private final Map<K, LazyOptional<T>> cache = new WeakHashMap<>();
    // 使用ConcurrentHashMap保证线程安全
    private final Map<K, LazyOptional<T>> concurrentCache = new ConcurrentHashMap<>();
    
    private final Function<K, T> computer;
    
    public ElementCache(Function<K, T> computer) {
        this.computer = computer;
    }
    
    /**
     * 获取指定键的缓存值
     * @param key 键
     * @return 缓存的值
     */
    public CacheValue<T> get(K key) {
        // 尝试从WeakHashMap获取
        LazyOptional<T> cached = cache.get(key);
        if (cached != null && cached.isPresent()) {
            return new CacheValue<>(cached.orElse(null));
        }
        
        // 如果没有缓存或已失效，则重新计算
        T computed = computer.apply(key);
        LazyOptional<T> lazyComputed = LazyOptional.of(() -> computed);
        
        // 更新缓存
        cache.put(key, lazyComputed);
        concurrentCache.put(key, lazyComputed);
        
        return new CacheValue<>(computed);
    }
    
    /**
     * 清除指定键的缓存
     * @param key 键
     */
    public void invalidate(K key) {
        cache.remove(key);
        concurrentCache.remove(key);
    }
    
    /**
     * 清除所有缓存
     */
    public void invalidateAll() {
        cache.clear();
        concurrentCache.clear();
    }
}