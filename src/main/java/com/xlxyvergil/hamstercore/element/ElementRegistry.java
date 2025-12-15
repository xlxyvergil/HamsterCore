package com.xlxyvergil.hamstercore.element;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.xlxyvergil.hamstercore.util.ElementUUIDManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;

import java.util.*;
import java.util.function.Supplier;

import static com.xlxyvergil.hamstercore.HamsterCore.MODID;

/**
 * 元素注册表
 * 参考Apotheosis的注册表实现方式
 */
public class ElementRegistry {

    public static final ElementRegistry INSTANCE = new ElementRegistry();

    // 存储所有已注册的元素类型
    private static final Map<ResourceLocation, ElementType> ELEMENTS = Maps.newHashMap();
    
    // 存储元素的复合关系（基础元素 -> 复合元素）
    private static final Multimap<Set<ElementType>, ElementType> COMPLEX_ELEMENTS = HashMultimap.create();
    
    // 存储元素的逆复合关系（复合元素 -> 基础元素集合）
    private static final Map<ElementType, Set<ElementType>> COMPLEX_ELEMENT_COMPONENTS = Maps.newHashMap();

    /**
     * 注册元素类型
     * @param name 元素名称
     * @param element 元素实例
     * @return 元素的供应商
     */
    public static <T extends ElementType> Supplier<T> register(String name, Supplier<T> element) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MODID, name);
        return () -> {
            T elem = element.get();
            ELEMENTS.put(id, elem);
            return elem;
        };
    }

    /**
     * 获取所有已注册的元素
     * @return 所有元素的集合
     */
    public static Collection<ElementType> getElements() {
        return Collections.unmodifiableCollection(ELEMENTS.values());
    }

    /**
     * 根据ID获取元素
     * @param id 元素ID
     * @return 元素实例，如果不存在则返回null
     */
    public static ElementType getElement(ResourceLocation id) {
        return ELEMENTS.get(id);
    }

    /**
     * 根据名称获取元素
     * @param name 元素名称
     * @return 元素实例，如果不存在则返回null
     */
    public static ElementType getElement(String name) {
        return getElement(ResourceLocation.fromNamespaceAndPath(MODID, name));
    }

    /**
     * 注册复合元素
     * @param complexElement 复合元素
     * @param baseElements 构成复合元素的基础元素
     */
    public static void registerComplexElement(ElementType complexElement, Set<ElementType> baseElements) {
        if (!complexElement.isComplex()) {
            throw new IllegalArgumentException("Cannot register a non-complex element as a complex element: " + complexElement);
        }
        
        for (ElementType element : baseElements) {
            if (!element.isBasic()) {
                throw new IllegalArgumentException("Complex element components must be basic elements: " + element);
            }
        }
        
        // 存储复合关系
        COMPLEX_ELEMENTS.put(baseElements, complexElement);
        COMPLEX_ELEMENT_COMPONENTS.put(complexElement, baseElements);
    }

    /**
     * 获取由给定基础元素可以组合成的复合元素
     * @param baseElements 基础元素集合
     * @return 可以组合成的复合元素集合
     */
    public static Collection<ElementType> getComplexElements(Set<ElementType> baseElements) {
        return COMPLEX_ELEMENTS.get(baseElements);
    }

    /**
     * 获取复合元素的构成基础元素
     * @param complexElement 复合元素
     * @return 构成复合元素的基础元素集合
     */
    public static Set<ElementType> getComplexElementComponents(ElementType complexElement) {
        return COMPLEX_ELEMENT_COMPONENTS.getOrDefault(complexElement, Collections.emptySet());
    }

    /**
     * 检查两个元素是否可以组合成复合元素
     * @param element1 第一个元素
     * @param element2 第二个元素
     * @return 是否可以组合成复合元素
     */
    public static boolean canCombine(ElementType element1, ElementType element2) {
        Set<ElementType> elements = new HashSet<>(2);
        elements.add(element1);
        elements.add(element2);
        return !getComplexElements(elements).isEmpty();
    }

    /**
     * 组合两个元素，返回生成的复合元素
     * @param element1 第一个元素
     * @param element2 第二个元素
     * @return 生成的复合元素，如果无法组合则返回null
     */
    public static ElementType combineElements(ElementType element1, ElementType element2) {
        Set<ElementType> elements = new HashSet<>(2);
        elements.add(element1);
        elements.add(element2);
        
        Collection<ElementType> complexElements = getComplexElements(elements);
        if (complexElements.isEmpty()) {
            return null;
        }
        
        // 返回第一个匹配的复合元素（应该只有一个）
        return complexElements.iterator().next();
    }

    /**
     * 初始化元素注册表
     * 应该在FMLCommonSetupEvent中调用
     */
    public static void init() {
        // 注册所有复合元素关系
        registerComplexElements();
    }

    /**
     * 注册所有复合元素关系
     */
    private static void registerComplexElements() {
        // 注册爆炸元素（火焰 + 冰冻）
        Set<ElementType> blastComponents = new HashSet<>(2);
        blastComponents.add(ElementType.HEAT);
        blastComponents.add(ElementType.COLD);
        registerComplexElement(ElementType.BLAST, blastComponents);
        
        // 注册腐蚀元素（火焰 + 毒素）
        Set<ElementType> corrosiveComponents = new HashSet<>(2);
        corrosiveComponents.add(ElementType.HEAT);
        corrosiveComponents.add(ElementType.TOXIN);
        registerComplexElement(ElementType.CORROSIVE, corrosiveComponents);
        
        // 注册毒气元素（毒素 + 冰冻）
        Set<ElementType> gasComponents = new HashSet<>(2);
        gasComponents.add(ElementType.TOXIN);
        gasComponents.add(ElementType.COLD);
        registerComplexElement(ElementType.GAS, gasComponents);
        
        // 注册磁力元素（电击 + 冰冻）
        Set<ElementType> magneticComponents = new HashSet<>(2);
        magneticComponents.add(ElementType.ELECTRICITY);
        magneticComponents.add(ElementType.COLD);
        registerComplexElement(ElementType.MAGNETIC, magneticComponents);
        
        // 注册辐射元素（火焰 + 电击）
        Set<ElementType> radiationComponents = new HashSet<>(2);
        radiationComponents.add(ElementType.HEAT);
        radiationComponents.add(ElementType.ELECTRICITY);
        registerComplexElement(ElementType.RADIATION, radiationComponents);
        
        // 注册病毒元素（毒素 + 电击）
        Set<ElementType> viralComponents = new HashSet<>(2);
        viralComponents.add(ElementType.TOXIN);
        viralComponents.add(ElementType.ELECTRICITY);
        registerComplexElement(ElementType.VIRAL, viralComponents);
    }

    /**
     * 获取元素类型对应的属性
     * @param elementType 元素类型
     * @return 属性对象，如果未注册则返回null
     */
    public static ElementAttribute getAttributeValue(ElementType elementType) {
        return ElementAttributes.getAttributeValue(elementType);
    }
    
    /**
     * 获取元素类型对应的属性的RegistryObject
     * @param elementType 元素类型
     * @return 属性的RegistryObject，如果未注册则返回null
     */
    public static net.minecraftforge.registries.RegistryObject<ElementAttribute> getAttribute(ElementType elementType) {
        return ElementAttributes.getAttribute(elementType);
    }

    /**
     * 注册元素类型注册表
     * @param bus 事件总线
     */
    public static void register(IEventBus bus) {
        // 初始化元素属性
        ElementAttributes.bootstrap();
    }
    
    /**
     * 获取元素修饰符的UUID
     * 使用ElementUUIDManager生成固定的UUID
     * 
     * @param elementType 元素类型
     * @param index 索引
     * @return 元素修饰符的UUID
     */
    public static UUID getModifierUUID(ElementType elementType, int index) {
        // 生成UUID的名称，确保相同的元素类型和索引生成相同的UUID
        String name = MODID + ":" + elementType.getName() + ":" + index;
        return ElementUUIDManager.getElementUUID(name);
    }
    
    /**
     * 获取元素修饰符的名称
     * 
     * @param elementType 元素类型
     * @param index 索引
     * @return 元素修饰符的名称
     */
    public static String getModifierName(ElementType elementType, int index) {
        return MODID + ":" + elementType.getName() + ":modifier:" + index;
    }
}