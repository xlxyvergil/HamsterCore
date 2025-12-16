package com.xlxyvergil.hamstercore.element;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 定义所有可用的元素类型
 * 包括物理元素、基础元素、复合元素和特殊属性
 * 重构为可扩展类，支持通过API注册新的元素类型
 */
public class ElementType {
    private static final Map<String, ElementType> REGISTRY = new ConcurrentHashMap<>();
    
    // 物理元素
    public static final ElementType IMPACT = registerBuiltIn("impact", "冲击", ChatFormatting.GRAY, TypeCategory.PHYSICAL);
    public static final ElementType PUNCTURE = registerBuiltIn("puncture", "穿刺", ChatFormatting.LIGHT_PURPLE, TypeCategory.PHYSICAL);
    public static final ElementType SLASH = registerBuiltIn("slash", "切割", ChatFormatting.RED, TypeCategory.PHYSICAL);
    
    // 基础元素
    public static final ElementType COLD = registerBuiltIn("cold", "冰冻", ChatFormatting.AQUA, TypeCategory.BASIC);
    public static final ElementType ELECTRICITY = registerBuiltIn("electricity", "电击", ChatFormatting.YELLOW, TypeCategory.BASIC);
    public static final ElementType HEAT = registerBuiltIn("heat", "火焰", ChatFormatting.GOLD, TypeCategory.BASIC);
    public static final ElementType TOXIN = registerBuiltIn("toxin", "毒素", ChatFormatting.DARK_GREEN, TypeCategory.BASIC);
    
    // 复合元素
    public static final ElementType BLAST = registerBuiltIn("blast", "爆炸", ChatFormatting.DARK_RED, TypeCategory.COMPLEX);
    public static final ElementType CORROSIVE = registerBuiltIn("corrosive", "腐蚀", ChatFormatting.DARK_GRAY, TypeCategory.COMPLEX);
    public static final ElementType GAS = registerBuiltIn("gas", "毒气", ChatFormatting.GREEN, TypeCategory.COMPLEX);
    public static final ElementType MAGNETIC = registerBuiltIn("magnetic", "磁力", ChatFormatting.BLUE, TypeCategory.COMPLEX);
    public static final ElementType RADIATION = registerBuiltIn("radiation", "辐射", ChatFormatting.WHITE, TypeCategory.COMPLEX);
    public static final ElementType VIRAL = registerBuiltIn("viral", "病毒", ChatFormatting.DARK_PURPLE, TypeCategory.COMPLEX);
    
    // 特殊属性
    public static final ElementType CRITICAL_CHANCE = registerBuiltIn("critical_chance", "暴击率", ChatFormatting.BOLD, TypeCategory.CRITICAL_CHANCE);
    public static final ElementType CRITICAL_DAMAGE = registerBuiltIn("critical_damage", "暴击伤害", ChatFormatting.BOLD, TypeCategory.CRITICAL_DAMAGE);
    public static final ElementType TRIGGER_CHANCE = registerBuiltIn("trigger_chance", "触发率", ChatFormatting.BOLD, TypeCategory.TRIGGER_CHANCE);
    
    // 派系元素
    public static final ElementType GRINEER = registerBuiltIn("grineer", "Grineer派系", ChatFormatting.RED, TypeCategory.SPECIAL);
    public static final ElementType INFESTED = registerBuiltIn("infested", "Infested派系", ChatFormatting.GREEN, TypeCategory.SPECIAL);
    public static final ElementType CORPUS = registerBuiltIn("corpus", "Corpus派系", ChatFormatting.BLUE, TypeCategory.SPECIAL);
    public static final ElementType OROKIN = registerBuiltIn("orokin", "Orokin派系", ChatFormatting.LIGHT_PURPLE, TypeCategory.SPECIAL);
    public static final ElementType SENTIENT = registerBuiltIn("sentient", "Sentient派系", ChatFormatting.DARK_RED, TypeCategory.SPECIAL);
    public static final ElementType MURMUR = registerBuiltIn("murmur", "Murmur派系", ChatFormatting.AQUA, TypeCategory.SPECIAL);

    private final String name;
    private final String displayName;
    private final ChatFormatting color;
    private final TypeCategory category;
    
    private ElementType(String name, String displayName, ChatFormatting color, TypeCategory category) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
        this.category = category;
    }
    
    private static ElementType registerBuiltIn(String name, String displayName, ChatFormatting color, TypeCategory category) {
        ElementType type = new ElementType(name, displayName, color, category);
        REGISTRY.put(name, type);
        return type;
    }
    
    public static ElementType register(String name, String displayName, ChatFormatting color, TypeCategory category) {
        if (REGISTRY.containsKey(name)) {
            throw new IllegalArgumentException("Duplicate element type registered: " + name);
        }
        ElementType type = new ElementType(name, displayName, color, category);
        REGISTRY.put(name, type);
        return type;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public ChatFormatting getColor() {
        return color;
    }
    
    public MutableComponent getColoredName() {
        return Component.translatable("element." + name + ".name").withStyle(color);
    }
    
    /**
     * 检查是否为物理元素
     */
    public boolean isPhysical() {
        return category == TypeCategory.PHYSICAL;
    }
    
    /**
     * 检查是否为基础元素
     */
    public boolean isBasic() {
        return category == TypeCategory.BASIC;
    }
    
    /**
     * 检查是否为复合元素
     */
    public boolean isComplex() {
        return category == TypeCategory.COMPLEX;
    }
    
    /**
     * 检查是否为特殊属性
     */
    public boolean isSpecial() {
        return category == TypeCategory.SPECIAL;
    }
    
    /**
     * 检查是否为暴击率属性
     */
    public boolean isCriticalChance() {
        return category == TypeCategory.CRITICAL_CHANCE;
    }
    
    /**
     * 检查是否为暴击伤害属性
     */
    public boolean isCriticalDamage() {
        return category == TypeCategory.CRITICAL_DAMAGE;
    }
    
    /**
     * 检查是否为触发率属性
     */
    public boolean isTriggerChance() {
        return category == TypeCategory.TRIGGER_CHANCE;
    }
    
    /**
     * 获取组成此复合元素的基础元素组合
     * 例如：爆炸 = 火焰 + 冰冻
     */
    public List<ElementType> getComposition() {
        if (!isComplex()) {
            return Collections.emptyList();
        }
        
        switch (this.getName()) {
            case "blast": return Arrays.asList(HEAT, COLD);
            case "corrosive": return Arrays.asList(ELECTRICITY, TOXIN);
            case "gas": return Arrays.asList(HEAT, TOXIN);
            case "magnetic": return Arrays.asList(COLD, ELECTRICITY);
            case "radiation": return Arrays.asList(HEAT, ELECTRICITY);
            case "viral": return Arrays.asList(COLD, TOXIN);
            default: return Collections.emptyList();
        }
    }
    
    /**
     * 从两个基础元素创建复合元素
     * @param element1 第一个基础元素
     * @param element2 第二个基础元素
     * @return 对应的复合元素，如果不匹配则返回null
     */
    public static ElementType createComplex(ElementType element1, ElementType element2) {
        if (!element1.isBasic() || !element2.isBasic()) {
            return null;
        }
        
        // 精确匹配复合元素组合
        Set<ElementType> elements = new HashSet<>(Arrays.asList(element1, element2));
        
        if (elements.contains(HEAT) && elements.contains(COLD)) {
            return BLAST; // 爆炸（火焰+冰冻）
        }
        if (elements.contains(ELECTRICITY) && elements.contains(TOXIN)) {
            return CORROSIVE; // 腐蚀（电击+毒素）
        }
        if (elements.contains(HEAT) && elements.contains(TOXIN)) {
            return GAS; // 毒气（火焰+毒素）
        }
        if (elements.contains(COLD) && elements.contains(ELECTRICITY)) {
            return MAGNETIC; // 磁力（冰冻+电击）
        }
        if (elements.contains(HEAT) && elements.contains(ELECTRICITY)) {
            return RADIATION; // 辐射（火焰+电击）
        }
        if (elements.contains(COLD) && elements.contains(TOXIN)) {
            return VIRAL; // 病毒（冰冻+毒素）
        }
        
        return null; // 没有匹配的复合元素
    }
    
    /**
     * 获取所有物理元素
     */
    public static Collection<ElementType> getPhysicalElements() {
        return REGISTRY.values().stream()
                .filter(ElementType::isPhysical)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有基础元素
     */
    public static Collection<ElementType> getBasicElements() {
        return REGISTRY.values().stream()
                .filter(ElementType::isBasic)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有复合元素
     */
    public static Collection<ElementType> getComplexElements() {
        return REGISTRY.values().stream()
                .filter(ElementType::isComplex)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有特殊属性元素
     */
    public static Collection<ElementType> getSpecialElements() {
        return REGISTRY.values().stream()
                .filter(ElementType::isSpecial)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取暴击率属性
     */
    public static Collection<ElementType> getCriticalChanceElements() {
        return REGISTRY.values().stream()
                .filter(ElementType::isCriticalChance)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取暴击伤害属性
     */
    public static Collection<ElementType> getCriticalDamageElements() {
        return REGISTRY.values().stream()
                .filter(ElementType::isCriticalDamage)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取触发率属性
     */
    public static Collection<ElementType> getTriggerChanceElements() {
        return REGISTRY.values().stream()
                .filter(ElementType::isTriggerChance)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据名称获取元素类型
     */
    public static ElementType byName(String name) {
        return REGISTRY.get(name);
    }
    
    /**
     * 获取所有已注册的元素类型
     */
    public static Collection<ElementType> getAllTypes() {
        return REGISTRY.values();
    }
    
    public enum TypeCategory {
        PHYSICAL,
        BASIC,
        COMPLEX,
        SPECIAL,
        CRITICAL_CHANCE,
        CRITICAL_DAMAGE,
        TRIGGER_CHANCE
    }

    public TypeCategory getTypeCategory() {
        return this.category;
    }

    public String name() {
        return this.name;
    }
}