package com.xlxyvergil.hamstercore.config;

import com.google.gson.annotations.SerializedName;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 武器配置数据类
 * 存储武器的基础属性和元素配比
 */
public class WeaponConfig {
    @SerializedName("criticalChance")
    private double criticalChance = 0.1; // 默认10%暴击率
    
    @SerializedName("criticalDamage")
    private double criticalDamage = 0.5; // 默认50%暴击伤害
    
    @SerializedName("triggerChance")
    private double triggerChance = 0.1; // 默认10%触发率
    
    @SerializedName("elementRatios")
    private Map<String, Double> elementRatios = new HashMap<>(); // 元素伤害配比
    
    public WeaponConfig() {
        // 默认物理元素配比：切割40%，穿刺30%，冲击30%
        elementRatios.put("slash", 0.4);
        elementRatios.put("puncture", 0.3);
        elementRatios.put("impact", 0.3);
    }
    
    /**
     * 获取暴击率
     */
    public double getCriticalChance() {
        return criticalChance;
    }
    
    /**
     * 设置暴击率
     */
    public void setCriticalChance(double criticalChance) {
        this.criticalChance = criticalChance;
    }
    
    /**
     * 获取暴击伤害
     */
    public double getCriticalDamage() {
        return criticalDamage;
    }
    
    /**
     * 设置暴击伤害
     */
    public void setCriticalDamage(double criticalDamage) {
        this.criticalDamage = criticalDamage;
    }
    
    /**
     * 获取触发率
     */
    public double getTriggerChance() {
        return triggerChance;
    }
    
    /**
     * 设置触发率
     */
    public void setTriggerChance(double triggerChance) {
        this.triggerChance = triggerChance;
    }
    
    /**
     * 获取元素伤害配比
     */
    public Map<String, Double> getElementRatios() {
        return new HashMap<>(elementRatios);
    }
    
    /**
     * 设置元素伤害配比
     */
    public void setElementRatios(Map<String, Double> elementRatios) {
        this.elementRatios = new HashMap<>(elementRatios);
    }
    
    /**
     * 添加或更新元素配比
     */
    public void setElementRatio(String elementName, double ratio) {
        elementRatios.put(elementName, ratio);
    }
    
    /**
     * 获取指定元素的配比
     */
    public double getElementRatio(ElementType type) {
        return elementRatios.getOrDefault(type.getName(), 0.0);
    }
    
    /**
     * 获取指定元素名称的配比
     */
    public double getElementRatio(String elementName) {
        return elementRatios.getOrDefault(elementName, 0.0);
    }
    
    /**
     * 根据基础伤害和配比计算实际元素伤害值
     * @param baseDamage 基础伤害值
     * @param elementRatios 元素配比
     * @return 实际元素伤害值映射
     */
    public static Map<String, Double> calculateActualElementDamages(double baseDamage, Map<String, Double> elementRatios) {
        Map<String, Double> actualElementDamages = new HashMap<>();
        for (Map.Entry<String, Double> entry : elementRatios.entrySet()) {
            String elementName = entry.getKey();
            double ratio = entry.getValue();
            double actualDamage = baseDamage * ratio;
            actualElementDamages.put(elementName, actualDamage);
        }
        return actualElementDamages;
    }
    
    /**
     * 获取武器的基础攻击伤害值
     * @param stack 武器物品堆
     * @return 基础攻击伤害值
     */
    public static double getBaseAttackDamage(ItemStack stack) {
        // 检查是否是拔刀剑
        if (isSlasherSword(stack)) {
            return getSlasherSwordDamage(stack);
        }
        
        // 检查是否是TACZ枪械
        if (isTaczGun(stack)) {
            return getTaczGunDamage(stack);
        }
        
        // 检查是否是原版工具或武器
        if (isVanillaToolOrWeapon(stack)) {
            return getVanillaToolOrWeaponDamage(stack);
        }
        
        // 默认返回0
        return 0.0;
    }
    
    /**
     * 判断是否为拔刀剑
     */
    private static boolean isSlasherSword(ItemStack stack) {
        // 检查是否有拔刀剑特有的NBT标签
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            // 拔刀剑通常会有特定的NBT标签
            return tag.contains("SBTier") || tag.contains("SlashBlade");
        }
        return false;
    }
    
    /**
     * 获取拔刀剑的伤害值
     */
    private static double getSlasherSwordDamage(ItemStack stack) {
        // 对于拔刀剑，我们使用固定值或者其他简单方法
        // 这里可以根据实际情况调整
        return 4.0; // 示例值
    }
    
    /**
     * 判断是否为TACZ枪械
     */
    private static boolean isTaczGun(ItemStack stack) {
        // 检查是否有TACZ枪械特有的NBT标签
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            // TACZ枪械通常会有特定的NBT标签
            return tag.contains("tacz") || tag.contains("gun") || tag.contains("Gun");
        }
        return false;
    }
    
    /**
     * 获取TACZ枪械的伤害值
     */
    private static double getTaczGunDamage(ItemStack stack) {
        // 对于TACZ枪械，我们使用固定值或者其他简单方法
        // 这里可以根据实际情况调整
        return 8.0; // 示例值
    }
    
    /**
     * 判断是否为原版工具或武器
     */
    private static boolean isVanillaToolOrWeapon(ItemStack stack) {
        // 检查物品名称是否包含工具或武器关键词
        String itemName = stack.getItem().toString().toLowerCase();
        return itemName.contains("sword") || itemName.contains("pickaxe") || 
               itemName.contains("axe") || itemName.contains("shovel") || 
               itemName.contains("hoe") || itemName.contains("tool");
    }
    
    /**
     * 获取原版工具或武器的伤害值
     */
    private static double getVanillaToolOrWeaponDamage(ItemStack stack) {
        // 从物品属性中获取伤害值
        return stack.getAttributeModifiers(null).get(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)
            .stream().mapToDouble(modifier -> modifier.getAmount()).findFirst().orElse(0.0);
    }
    
    /**
     * 根据基础伤害值获取默认元素配比
     * @param stack 物品堆
     * @return 元素配比映射
     */
    public static Map<String, Double> getDefaultElementRatios(ItemStack stack) {
        // 检查是否是拔刀剑
        if (isSlasherSword(stack)) {
            // 拔刀剑使用切割为主的倍率
            Map<String, Double> ratios = new HashMap<>();
            ratios.put("slash", 0.7);   // 切割70%
            ratios.put("puncture", 0.2);  // 穿刺20%
            ratios.put("impact", 0.1);  // 冲击10%
            return normalizeRatios(ratios);
        }
        
        // 检查是否是TACZ枪械
        if (isTaczGun(stack)) {
            // TACZ枪械使用穿刺为主的倍率
            Map<String, Double> ratios = new HashMap<>();
            ratios.put("slash", 0.1);   // 切割10%
            ratios.put("puncture", 0.7);  // 穿刺70%
            ratios.put("impact", 0.2);  // 冲击20%
            return normalizeRatios(ratios);
        }
        
        // 检查是否是原版工具或武器
        if (isVanillaToolOrWeapon(stack)) {
            String itemName = stack.getItem().toString().toLowerCase();
            if (itemName.contains("sword")) {
                // 剑类使用切割为主的倍率
                Map<String, Double> ratios = new HashMap<>();
                ratios.put("slash", 0.6);   // 切割60%
                ratios.put("puncture", 0.2);  // 穿刺20%
                ratios.put("impact", 0.2);  // 冲击20%
                return normalizeRatios(ratios);
            } else if (itemName.contains("pickaxe") || itemName.contains("axe") || 
                      itemName.contains("shovel") || itemName.contains("hoe")) {
                // 工具类使用冲击为主的倍率
                Map<String, Double> ratios = new HashMap<>();
                ratios.put("slash", 0.2);   // 切割20%
                ratios.put("puncture", 0.2);  // 穿刺20%
                ratios.put("impact", 0.6);  // 冲击60%
                return normalizeRatios(ratios);
            }
        }
        
        // 默认倍率
        Map<String, Double> ratios = new HashMap<>();
        ratios.put("slash", 0.4);   // 切割40%
        ratios.put("puncture", 0.3);  // 穿刺30%
        ratios.put("impact", 0.3);  // 冲击30%
        return normalizeRatios(ratios);
    }
    
    /**
     * 标准化倍率，确保总和至少为1
     * @param ratios 原始倍率
     * @return 标准化后的倍率
     */
    private static Map<String, Double> normalizeRatios(Map<String, Double> ratios) {
        double sum = ratios.values().stream().mapToDouble(Double::doubleValue).sum();
        if (sum < 1.0) {
            // 如果总和小于1，则按比例放大至1
            Map<String, Double> normalized = new HashMap<>();
            for (Map.Entry<String, Double> entry : ratios.entrySet()) {
                normalized.put(entry.getKey(), entry.getValue() / sum);
            }
            return normalized;
        }
        return ratios;
    }
    
    /**
     * 根据武器类型和基础伤害值创建武器配置
     * @param stack 武器物品堆
     * @return 武器配置
     */
    public static WeaponConfig createWeaponConfig(ItemStack stack) {
        // 创建新配置
        WeaponConfig weaponConfig = new WeaponConfig();
        
        // 使用默认元素配比
        Map<String, Double> elementRatios = getDefaultElementRatios(stack);
        weaponConfig.setElementRatios(elementRatios);
        
        return weaponConfig;
    }
}