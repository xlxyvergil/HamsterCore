package com.xlxyvergil.hamstercore.api.element;

import com.xlxyvergil.hamstercore.element.ElementBasedAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraftforge.registries.RegistryObject;

/**
 * 元素属性API
 * 提供元素属性注册和管理的相关接口
 */
public class ElementAttributeAPI {
    
    /**
     * 获取指定类型的元素属性
     * 
     * @param type 元素类型
     * @return 元素属性实例
     */
    public static RegistryObject<ElementBasedAttribute> getElementAttribute(ElementType type) {
        return ElementRegistry.getAttribute(type);
    }
    
    /**
     * 获取指定类型的元素属性值
     * 
     * @param type 元素类型
     * @return 元素属性实例，如果未注册则返回null
     */
    public static ElementBasedAttribute getElementAttributeValue(ElementType type) {
        return ElementRegistry.getAttributeValue(type);
    }
}