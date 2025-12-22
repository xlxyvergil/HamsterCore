package com.xlxyvergil.hamstercore.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.ElementType.*;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectRegistry;
import com.xlxyvergil.hamstercore.element.effect.effects.*;


import net.minecraft.world.damagesource.DamageSource;

import net.minecraft.world.entity.LivingEntity;

/**
 * 元素触发效果处理器
 * 负责处理元素触发的异常状态效果
 */
public class ElementTriggerHandler {
    
    private static final Random RANDOM = new Random();
    
    // 存储当前攻击会话中触发的元素信息（ThreadLocal确保线程安全）
    private static final ThreadLocal<List<ElementType>> triggeredElements = ThreadLocal.withInitial(ArrayList::new);
    

    /**
     * 处理元素触发效果（带伤害源）
     * @param attacker 攻击者
     * @param target 目标实体
     * @param cacheData 缓存的元素数据
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    public static void handleElementTriggers(LivingEntity attacker, LivingEntity target, AffixCacheManager.AffixCacheData cacheData, float finalDamage, DamageSource damageSource) {
        // 只处理玩家攻击的情况
        if (!(attacker instanceof Player)) {
            return;
        }
        
        // 清空之前会话的触发元素记录
        triggeredElements.get().clear();
        
        // 从缓存数据中获取元素属性
        List<Map.Entry<ElementType, Double>> elementList = new ArrayList<>();
        
        // 添加物理元素
        Map<String, Double> physicalElements = cacheData.getPhysicalElements();
        for (Map.Entry<String, Double> entry : physicalElements.entrySet()) {
            if (entry.getValue() > 0.0) {
                ElementType elementType = ElementType.byName(entry.getKey());
                if (elementType != null) {
                    elementList.add(new HashMap.SimpleEntry<>(elementType, entry.getValue()));
                }
            }
        }
        
        // 添加复合元素
        Map<String, Double> combinedElements = cacheData.getCombinedElements();
        for (Map.Entry<String, Double> entry : combinedElements.entrySet()) {
            if (entry.getValue() > 0.0) {
                ElementType elementType = ElementType.byName(entry.getKey());
                if (elementType != null) {
                    elementList.add(new HashMap.SimpleEntry<>(elementType, entry.getValue()));
                }
            }
        }
        
        // 检查武器是否有元素属性
        if (elementList.isEmpty()) {
            return;
        }
        
        Map<ElementType, Map.Entry<ElementType, Double>> elements = new HashMap<>();
        
        // 只收集物理、基础和复合元素
        for (Map.Entry<ElementType, Double> element : elementList) {
            ElementType type = element.getKey();
            if (type.isPhysical() || type.isBasic() || type.isComplex()) {
                elements.put(type, element);
            }
        }
        
        if (elements.isEmpty()) {
            return;
        }
        
        // 计算元素总触发值（不包括特殊属性）
        double totalElementValue = 0.0;
        for (Map.Entry<ElementType, Double> element : elements.values()) {
            totalElementValue += element.getValue();
        }
        
        if (totalElementValue <= 0.0) {
            return;
        }
        
        // 获取特殊元素值（从缓存数据中获取）
        Map<String, Double> criticalStats = cacheData.getCriticalStats();
        double triggerChance = criticalStats.getOrDefault("trigger_chance", 0.0);
        
        // 判断是否触发
        if (RANDOM.nextDouble() > triggerChance) {
            return; // 没有触发
        }
        
        // 计算触发等级
        double chancePercent = triggerChance * 100;
        int guaranteedTriggerLevel = (int) Math.floor((chancePercent + 100) / 100) - 1; // 保底触发等级
        int maxTriggerLevel = (int) Math.floor((chancePercent + 100) / 100); // 最大可能触发等级
        
        // 确保触发等级至少为0
        if (guaranteedTriggerLevel < 0) {
            guaranteedTriggerLevel = 0;
        }
        
        if (maxTriggerLevel < 0) {
            maxTriggerLevel = 0;
        }
        
        // 判断是否能达到更高的触发等级
        int triggerLevel = guaranteedTriggerLevel;
        double extraChance = chancePercent - (guaranteedTriggerLevel * 100); // 超出保底等级的部分
        
        if (RANDOM.nextDouble() * 100 < extraChance) {
            triggerLevel = maxTriggerLevel;
        }
        
        // 触发等级为0时，也要至少触发一个效果
        if (triggerLevel == 0 && triggerChance > 0) {
            triggerLevel = 1;
        }
        
        // 计算每个元素的触发概率
        Map<ElementType, Double> elementProbabilities = new HashMap<>();
        for (Map.Entry<ElementType, Map.Entry<ElementType, Double>> entry : elements.entrySet()) {
            double probability = entry.getValue().getValue() / totalElementValue;
            elementProbabilities.put(entry.getKey(), probability);
        }
        
        // 根据触发等级和概率触发效果
        for (int i = 0; i < triggerLevel; i++) {
            triggerRandomElementEffect(elementProbabilities, attacker, target, finalDamage, damageSource);
        }
    }
    
    /**
     * 根据概率随机触发一个元素效果
     * @param elementProbabilities 元素概率映射
     * @param attacker 攻击者
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void triggerRandomElementEffect(Map<ElementType, Double> elementProbabilities, 
                                              LivingEntity attacker, LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 根据概率随机选择一个元素
        double random = RANDOM.nextDouble();
        double cumulative = 0.0;
        
        ElementType selectedElement = null;
        for (Map.Entry<ElementType, Double> entry : elementProbabilities.entrySet()) {
            cumulative += entry.getValue();
            if (random <= cumulative) {
                selectedElement = entry.getKey();
                break;
            }
        }
        
        // 如果由于浮点数精度问题没有选中，返回最后一个元素
        if (selectedElement == null && !elementProbabilities.isEmpty()) {
            selectedElement = elementProbabilities.keySet().iterator().next();
        }
        
        if (selectedElement != null) {
            applyElementEffect(selectedElement, attacker, target, finalDamage, damageSource);
        }
    }
    
    /**
     * 应用元素效果
     * @param elementType 元素类型
     * @param attacker 攻击者
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyElementEffect(ElementType elementType, LivingEntity attacker, LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 记录触发的元素
        triggeredElements.get().add(elementType);
        
        // 根据元素类型应用不同的效果
        if (elementType == IMPACT) {
            // 冲击效果：击退效果
            applyImpactEffect(target);
        } else if (elementType == PUNCTURE) {
            // 穿刺效果：伤害输出减少
            applyPunctureEffect(target);
        } else if (elementType == SLASH) {
            // 切割效果：出血DoT
            applyBleedingEffect(target, finalDamage, damageSource);
        } else if (elementType == COLD) {
            // 冰冻效果：减速和暴击伤害加成
            applyFreezeEffect(target);
        } else if (elementType == ELECTRICITY) {
            // 电击效果：电击DoT和眩晕
            applyShockEffect(target, damageSource);
        } else if (elementType == HEAT) {
            // 火焰效果：护甲减少和火焰DoT
            applyFireEffect(target, finalDamage, damageSource);
        } else if (elementType == TOXIN) {
            // 毒素效果：可绕过护盾的毒素DoT
            applyToxinEffect(target, finalDamage, damageSource);
        } else if (elementType == BLAST) {
            // 爆炸效果：延迟范围伤害
            applyExplosionEffect(target, finalDamage, damageSource);
        } else if (elementType == CORROSIVE) {
            // 腐蚀效果：护甲削减
            applyCorrosionEffect(target, finalDamage, damageSource);
        } else if (elementType == GAS) {
            // 毒气效果：AoE毒气DoT
            applyGasEffect(target, finalDamage, damageSource);
        } else if (elementType == MAGNETIC) {
            // 磁力效果：护盾伤害和护盾再生失效
            applyMagneticEffect(target);
        } else if (elementType == RADIATION) {
            // 辐射效果：敌我不分攻击友军
            applyRadiationEffect(target);
        } else if (elementType == VIRAL) {
            // 病毒效果：受到生命值伤害增伤
            applyVirusEffect(target);
        }
    }
    
    /**
     * 应用冲击效果
     * @param target 目标实体
     */
    private static void applyImpactEffect(LivingEntity target) {
        // 应用冲击效果，只有1级，直接向后击退一格
        ImpactEffect effect = (ImpactEffect) ElementEffectRegistry.IMPACT.get();
        effect.applyEffect(target, 0); // 只有1级效果
    }    
    /**
     * 应用穿刺效果
     * @param target 目标实体
     */
    private static void applyPunctureEffect(LivingEntity target) {
        // 应用穿刺效果，最大等级5
        PunctureEffect effect = (PunctureEffect) ElementEffectRegistry.PUNCTURE.get();
        // 获取触发等级，这里简化处理，实际应根据触发逻辑确定等级
        int level = 1;
        effect.applyEffect(target, level);
    }
    
    /**
     * 应用切割效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyBleedingEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 应用切割效果，最大等级10
        SlashEffect effect = (SlashEffect) ElementEffectRegistry.SLASH.get();
        // 获取触发等级，这里简化处理，实际应根据触发逻辑确定等级
        int level = 1;
        effect.applyEffect(target, level, finalDamage, damageSource);
    }
    
    /**
     * 应用冰冻效果
     * @param target 目标实体
     */
    private static void applyFreezeEffect(LivingEntity target) {
        // 应用冰冻效果，最大等级6
        ElementEffectManager.applyEffect(target, COLD, (ElementEffect) ElementEffectRegistry.COLD.get(), 6, 120);
    }
    
    /**
     * 应用电击效果
     * @param target 目标实体
     * @param damageSource 原始伤害源
     */
    private static void applyShockEffect(LivingEntity target, DamageSource damageSource) {
        // 应用电击效果，最大等级10
        ElectricityEffect effect = (ElectricityEffect) ElementEffectRegistry.ELECTRICITY.get();
        // 获取触发等级，这里简化处理，实际应根据触发逻辑确定等级
        int level = 1;
        effect.applyEffect(target, level, damageSource);
    }
    
    /**
     * 应用火焰效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyFireEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 应用火焰效果，最大等级10
        HeatEffect effect = (HeatEffect) ElementEffectRegistry.HEAT.get();
        // 获取触发等级，这里简化处理，实际应根据触发逻辑确定等级
        int level = 1;
        effect.applyEffect(target, level, finalDamage, damageSource);
    }
    
    /**
     * 应用毒素效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyToxinEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 应用毒素效果，最大等级10
        ToxinEffect effect = (ToxinEffect) ElementEffectRegistry.TOXIN.get();
        // 获取触发等级，这里简化处理，实际应根据触发逻辑确定等级
        int level = 1;
        effect.applyEffect(target, level, finalDamage, damageSource);
    }
    
    /**
     * 应用爆炸效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyExplosionEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 应用爆炸效果，最大等级10
        BlastEffect effect = (BlastEffect) ElementEffectRegistry.BLAST.get();
        // 获取触发等级，这里简化处理，实际应根据触发逻辑确定等级
        int level = 1;
        effect.applyEffect(target, level, finalDamage, damageSource);
    }
    
    /**
     * 应用腐蚀效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyCorrosionEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 应用腐蚀效果，最大等级10
        CorrosiveEffect effect = (CorrosiveEffect) ElementEffectRegistry.CORROSIVE.get();
        // 获取触发等级，这里简化处理，实际应根据触发逻辑确定等级
        int level = 1;
        effect.applyEffect(target, level, finalDamage, damageSource);
    }
    
    /**
     * 应用毒气效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyGasEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 应用毒气效果，最大等级10
        GasEffect effect = (GasEffect) ElementEffectRegistry.GAS.get();
        // 获取触发等级，这里简化处理，实际应根据触发逻辑确定等级
        int level = 1;
        effect.applyEffect(target, level, finalDamage, damageSource);
    }
    
    /**
     * 应用磁力效果
     * @param target 目标实体
     */
    private static void applyMagneticEffect(LivingEntity target) {
        // 应用磁力效果，最大等级10，持续6秒（120 ticks）
        // 从现有效果获取等级，如果没有则从1级开始
        int currentLevel = 1;
        if (target.hasEffect(ElementEffectRegistry.MAGNETIC.get())) {
            currentLevel = Math.min(target.getEffect(ElementEffectRegistry.MAGNETIC.get()).getAmplifier() + 1, 10);
        }
        
        ElementEffectManager.applyEffect(target, MAGNETIC, (ElementEffect) ElementEffectRegistry.MAGNETIC.get(), 10, 120);
        
        // 立即触发磁力效果的护盾再生失效
        // 直接调用静态方法，无需类型转换
        if (target.hasEffect(ElementEffectRegistry.MAGNETIC.get())) {
            int amplifier = target.getEffect(ElementEffectRegistry.MAGNETIC.get()).getAmplifier();
            // 创建一个临时MagneticEffect实例来应用效果
            MagneticEffect tempEffect = new MagneticEffect();
            tempEffect.applyEffect(target, amplifier);
        }
    }
    
    /**
     * 应用辐射效果
     * @param target 目标实体
     */
    private static void applyRadiationEffect(LivingEntity target) {
        // 检查是否已有辐射效果
        int currentLevel = 0;
        if (target.hasEffect((ElementEffect) ElementEffectRegistry.RADIATION.get())) {
            currentLevel = target.getEffect((ElementEffect) ElementEffectRegistry.RADIATION.get()).getAmplifier();
        }
        
        // 基础12秒，每级+1秒
        int durationTicks = 240 + (currentLevel * 20); // 20tick = 1秒
        
        // 应用辐射效果，最大等级10
        ElementEffectManager.applyEffect(target, RADIATION, (ElementEffect) ElementEffectRegistry.RADIATION.get(), 10, durationTicks);
        
        // 立即触发辐射效果的即时效果
        RadiationEffect radiationEffect = (RadiationEffect) ElementEffectRegistry.RADIATION.get();
        if (radiationEffect != null) {
            radiationEffect.applyEffect(target, currentLevel);
        }
    }
    
    /**
     * 应用病毒效果
     * @param target 目标实体
     */
    private static void applyVirusEffect(LivingEntity target) {
        // 应用病毒效果，最大等级10
        ElementEffectManager.applyEffect(target, VIRAL, (ElementEffect) ElementEffectRegistry.VIRAL.get(), 10, 120);
    }
    
    /**
     * 获取当前攻击会话中触发的元素列表
     * @return 触发的元素列表（不可修改的副本）
     */
    public static List<ElementType> getTriggeredElements() {
        return new ArrayList<>(triggeredElements.get());
    }
    
    /**
     * 清空当前会话的触发元素记录
     */
    public static void clearTriggeredElements() {
        triggeredElements.get().clear();
    }
}