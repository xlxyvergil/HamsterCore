package com.xlxyvergil.hamstercore.api.element;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;

/**
 * 元素属性API
 * 提供元素属性注册和管理的相关接口
 */
public class ElementAttributeAPI {
    
    /**
     * 注册一个元素属性
     * 
     * @param attribute 元素属性实例
     */
    public static void registerElementAttribute(ElementAttribute attribute) {
        ElementRegistry.register(attribute);
    }
    
    /**
     * 获取指定类型的元素属性
     * 
     * @param type 元素类型
     * @return 元素属性实例
     */
    public static ElementAttribute getElementAttribute(ElementType type) {
        return ElementRegistry.getAttribute(type);
    }
}
