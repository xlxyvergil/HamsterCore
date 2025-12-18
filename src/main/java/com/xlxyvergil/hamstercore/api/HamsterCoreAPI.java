package com.xlxyvergil.hamstercore.api;

import com.xlxyvergil.hamstercore.api.element.AffixAPI;
import com.xlxyvergil.hamstercore.api.element.ElementTypeAPI;
import com.xlxyvergil.hamstercore.element.ElementType;

/**
 * HamsterCore API主类
 * 提供统一的API入口
 */
public class HamsterCoreAPI {
    /**
     * 词缀系统API
     */
    public static final AffixAPI AFFIX = new AffixAPI();

    /**
     * 词缀类型API
     */
    public static final ElementTypeAPI ELEMENT_TYPE = new ElementTypeAPI();

    /**
     * 词缀类型枚举类
     */
    public static final Class<? extends ElementType.TypeCategory> ELEMENT_CATEGORY = ElementType.TypeCategory.class;
}