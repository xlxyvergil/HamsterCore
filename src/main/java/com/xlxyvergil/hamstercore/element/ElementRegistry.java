package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.element.impl.*;
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
     * 初始化默认元素属性
     */
    public static void init() {
        // 物理元素
        register(new ImpactAttribute());
        register(new PunctureAttribute());
        register(new SlashAttribute());
        
        // 基础元素
        register(new ColdAttribute());
        register(new ElectricityAttribute());
        register(new HeatAttribute());
        register(new ToxinAttribute());
        
        // 复合元素
        register(new BlastAttribute());
        register(new CorrosiveAttribute());
        register(new GasAttribute());
        register(new MagneticAttribute());
        register(new RadiationAttribute());
        register(new ViralAttribute());
        
        // 派系元素
        register(new GrineerAttribute());
        register(new InfestedAttribute());
        register(new CorpusAttribute());
        register(new OrokinAttribute());
        register(new SentientAttribute());
        register(new MurmurAttribute());
        
        // 特殊属性
        register(new CriticalChanceAttribute());
        register(new CriticalDamageAttribute());
        register(new TriggerChanceAttribute());
        
        LOGGER.info("ElementRegistry initialized with " + REGISTRY.size() + " element attributes");
    }
    
    /**
     * 获取所有注册的元素属性
     */
    public static Map<ElementType, ElementAttribute> getAllAttributes() {
        return new HashMap<>(REGISTRY);
    }
}