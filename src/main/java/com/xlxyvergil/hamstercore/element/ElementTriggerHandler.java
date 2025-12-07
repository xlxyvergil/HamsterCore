package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.faction.FactionDamageHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class ElementTriggerHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * 处理武器元素触发
     * @param attacker 攻击者
     * @param target 目标实体
     */
    public static void handleElementTriggers(LivingEntity attacker, LivingEntity target) {
        if (attacker instanceof Player player) {
            // 获取攻击者使用的物品
            ItemStack weapon = player.getMainHandItem();
            
            // 检查物品是否有元素属性
            if (ElementHelper.hasElementAttributes(weapon)) {
                // 使用WeaponTriggerHandler处理元素触发
                List<ElementType> triggeredElements = WeaponTriggerHandler.processWeaponTrigger(weapon);
                
                // 保存触发信息到FactionDamageHandler
                FactionDamageHandler.lastTriggeredElements = triggeredElements;
                
                // 如果有任何元素被触发
                if (!triggeredElements.isEmpty()) {
                    LOGGER.info("Weapon triggered " + triggeredElements.size() + " elements");
                    
                    // 遍历所有触发的元素
                    for (ElementType elementType : triggeredElements) {
                        LOGGER.info("Triggered element effect: " + elementType.getName());
                        // 向玩家发送触发信息
                        player.sendSystemMessage(net.minecraft.network.chat.Component.literal("触发元素效果: " + elementType.getName()).withStyle(net.minecraft.ChatFormatting.GREEN));
                        // 触发元素效果
                        triggerElementEffect(elementType, attacker, target);
                    }
                } else {
                    LOGGER.info("No elements were triggered");
                }
            }
        }
    }
    
    /**
     * 触发特定元素的效果
     * @param elementType 元素类型
     * @param attacker 攻击者
     * @param target 目标
     */
    private static void triggerElementEffect(ElementType elementType, LivingEntity attacker, LivingEntity target) {
        // TODO: 实现各种元素的具体触发效果
        switch (elementType) {
            case IMPACT:
                // 冲击效果：使敌人蹒跚后退
                triggerImpactEffect(target);
                break;
            case PUNCTURE:
                // 穿刺效果：减少敌人伤害输出
                triggerPunctureEffect(target);
                break;
            case SLASH:
                // 切割效果：造成流血伤害
                triggerSlashEffect(target);
                break;
            case COLD:
                // 冰冻效果：减速目标
                triggerColdEffect(target);
                break;
            case ELECTRICITY:
                // 电击效果：造成电击伤害并可能眩晕
                triggerElectricityEffect(target);
                break;
            case HEAT:
                // 火焰效果：减少目标护甲
                triggerHeatEffect(target);
                break;
            case TOXIN:
                // 毒素效果：造成毒素伤害
                triggerToxinEffect(target);
                break;
            case BLAST:
                // 爆炸效果：范围伤害
                triggerBlastEffect(target);
                break;
            case CORROSIVE:
                // 腐蚀效果：削减护甲
                triggerCorrosiveEffect(target);
                break;
            case GAS:
                // 毒气效果：范围毒素伤害
                triggerGasEffect(target);
                break;
            case MAGNETIC:
                // 磁力效果：对护盾造成额外伤害
                triggerMagneticEffect(target);
                break;
            case RADIATION:
                // 辐射效果：使目标攻击友军
                triggerRadiationEffect(target);
                break;
            case VIRAL:
                // 病毒效果：对生命值造成额外伤害
                triggerViralEffect(target);
                break;
        }
    }
    
    private static void triggerImpactEffect(LivingEntity target) {
        // TODO: 实现冲击效果
        LOGGER.info("Triggering Impact effect on " + target.getName().getString());
    }
    
    private static void triggerPunctureEffect(LivingEntity target) {
        // TODO: 实现穿刺效果
        LOGGER.info("Triggering Puncture effect on " + target.getName().getString());
    }
    
    private static void triggerSlashEffect(LivingEntity target) {
        // TODO: 实现切割效果
        LOGGER.info("Triggering Slash effect on " + target.getName().getString());
    }
    
    private static void triggerColdEffect(LivingEntity target) {
        // TODO: 实现冰冻效果
        LOGGER.info("Triggering Cold effect on " + target.getName().getString());
    }
    
    private static void triggerElectricityEffect(LivingEntity target) {
        // TODO: 实现电击效果
        LOGGER.info("Triggering Electricity effect on " + target.getName().getString());
    }
    
    private static void triggerHeatEffect(LivingEntity target) {
        // TODO: 实现火焰效果
        LOGGER.info("Triggering Heat effect on " + target.getName().getString());
    }
    
    private static void triggerToxinEffect(LivingEntity target) {
        // TODO: 实现毒素效果
        LOGGER.info("Triggering Toxin effect on " + target.getName().getString());
    }
    
    private static void triggerBlastEffect(LivingEntity target) {
        // TODO: 实现爆炸效果
        LOGGER.info("Triggering Blast effect on " + target.getName().getString());
    }
    
    private static void triggerCorrosiveEffect(LivingEntity target) {
        // TODO: 实现腐蚀效果
        LOGGER.info("Triggering Corrosive effect on " + target.getName().getString());
    }
    
    private static void triggerGasEffect(LivingEntity target) {
        // TODO: 实现毒气效果
        LOGGER.info("Triggering Gas effect on " + target.getName().getString());
    }
    
    private static void triggerMagneticEffect(LivingEntity target) {
        // TODO: 实现磁力效果
        LOGGER.info("Triggering Magnetic effect on " + target.getName().getString());
    }
    
    private static void triggerRadiationEffect(LivingEntity target) {
        // TODO: 实现辐射效果
        LOGGER.info("Triggering Radiation effect on " + target.getName().getString());
    }
    
    private static void triggerViralEffect(LivingEntity target) {
        // TODO: 实现病毒效果
        LOGGER.info("Triggering Viral effect on " + target.getName().getString());
    }
}