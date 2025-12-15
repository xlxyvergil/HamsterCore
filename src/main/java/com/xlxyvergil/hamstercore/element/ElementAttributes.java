package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.HamsterCore;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 元素属性注册类，使用DeferredHelper注册所有元素属性
 * 完全参考Apotheosis的ALObjects.Attributes实现
 */
public class ElementAttributes {

    // 存储已注册的元素属性映射
    private static final Map<ElementType, RegistryObject<ElementAttribute>> ELEMENT_ATTRIBUTE_MAP = new HashMap<>();

    /**
     * 静态初始化，注册所有元素属性
     */
    public static void bootstrap() {
        // 遍历所有元素类型并注册对应的属性
        for (ElementType elementType : ElementType.getAllTypes()) {
            // 为每种元素类型创建对应的属性
            registerElementAttribute(elementType, 0.0, 0.0, Double.POSITIVE_INFINITY, false);
        }
    }

    /**
     * 注册元素属性
     * @param elementType 元素类型
     * @param defaultValue 默认值
     * @param min 最小值
     * @param max 最大值
     * @return 注册的元素属性对象
     */
    public static RegistryObject<ElementAttribute> registerElementAttribute(ElementType elementType, double defaultValue, double min, double max) {
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
    public static RegistryObject<ElementAttribute> registerElementAttribute(ElementType elementType, double defaultValue, double min, double max, boolean isPercentBased) {
        if (ELEMENT_ATTRIBUTE_MAP.containsKey(elementType)) {
            return ELEMENT_ATTRIBUTE_MAP.get(elementType);
        }

        String name = elementType.getName();
        String descriptionId = "attribute.name.hamstercore." + name;

        // 使用DeferredHelper注册属性
        RegistryObject<ElementAttribute> attribute = HamsterCore.R.attribute(name, () -> {
            ElementAttribute elementAttribute = new ElementAttribute(elementType, defaultValue, min, max, isPercentBased);
            return elementAttribute;
        });

        ELEMENT_ATTRIBUTE_MAP.put(elementType, attribute);
        return attribute;
    }

    /**
     * 获取元素类型对应的属性注册对象
     * @param elementType 元素类型
     * @return 属性注册对象
     */
    public static RegistryObject<ElementAttribute> getAttribute(ElementType elementType) {
        return ELEMENT_ATTRIBUTE_MAP.get(elementType);
    }

    /**
     * 获取元素类型对应的属性
     * @param elementType 元素类型
     * @return 属性对象，如果未注册则返回null
     */
    public static ElementAttribute getAttributeValue(ElementType elementType) {
        RegistryObject<ElementAttribute> attribute = getAttribute(elementType);
        return attribute != null && attribute.isPresent() ? attribute.get() : null;
    }

    /**
     * 获取所有已注册的元素属性
     * @return 元素属性集合
     */
    public static Map<ElementType, RegistryObject<ElementAttribute>> getAllAttributes() {
        return ELEMENT_ATTRIBUTE_MAP;
    }
}
