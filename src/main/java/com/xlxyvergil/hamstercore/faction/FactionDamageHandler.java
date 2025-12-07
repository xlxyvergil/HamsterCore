package com.xlxyvergil.hamstercore.faction;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.element.ElementHelper;
import com.xlxyvergil.hamstercore.element.ElementInstance;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.ElementTriggerHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "hamstercore")
public class FactionDamageHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    
    // 添加静态变量存储最近一次战斗信息
    public static boolean lastAttackWasCritical = false;
    public static int lastCriticalLevel = 0;
    public static double lastCriticalMultiplier = 1.0;
    public static List<ElementType> lastTriggeredElements = null;
    
    // 基础等级
    private static final int BASE_LEVEL = 20;
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        // 获取被攻击的实体
        LivingEntity target = event.getEntity();
        
        // 获取攻击源
        Entity sourceEntity = event.getSource().getEntity();
        
        // 确保攻击者也是生物实体
        if (sourceEntity instanceof LivingEntity livingAttacker) {
            // 使用公式 ID = BD × (1+HM) × 元素总倍率 × 暴击伤害 × (1-AM)
            // BD = 基础伤害
            float baseDamage = event.getAmount();
            
            // 计算各部分的伤害修正系数
            double factionModifier = calculateFactionModifier(livingAttacker, target); // HM
            double elementMultiplier = calculateElementMultiplier(livingAttacker); // 元素总倍率
            double criticalMultiplier = calculateCriticalMultiplier(livingAttacker); // 暴击伤害
            double armorReduction = calculateArmorReduction(target); // (1-AM)
            
            // 应用公式: ID = BD × (1+HM) × 元素总倍率 × 暴击伤害 × (1-AM)
            float inflictedDamage = (float) (baseDamage * (1.0 + factionModifier) * elementMultiplier * criticalMultiplier * armorReduction);
            
            // 确保伤害不会小于0
            if (inflictedDamage < 0) {
                inflictedDamage = 0;
            }
            
            // 设置最终伤害
            event.setAmount(inflictedDamage);
            
            // 处理元素触发效果，使用新的ElementTriggerHandler类
            ElementTriggerHandler.handleElementTriggers(livingAttacker, target);
        }
    }
    
    /**
     * 计算派系克制系数 (HM)
     * @param attacker 攻击者
     * @param target 目标实体
     * @return 派系克制系数
     */
    private static double calculateFactionModifier(LivingEntity attacker, LivingEntity target) {
        // 获取目标实体的派系
        String targetFaction = target.getCapability(com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider.CAPABILITY)
            .map(factionCap -> {
                com.xlxyvergil.hamstercore.faction.Faction faction = factionCap.getFaction();
                return faction != null ? faction.name() : "OROKIN";
            })
            .orElse("OROKIN");
        
        // 获取攻击者使用的物品
        ItemStack weapon = attacker instanceof Player ? ((Player) attacker).getMainHandItem() : ItemStack.EMPTY;
        
        // 获取武器上的元素属性
        Map<ElementType, ElementInstance> elements = 
            ElementHelper.hasElementAttributes(weapon) ? 
            ElementHelper.getElementAttributes(weapon) : 
            new java.util.HashMap<>();
        
        // 计算克制系数
        double modifier = 0.0;
        
        // 根据规划文档中的克制关系计算
        switch (targetFaction) {
            case "GRINEER":
                // Grineer: 冲击+50% 腐蚀+50%
                if (elements.containsKey(ElementType.IMPACT)) {
                    modifier += 0.5;
                }
                if (elements.containsKey(ElementType.CORROSIVE)) {
                    modifier += 0.5;
                }
                break;
                
            case "INFESTED":
                // Infested: 切割+50% 火焰+50%
                if (elements.containsKey(ElementType.SLASH)) {
                    modifier += 0.5;
                }
                if (elements.containsKey(ElementType.HEAT)) {
                    modifier += 0.5;
                }
                break;
                
            case "CORPUS":
                // Corpus: 穿刺+50% 磁力+50%
                if (elements.containsKey(ElementType.PUNCTURE)) {
                    modifier += 0.5;
                }
                if (elements.containsKey(ElementType.MAGNETIC)) {
                    modifier += 0.5;
                }
                break;
                
            case "OROKIN":
                // Orokin: 穿刺+50% 病毒+50% 辐射-50%
                if (elements.containsKey(ElementType.PUNCTURE)) {
                    modifier += 0.5;
                }
                if (elements.containsKey(ElementType.VIRAL)) {
                    modifier += 0.5;
                }
                if (elements.containsKey(ElementType.RADIATION)) {
                    modifier -= 0.5;
                }
                break;
                
            case "SENTIENT":
                // Sentient: 冰冻50% 辐射+50% 腐蚀-50%
                if (elements.containsKey(ElementType.COLD)) {
                    modifier += 0.5;
                }
                if (elements.containsKey(ElementType.RADIATION)) {
                    modifier += 0.5;
                }
                if (elements.containsKey(ElementType.CORROSIVE)) {
                    modifier -= 0.5;
                }
                break;
                
            case "MURMUR":
                // 低语者: 电击+50% 辐射+50% 病毒-50%
                if (elements.containsKey(ElementType.ELECTRICITY)) {
                    modifier += 0.5;
                }
                if (elements.containsKey(ElementType.RADIATION)) {
                    modifier += 0.5;
                }
                if (elements.containsKey(ElementType.VIRAL)) {
                    modifier -= 0.5;
                }
                break;
        }
        
        return modifier;
    }
    
    /**
     * 计算元素总倍率
     * @param attacker 攻击者
     * @return 元素总倍率
     */
    private static double calculateElementMultiplier(LivingEntity attacker) {
        double totalElementMultiplier = 1.0; // 默认为1，即无元素加成
        
        if (attacker instanceof Player player) {
            // 获取攻击者使用的物品
            ItemStack weapon = player.getMainHandItem();
            
            // 检查物品是否有元素属性
            if (ElementHelper.hasElementAttributes(weapon)) {
                // 从武器NBT中获取元素属性以计算元素总倍率
                Map<ElementType, ElementInstance> elements = ElementHelper.getElementAttributes(weapon);
                
                // 计算元素总倍率（所有元素倍率之和，除了暴击相关属性和触发率）
                double elementTotalRatio = 0.0;
                for (Map.Entry<ElementType, ElementInstance> entry : elements.entrySet()) {
                    ElementType elementType = entry.getKey();
                    ElementInstance elementInstance = entry.getValue();
                    
                    // 排除暴击相关属性和触发率，只计算元素伤害倍率
                    if (elementType != ElementType.CRITICAL_CHANCE && 
                        elementType != ElementType.CRITICAL_DAMAGE &&
                        elementType != ElementType.TRIGGER_CHANCE) {
                        elementTotalRatio += elementInstance.value();
                    }
                }
                
                // 元素总倍率 = 所有元素倍率之和（默认值保证了至少为1）
                totalElementMultiplier = elementTotalRatio;
            }
        }
        
        return totalElementMultiplier;
    }
    
    /**
     * 计算暴击伤害倍率
     * @param attacker 攻击者
     * @return 暴击伤害倍率
     */
    private static double calculateCriticalMultiplier(LivingEntity attacker) {
        double criticalMultiplier = 1.0; // 默认无暴击
        
        if (attacker instanceof Player player) {
            // 获取攻击者使用的物品
            ItemStack weapon = player.getMainHandItem();
            
            // 检查物品是否有元素属性
            if (ElementHelper.hasElementAttributes(weapon)) {
                // 从武器NBT中获取元素属性
                Map<ElementType, ElementInstance> elements = ElementHelper.getElementAttributes(weapon);
                
                // 获取暴击率和暴击伤害
                double criticalChance = 0.0;
                double criticalDamage = 1.0; // 默认暴击伤害倍率
                
                if (elements.containsKey(ElementType.CRITICAL_CHANCE)) {
                    criticalChance = elements.get(ElementType.CRITICAL_CHANCE).value();
                }
                
                if (elements.containsKey(ElementType.CRITICAL_DAMAGE)) {
                    criticalDamage = elements.get(ElementType.CRITICAL_DAMAGE).value();
                }
                
                // 使用Random判断是否暴击
                Random random = new Random();
                // 暴击等级判断：n% < 暴击几率 ≤ (n% + 100%)
                // 概率达到的暴击等级：(n% + 100%) ÷ 100
                // 保底达到的暴击等级：(n% + 100%) ÷ 100 - 1
                
                double chancePercent = criticalChance * 100;
                int guaranteedCriticalLevel = (int) Math.floor((chancePercent + 100) / 100) - 1; // 保底暴击等级
                int maxCriticalLevel = (int) Math.floor((chancePercent + 100) / 100); // 最大可能暴击等级
                
                // 确保暴击等级至少为0
                if (guaranteedCriticalLevel < 0) {
                    guaranteedCriticalLevel = 0;
                }
                
                if (maxCriticalLevel < 0) {
                    maxCriticalLevel = 0;
                }
                
                // 判断是否能达到更高的暴击等级
                int criticalLevel = guaranteedCriticalLevel;
                double extraChance = chancePercent - (guaranteedCriticalLevel * 100); // 超出保底等级的部分
                
                if (random.nextDouble() * 100 < extraChance) {
                    criticalLevel = maxCriticalLevel;
                }
                
                // 暴击倍率（暴击伤害） =1 + 暴击等级 × (武器总暴击倍率 − 1)
                // 武器总暴击倍率 = 武器基础暴击倍率 × (1 +暴击倍率增益)
                double totalCriticalDamage = criticalDamage; // 简化处理，暂不考虑暴击倍率增益
                criticalMultiplier = 1 + criticalLevel * (totalCriticalDamage - 1);
                
                // 更新暴击信息
                lastAttackWasCritical = true;
                lastCriticalLevel = criticalLevel;
                lastCriticalMultiplier = criticalMultiplier;
                
                LOGGER.info("Critical hit! Level: " + criticalLevel + ", Damage multiplied by " + criticalMultiplier);
                
                // 向玩家发送暴击信息
                player.sendSystemMessage(Component.literal("暴击! 等级: " + criticalLevel + ", 伤害倍率: " + String.format("%.2f", criticalMultiplier)).withStyle(ChatFormatting.GOLD));
            }
        }
        
        return criticalMultiplier;
    }
    
    /**
     * 计算护甲减免系数 (1-AM)
     * @param target 目标实体
     * @return 护甲减免系数
     */
    private static double calculateArmorReduction(LivingEntity target) {
        // 获取我们自定义的护甲值
        double customArmor = target.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
            .map(armorCap -> {
                LOGGER.debug("Getting armor from capability for entity: " + target.getType().getDescriptionId());
                double armor = armorCap.getArmor();
                LOGGER.debug("Got armor value: " + armor);
                return armor;
            })
            .orElse(0.0);
        
        // 计算AM = 0.9 × √(AR/2700)
        double AM = 0.9 * Math.sqrt(customArmor / 2700.0);
        
        return 1.0 - AM;
    }
}