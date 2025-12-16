package com.xlxyvergil.hamstercore.events;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.xlxyvergil.hamstercore.config.DisplayConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.handler.AffixCacheManager;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager;

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
            
            // 获取缓存数据
            AffixCacheManager.AffixCacheData cacheData = AffixCacheManager.getOrCreateCache(weapon);
            
            // 合并特殊元素和派系元素用于伤害计算
            Map<String, Double> specialAndFactionValues = new java.util.HashMap<>();
            specialAndFactionValues.putAll(cacheData.getCriticalStats());
            specialAndFactionValues.putAll(cacheData.getFactionElements());
            
            // 使用ElementDamageManager计算真实的伤害数据
            ElementDamageManager.ElementDamageData damageData = 
                ElementDamageManager.calculateElementDamage(player, target, baseDamage, weapon, targetFaction, armor, specialAndFactionValues);
            
            // 获取经过完整计算后的实际伤害
            float inflictedDamage = damageData.getFinalDamage();
            
            // 构造实体信息消息
            MutableComponent message = Component.literal("")
                .append(target.getName())
                .append(Component.translatable("hamstercore.ui.level_prefix"))
                .append(Component.literal("" + level).withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("hamstercore.ui.armor_prefix"))
                .append(Component.literal(String.format("%.2f", armor)).withStyle(ChatFormatting.WHITE))
                .append(Component.translatable("hamstercore.ui.faction_prefix"))
                .append(Component.literal(factionName + "]").withStyle(factionColor))
                .append(Component.translatable("hamstercore.ui.damage_prefix"))
                .append(Component.literal(String.format("%.2f", baseDamage)).withStyle(ChatFormatting.RED))
                .append(Component.literal(" -> ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(String.format("%.2f", inflictedDamage)).withStyle(ChatFormatting.GOLD));
            
            // 添加武器属性信息
            if (!weapon.isEmpty()) {
                message.append(Component.literal("\n").append(Component.translatable("hamstercore.ui.weapon_attributes").append(": " + weapon.getDisplayName().getString())).withStyle(ChatFormatting.AQUA));
                
                // 显示暴击率
                double critChance = cacheData.getCriticalStats().getOrDefault("critical_chance", 0.0);
                if (critChance > 0) {
                    message.append(Component.translatable("hamstercore.ui.critical_chance").append(":" + String.format("%.1f%%", critChance * 100)).withStyle(ChatFormatting.YELLOW));
                }
                
                // 显示暴击伤害
                double critDamage = cacheData.getCriticalStats().getOrDefault("critical_damage", 0.0);
                if (critDamage > 0) {
                    message.append(Component.translatable("hamstercore.ui.critical_damage").append(":" + String.format("%.1f%%", critDamage * 100)).withStyle(ChatFormatting.YELLOW));
                }
                
                // 显示触发率
                double triggerChance = cacheData.getCriticalStats().getOrDefault("trigger_chance", 0.0);
                if (triggerChance > 0) {
                    message.append(Component.translatable("hamstercore.ui.trigger_chance").append(":" + String.format("%.1f%%", triggerChance * 100)).withStyle(ChatFormatting.YELLOW));
                }

                // 添加武器元素属性信息
                Map<String, Double> physicalElements = cacheData.getPhysicalElements();
                if (!physicalElements.isEmpty()) {
                    // 添加元素属性标题
                    message.append(Component.translatable("hamstercore.ui.element_ratios").withStyle(ChatFormatting.DARK_GREEN));
                    
                    // 遍历所有基础元素和复合元素
                    for (ElementType elementType : ElementType.getAllTypes()) {
                        if (elementType != null && (elementType.isBasic() || elementType.isComplex())) {
                            Double elementValue = physicalElements.get(elementType.getName());
                            if (elementValue != null && elementValue > 0) {
                                String elementName = Component.translatable("element." + elementType.getName() + ".name").getString();
                                message.append(Component.literal(String.format("  %s: %.2f", elementName, elementValue))
                                    .withStyle(style -> style.withColor(elementType.getColor().getColor())));
                            }
                        }
                    }
                }

                // 添加派系增伤信息
                addFactionModifiersToMessage(message, weapon, cacheData);
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
    private static void addFactionModifiersToMessage(MutableComponent message, ItemStack weapon, AffixCacheManager.AffixCacheData cacheData) {
        // 从缓存中获取派系元素
        Map<String, Double> factionElements = cacheData.getFactionElements();
        
        // 遍历所有元素，只处理派系类型的元素
        for (Map.Entry<String, Double> entry : factionElements.entrySet()) {
            String elementType = entry.getKey();
            Double factionModifier = entry.getValue();
            
            // 检查是否为派系类型并且值大于0
            if (isFactionType(elementType) && factionModifier != null && factionModifier > 0) {
                message.append(
                    net.minecraft.network.chat.Component.translatable(
                        "hamstercore.ui.faction_damage_bonus." + elementType.toLowerCase())
                        .append(":" + String.format("%.1f%%", factionModifier * 100))
                        .withStyle(net.minecraft.ChatFormatting.GOLD)
                );
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
}