package com.xlxyvergil.hamstercore.events;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.xlxyvergil.hamstercore.config.DisplayConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager.ModifierResults;
import com.xlxyvergil.hamstercore.handler.ElementTriggerHandler;
import com.xlxyvergil.hamstercore.util.ElementNBTUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "hamstercore")
public class EntityInfoDisplayHandler {

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        // 检查配置是否启用了攻击时显示实体信息
        if (!DisplayConfig.getInstance().isShowEntityInfoOnDamage()) {
            return;
        }
        
        // 只有玩家攻击怪物时才显示信息
        if (event.getSource().getEntity() instanceof Player) {
            Player player = (Player) event.getSource().getEntity();
            LivingEntity target = event.getEntity();
            ItemStack weapon = player.getMainHandItem();
            
            // 获取怪物的等级、护甲和派系信息
            int level = target.getCapability(EntityLevelCapabilityProvider.CAPABILITY)
                    .map(cap -> cap.getLevel())
                    .orElse(20);
            
            double armor = target.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
                    .map(cap -> cap.getArmor())
                    .orElse(0.0);
            
            // 限制护甲值上限为2700（用于显示）
            armor = Math.min(armor, 2700.0);
                    
            // 获取目标实体的派系名称（用于ElementDamageManager和显示）
            String targetFaction = target.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
                    .map(cap -> cap.getFaction() != null ? cap.getFaction().name() : "OROKIN")
                    .orElse("OROKIN");
                    
            String factionName = target.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
                    .map(cap -> cap.getFaction().getDisplayName())
                    .orElse("Unknown");
                    
            // 获取派系颜色
            ChatFormatting factionColor = getFactionColor(targetFaction);
            
            // 获取基础伤害（FactionDamageHandler处理前的伤害）
            float baseDamage = event.getAmount();
            
            // 使用ElementNBTUtils获取武器元素数据
            Map<String, Double> specialAndFactionValues = new java.util.HashMap<>();
            
            // 使用ElementDamageManager计算真实的伤害数据（现在不依赖缓存数据）
            ElementDamageManager.ElementDamageData damageData = 
                ElementDamageManager.calculateElementDamage(player, target, baseDamage, weapon, targetFaction, armor);
            
            // 获取经过完整计算后的实际伤害
            float inflictedDamage = damageData.getFinalDamage();
            
            // 构造实体信息消息
            MutableComponent message = Component.literal("")
                .append(target.getName())
                .append(Component.literal(" "))
                .append(Component.translatable("hamstercore.ui.level_prefix"))
                .append(Component.literal("" + level).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(", "))
                .append(Component.translatable("hamstercore.ui.armor_prefix"))
                .append(Component.literal(String.format("%.2f", armor)).withStyle(ChatFormatting.WHITE))
                .append(Component.literal(", "))
                .append(Component.translatable("hamstercore.ui.faction_prefix"))
                .append(Component.literal(factionName + "]").withStyle(factionColor))
                .append(Component.literal(", "))
                .append(Component.translatable("hamstercore.ui.damage_prefix"))
                .append(Component.literal(String.format("%.2f", baseDamage)).withStyle(ChatFormatting.RED))
                .append(Component.literal(" -> ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(String.format("%.2f", inflictedDamage)).withStyle(ChatFormatting.GOLD));
            
            // 添加武器属性信息
            if (!weapon.isEmpty()) {
                message.append(Component.literal("\n"))
                    .append(Component.translatable("hamstercore.ui.weapon_attributes"))
                    .append(": " + weapon.getDisplayName().getString())
                    .withStyle(ChatFormatting.AQUA);
                
                // 从ElementDamageData中获取暴击信息
                int currentCriticalLevel = damageData.getCriticalLevel();
                double currentCriticalDamage = damageData.getCriticalDamage();
                double currentCriticalMultiplier = damageData.getCriticalMultiplier();
                
                // 构建武器详细属性行
                boolean hasWeaponDetails = false;
                
                // 显示暴击率
                double critChance = ElementNBTUtils.readCriticalChance(weapon);
                if (critChance > 0) {
                    message.append(Component.literal("\n  "))
                        .append(Component.translatable("hamstercore.ui.critical_chance"))
                        .append(": " + String.format("%.1f%%", critChance * 100))
                        .withStyle(ChatFormatting.YELLOW);
                    hasWeaponDetails = true;
                }
                
                // 显示暴击伤害
                if (currentCriticalDamage > 0) {
                    if (hasWeaponDetails) {
                        message.append(Component.literal(", "));
                    } else {
                        message.append(Component.literal("\n  "));
                    }
                    message.append(Component.translatable("hamstercore.ui.critical_damage"))
                        .append(": " + String.format("%.1f%%", currentCriticalDamage * 100))
                        .withStyle(ChatFormatting.YELLOW);
                    hasWeaponDetails = true;
                }
                
                // 显示当前暴击等级和暴击倍率
                if (critChance > 0 && currentCriticalLevel > 0) {
                    if (hasWeaponDetails) {
                        message.append(Component.literal(", "));
                    } else {
                        message.append(Component.literal("\n  "));
                    }
                    message.append(Component.literal("暴击等级: " + currentCriticalLevel + ", 暴击倍率: " + String.format("%.2f", currentCriticalMultiplier)).withStyle(ChatFormatting.RED));
                    hasWeaponDetails = true;
                }
                
                // 显示触发率
                double triggerChance = ElementNBTUtils.readTriggerChance(weapon);
                if (triggerChance > 0) {
                    if (hasWeaponDetails) {
                        message.append(Component.literal(", "));
                    } else {
                        message.append(Component.literal("\n  "));
                    }
                    message.append(Component.translatable("hamstercore.ui.trigger_chance"))
                        .append(": " + String.format("%.1f%%", triggerChance * 100))
                        .withStyle(ChatFormatting.YELLOW);
                    hasWeaponDetails = true;
                }
                
                // 显示详细的modifier计算结果
                ModifierResults modifierResults = damageData.getModifierResults();
                if (modifierResults != null) {
                    boolean hasModifierInfo = false;
                    
                    // 显示派系克制
                    if (modifierResults.getFactionModifier() > 0) {
                        message.append(Component.literal("\n  ")
                            .append("派系克制(HM): " + String.format("%.2f", modifierResults.getFactionModifier()))
                            .withStyle(ChatFormatting.GOLD));
                        hasModifierInfo = true;
                    }
                    
                    // 显示元素总倍率
                    if (modifierResults.getElementMultiplier() > 0) {
                        if (hasModifierInfo) {
                            message.append(Component.literal(", "));
                        } else {
                            message.append(Component.literal("\n  "));
                        }
                        message.append(Component.literal("元素总倍率: " + String.format("%.2f", modifierResults.getElementMultiplier()))
                            .withStyle(ChatFormatting.GREEN));
                        hasModifierInfo = true;
                    }
                    
                    // 显示物理元素总倍率
                    if (modifierResults.getPhysicalElementMultiplier() > 0) {
                        if (hasModifierInfo) {
                            message.append(Component.literal(", "));
                        } else {
                            message.append(Component.literal("\n  "));
                        }
                        message.append(Component.literal("物理元素总倍率: " + String.format("%.2f", modifierResults.getPhysicalElementMultiplier()))
                            .withStyle(ChatFormatting.BLUE));
                        hasModifierInfo = true;
                    }
                    
                    // 显示护甲减免
                    if (modifierResults.getArmorReduction() < 1.0) {
                        if (hasModifierInfo) {
                            message.append(Component.literal(", "));
                        } else {
                            message.append(Component.literal("\n  "));
                        }
                        message.append(Component.literal("护甲减免(1-AM): " + String.format("%.2f%%", (1.0 - modifierResults.getArmorReduction()) * 100))
                            .withStyle(ChatFormatting.GRAY));
                    }
                }

                // 添加武器元素属性信息
                // 使用ElementNBTUtils读取元素数据
                Map<String, Double> combinedElements = ElementNBTUtils.readCombinedElements(weapon);
                Map<String, Double> physicalElements = ElementNBTUtils.readPhysicalElements(weapon);
                
                // 检查是否有任何元素值
                boolean hasElements = ElementNBTUtils.hasNonZeroElements(weapon);
                
                if (hasElements) {
                    // 添加元素属性标题
                    message.append(Component.literal("\n"))
                        .append(Component.translatable("hamstercore.ui.element_ratios"))
                        .withStyle(ChatFormatting.DARK_GREEN);
                    
                    boolean hasAnyElement = false;
                    
                    // 先显示物理元素
                    for (ElementType elementType : ElementType.getPhysicalElements()) {
                        double elementValue = ElementNBTUtils.readPhysicalElementValue(weapon, elementType.getName());
                        if (elementValue > 0) {
                            MutableComponent elementName = elementType.getColoredName();
                            message.append(Component.literal(" ")
                                .append(String.format("%s: %.2f", elementName.getString(), elementValue))
                                .withStyle(style -> style.withColor(elementType.getColor().getColor())));
                            hasAnyElement = true;
                        }
                    }
                    
                    // 再显示基础元素
                    for (ElementType elementType : ElementType.getBasicElements()) {
                        double elementValue = ElementNBTUtils.readBasicElementValue(weapon, elementType.getName());
                        if (elementValue > 0) {
                            MutableComponent elementName = elementType.getColoredName();
                            message.append(Component.literal(" ")
                                .append(String.format("%s: %.2f", elementName.getString(), elementValue))
                                .withStyle(style -> style.withColor(elementType.getColor().getColor())));
                            hasAnyElement = true;
                        }
                    }
                    
                    // 最后显示复合元素
                    for (ElementType elementType : ElementType.getComplexElements()) {
                        double elementValue = ElementNBTUtils.readCombinedElementValue(weapon, elementType.getName());
                        if (elementValue > 0) {
                            MutableComponent elementName = elementType.getColoredName();
                            message.append(Component.literal(" ")
                                .append(String.format("%s: %.2f", elementName.getString(), elementValue))
                                .withStyle(style -> style.withColor(elementType.getColor().getColor())));
                            hasAnyElement = true;
                        }
                    }
                    
                    // 如果没有元素，至少显示一个空行来保持格式
                    if (!hasAnyElement) {
                        message.append(Component.literal("\n  ")
                            .append(Component.translatable("hamstercore.ui.no_elements"))
                            .withStyle(ChatFormatting.GRAY));
                    }
                }

                // 添加触发的元素信息
                addTriggeredElementsToMessage(message);
                
                // 添加派系增伤信息
                addFactionModifiersToMessage(message, weapon);
            }
            
            player.sendSystemMessage(message);
        }
    }
    
    /**
     * 获取派系颜色
     */
    private static ChatFormatting getFactionColor(String faction) {
        // 根据派系设置颜色
        ChatFormatting color = ChatFormatting.WHITE; // 默认颜色
        switch (faction.toUpperCase()) {
            case "GRINEER":
                color = ChatFormatting.RED;
                break;
            case "INFESTED":
                color = ChatFormatting.GREEN;
                break;
            case "CORPUS":
                color = ChatFormatting.BLUE;
                break;
            case "OROKIN":
                color = ChatFormatting.LIGHT_PURPLE;
                break;
            case "SENTIENT":
                color = ChatFormatting.DARK_RED;
                break;
            case "MURMUR":
                color = ChatFormatting.AQUA;
                break;
        }
        return color;
    }
    
    /**
     * 添加派系增伤属性信息到消息中
     * @param message 消息组件
     * @param weapon 武器物品堆
     */
    private static void addFactionModifiersToMessage(MutableComponent message, ItemStack weapon) {
        // 使用ElementNBTUtils获取派系元素
        Map<String, Double> factionElements = ElementNBTUtils.readFactionElements(weapon);
        
        boolean hasFactionBonus = false;
        
        // 遍历所有元素，只处理派系类型的元素
        for (Map.Entry<String, Double> entry : factionElements.entrySet()) {
            String elementType = entry.getKey();
            Double factionModifier = entry.getValue();
            
            // 检查是否为派系类型并且值大于0
            if (isFactionType(elementType) && factionModifier != null && factionModifier > 0) {
                if (hasFactionBonus) {
                    message.append(Component.literal(", "));
                } else {
                    message.append(Component.literal("\n  "));
                }
                message.append(net.minecraft.network.chat.Component.translatable(
                        "hamstercore.ui.faction_damage_bonus." + elementType.toLowerCase())
                        .append(": " + String.format("%.1f%%", factionModifier * 100))
                        .withStyle(net.minecraft.ChatFormatting.GOLD));
                hasFactionBonus = true;
            }
        }
    }
    
    /**
     * 判断元素类型是否为派系类型
     */
    private static boolean isFactionType(String elementType) {
        Set<String> factionTypes = new HashSet<>();
        factionTypes.add("grineer");
        factionTypes.add("infested");
        factionTypes.add("corpus");
        factionTypes.add("orokin");
        factionTypes.add("sentient");
        factionTypes.add("murmur");
        
        return factionTypes.contains(elementType);
    }
    
    /**
     * 添加触发的元素信息到消息中
     * @param message 消息组件
     */
    private static void addTriggeredElementsToMessage(MutableComponent message) {
        java.util.List<ElementType> triggeredElements = ElementTriggerHandler.getTriggeredElements();
        
        if (!triggeredElements.isEmpty()) {
            message.append(Component.literal("\n"))
                .append(Component.translatable("hamstercore.ui.triggered_elements"))
                .withStyle(ChatFormatting.LIGHT_PURPLE);
            
            // 统计每种元素的触发次数
            java.util.Map<ElementType, Integer> elementCount = new java.util.HashMap<>();
            for (ElementType element : triggeredElements) {
                elementCount.put(element, elementCount.getOrDefault(element, 0) + 1);
            }
            
            // 显示触发的元素和次数
            boolean isFirst = true;
            for (java.util.Map.Entry<ElementType, Integer> entry : elementCount.entrySet()) {
                ElementType elementType = entry.getKey();
                int count = entry.getValue();
                MutableComponent elementName = elementType.getColoredName();
                
                if (isFirst) {
                    isFirst = false;
                } else {
                    message.append(Component.literal(" "));
                }
                
                if (count > 1) {
                    message.append(String.format("%s x%d", elementName.getString(), count))
                        .withStyle(style -> style.withColor(elementType.getColor().getColor()));
                } else {
                    message.append(String.format("%s", elementName.getString()))
                        .withStyle(style -> style.withColor(elementType.getColor().getColor()));
                }
            }
        }
    }
}