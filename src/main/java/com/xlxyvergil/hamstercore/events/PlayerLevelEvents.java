package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.level.PlayerLevelManager;
import com.xlxyvergil.hamstercore.level.PlayerLevelUpEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HamsterCore.MODID)
public class PlayerLevelEvents {
    
    // 监听生物受到伤害的事件（用于处理玩家造成伤害获取经验）
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查伤害来源是否是玩家
        DamageSource source = event.getSource();
        if (source.getEntity() instanceof Player player && !(event.getEntity() instanceof Player)) {
            // 计算经验：每5点伤害获得1点经验
            float damage = event.getAmount();
            int experience = (int) (damage / 5.0f);
            
            // 确保至少获得1点经验（如果伤害大于0）
            if (damage > 0 && experience == 0) {
                experience = 1;
            }
            
            if (experience > 0) {
                // 添加经验到我们的系统中
                PlayerLevelManager.addExperience(player, experience);
            }
        }
    }
    
    // 监听玩家经验变化事件
    @SubscribeEvent
    public static void onPlayerXpChange(PlayerXpEvent.XpChange event) {
        // 只处理经验增加的情况
        if (event.getAmount() > 0) {
            Player player = event.getEntity();
            // 将原版经验值的一半作为我们系统的经验值
            int customExperience = event.getAmount() / 2;
            PlayerLevelManager.addExperience(player, customExperience);
        }
    }
    
    // 玩家复活时触发等级更新
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            // 发布等级更新事件，触发PlayerCapabilityAttacher重新初始化数据
            MinecraftForge.EVENT_BUS.post(new PlayerLevelUpEvent(player, cap.getPlayerLevel()));
        });
    }
}