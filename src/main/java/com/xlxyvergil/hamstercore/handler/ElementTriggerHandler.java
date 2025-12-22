package com.xlxyvergil.hamstercore.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectInstance;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectRegistry;
import com.xlxyvergil.hamstercore.element.effect.effects.*;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

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
        
        // 检查伤害源是否为毒云伤害，如果是则不触发元素效果
        if (damageSource == DamageSource.MAGIC || damageSource.getType() == net.minecraft.world.damagesource.DamageTypes.MAGIC) {
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
        if (elementType == ElementType.IMPACT) {
            // 冲击效果：击退效果
            applyImpactEffect(target, finalDamage, damageSource);
        } else if (elementType == ElementType.PUNCTURE) {
            // 穿刺效果：伤害输出减少
            applyPunctureEffect(target, finalDamage, damageSource);
        } else if (elementType == ElementType.SLASH) {
            // 切割效果：出血DoT
            applyBleedingEffect(target, finalDamage, damageSource);
        } else if (elementType == ElementType.COLD) {
            // 冰冻效果：减速和暴击伤害加成
            applyFreezeEffect(target, finalDamage, damageSource);
        } else if (elementType == ElementType.ELECTRICITY) {
            // 电击效果：电击DoT和眩晕
            applyShockEffect(target, finalDamage, damageSource);
        } else if (elementType == ElementType.HEAT) {
            // 火焰效果：护甲减少和火焰DoT
            applyFireEffect(target, finalDamage, damageSource);
        } else if (elementType == ElementType.TOXIN) {
            // 毒素效果：可绕过护盾的毒素DoT
            applyToxinEffect(target, finalDamage, damageSource);
        } else if (elementType == ElementType.BLAST) {
            // 爆炸效果：延迟范围伤害
            applyExplosionEffect(target, finalDamage, damageSource);
        } else if (elementType == ElementType.CORROSIVE) {
            // 腐蚀效果：护甲削减
            applyCorrosionEffect(target, finalDamage, damageSource);
        } else if (elementType == ElementType.GAS) {
            // 毒气效果：AoE毒气DoT
            applyGasEffect(target, finalDamage, damageSource);
        } else if (elementType == ElementType.MAGNETIC) {
            // 磁力效果：护盾伤害和护盾再生失效
            applyMagneticEffect(target, finalDamage, damageSource);
        } else if (elementType == ElementType.RADIATION) {
            // 辐射效果：敌我不分攻击友军
            applyRadiationEffect(target, finalDamage, damageSource);
        } else if (elementType == ElementType.VIRAL) {
            // 病毒效果：受到生命值伤害增伤
            applyVirusEffect(target, finalDamage, damageSource);
        }
    }
    
    /**
     * 应用冲击效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyImpactEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 应用冲击效果，最大等级1，持续6秒（120 ticks）
        ElementEffectManager.applyEffect(target, ElementType.IMPACT, (ElementEffect) ElementEffectRegistry.IMPACT.get(), 0, 120, finalDamage, damageSource);
    }    
    
    /**
     * 应用穿刺效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyPunctureEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 应用穿刺效果，最大等级5，持续6秒（120 ticks）
        ElementEffectManager.applyEffect(target, ElementType.PUNCTURE, (ElementEffect) ElementEffectRegistry.PUNCTURE.get(), 5, 120, finalDamage, damageSource);
    }
    
    /**
     * 应用切割效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyBleedingEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 使用我们自己的公式计算伤害值，然后传递给SlashEffect
        if (ElementEffectRegistry.SLASH.get() instanceof SlashEffect) {
            // 伤害数值为最终伤害的35%乘以效果等级
            // 这里我们假设等级为1（amplifier=0），实际等级会在ElementEffectManager中处理
            float dotDamage = finalDamage * 0.35f * (0 + 1); // 0是amplifier值
            
            // 将计算后的伤害值转换为等效的amplifier值
            int adjustedAmplifier = Math.max(0, (int) (dotDamage - 1.0F));
            
            // 应用效果，使用调整后的amplifier
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.SLASH.get(), 120, adjustedAmplifier));
        }
    }
    
    /**
     * 应用冰冻效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyFreezeEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 冰冻效果通过addAttributeModifiers处理，不需要额外的DoT伤害
    }
    
    /**
     * 应用电击效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyShockEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 使用我们自己的公式计算伤害值，然后传递给ElectricityEffect
        if (ElementEffectRegistry.ELECTRICITY.get() instanceof ElectricityEffect) {
            // 伤害数值为最终伤害的25% * (amplifier + 1)
            // 这里我们假设等级为1（amplifier=0），实际等级会在ElementEffectManager中处理
            float damagePerSecond = finalDamage * 0.25f * (0 + 1); // 0是amplifier值
            
            // 将计算后的伤害值转换为等效的amplifier值
            int adjustedAmplifier = Math.max(0, (int) (damagePerSecond - 1.0F));
            
            // 应用效果，使用调整后的amplifier
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.ELECTRICITY.get(), 120, adjustedAmplifier));
        }
    }
    
    /**
     * 应用火焰效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyFireEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 使用我们自己的公式计算伤害值，然后传递给HeatEffect
        if (ElementEffectRegistry.HEAT.get() instanceof HeatEffect) {
            // 伤害数值为最终伤害的50% * (amplifier + 1)
            // 这里我们假设等级为1（amplifier=0），实际等级会在ElementEffectManager中处理
            float damagePerSecond = finalDamage * 0.5f * (0 + 1); // 0是amplifier值
            
            // 将计算后的伤害值转换为等效的amplifier值
            int adjustedAmplifier = Math.max(0, (int) (damagePerSecond - 1.0F));
            
            // 应用效果，使用调整后的amplifier
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.HEAT.get(), 120, adjustedAmplifier));
        }
    }
    
    /**
     * 应用毒素效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyToxinEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 使用我们自己的公式计算伤害值，然后传递给ToxinEffect
        if (ElementEffectRegistry.TOXIN.get() instanceof ToxinEffect) {
            // 伤害数值为最终伤害的30% * (amplifier + 1)
            // 这里我们假设等级为1（amplifier=0），实际等级会在ElementEffectManager中处理
            float dotDamage = finalDamage * 0.3f * (0 + 1); // 0是amplifier值
            
            // 将计算后的伤害值转换为等效的amplifier值
            int adjustedAmplifier = Math.max(0, (int) (dotDamage - 1.0F));
            
            // 应用效果，使用调整后的amplifier
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.TOXIN.get(), 120, adjustedAmplifier));
        }
    }
    
    /**
     * 应用爆炸效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyExplosionEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 使用我们自己的公式计算伤害值，然后传递给BlastEffect
        if (ElementEffectRegistry.BLAST.get() instanceof BlastEffect) {
            // 伤害数值为最终伤害的40%乘以效果等级
            // 这里我们假设等级为1（amplifier=0），实际等级会在ElementEffectManager中处理
            float dotDamage = finalDamage * 0.4f * (0 + 1); // 0是amplifier值
            
            // 将计算后的伤害值转换为等效的amplifier值
            int adjustedAmplifier = Math.max(0, (int) (dotDamage - 1.0F));
            
            // 应用效果，使用调整后的amplifier
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.BLAST.get(), 120, adjustedAmplifier));
        }
    }
    
    /**
     * 应用腐蚀效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyCorrosionEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 腐蚀效果通过addAttributeModifiers处理，不需要额外的DoT伤害
    }
    
    /**
     * 应用毒气效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyGasEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 使用我们自己的公式计算伤害值，然后传递给GasEffect
        if (ElementEffectRegistry.GAS.get() instanceof GasEffect) {
            // 伤害数值为最终伤害的25%乘以效果等级
            // 这里我们假设等级为1（amplifier=0），实际等级会在ElementEffectManager中处理
            float dotDamage = finalDamage * 0.25f * (0 + 1); // 0是amplifier值
            
            // 将计算后的伤害值转换为等效的amplifier值
            int adjustedAmplifier = Math.max(0, (int) (dotDamage - 1.0F));
            
            // 应用效果，使用调整后的amplifier
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.GAS.get(), 120, adjustedAmplifier));
        }
    }
    
    /**
     * 应用磁力效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyMagneticEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 磁力效果通过addAttributeModifiers处理，不需要额外的DoT伤害
    }
    
    /**
     * 应用辐射效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyRadiationEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 辐射效果通过事件监听器处理，不需要额外的DoT伤害
    }
    
    /**
     * 应用病毒效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyVirusEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 病毒效果通过addAttributeModifiers处理，不需要额外的DoT伤害
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