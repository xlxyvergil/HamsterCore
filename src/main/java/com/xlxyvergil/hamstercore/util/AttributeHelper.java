package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.attribute.EntityAttributeRegistry;
import net.minecraft.world.entity.LivingEntity;

/**
 * 属性助手类
 * 用于获取实体身上的各种属性值
 */
public class AttributeHelper {
    

    
    /**
     * 获取实体的护盾值
     * @param entity 实体
     * @return 护盾值
     */
    public static double getShield(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.SHIELD.get()).getValue();
    }
    
    /**
     * 获取实体的护甲值
     * @param entity 实体
     * @return 护甲值
     */
    public static double getArmor(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.ARMOR.get()).getValue();
    }
    

    
    /**
     * 获取实体的护盾恢复速率
     * @param entity 实体
     * @return 护盾恢复速率（每秒恢复值）
     */
    public static double getRegenRate(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.REGEN_RATE.get()).getValue();
    }
    
    /**
     * 获取实体的护盾恢复延迟
     * @param entity 实体
     * @return 护盾恢复延迟（毫秒）
     */
    public static double getRegenDelay(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.REGEN_DELAY.get()).getValue();
    }
    
    /**
     * 获取实体的护盾耗尽恢复延迟
     * @param entity 实体
     * @return 护盾耗尽恢复延迟（毫秒）
     */
    public static double getDepletedRegenDelay(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.DEPLETED_REGEN_DELAY.get()).getValue();
    }
    
    /**
     * 获取实体的护盾保险时间
     * @param entity 实体
     * @return 护盾保险时间
     */
    public static double getImmunityTime(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.IMMUNITY_TIME.get()).getValue();
    }
    
    // 元素属性获取方法
    
    // 物理元素
    /**
     * 获取实体的冲击属性值
     * @param entity 实体
     * @return 冲击属性值
     */
    public static double getImpact(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.IMPACT.get()).getValue();
    }
    
    /**
     * 获取实体的穿刺属性值
     * @param entity 实体
     * @return 穿刺属性值
     */
    public static double getPuncture(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.PUNCTURE.get()).getValue();
    }
    
    /**
     * 获取实体的切割属性值
     * @param entity 实体
     * @return 切割属性值
     */
    public static double getSlash(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.SLASH.get()).getValue();
    }
    
    // 基础元素
    /**
     * 获取实体的冰冻属性值
     * @param entity 实体
     * @return 冰冻属性值
     */
    public static double getCold(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.COLD.get()).getValue();
    }
    
    /**
     * 获取实体的电击属性值
     * @param entity 实体
     * @return 电击属性值
     */
    public static double getElectricity(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.ELECTRICITY.get()).getValue();
    }
    
    /**
     * 获取实体的火焰属性值
     * @param entity 实体
     * @return 火焰属性值
     */
    public static double getHeat(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.HEAT.get()).getValue();
    }
    
    /**
     * 获取实体的毒素属性值
     * @param entity 实体
     * @return 毒素属性值
     */
    public static double getToxin(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.TOXIN.get()).getValue();
    }
    
    // 复合元素
    /**
     * 获取实体的爆炸属性值
     * @param entity 实体
     * @return 爆炸属性值
     */
    public static double getBlast(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.BLAST.get()).getValue();
    }
    
    /**
     * 获取实体的腐蚀属性值
     * @param entity 实体
     * @return 腐蚀属性值
     */
    public static double getCorrosive(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.CORROSIVE.get()).getValue();
    }
    
    /**
     * 获取实体的毒气属性值
     * @param entity 实体
     * @return 毒气属性值
     */
    public static double getGas(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.GAS.get()).getValue();
    }
    
    /**
     * 获取实体的磁力属性值
     * @param entity 实体
     * @return 磁力属性值
     */
    public static double getMagnetic(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.MAGNETIC.get()).getValue();
    }
    
    /**
     * 获取实体的辐射属性值
     * @param entity 实体
     * @return 辐射属性值
     */
    public static double getRadiation(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.RADIATION.get()).getValue();
    }
    
    /**
     * 获取实体的病毒属性值
     * @param entity 实体
     * @return 病毒属性值
     */
    public static double getViral(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.VIRAL.get()).getValue();
    }
    
    // 特殊属性
    /**
     * 获取实体的暴击率属性值
     * @param entity 实体
     * @return 暴击率属性值
     */
    public static double getCriticalChance(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.CRITICAL_CHANCE.get()).getValue();
    }
    
    /**
     * 获取实体的暴击伤害属性值
     * @param entity 实体
     * @return 暴击伤害属性值
     */
    public static double getCriticalDamage(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.CRITICAL_DAMAGE.get()).getValue();
    }
    
    /**
     * 获取实体的触发率属性值
     * @param entity 实体
     * @return 触发率属性值
     */
    public static double getTriggerChance(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.TRIGGER_CHANCE.get()).getValue();
    }
    
    // 派系元素
    /**
     * 获取实体的Grineer派系属性值
     * @param entity 实体
     * @return Grineer派系属性值
     */
    public static double getGrineer(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.GRINEER.get()).getValue();
    }
    
    /**
     * 获取实体的Infested派系属性值
     * @param entity 实体
     * @return Infested派系属性值
     */
    public static double getInfested(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.INFESTED.get()).getValue();
    }
    
    /**
     * 获取实体的Corpus派系属性值
     * @param entity 实体
     * @return Corpus派系属性值
     */
    public static double getCorpus(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.CORPUS.get()).getValue();
    }
    
    /**
     * 获取实体的Orokin派系属性值
     * @param entity 实体
     * @return Orokin派系属性值
     */
    public static double getOrokin(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.OROKIN.get()).getValue();
    }
    
    /**
     * 获取实体的Sentient派系属性值
     * @param entity 实体
     * @return Sentient派系属性值
     */
    public static double getSentient(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.SENTIENT.get()).getValue();
    }
    
    /**
     * 获取实体的Murmur派系属性值
     * @param entity 实体
     * @return Murmur派系属性值
     */
    public static double getMurmur(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.MURMUR.get()).getValue();
    }
}