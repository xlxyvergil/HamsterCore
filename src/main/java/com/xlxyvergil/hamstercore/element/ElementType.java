package com.xlxyvergil.hamstercore.element;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 定义所有可用的元素类型常量
 * 包括物理元素、基础元素、复合元素和特殊属性
 */
public enum ElementType {
    // 物理元素
    IMPACT("impact", "冲击", ChatFormatting.GRAY),
    PUNCTURE("puncture", "穿刺", ChatFormatting.LIGHT_PURPLE),
    SLASH("slash", "切割", ChatFormatting.RED),
    
    // 基础元素
    COLD("cold", "冰冻", ChatFormatting.AQUA),
    ELECTRICITY("electricity", "电击", ChatFormatting.YELLOW),
    HEAT("heat", "火焰", ChatFormatting.GOLD),
    TOXIN("toxin", "毒素", ChatFormatting.DARK_GREEN),
    
    // 复合元素
    BLAST("blast", "爆炸", ChatFormatting.DARK_RED),
    CORROSIVE("corrosive", "腐蚀", ChatFormatting.DARK_GRAY),
    GAS("gas", "毒气", ChatFormatting.GREEN),
    MAGNETIC("magnetic", "磁力", ChatFormatting.BLUE),
    RADIATION("radiation", "辐射", ChatFormatting.WHITE),
    VIRAL("viral", "病毒", ChatFormatting.DARK_PURPLE),
    
    // 特殊属性
    CRITICAL_CHANCE("critical_chance", "暴击率", ChatFormatting.BOLD),
    CRITICAL_DAMAGE("critical_damage", "暴击伤害", ChatFormatting.BOLD),
    TRIGGER_CHANCE("trigger_chance", "触发率", ChatFormatting.BOLD);
    
    private final String name;
    private final String displayName;
    private final ChatFormatting color;
    
    ElementType(String name, String displayName, ChatFormatting color) {
        this.name = name;
        this.displayName = displayName;
        this.color = color;
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
        return this == IMPACT || this == PUNCTURE || this == SLASH;
    }
    
    /**
     * 检查是否为基础元素
     */
    public boolean isBasic() {
        return this == COLD || this == ELECTRICITY || this == HEAT || this == TOXIN;
    }
    
    /**
     * 检查是否为复合元素
     */
    public boolean isComplex() {
        return this == BLAST || this == CORROSIVE || this == GAS || 
               this == MAGNETIC || this == RADIATION || this == VIRAL;
    }
    
    /**
     * 检查是否为特殊属性
     */
    public boolean isSpecial() {
        return this == CRITICAL_CHANCE || this == CRITICAL_DAMAGE || this == TRIGGER_CHANCE;
    }
    
    /**
     * 获取组成此复合元素的基础元素组合
     * 例如：爆炸 = 火焰 + 冰冻
     */
    public List<ElementType> getComposition() {
        switch (this) {
            case BLAST: return Arrays.asList(HEAT, COLD);
            case CORROSIVE: return Arrays.asList(ELECTRICITY, TOXIN);
            case GAS: return Arrays.asList(HEAT, TOXIN);
            case MAGNETIC: return Arrays.asList(COLD, ELECTRICITY);
            case RADIATION: return Arrays.asList(HEAT, ELECTRICITY);
            case VIRAL: return Arrays.asList(COLD, TOXIN);
            default: return Arrays.asList();
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
        if ((element1 == HEAT && element2 == COLD) || (element1 == COLD && element2 == HEAT)) {
            return BLAST; // 爆炸（火焰+冰冻）
        }
        if ((element1 == ELECTRICITY && element2 == TOXIN) || (element1 == TOXIN && element2 == ELECTRICITY)) {
            return CORROSIVE; // 腐蚀（电击+毒素）
        }
        if ((element1 == HEAT && element2 == TOXIN) || (element1 == TOXIN && element2 == HEAT)) {
            return GAS; // 毒气（火焰+毒素）
        }
        if ((element1 == COLD && element2 == ELECTRICITY) || (element1 == ELECTRICITY && element2 == COLD)) {
            return MAGNETIC; // 磁力（冰冻+电击）
        }
        if ((element1 == HEAT && element2 == ELECTRICITY) || (element1 == ELECTRICITY && element2 == HEAT)) {
            return RADIATION; // 辐射（火焰+电击）
        }
        if ((element1 == COLD && element2 == TOXIN) || (element1 == TOXIN && element2 == COLD)) {
            return VIRAL; // 病毒（冰冻+毒素）
        }
        
        return null; // 没有匹配的复合元素
    }
    
    /**
     * 获取所有物理元素
     */
    public static List<ElementType> getPhysicalElements() {
        return Arrays.stream(values())
                .filter(ElementType::isPhysical)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有基础元素
     */
    public static List<ElementType> getBasicElements() {
        return Arrays.stream(values())
                .filter(ElementType::isBasic)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有复合元素
     */
    public static List<ElementType> getComplexElements() {
        return Arrays.stream(values())
                .filter(ElementType::isComplex)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据名称获取元素类型
     */
    public static ElementType byName(String name) {
        for (ElementType type : values()) {
            if (type.getName().equals(name)) {
                return type;
            }
        }
        return null;
    }
}