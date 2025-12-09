package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.config.DisplayConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

@Mod.EventBusSubscriber(modid = "hamstercore")
public class EntityInfoDisplayHandler {

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        // 检查配置是否启用了攻击时显示实体信息
        if (!DisplayConfig.getInstance().isShowEntityInfoOnDamage()) {
            return;
        }
        
        // 只有玩家攻击怪物时才显示信息
        if (event.getSource().getEntity() instanceof Player player) {
            LivingEntity target = event.getEntity();
            ItemStack weapon = player.getMainHandItem();
            
            // 获取怪物的等级、护甲和派系信息
            int level = target.getCapability(EntityLevelCapabilityProvider.CAPABILITY)
                    .map(cap -> cap.getLevel())
                    .orElse(20);
            
            double armor = target.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
                    .map(cap -> cap.getArmor())
                    .orElse(0.0);
                    
            String factionName = target.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
                    .map(cap -> cap.getFaction().getDisplayName())
                    .orElse("Unknown");
            
            // 获取基础伤害（FactionDamageHandler处理前的伤害）
            float baseDamage = event.getAmount();
            
            // 手动计算经过阵营修正后的伤害（模拟FactionDamageHandler的计算）
            double customArmor = target.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
                .map(cap -> cap.getArmor())
                .orElse(0.0);
            
            // 计算AM = 0.9 × √(AR/2700)
            double AM = 0.9 * Math.sqrt(customArmor / 2700.0);
            
            // 应用公式: ID = BD × (1-AM)
            float inflictedDamage = (float) (baseDamage * (1.0 - AM));
            
            // 确保伤害不会小于0
            if (inflictedDamage < 0) {
                inflictedDamage = 0;
            }
            
            // 构造实体信息消息
            MutableComponent message = Component.literal("")
                .append(target.getName())
                .append(Component.translatable("hamstercore.ui.level_prefix"))
                .append(Component.literal("" + level).withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("hamstercore.ui.armor_prefix"))
                .append(Component.literal(String.format("%.2f", armor)).withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("hamstercore.ui.faction_prefix"))
                .append(Component.literal(factionName + "]").withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("hamstercore.ui.damage_prefix"))
                .append(Component.literal(String.format("%.2f", baseDamage)).withStyle(ChatFormatting.RED))
                .append(Component.literal(" -> ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(String.format("%.2f", inflictedDamage)).withStyle(ChatFormatting.GOLD));
            
            // 添加武器属性信息
            if (!weapon.isEmpty()) {
                message.append(Component.literal("\n").append(Component.translatable("hamstercore.ui.weapon_attributes").append(": " + weapon.getDisplayName().getString())).withStyle(ChatFormatting.AQUA));
                
                // 获取缓存中的激活元素数据，用于显示暴击率、暴击伤害和触发率
                List<Map.Entry<ElementType, Double>> activeElements = ElementDamageManager.getActiveElements(weapon);
                
                // 显示暴击率（使用缓存中的计算数据）
                double critChance = 0.0;
                for (Map.Entry<ElementType, Double> entry : activeElements) {
                    if (ElementType.CRITICAL_CHANCE == entry.getKey()) {
                        critChance = entry.getValue();
                        break;
                    }
                }
                // 只有当值大于0时才显示
                if (critChance > 0) {
                    message.append(Component.translatable("hamstercore.ui.critical_chance").append(":" + String.format("%.1f%%", critChance * 100)).withStyle(ChatFormatting.YELLOW));
                }
                
                // 显示暴击伤害（使用缓存中的计算数据）
                double critDamage = 0.0;
                for (Map.Entry<ElementType, Double> entry : activeElements) {
                    if (ElementType.CRITICAL_DAMAGE == entry.getKey()) {
                        critDamage = entry.getValue();
                        break;
                    }
                }
                // 只有当值大于0时才显示
                if (critDamage > 0) {
                    message.append(Component.translatable("hamstercore.ui.critical_damage").append(":" + String.format("%.1f", critDamage)).withStyle(ChatFormatting.YELLOW));
                }
                
                // 显示触发率（使用缓存中的计算数据）
                double triggerChance = 0.0;
                for (Map.Entry<ElementType, Double> entry : activeElements) {
                    if (ElementType.TRIGGER_CHANCE == entry.getKey()) {
                        triggerChance = entry.getValue();
                        break;
                    }
                }
                // 只有当值大于0时才显示
                if (triggerChance > 0) {
                    message.append(Component.translatable("hamstercore.ui.trigger_chance").append(":" + String.format("%.1f%%", triggerChance * 100)).withStyle(ChatFormatting.YELLOW));
                }

                // 添加武器元素属性信息（使用缓存中的计算数据）
                Set<String> elementTypes = ElementNBTUtils.getAllUsageElementTypes(weapon);
                
                if (!elementTypes.isEmpty()) {
                    message.append(Component.translatable("hamstercore.ui.element_ratios").withStyle(ChatFormatting.DARK_GREEN));
                    
                    // 使用之前获取的缓存中的激活元素数据
                    for (Map.Entry<ElementType, Double> entry : activeElements) {
                        ElementType elementType = entry.getKey();
                        double elementValue = entry.getValue();
                        
                        // 只显示物理元素、基础元素和复合元素，不显示特殊属性
                        if (elementType != null && (elementType.isPhysical() || elementType.isBasic() || elementType.isComplex())) {
                            String elementName = Component.translatable("element." + elementType.getName() + ".name").getString();
                            message.append(Component.literal(String.format("  %s: %.2f", elementName, elementValue))
                                .withStyle(style -> style.withColor(elementType.getColor().getColor())));
                        }
                    }
                }

                // 添加派系增伤信息
                Set<String> factions = ElementNBTUtils.getAllExtraFactions(weapon);
                if (!factions.isEmpty()) {
                    message.append(Component.translatable("hamstercore.ui.faction_damage_bonus").withStyle(ChatFormatting.GOLD));
                    
                    for (String faction : factions) {
                        double modifier = ElementNBTUtils.getExtraFactionModifier(weapon, faction);
                        if (modifier != 0) {
                            String fName = Component.translatable("faction." + faction.toLowerCase() + ".name").getString();
                            message.append(Component.literal(String.format("  %s: %.1f%%", fName, modifier * 100))
                                .withStyle(getFactionColor(faction)));
                        }
                    }
                }
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
        // 获取Extra层派系数据
        java.util.Set<String> factions = com.xlxyvergil.hamstercore.util.ElementNBTUtils.getAllExtraFactions(weapon);
        
        // 如果没有派系增伤数据，则直接返回
        if (factions.isEmpty()) {
            return;
        }
        
        // 添加每个派系的增伤数值
        for (String faction : factions) {
            double modifier = com.xlxyvergil.hamstercore.util.ElementNBTUtils.getExtraFactionModifier(weapon, faction);
            
            // 只显示非零的派系增伤
            if (modifier != 0) {
                message.append(
                    net.minecraft.network.chat.Component.translatable(
                        "hamstercore.ui.faction_damage_bonus." + faction.toLowerCase())
                        .append(":" + String.format("%.1f%%", modifier * 100))
                        .withStyle(net.minecraft.ChatFormatting.GOLD)
                );
            }
        }
    }
}