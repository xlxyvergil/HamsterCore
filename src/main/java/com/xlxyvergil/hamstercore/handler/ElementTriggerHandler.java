package com.xlxyvergil.hamstercore.handler;

import com.xlxyvergil.hamstercore.util.ElementNBTUtils;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.util.ElementModifierValueUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.xlxyvergil.hamstercore.element.ElementType.*;

import java.util.*;

/**
 * 元素触发效果处理器
 * 负责处理元素触发的异常状态效果
 */
public class ElementTriggerHandler {
    
    private static final Random RANDOM = new Random();
    
    /**
     * 处理元素触发效果
     * @param attacker 攻击者
     * @param target 目标实体
     */
    public static void handleElementTriggers(LivingEntity attacker, LivingEntity target) {
        // 只处理玩家攻击的情况
        if (!(attacker instanceof Player)) {
            return;
        }
        
        Player player = (Player) attacker;
        ItemStack weapon = player.getMainHandItem();
        
        // 检查武器是否有元素属性
        if (!ElementNBTUtils.hasAnyElements(weapon)) {
            return;
        }
        
        // 获取武器上的元素属性（只获取实际生效的元素，从缓存中获取）
        List<Map.Entry<ElementType, Double>> elementList = ElementDamageManager.getActiveElements(weapon);
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
        
        // 获取触发率（从修饰符系统获取计算后的值）
        double triggerChance = ElementModifierValueUtil.getElementValueFromAttributes(weapon, ElementType.TRIGGER_CHANCE);
        
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
            triggerRandomElementEffect(elementProbabilities, attacker, target);
        }
    }
    
    /**
     * 根据概率随机触发一个元素效果
     * @param elementProbabilities 元素概率映射
     * @param attacker 攻击者
     * @param target 目标实体
     */
    private static void triggerRandomElementEffect(Map<ElementType, Double> elementProbabilities, 
                                              LivingEntity attacker, LivingEntity target) {
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
            applyElementEffect(selectedElement, attacker, target);
        }
    }
    
    /**
     * 应用元素效果
     * @param elementType 元素类型
     * @param attacker 攻击者
     * @param target 目标实体
     */
    private static void applyElementEffect(ElementType elementType, LivingEntity attacker, LivingEntity target) {
        // 根据元素类型应用不同的效果
        if (elementType == IMPACT) {
            // 冲击效果：待设计
        } else if (elementType == PUNCTURE) {
            // 穿刺效果：待设计
        } else if (elementType == SLASH) {
            // 切割效果：待设计
        } else if (elementType == COLD) {
            // 冰冻效果：待设计
        } else if (elementType == ELECTRICITY) {
            // 电击效果：待设计
        } else if (elementType == HEAT) {
            // 火焰效果：待设计
        } else if (elementType == TOXIN) {
            // 毒素效果：待设计
        } else if (elementType == BLAST) {
            // 爆炸效果：待设计
        } else if (elementType == CORROSIVE) {
            // 腐蚀效果：待设计
        } else if (elementType == GAS) {
            // 毒气效果：待设计
        } else if (elementType == MAGNETIC) {
            // 磁力效果：待设计
        } else if (elementType == RADIATION) {
            // 辐射效果：待设计
        } else if (elementType == VIRAL) {
            // 病毒效果：待设计
        }
    }
}