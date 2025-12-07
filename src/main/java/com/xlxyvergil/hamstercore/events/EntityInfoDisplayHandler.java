package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "hamstercore")
public class EntityInfoDisplayHandler {

    @SubscribeEvent
    public static void onEntityHurt(LivingHurtEvent event) {
        // 只有玩家攻击怪物时才显示信息
        if (event.getSource().getEntity() instanceof Player player) {
            LivingEntity target = event.getEntity();
            
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
            
            // 构造消息并发送给玩家
            String message = String.format("%s [等级:%d 护甲:%.2f 派系:%s] 伤害:%.2f -> %.2f", 
                    target.getName().getString(), 
                    level, 
                    armor, 
                    factionName,
                    baseDamage,
                    inflictedDamage);
            
            player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.GOLD));
        }
    }
}