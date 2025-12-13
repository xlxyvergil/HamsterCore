package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.api.element.RegisterElementsEvent;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 元素注册表
 * 管理所有元素属性的注册和查询
 */
@Mod.EventBusSubscriber(modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ElementRegistry {
    private static final Map<ElementType, ElementAttribute> ELEMENT_ATTRIBUTES = new HashMap<>();
    
    // 注册所有内置元素属性
    static {
        // 物理元素
        register(new ElementAttribute(ElementType.IMPACT, 1.0, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.PUNCTURE, 1.0, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.SLASH, 1.0, 0, Double.MAX_VALUE));
        
        // 基础元素
        register(new ElementAttribute(ElementType.COLD, 1.0, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.ELECTRICITY, 1.0, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.HEAT, 1.0, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.TOXIN, 1.0, 0, Double.MAX_VALUE));
        
        // 复合元素
        register(new ElementAttribute(ElementType.BLAST, 1.0, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.CORROSIVE, 1.0, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.GAS, 1.0, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.MAGNETIC, 1.0, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.RADIATION, 1.0, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.VIRAL, 1.0, 0, Double.MAX_VALUE));
        
        // 特殊属性
        register(new ElementAttribute(ElementType.CRITICAL_CHANCE, 0.05, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.CRITICAL_DAMAGE, 0.2, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.TRIGGER_CHANCE, 0.05, 0, Double.MAX_VALUE));
        
        // 派系元素
        register(new ElementAttribute(ElementType.GRINEER, 0.05, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.INFESTED, 0.05, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.CORPUS, 0.05, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.OROKIN, 0.05, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.SENTIENT, 0.05, 0, Double.MAX_VALUE));
        register(new ElementAttribute(ElementType.MURMUR, 0.05, 0, Double.MAX_VALUE));
    }
    
    /**
     * 注册元素属性
     * @param attribute 元素属性
     */
    public static void register(ElementAttribute attribute) {
        ELEMENT_ATTRIBUTES.put(attribute.getElementType(), attribute);
    }
    
    /**
     * 获取指定类型的元素属性
     * @param type 元素类型
     * @return 元素属性
     */
    public static ElementAttribute getAttribute(ElementType type) {
        return ELEMENT_ATTRIBUTES.get(type);
    }
    
    /**
     * 获取所有已注册的元素属性
     * @return 元素属性集合
     */
    public static Collection<ElementAttribute> getAllAttributes() {
        return ELEMENT_ATTRIBUTES.values();
    }
    
    /**
     * 为指定的元素类型和索引生成唯一标识符
     * @param type 元素类型
     * @param index 索引
     * @return 唯一标识符
     */
    public static String getModifierName(ElementType type, int index) {
        return "hamstercore:" + type.getName() + ":" + index;
    }
    
    /**
     * 为指定的元素类型和索引生成UUID
     * @param type 元素类型
     * @param index 索引
     * @return UUID
     */
    public static UUID getModifierUUID(ElementType type, int index) {
        return UUID.nameUUIDFromBytes((type.getName() + ":" + index).getBytes());
    }
    
    @SubscribeEvent
    public static void onRegisterElements(RegisterElementsEvent event) {
        // 这里可以添加默认的元素注册逻辑
        // 其他模组可以通过监听这个事件来注册他们自己的元素
    }
}