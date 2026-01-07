package com.xlxyvergil.hamstercore.api.element;

import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.ChatFormatting;

import java.util.Collection;
import java.util.List;

/**
 * 词缀类型API接口
 * 提供词缀类型的管理和查询功能
 */
public class ElementTypeAPI {
    /**
     * 注册物理元素类型
     * @param name 元素名称
     * @param displayName 显示名称
     * @param color 颜色
     * @return 注册的元素类型
     */
    public static ElementType registerPhysicalElement(String name, String displayName, ChatFormatting color) {
        return ElementType.register(name, displayName, color, ElementType.TypeCategory.PHYSICAL);
    }

    /**
     * 注册基础元素类型
     * @param name 元素名称
     * @param displayName 显示名称
     * @param color 颜色
     * @return 注册的元素类型
     */
    public static ElementType registerBasicElement(String name, String displayName, ChatFormatting color) {
        return ElementType.register(name, displayName, color, ElementType.TypeCategory.BASIC);
    }

    /**
     * 注册复合元素类型
     * @param name 元素名称
     * @param displayName 显示名称
     * @param color 颜色
     * @return 注册的元素类型
     */
    public static ElementType registerComplexElement(String name, String displayName, ChatFormatting color) {
        return ElementType.register(name, displayName, color, ElementType.TypeCategory.COMPLEX);
    }

    /**
     * 注册特殊元素类型
     * @param name 元素名称
     * @param displayName 显示名称
     * @param color 颜色
     * @return 注册的元素类型
     */
    public static ElementType registerSpecialElement(String name, String displayName, ChatFormatting color) {
        return ElementType.register(name, displayName, color, ElementType.TypeCategory.SPECIAL);
    }



    /**
     * 注册触发率元素类型
     * @param name 元素名称
     * @param displayName 显示名称
     * @param color 颜色
     * @return 注册的元素类型
     */
    public static ElementType registerTriggerChanceElement(String name, String displayName, ChatFormatting color) {
        return ElementType.register(name, displayName, color, ElementType.TypeCategory.TRIGGER_CHANCE);
    }

    /**
     * 获取物理元素类型列表
     * @return 物理元素类型列表
     */
    public static Collection<ElementType> getPhysicalElements() {
        return ElementType.getPhysicalElements();
    }

    /**
     * 获取基础元素类型列表
     * @return 基础元素类型列表
     */
    public static Collection<ElementType> getBasicElements() {
        return ElementType.getBasicElements();
    }

    /**
     * 获取复合元素类型列表
     * @return 复合元素类型列表
     */
    public static Collection<ElementType> getComplexElements() {
        return ElementType.getComplexElements();
    }

    /**
     * 获取特殊元素类型列表
     * @return 特殊元素类型列表
     */
    public static Collection<ElementType> getSpecialElements() {
        return ElementType.getSpecialElements();
    }

    /**
     * 获取暴击率元素类型列表
     * @return 暴击率元素类型列表
     */
    public static Collection<ElementType> getCriticalChanceElements() {
        return ElementType.getCriticalChanceElements();
    }

    /**
     * 获取暴击伤害元素类型列表
     * @return 暴击伤害元素类型列表
     */
    public static Collection<ElementType> getCriticalDamageElements() {
        return ElementType.getCriticalDamageElements();
    }

    /**
     * 获取触发率元素类型列表
     * @return 触发率元素类型列表
     */
    public static Collection<ElementType> getTriggerChanceElements() {
        return ElementType.getTriggerChanceElements();
    }

    /**
     * 根据名称获取元素类型
     * @param name 元素名称
     * @return 元素类型
     */
    public static ElementType getElementTypeByName(String name) {
        return ElementType.byName(name);
    }

    /**
     * 获取所有注册的元素类型
     * @return 所有元素类型
     */
    public static Collection<ElementType> getAllElementTypes() {
        return ElementType.getAllTypes();
    }

    /**
     * 检查元素类型是否为物理元素
     * @param elementType 元素类型
     * @return 是否为物理元素
     */
    public static boolean isPhysical(ElementType elementType) {
        return elementType.isPhysical();
    }

    /**
     * 检查元素类型是否为基础元素
     * @param elementType 元素类型
     * @return 是否为基础元素
     */
    public static boolean isBasic(ElementType elementType) {
        return elementType.isBasic();
    }

    /**
     * 检查元素类型是否为复合元素
     * @param elementType 元素类型
     * @return 是否为复合元素
     */
    public static boolean isComplex(ElementType elementType) {
        return elementType.isComplex();
    }

    /**
     * 检查元素类型是否为特殊元素
     * @param elementType 元素类型
     * @return 是否为特殊元素
     */
    public static boolean isSpecial(ElementType elementType) {
        return elementType.isSpecial();
    }



    /**
     * 检查元素类型是否为触发率元素
     * @param elementType 元素类型
     * @return 是否为触发率元素
     */
    public static boolean isTriggerChance(ElementType elementType) {
        return elementType.isTriggerChance();
    }

    /**
     * 获取复合元素的组成元素
     * @param complexElement 复合元素
     * @return 组成元素列表
     */
    public static List<ElementType> getComplexElementComposition(ElementType complexElement) {
        return complexElement.getComposition();
    }

    /**
     * 从两个基础元素创建复合元素
     * @param element1 第一个基础元素
     * @param element2 第二个基础元素
     * @return 对应的复合元素，如果不匹配则返回null
     */
    public static ElementType createComplexElement(ElementType element1, ElementType element2) {
        return ElementType.createComplex(element1, element2);
    }
}