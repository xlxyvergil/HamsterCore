package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.config.DisplayConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.ElementNBTUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

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
                
                // 显示暴击率
                double critChance = ElementNBTUtils.getCriticalChance(weapon);
                message.append(Component.translatable("hamstercore.ui.critical_chance").append(":" + String.format("%.1f%%", critChance * 100)).withStyle(ChatFormatting.YELLOW));
                
                // 显示暴击伤害
                double critDamage = ElementNBTUtils.getCriticalDamage(weapon);
                message.append(Component.translatable("hamstercore.ui.critical_damage").append(":" + String.format("%.1f", critDamage)).withStyle(ChatFormatting.YELLOW));
                
                // 显示触发率
                double triggerChance = ElementNBTUtils.getTriggerChance(weapon);
                message.append(Component.translatable("hamstercore.ui.trigger_chance").append(":" + String.format("%.1f%%", triggerChance * 100)).withStyle(ChatFormatting.YELLOW));
                
                // 添加武器元素属性信息
                // 直接从NBT获取元素类型
                java.util.Set<ElementType> elementTypes = ElementNBTUtils.getAllElementTypes(weapon);
                
                if (!elementTypes.isEmpty()) {
                    message.append(Component.translatable("hamstercore.ui.element_ratios").withStyle(ChatFormatting.DARK_GREEN));
                    
                    for (ElementType elementType : elementTypes) {
                        // 获取元素值
                        double elementValue = ElementNBTUtils.getElementValue(weapon, elementType);
                        
                        // 只显示物理元素、基础元素和复合元素，不显示特殊属性
                        if (elementType.isPhysical() || elementType.isBasic() || elementType.isComplex()) {
                            // 元素属性数值不以百分比形式展示
                            message.append(Component.literal(" " + elementType.getColoredName().getString()).withStyle(elementType.getColor()));
                            message.append(Component.literal(":" + String.format("%.2f", elementValue)).withStyle(ChatFormatting.WHITE));
                        }
                    }
                }
            }
            
            player.sendSystemMessage(message);
        }
    }
}