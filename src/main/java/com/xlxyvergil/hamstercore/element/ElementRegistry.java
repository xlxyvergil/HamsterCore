package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.api.element.ElementAttributeAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 元素属性注册系统，管理和注册所有元素类型
 */
public class ElementRegistry {
    
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<ElementType, ElementAttribute> REGISTRY = new HashMap<>();
    
    /**
     * 注册元素属性
     */
    public static void register(ElementAttribute attribute) {
        if (REGISTRY.containsKey(attribute.getType())) {
            LOGGER.warn("Duplicate element attribute registration: " + attribute.getType().getName());
            return;
        }
        REGISTRY.put(attribute.getType(), attribute);
        LOGGER.info("Registered element attribute: " + attribute.getType().getName());
    }
    
    /**
     * 获取元素属性
     */
    public static ElementAttribute getAttribute(ElementType type) {
        return REGISTRY.get(type);
    }
    
    /**
     * 获取所有注册的元素属性
     */
    public static Map<ElementType, ElementAttribute> getAllAttributes() {
        return new HashMap<>(REGISTRY);
    }
    
    /**
     * 移除指定的元素属性
     * @param type 要移除的元素类型
     */
    public static void unregister(ElementType type) {
        ElementAttribute removed = REGISTRY.remove(type);
        if (removed != null) {
            LOGGER.info("Unregistered element attribute: " + type.getName());
        } else {
            LOGGER.warn("Attempted to unregister non-existent element attribute: " + type.getName());
        }
    }
}