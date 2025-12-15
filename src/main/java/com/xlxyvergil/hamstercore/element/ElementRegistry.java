package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.HamsterCore;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.Consumer;

/**
 * 元素属性注册器
 * 完全参考GunsmithLib的实现模式
 */
public class ElementRegistry {
    /**
     * 元素属性注册类
     */
    public static class Attributes {
        private static final Consumer<Attribute> SET_SYNCED = attribute -> attribute.setSyncable(true);
        private static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, HamsterCore.MODID);
        
        // 存储已注册的元素属性映射
        private static final Map<ElementType, RegistryObject<ElementBasedAttribute>> ELEMENT_ATTRIBUTE_MAP = new HashMap<>();
        
        /**
         * 注册元素属性
         * @param elementType 元素类型
         * @param defaultValue 默认值
         * @param min 最小值
         * @param max 最大值
         * @return 注册的元素属性对象
         */
        public static RegistryObject<ElementBasedAttribute> registerElementAttribute(ElementType elementType, double defaultValue, double min, double max) {
            return registerElementAttribute(elementType, defaultValue, min, max, false);
        }
        
        /**
         * 注册元素属性
         * @param elementType 元素类型
         * @param defaultValue 默认值
         * @param min 最小值
         * @param max 最大值
         * @param isPercentBased 是否为百分比属性
         * @return 注册的元素属性对象
         */
        public static RegistryObject<ElementBasedAttribute> registerElementAttribute(ElementType elementType, double defaultValue, double min, double max, boolean isPercentBased) {
            if (ELEMENT_ATTRIBUTE_MAP.containsKey(elementType)) {
                return ELEMENT_ATTRIBUTE_MAP.get(elementType);
            }
            
            String name = elementType.getName();
            RegistryObject<ElementBasedAttribute> attribute = REGISTRY.register(name, () -> {
                ElementBasedAttribute elementAttribute = new ElementBasedAttribute(elementType, defaultValue, min, max, isPercentBased);
                SET_SYNCED.accept(elementAttribute);
                return elementAttribute;
            });
            
            ELEMENT_ATTRIBUTE_MAP.put(elementType, attribute);
            return attribute;
        }
        
        /**
         * 获取属性注册器
         * @return 属性注册器
         */
        public static DeferredRegister<Attribute> getRegistry() {
            return REGISTRY;
        }
        
        /**
         * 获取元素类型对应的属性注册对象
         * @param elementType 元素类型
         * @return 属性注册对象
         */
        public static RegistryObject<ElementBasedAttribute> getAttribute(ElementType elementType) {
            return ELEMENT_ATTRIBUTE_MAP.get(elementType);
        }
        
        /**
         * 获取元素类型对应的属性
         * @param elementType 元素类型
         * @return 属性对象，如果未注册则返回null
         */
        public static ElementBasedAttribute getAttributeValue(ElementType elementType) {
            RegistryObject<ElementBasedAttribute> attribute = getAttribute(elementType);
            return attribute != null && attribute.isPresent() ? attribute.get() : null;
        }
        
        /**
         * 获取所有已注册的元素属性
         * @return 元素属性集合
         */
        public static Collection<RegistryObject<ElementBasedAttribute>> getAllAttributes() {
            return ELEMENT_ATTRIBUTE_MAP.values();
        }
        
        /**
         * 创建元素属性的语言键
         * @param name 属性名称
         * @return 语言键
         */
        private static String createLangKey(String name) {
            return "attribute.name." + HamsterCore.MODID + "." + name;
        }
    }
    
    /**
     * 初始化所有元素属性
     */
    public static void init() {
        // 遍历所有元素类型并注册对应的属性
        for (ElementType elementType : ElementType.getAllTypes()) {
            // 为每种元素类型创建对应的属性
            Attributes.registerElementAttribute(elementType, 0.0, 0.0, Double.POSITIVE_INFINITY);
        }
    }
    
    /**
     * 获取元素类型对应的属性注册对象
     * @param elementType 元素类型
     * @return 属性注册对象
     */
    public static RegistryObject<ElementBasedAttribute> getAttribute(ElementType elementType) {
        return Attributes.getAttribute(elementType);
    }
    
    /**
     * 获取元素类型对应的属性
     * @param elementType 元素类型
     * @return 属性对象，如果未注册则返回null
     */
    public static ElementBasedAttribute getAttributeValue(ElementType elementType) {
        return Attributes.getAttributeValue(elementType);
    }
    
    /**
     * 获取元素属性注册器
     * @return 属性注册器
     */
    public static DeferredRegister<Attribute> getRegistry() {
        return Attributes.getRegistry();
    }
    
    /**
     * 生成元素属性修饰符的UUID
     * 确保相同元素类型和索引生成的UUID一致
     * @param elementType 元素类型
     * @param index 索引
     * @return UUID
     */
    public static UUID getModifierUUID(ElementType elementType, int index) {
        String key = elementType.getName() + ":" + index;
        return UUID.nameUUIDFromBytes(key.getBytes());
    }
    
    /**
     * 获取元素修饰符名称
     * @param elementType 元素类型
     * @param index 索引
     * @return 修饰符名称
     */
    public static String getModifierName(ElementType elementType, int index) {
        return elementType.getDisplayName() + " " + index;
    }
}