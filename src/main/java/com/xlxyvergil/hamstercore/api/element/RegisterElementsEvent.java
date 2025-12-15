package com.xlxyvergil.hamstercore.api.element;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.ChatFormatting;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.event.IModBusEvent;

import java.util.function.Consumer;

/**
 * 元素注册事件
 * 允许其他模组通过这个事件注册新的元素类型和元素属性
 */
public class RegisterElementsEvent extends Event implements IModBusEvent {
    private final Consumer<ElementAttribute> registerAttributeFunction;
    private final ElementTypeRegistrationHandler registerTypeFunction;
    
    public RegisterElementsEvent(Consumer<ElementAttribute> registerAttributeFunction, ElementTypeRegistrationHandler registerTypeFunction) {
        this.registerAttributeFunction = registerAttributeFunction;
        this.registerTypeFunction = registerTypeFunction;
    }
    
    /**
     * 注册一个新的元素属性
     * @param attribute 元素属性
     */
    public void register(ElementAttribute attribute) {
        registerAttributeFunction.accept(attribute);
    }
    
    /**
     * 注册一个新的元素类型及其属性
     * @param type 元素类型
     * @param defaultValue 默认值
     * @param min 最小值
     * @param max 最大值
     * @param isPercentBased 是否为百分比属性
     */
    public void register(ElementType type, double defaultValue, double min, double max, boolean isPercentBased) {
        register(new ElementAttribute(type, defaultValue, min, max, isPercentBased));
    }
    
    /**
     * 注册一个新的元素类型
     * @param name 元素类型名称
     * @param displayName 显示名称
     * @param color 颜色
     * @param category 类别
     * @return 新注册的元素类型
     */
    public ElementType registerType(String name, String displayName, ChatFormatting color, ElementType.TypeCategory category) {
        return registerTypeFunction.register(name, displayName, color, category);
    }
    
    @FunctionalInterface
    public interface ElementTypeRegistrationHandler {
        ElementType register(String name, String displayName, ChatFormatting color, ElementType.TypeCategory category);
    }
}