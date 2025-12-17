package com.xlxyvergil.hamstercore.handler;


import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.handler.modifier.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;



/**
 * 元素伤害管理器
 * 负责管理武器元素数据的计算
 * 使用 AffixCacheManager 进行数据缓存
 */
public class ElementDamageManager {
    
    
    /**
     * 计算元素伤害
     * @param attacker 攻击者
     * @param target 目标实体
     * @param baseDamage 基础伤害
     * @param weapon 武器物品
     * @param targetFaction 目标派系
     * @param targetArmor 目标护甲值
     * @param cacheData 缓存的元素数据
     * @return 元素伤害数据
     */
    public static ElementDamageData calculateElementDamage(LivingEntity attacker, LivingEntity target, float baseDamage, ItemStack weapon, String targetFaction, Double targetArmor, AffixCacheManager.AffixCacheData cacheData) {
        // 直接计算元素伤害，不再缓存
        return calculateElementDamageInternal(attacker, target, baseDamage, weapon, targetFaction, targetArmor, cacheData);
    }

    
    /**
     * 计算元素伤害的核心逻辑
     * @param attacker 攻击者
     * @param target 目标实体
     * @param baseDamage 基础伤害
     * @param weapon 武器物品
     * @param targetFaction 目标派系
     * @param targetArmor 目标护甲值
     * @param cacheData 缓存的元素数据
     * @return 元素伤害数据
     */
    private static ElementDamageData calculateElementDamageInternal(LivingEntity attacker, LivingEntity target, float baseDamage, ItemStack weapon, String targetFaction, Double targetArmor, AffixCacheManager.AffixCacheData cacheData) {
        ElementDamageData damageData = new ElementDamageData(baseDamage);
        
        // 对于空的武器栈，直接返回基础数据
        if (weapon.isEmpty()) {
            damageData.finalDamage = baseDamage;
            return damageData;
        }
        
        // 获取武器数据
        WeaponData data = WeaponDataManager.loadElementData(weapon);
        
        // 计算各部分的伤害修正系数，使用传递的缓存数据，并存储详细结果
        ModifierResults modifierResults = new ModifierResults();
        
        // 计算派系克制
        FactionModifierCalculator.FactionResult factionResult = FactionModifierCalculator.calculateFactionModifier(data, targetFaction, cacheData);
        damageData.factionModifier = factionResult.getFactionModifier();
        modifierResults.setFactionModifier(factionResult.getFactionModifier());
        modifierResults.setFactionBreakdown(factionResult.getBreakdown());
        
        // 计算元素倍率
        ElementMultiplierCalculator.ElementResult elementResult = ElementMultiplierCalculator.calculateElementMultiplier(attacker, cacheData);
        damageData.elementMultiplier = elementResult.getElementMultiplier();
        modifierResults.setElementMultiplier(elementResult.getElementMultiplier());
        modifierResults.setElementBreakdown(elementResult.getBreakdown());
        
        // 计算物理元素倍率
        PhysicalElementMultiplierCalculator.PhysicalElementResult physicalResult = PhysicalElementMultiplierCalculator.calculatePhysicalElementMultiplier(attacker, cacheData);
        damageData.physicalElementMultiplier = physicalResult.getPhysicalElementMultiplier();
        modifierResults.setPhysicalElementMultiplier(physicalResult.getPhysicalElementMultiplier());
        modifierResults.setPhysicalElementBreakdown(physicalResult.getBreakdown());
        
        // 计算暴击倍率和暴击信息
        CriticalMultiplierCalculator.CriticalResult criticalResult = CriticalMultiplierCalculator.calculateCriticalMultiplier(attacker, weapon, null, cacheData);
        damageData.criticalMultiplier = criticalResult.getMultiplier();
        damageData.setCriticalInfo(criticalResult.getLevel(), criticalResult.getDamage());
        modifierResults.setCriticalMultiplier(criticalResult.getMultiplier());
        modifierResults.setCriticalLevel(criticalResult.getLevel());
        modifierResults.setCriticalDamage(criticalResult.getDamage());
        modifierResults.setCriticalChance(cacheData.getCriticalStats().getOrDefault("critical_chance", 0.0));
        
        // 计算护甲减免
        ArmorReductionCalculator.ArmorReductionResult armorResult = ArmorReductionCalculator.calculateArmorReduction(target, targetArmor);
        damageData.armorReduction = armorResult.getArmorReduction();
        modifierResults.setArmorReduction(armorResult.getArmorReduction());
        modifierResults.setArmorValue(armorResult.getArmorValue());
        modifierResults.setArmorModifier(armorResult.getArmorModifier());
        
        // 存储所有modifier结果
        damageData.setModifierResults(modifierResults);
        
        // 如果武器没有元素属性，则只应用护甲减免（不应用元素相关的修正）
        if (data == null) {
            // 无元素武器：只应用护甲减免
            damageData.finalDamage = (float) (baseDamage * damageData.armorReduction);
            return damageData;
        }
        
        
        
        // 计算最终伤害：将物理元素总倍率与元素倍率相加（减去1.0是因为两者都以1.0为基准）
        damageData.finalDamage = (float) (baseDamage * (1.0 + damageData.factionModifier) 
                                         * (damageData.elementMultiplier + damageData.physicalElementMultiplier) 
                                         * damageData.criticalMultiplier 
                                         * damageData.armorReduction);
        
        // 确保伤害不会小于0
        if (damageData.finalDamage < 0) {
            damageData.finalDamage = 0;
        }
        
        return damageData;
    }
    
    
    
    
    /**
     * 统一的 modifier 计算结果类
     */
    public static class ModifierResults {
        // 派系克制结果
        private double factionModifier; // HM 总克制系数
        private Map<String, Double> factionBreakdown; // 各派系元素的克制系数分解
        
        // 元素倍率结果
        private double elementMultiplier; // 元素总倍率
        private Map<String, Double> elementBreakdown; // 各元素的倍率分解
        
        // 物理元素倍率结果
        private double physicalElementMultiplier; // 物理元素总倍率
        private Map<String, Double> physicalElementBreakdown; // 各物理元素的倍率分解
        
        // 暴击结果
        private double criticalMultiplier; // 暴击倍率
        private int criticalLevel; // 暴击等级
        private double criticalDamage; // 暴击伤害值
        private double criticalChance; // 暴击率
        
        // 护甲减免结果
        private double armorReduction; // (1-AM) 护甲减免系数
        private double armorValue; // 原始护甲值
        private double armorModifier; // AM值
        
        public ModifierResults() {
            this.factionModifier = 0.0;
            this.elementMultiplier = 0.0;
            this.physicalElementMultiplier = 0.0;
            this.criticalMultiplier = 1.0;
            this.criticalLevel = 0;
            this.criticalDamage = 0.0;
            this.criticalChance = 0.0;
            this.armorReduction = 1.0;
            this.armorValue = 0.0;
            this.armorModifier = 0.0;
            this.factionBreakdown = new HashMap<>();
            this.elementBreakdown = new HashMap<>();
            this.physicalElementBreakdown = new HashMap<>();
        }
        
        // Getters and Setters
        public double getFactionModifier() { return factionModifier; }
        public void setFactionModifier(double factionModifier) { this.factionModifier = factionModifier; }
        
        public Map<String, Double> getFactionBreakdown() { return factionBreakdown; }
        public void setFactionBreakdown(Map<String, Double> factionBreakdown) { this.factionBreakdown = factionBreakdown; }
        
        public double getElementMultiplier() { return elementMultiplier; }
        public void setElementMultiplier(double elementMultiplier) { this.elementMultiplier = elementMultiplier; }
        
        public Map<String, Double> getElementBreakdown() { return elementBreakdown; }
        public void setElementBreakdown(Map<String, Double> elementBreakdown) { this.elementBreakdown = elementBreakdown; }
        
        public double getPhysicalElementMultiplier() { return physicalElementMultiplier; }
        public void setPhysicalElementMultiplier(double physicalElementMultiplier) { this.physicalElementMultiplier = physicalElementMultiplier; }
        
        public Map<String, Double> getPhysicalElementBreakdown() { return physicalElementBreakdown; }
        public void setPhysicalElementBreakdown(Map<String, Double> physicalElementBreakdown) { this.physicalElementBreakdown = physicalElementBreakdown; }
        
        public double getCriticalMultiplier() { return criticalMultiplier; }
        public void setCriticalMultiplier(double criticalMultiplier) { this.criticalMultiplier = criticalMultiplier; }
        
        public int getCriticalLevel() { return criticalLevel; }
        public void setCriticalLevel(int criticalLevel) { this.criticalLevel = criticalLevel; }
        
        public double getCriticalDamage() { return criticalDamage; }
        public void setCriticalDamage(double criticalDamage) { this.criticalDamage = criticalDamage; }
        
        public double getCriticalChance() { return criticalChance; }
        public void setCriticalChance(double criticalChance) { this.criticalChance = criticalChance; }
        
        public double getArmorReduction() { return armorReduction; }
        public void setArmorReduction(double armorReduction) { this.armorReduction = armorReduction; }
        
        public double getArmorValue() { return armorValue; }
        public void setArmorValue(double armorValue) { this.armorValue = armorValue; }
        
        public double getArmorModifier() { return armorModifier; }
        public void setArmorModifier(double armorModifier) { this.armorModifier = armorModifier; }
    }
    
    /**
     * 元素伤害数据类
     */
    public static class ElementDamageData {
        private final float baseDamage;
        private float finalDamage;
        private double factionModifier;
        private double elementMultiplier;
        private double physicalElementMultiplier;
        private double criticalMultiplier;
        private double armorReduction;
        private int criticalLevel;
        private double criticalDamage;
        private ModifierResults modifierResults;
        
        public ElementDamageData(float baseDamage) {
            this.baseDamage = baseDamage;
            this.finalDamage = baseDamage;
            this.factionModifier = 0.0;
            this.elementMultiplier = 1.0;
            this.physicalElementMultiplier = 1.0;
            this.criticalMultiplier = 1.0;
            this.armorReduction = 1.0;
            this.criticalLevel = 0;
            this.criticalDamage = 0.0;
            this.modifierResults = new ModifierResults();
        }
        
        // Getters
        public float getBaseDamage() {
            return baseDamage;
        }
        
        public float getFinalDamage() {
            return finalDamage;
        }
        
        public double getFactionModifier() {
            return factionModifier;
        }
        
        public double getElementMultiplier() {
            return elementMultiplier;
        }
        
        public double getPhysicalElementMultiplier() {
            return physicalElementMultiplier;
        }
        
        public double getCriticalMultiplier() {
            return criticalMultiplier;
        }
        
        public double getArmorReduction() {
            return armorReduction;
        }
        
        public int getCriticalLevel() {
            return criticalLevel;
        }
        
        public double getCriticalDamage() {
            return criticalDamage;
        }
        
        /**
         * 设置暴击等级和暴击伤害
         * @param criticalLevel 暴击等级
         * @param criticalDamage 暴击伤害
         */
        public void setCriticalInfo(int criticalLevel, double criticalDamage) {
            this.criticalLevel = criticalLevel;
            this.criticalDamage = criticalDamage;
        }
        
        public ModifierResults getModifierResults() {
            return modifierResults;
        }
        
        public void setModifierResults(ModifierResults modifierResults) {
            this.modifierResults = modifierResults;
        }
    }
}