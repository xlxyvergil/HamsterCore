package com.xlxyvergil.hamstercore.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectDataHelper;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectRegistry;
import com.xlxyvergil.hamstercore.element.effect.effects.*;
import com.xlxyvergil.hamstercore.util.AttributeHelper;

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
    
    // 用于防止DoT伤害触发新的元素效果的标志
    private static final ThreadLocal<Boolean> processingDotDamage = ThreadLocal.withInitial(() -> false);

    /**
     * 设置是否正在处理DoT伤害
     * @param processing 是否正在处理DoT伤害
     */
    public static void setProcessingDotDamage(boolean processing) {
        processingDotDamage.set(processing);
    }
    
    /**
     * 检查是否正在处理DoT伤害
     * @return 是否正在处理DoT伤害
     */
    public static boolean isProcessingDotDamage() {
        return processingDotDamage.get();
    }

    /**
     * 处理元素触发效果（带伤害源）
     * @param attacker 攻击者
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    public static void handleElementTriggers(LivingEntity attacker, LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 检查是否正在处理DoT伤害，如果是，则不触发新的元素效果
        if (isProcessingDotDamage()) {
            return;
        }
        
        // 只处理玩家攻击的情况
        if (!(attacker instanceof Player)) {
            return;
        }
        
        // 检查伤害源是否为毒云伤害，如果是则不触发元素效果
        if (damageSource == attacker.damageSources().magic() || damageSource.getMsgId().equals("magic")) {
            return;
        }
        
        // 清空之前会话的触发元素记录
        triggeredElements.get().clear();
        
        // 从攻击者实体获取元素属性
        List<Map.Entry<ElementType, Double>> elementList = new ArrayList<>();
        
        // 添加物理元素
        addElementIfPositive(elementList, ElementType.IMPACT, AttributeHelper.getImpact(attacker));
        addElementIfPositive(elementList, ElementType.PUNCTURE, AttributeHelper.getPuncture(attacker));
        addElementIfPositive(elementList, ElementType.SLASH, AttributeHelper.getSlash(attacker));
        
        // 添加基础元素
        addElementIfPositive(elementList, ElementType.COLD, AttributeHelper.getCold(attacker));
        addElementIfPositive(elementList, ElementType.ELECTRICITY, AttributeHelper.getElectricity(attacker));
        addElementIfPositive(elementList, ElementType.HEAT, AttributeHelper.getHeat(attacker));
        addElementIfPositive(elementList, ElementType.TOXIN, AttributeHelper.getToxin(attacker));
        
        // 添加复合元素
        addElementIfPositive(elementList, ElementType.BLAST, AttributeHelper.getBlast(attacker));
        addElementIfPositive(elementList, ElementType.CORROSIVE, AttributeHelper.getCorrosive(attacker));
        addElementIfPositive(elementList, ElementType.GAS, AttributeHelper.getGas(attacker));
        addElementIfPositive(elementList, ElementType.MAGNETIC, AttributeHelper.getMagnetic(attacker));
        addElementIfPositive(elementList, ElementType.RADIATION, AttributeHelper.getRadiation(attacker));
        addElementIfPositive(elementList, ElementType.VIRAL, AttributeHelper.getViral(attacker));
        
        // 检查攻击者是否有元素属性
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
        
        // 获取触发率值（从攻击者实体获取）
        double triggerChance = AttributeHelper.getTriggerChance(attacker);
        
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
        
        // 根据触发等级和概率触发效果，但确保不重复触发相同元素
        Set<ElementType> triggeredElementsSet = new HashSet<>(); // 记录已触发的元素类型
        for (int i = 0; i < triggerLevel && triggeredElementsSet.size() < elementProbabilities.size(); i++) {
            ElementType selectedElement = selectRandomElementExcluding(elementProbabilities, triggeredElementsSet);
            if (selectedElement != null) {
                triggeredElementsSet.add(selectedElement);
                applyElementEffect(selectedElement, attacker, target, finalDamage, damageSource);
            }
        }
    }
    
    /**
     * 辅助方法：如果元素值为正，则添加到元素列表中
     * @param elementList 元素列表
     * @param elementType 元素类型
     * @param value 元素值
     */
    private static void addElementIfPositive(List<Map.Entry<ElementType, Double>> elementList, ElementType elementType, double value) {
        if (value > 0.0) {
            elementList.add(new HashMap.SimpleEntry<>(elementType, value));
        }
    }
    
    /**
     * 根据概率随机选择一个元素，排除已选择的元素
     * @param elementProbabilities 元素概率映射
     * @param excludedElements 已选择的元素集合
     * @return 选中的元素类型，如果没有可选元素则返回null
     */
    private static ElementType selectRandomElementExcluding(Map<ElementType, Double> elementProbabilities, Set<ElementType> excludedElements) {
        // 创建一个不包含已排除元素的概率映射
        Map<ElementType, Double> filteredProbabilities = new HashMap<>();
        double totalProbability = 0.0;
        
        for (Map.Entry<ElementType, Double> entry : elementProbabilities.entrySet()) {
            if (!excludedElements.contains(entry.getKey())) {
                filteredProbabilities.put(entry.getKey(), entry.getValue());
                totalProbability += entry.getValue();
            }
        }
        
        if (filteredProbabilities.isEmpty()) {
            return null; // 没有可选的元素
        }
        
        // 标准化概率，使其总和为1
        Map<ElementType, Double> normalizedProbabilities = new HashMap<>();
        for (Map.Entry<ElementType, Double> entry : filteredProbabilities.entrySet()) {
            normalizedProbabilities.put(entry.getKey(), entry.getValue() / totalProbability);
        }
        
        // 根据标准化概率随机选择一个元素
        double random = RANDOM.nextDouble();
        double cumulative = 0.0;
        
        for (Map.Entry<ElementType, Double> entry : normalizedProbabilities.entrySet()) {
            cumulative += entry.getValue();
            if (random <= cumulative) {
                return entry.getKey();
            }
        }
        
        // 如果由于浮点数精度问题没有选中，返回最后一个元素
        return normalizedProbabilities.keySet().iterator().next();
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
            // 电击效果：添加电云效果进行AoE传播
            applyElectricCloudEffect(target, finalDamage, damageSource);
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
            // 毒气效果：添加毒云效果进行AoE传播
            applyGasCloudEffect(target, finalDamage, damageSource);
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
     * 应用电云效果（AoE电击传播）
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyElectricCloudEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 使用我们自己的公式计算伤害值，然后传递给ElectricCloudEffect
        if (ElementEffectRegistry.ELECTRIC_CLOUD.get() instanceof ElectricCloudEffect) {
            // 检查目标是否已有相同效果
            int currentAmplifier = 0;
            if (target.hasEffect(ElementEffectRegistry.ELECTRIC_CLOUD.get())) {
                MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.ELECTRIC_CLOUD.get());
                currentAmplifier = existingEffect.getAmplifier();

                // 获取当前持续时间
                int currentDuration = existingEffect.getDuration();

                // 增加效果等级，但不超过最大等级
                int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10

                // 对于范围效果，直接使用计算出的等级作为amplifier，固定持续时间为6秒（120 ticks）
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.ELECTRIC_CLOUD.get(), 120, newAmplifier));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.ELECTRIC_CLOUD.get(), finalDamage);
            } else {
                // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.ELECTRIC_CLOUD.get(), 120, 0));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.ELECTRIC_CLOUD.get(), finalDamage);
            }
        }
    }
    
    /**
     * 应用毒云效果（AoE毒气传播）
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyGasCloudEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 使用我们自己的公式计算伤害值，然后传递给GasCloudEffect
        if (ElementEffectRegistry.GAS_CLOUD.get() instanceof GasCloudEffect) {
            // 检查目标是否已有相同效果
            int currentAmplifier = 0;
            if (target.hasEffect(ElementEffectRegistry.GAS_CLOUD.get())) {
                MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.GAS_CLOUD.get());
                currentAmplifier = existingEffect.getAmplifier();

                // 获取当前持续时间
                int currentDuration = existingEffect.getDuration();

                // 增加效果等级，但不超过最大等级
                int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10

                // 对于范围效果，直接使用计算出的等级作为amplifier，固定持续时间为6秒（120 ticks）
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.GAS_CLOUD.get(), 120, newAmplifier));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.GAS_CLOUD.get(), finalDamage);
            } else {
                // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.GAS_CLOUD.get(), 120, 0));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.GAS_CLOUD.get(), finalDamage);
            }
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
        // 由于需要攻击者方向，我们直接应用击退效果而不是通过状态效果
        // 获取攻击者（从damageSource获取）
        LivingEntity attacker = (LivingEntity) damageSource.getEntity();
        if (attacker != null) {
            // 直接应用击退效果
            ((ImpactEffect) ElementEffectRegistry.IMPACT.get()).applyKnockback(target, attacker);
        }
        // 同时仍然应用状态效果以保持一致性
        target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.IMPACT.get(), 120, 0));
        ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.IMPACT.get(), finalDamage);
    }    
    
    /**
     * 应用穿刺效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyPunctureEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 应用穿刺效果，最大等级5，持续6秒（120 ticks）
        // 检查目标是否已有相同效果
        int currentAmplifier = 0;
        if (target.hasEffect(ElementEffectRegistry.PUNCTURE.get())) {
            MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.PUNCTURE.get());
            currentAmplifier = existingEffect.getAmplifier();
            
            // 获取当前持续时间
            int currentDuration = existingEffect.getDuration();
            
            // 增加效果等级，但不超过最大等级5
            int newAmplifier = Math.min(4, currentAmplifier + 1); // amplifier从0开始，对应等级1-5
            
            // 应用新效果，固定持续时间为6秒（120 ticks），提升等级
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.PUNCTURE.get(), 120, newAmplifier));
        } else {
            // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.PUNCTURE.get(), 120, 0));
        }
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
            // 检查目标是否已有相同效果
            int currentAmplifier = 0;
            if (target.hasEffect(ElementEffectRegistry.SLASH.get())) {
                MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.SLASH.get());
                currentAmplifier = existingEffect.getAmplifier();

                // 获取当前持续时间
                int currentDuration = existingEffect.getDuration();

                // 增加效果等级，但不超过最大等级
                int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10

                // 对于DoT效果，直接使用计算出的等级作为amplifier，固定持续时间为6秒（120 ticks）
                // SlashEffect的applyEffectTick会处理实际伤害
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.SLASH.get(), 120, newAmplifier));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.SLASH.get(), finalDamage);
            } else {
                // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.SLASH.get(), 120, 0));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.SLASH.get(), finalDamage);
            }
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
        // 检查目标是否已有相同效果
        int currentAmplifier = 0;
        if (target.hasEffect(ElementEffectRegistry.COLD.get())) {
            MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.COLD.get());
            currentAmplifier = existingEffect.getAmplifier();
            
            // 获取当前持续时间
            int currentDuration = existingEffect.getDuration();
            
            // 增加效果等级，但不超过最大等级10
            int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10
            
            // 应用新效果，固定持续时间为6秒（120 ticks），提升等级
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.COLD.get(), 120, newAmplifier));
        } else {
            // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.COLD.get(), 120, 0));
        }
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
            // 检查目标是否已有相同效果
            int currentAmplifier = 0;
            if (target.hasEffect(ElementEffectRegistry.ELECTRICITY.get())) {
                MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.ELECTRICITY.get());
                currentAmplifier = existingEffect.getAmplifier();

                // 获取当前持续时间
                int currentDuration = existingEffect.getDuration();

                // 增加效果等级，但不超过最大等级
                int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10

                // 对于范围效果，直接使用计算出的等级作为amplifier，固定持续时间为6秒（120 ticks）
                // ElectricityEffect的addAttributeModifiers会处理范围效果
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.ELECTRICITY.get(), 120, newAmplifier));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.ELECTRICITY.get(), finalDamage);
            } else {
                // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.ELECTRICITY.get(), 120, 0));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.ELECTRICITY.get(), finalDamage);
            }
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
            // 检查目标是否已有相同效果
            int currentAmplifier = 0;
            if (target.hasEffect(ElementEffectRegistry.HEAT.get())) {
                MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.HEAT.get());
                currentAmplifier = existingEffect.getAmplifier();

                // 获取当前持续时间
                int currentDuration = existingEffect.getDuration();

                // 增加效果等级，但不超过最大等级
                int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10

                // 对于DoT效果，直接使用计算出的等级作为amplifier，固定持续时间为6秒（120 ticks）
                // HeatEffect的applyEffectTick会处理实际伤害
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.HEAT.get(), 120, newAmplifier));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.HEAT.get(), finalDamage);
            } else {
                // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.HEAT.get(), 120, 0));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.HEAT.get(), finalDamage);
            }
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
            // 检查目标是否已有相同效果
            int currentAmplifier = 0;
            if (target.hasEffect(ElementEffectRegistry.TOXIN.get())) {
                MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.TOXIN.get());
                currentAmplifier = existingEffect.getAmplifier();

                // 获取当前持续时间
                int currentDuration = existingEffect.getDuration();

                // 增加效果等级，但不超过最大等级
                int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10

                // 对于DoT效果，直接使用计算出的等级作为amplifier，固定持续时间为6秒（120 ticks）
                // ToxinEffect的applyEffectTick会处理实际伤害
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.TOXIN.get(), 120, newAmplifier));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.TOXIN.get(), finalDamage);
            } else {
                // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.TOXIN.get(), 120, 0));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.TOXIN.get(), finalDamage);
            }
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
            // 检查目标是否已有相同效果
            int currentAmplifier = 0;
            if (target.hasEffect(ElementEffectRegistry.BLAST.get())) {
                MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.BLAST.get());
                currentAmplifier = existingEffect.getAmplifier();

                // 获取当前持续时间
                int currentDuration = existingEffect.getDuration();

                // 增加效果等级，但不超过最大等级
                int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10

                // 对于范围效果，直接使用计算出的等级作为amplifier，固定持续时间为6秒（120 ticks）
                // BlastEffect的addAttributeModifiers会处理范围伤害
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.BLAST.get(), 120, newAmplifier));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.BLAST.get(), finalDamage);
            } else {
                // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.BLAST.get(), 120, 0));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.BLAST.get(), finalDamage);
            }
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
        // 检查目标是否已有相同效果
        int currentAmplifier = 0;
        if (target.hasEffect(ElementEffectRegistry.CORROSIVE.get())) {
            MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.CORROSIVE.get());
            currentAmplifier = existingEffect.getAmplifier();
            
            // 获取当前持续时间
            int currentDuration = existingEffect.getDuration();
            
            // 增加效果等级，但不超过最大等级10
            int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10
            
            // 应用新效果，固定持续时间为6秒（120 ticks），提升等级
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.CORROSIVE.get(), 120, newAmplifier));
        } else {
            // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.CORROSIVE.get(), 120, 0));
        }
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
            // 检查目标是否已有相同效果
            int currentAmplifier = 0;
            if (target.hasEffect(ElementEffectRegistry.GAS.get())) {
                MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.GAS.get());
                currentAmplifier = existingEffect.getAmplifier();

                // 获取当前持续时间
                int currentDuration = existingEffect.getDuration();

                // 增加效果等级，但不超过最大等级
                int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10

                // 对于范围效果，直接使用计算出的等级作为amplifier，固定持续时间为6秒（120 ticks）
                // GasEffect的addAttributeModifiers会处理范围伤害
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.GAS.get(), 120, newAmplifier));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.GAS.get(), finalDamage);
            } else {
                // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
                target.addEffect(new MobEffectInstance((ElementEffect) ElementEffectRegistry.GAS.get(), 120, 0));
                ElementEffectDataHelper.setEffectDamage(target, (ElementEffect) ElementEffectRegistry.GAS.get(), finalDamage);
            }
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
        // 检查目标是否已有相同效果
        int currentAmplifier = 0;
        if (target.hasEffect(ElementEffectRegistry.MAGNETIC.get())) {
            MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.MAGNETIC.get());
            currentAmplifier = existingEffect.getAmplifier();
            
            // 获取当前持续时间
            int currentDuration = existingEffect.getDuration();
            
            // 增加效果等级，但不超过最大等级10
            int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10
            
            // 应用新效果，固定持续时间为6秒（120 ticks），提升等级
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.MAGNETIC.get(), 120, newAmplifier));
        } else {
            // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.MAGNETIC.get(), 120, 0));
        }
    }
    
    /**
     * 应用辐射效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyRadiationEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 辐射效果通过事件监听器处理，不需要额外的DoT伤害
        // 检查目标是否已有相同效果
        int currentAmplifier = 0;
        if (target.hasEffect(ElementEffectRegistry.RADIATION.get())) {
            MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.RADIATION.get());
            currentAmplifier = existingEffect.getAmplifier();
            
            // 获取当前持续时间
            int currentDuration = existingEffect.getDuration();
            
            // 增加效果等级，但不超过最大等级10
            int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10
            
            // 应用新效果，固定持续时间为6秒（120 ticks），提升等级
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.RADIATION.get(), 120, newAmplifier));
        } else {
            // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.RADIATION.get(), 120, 0));
        }
    }
    
    /**
     * 应用病毒效果
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param damageSource 原始伤害源
     */
    private static void applyVirusEffect(LivingEntity target, float finalDamage, DamageSource damageSource) {
        // 病毒效果通过addAttributeModifiers处理，不需要额外的DoT伤害
        // 检查目标是否已有相同效果
        int currentAmplifier = 0;
        if (target.hasEffect(ElementEffectRegistry.VIRAL.get())) {
            MobEffectInstance existingEffect = target.getEffect(ElementEffectRegistry.VIRAL.get());
            currentAmplifier = existingEffect.getAmplifier();
            
            // 获取当前持续时间
            int currentDuration = existingEffect.getDuration();
            
            // 增加效果等级，但不超过最大等级10
            int newAmplifier = Math.min(9, currentAmplifier + 1); // amplifier从0开始，对应等级1-10
            
            // 应用新效果，固定持续时间为6秒（120 ticks），提升等级
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.VIRAL.get(), 120, newAmplifier));
        } else {
            // 如果没有相同效果，则应用新效果，初始等级为0（对应1级）
            target.addEffect(new MobEffectInstance(ElementEffectRegistry.VIRAL.get(), 120, 0));
        }
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