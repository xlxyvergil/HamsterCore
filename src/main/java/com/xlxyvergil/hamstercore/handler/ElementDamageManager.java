package com.xlxyvergil.hamstercore.handler;

import com.xlxyvergil.hamstercore.util.ElementNBTUtils;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.WeaponData;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.handler.modifier.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.util.Set;
import java.util.concurrent.ExecutionException;


/**
 * 元素伤害管理器
 * 负责管理武器元素数据的计算和缓存
 * 参考TACZ的附件缓存系统设计
 */
public class ElementDamageManager {
    
    
    // 使用ElementCache缓存计算结果
    private static final ElementCache<ElementDamageKey, ElementDamageData> DAMAGE_CACHE = new ElementCache<>(ElementDamageManager::calculateElementDamageImpl);
    
    // 元素列表缓存
    private static final ElementCache<ItemStack, List<Map.Entry<ElementType, Double>>> ACTIVE_ELEMENTS_CACHE = new ElementCache<>(ElementDamageManager::getActiveElementsImpl);
    

    
    /**
     * 计算元素伤害
     * @param attacker 攻击者
     * @param target 目标实体
     * @param baseDamage 基础伤害
     * @param weapon 武器物品
     * @param targetFaction 目标派系
     * @param targetArmor 目标护甲值
     * @return 元素伤害数据
     */
    public static ElementDamageData calculateElementDamage(LivingEntity attacker, LivingEntity target, float baseDamage, ItemStack weapon, String targetFaction, Double targetArmor) {
        // 构建缓存键
        ElementDamageKey key = new ElementDamageKey(attacker, target, baseDamage, weapon, targetFaction, targetArmor);
        
        try {
            // 尝试从缓存中获取结果
            ElementCache.CacheValue<ElementDamageData> cached = DAMAGE_CACHE.get(key);
            return cached.orElse(calculateElementDamageInternal(attacker, target, baseDamage, weapon, targetFaction, targetArmor));
        } catch (Exception e) {
            // 如果缓存出错，则直接计算而不使用缓存
            return calculateElementDamageInternal(attacker, target, baseDamage, weapon, targetFaction, targetArmor);
        }
    }
    

    
    /**
     * 计算元素伤害的核心逻辑
     * @param attacker 攻击者
     * @param target 目标实体
     * @param baseDamage 基础伤害
     * @param weapon 武器物品
     * @param targetFaction 目标派系
     * @param targetArmor 目标护甲值
     * @return 元素伤害数据
     */
    private static ElementDamageData calculateElementDamageInternal(LivingEntity attacker, LivingEntity target, float baseDamage, ItemStack weapon, String targetFaction, Double targetArmor) {
        ElementDamageData damageData = new ElementDamageData(baseDamage);
        
        // 对于空的武器栈，直接返回基础数据
        if (weapon.isEmpty()) {
            damageData.finalDamage = baseDamage;
            return damageData;
        }
        
        // 获取武器数据并重新计算Usage层数据以确保准确性
        WeaponData data = WeaponDataManager.loadElementData(weapon);
        
        // 计算各部分的伤害修正系数
        damageData.factionModifier = FactionModifierCalculator.calculateFactionModifier(weapon, targetFaction); // HM
        damageData.elementMultiplier = ElementMultiplierCalculator.calculateElementMultiplier(attacker); // 元素总倍率
        damageData.criticalMultiplier = CriticalMultiplierCalculator.calculateCriticalMultiplier(attacker, weapon); // 暴击伤害
        damageData.armorReduction = ArmorReductionCalculator.calculateArmorReduction(target, targetArmor); // (1-AM)
        
        // 如果武器没有元素属性，则只应用护甲减免（不应用元素相关的修正）
        if (!ElementNBTUtils.hasAnyElements(weapon)) {
            // 无元素武器：只应用护甲减免
            damageData.finalDamage = (float) (baseDamage * damageData.armorReduction);
            return damageData;
        }
        
        // 设置激活的元素列表
        damageData.setActiveElements(getActiveElements(weapon));
        
        // 计算最终伤害
        damageData.finalDamage = (float) (baseDamage * (1.0 + damageData.factionModifier) 
                                         * damageData.elementMultiplier * damageData.criticalMultiplier 
                                         * damageData.armorReduction);
        
        // 确保伤害不会小于0
        if (damageData.finalDamage < 0) {
            damageData.finalDamage = 0;
        }
        
        return damageData;
    }
    
    /**
     * 获取武器上激活的元素列表
     * @param weapon 武器物品
     * @return 激活的元素实例列表
     */
    public static List<Map.Entry<ElementType, Double>> getActiveElements(ItemStack weapon) {
        // 对于空的武器栈，直接返回空列表
        if (weapon.isEmpty()) {
            return new ArrayList<>();
        }
        
        ElementCache.CacheValue<List<Map.Entry<ElementType, Double>>> cached = ACTIVE_ELEMENTS_CACHE.get(weapon);
        return cached.orElse(new ArrayList<>());
    }
    
    /**
     * 获取武器上激活的元素列表的实际实现
     * @param weapon 武器物品
     * @return 激活的元素实例列表
     */
    private static List<Map.Entry<ElementType, Double>> getActiveElementsImpl(ItemStack weapon) {
        // 使用新的四层数据结构，但不强制重新计算Usage层数据
        WeaponData data = WeaponDataManager.loadElementData(weapon);
        
        // 如果没有元素数据，返回空列表
        if (data == null) {
            return new ArrayList<>();
        }
        
        // 将Usage层数据转换为ElementType和值的映射列表
        List<Map.Entry<ElementType, Double>> activeElements = new ArrayList<>();
        
        // 添加物理元素
        String[] physicalTypes = {"slash", "puncture", "impact"};
        for (String type : physicalTypes) {
            Double value = data.getUsageValue(type);
            if (value != null && value > 0) {
                ElementType elementType = ElementType.byName(type);
                if (elementType != null) {
                    activeElements.add(new HashMap.SimpleEntry<>(elementType, value));
                }
            }
        }
        
        // 添加基础元素（未被复合的）
        String[] basicTypes = {"heat", "cold", "electricity", "toxin"};
        for (String type : basicTypes) {
            Double value = data.getUsageValue(type);
            if (value != null && value > 0) {
                ElementType elementType = ElementType.byName(type);
                if (elementType != null) {
                    activeElements.add(new HashMap.SimpleEntry<>(elementType, value));
                }
            }
        }
        
        // 添加复合元素
        String[] complexTypes = {"blast", "corrosive", "gas", "magnetic", "radiation", "viral"};
        for (String type : complexTypes) {
            Double value = data.getUsageValue(type);
            if (value != null && value > 0) {
                ElementType elementType = ElementType.byName(type);
                if (elementType != null) {
                    activeElements.add(new HashMap.SimpleEntry<>(elementType, value));
                }
            }
        }
        
        // 添加特殊属性元素（包括派系元素）
        String[] specialTypes = {"critical_chance", "critical_damage", "trigger_chance", 
                                "grineer", "infested", "corpus", "orokin", "sentient", "murmur"};
        for (String type : specialTypes) {
            Double value = data.getUsageValue(type);
            if (value != null && value > 0) {
                ElementType elementType = ElementType.byName(type);
                if (elementType != null) {
                    activeElements.add(new HashMap.SimpleEntry<>(elementType, value));
                }
            }
        }
        
        return activeElements;
    }
    
    /**
     * 计算元素伤害的实际实现
     * @param key 缓存键
     * @return 元素伤害数据
     */
    private static ElementDamageData calculateElementDamageImpl(ElementDamageKey key) {
        LivingEntity attacker = key.getAttacker();
        LivingEntity target = key.getTarget();
        float baseDamage = key.getBaseDamage();
        ItemStack weapon = key.getWeapon();
        String targetFaction = key.getTargetFaction();
        Double targetArmor = key.getTargetArmor();
        
        return calculateElementDamageInternal(attacker, target, baseDamage, weapon, targetFaction, targetArmor);
    }
    
    /**
     * 使指定武器的缓存失效
     * @param weapon 武器物品
     */
    public static void invalidateCache(ItemStack weapon) {
        // 注意：由于我们使用的是自定义键，无法直接使特定武器的缓存失效
        // 在实际应用中，可以考虑使用更复杂的缓存策略
        DAMAGE_CACHE.invalidateAll();
    }
    
    /**
     * 使所有缓存失效
     */
    public static void invalidateAllCache() {
        DAMAGE_CACHE.invalidateAll();
    }
    
    /**
     * 元素伤害数据类
     */
    public static class ElementDamageData {
        private final float baseDamage;
        private float finalDamage;
        private double factionModifier;
        private double elementMultiplier;
        private double criticalMultiplier;
        private double armorReduction;
        private List<Map.Entry<ElementType, Double>> activeElements;
        
        public ElementDamageData(float baseDamage) {
            this.baseDamage = baseDamage;
            this.finalDamage = baseDamage;
            this.factionModifier = 0.0;
            this.elementMultiplier = 1.0;
            this.criticalMultiplier = 1.0;
            this.armorReduction = 1.0;
            this.activeElements = new ArrayList<>();
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
        
        public double getCriticalMultiplier() {
            return criticalMultiplier;
        }
        
        public double getArmorReduction() {
            return armorReduction;
        }
        
        /**
         * 获取激活的元素列表
         * @return 激活的元素实例列表
         */
        public List<Map.Entry<ElementType, Double>> getActiveElements() {
            return activeElements;
        }
        
        /**
         * 设置激活的元素列表
         * @param activeElements 激活的元素实例列表
         */
        public void setActiveElements(List<Map.Entry<ElementType, Double>> activeElements) {
            this.activeElements = activeElements;
        }
    }
    
    /**
     * 元素伤害计算缓存键
     */
    private static class ElementDamageKey {
        private final LivingEntity attacker;
        private final LivingEntity target;
        private final float baseDamage;
        private final ItemStack weapon;
        private final String targetFaction;
        private final Double targetArmor;
        
        public ElementDamageKey(LivingEntity attacker, LivingEntity target, float baseDamage, ItemStack weapon, String targetFaction, Double targetArmor) {
            this.attacker = attacker;
            this.target = target;
            this.baseDamage = baseDamage;
            this.weapon = weapon;
            this.targetFaction = targetFaction;
            this.targetArmor = targetArmor;
        }
        
        public LivingEntity getAttacker() {
            return attacker;
        }
        
        public LivingEntity getTarget() {
            return target;
        }
        
        public float getBaseDamage() {
            return baseDamage;
        }
        
        public ItemStack getWeapon() {
            return weapon;
        }
        
        public String getTargetFaction() {
            return targetFaction;
        }
        
        public Double getTargetArmor() {
            return targetArmor;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            
            ElementDamageKey that = (ElementDamageKey) obj;
            
            if (Float.compare(that.baseDamage, baseDamage) != 0) return false;
            if (!attacker.equals(that.attacker)) return false;
            if (!target.equals(that.target)) return false;
            if (!ItemStack.matches(weapon, that.weapon)) return false;
            if (targetFaction != null ? !targetFaction.equals(that.targetFaction) : that.targetFaction != null) return false;
            return targetArmor != null ? targetArmor.equals(that.targetArmor) : that.targetArmor == null;
        }
        
        @Override
        public int hashCode() {
            int result = attacker.hashCode();
            result = 31 * result + target.hashCode();
            result = 31 * result + (baseDamage != 0.0f ? Float.floatToIntBits(baseDamage) : 0);
            result = 31 * result + (weapon.isEmpty() ? 0 : weapon.getItem().hashCode());
            result = 31 * result + (targetFaction != null ? targetFaction.hashCode() : 0);
            result = 31 * result + (targetArmor != null ? targetArmor.hashCode() : 0);
            return result;
        }
    }
}