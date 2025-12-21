package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.level.PlayerLevelManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerLevelEvents {
    
    // 监听玩家获得经验的事件
    @SubscribeEvent
    public static void onPlayerPickupXp(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        ExperienceOrb orb = event.getOrb();
        // 添加与拾取的经验球相同数量的经验到我们的系统中
        PlayerLevelManager.addExperience(player, orb.value);
    }
    
    // 玩家复活时触发等级更新
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        player.getCapability(com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapability.CAPABILITY).ifPresent(cap -> {
            // 发布等级更新事件，触发PlayerCapabilityAttacher重新初始化数据
            net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new com.xlxyvergil.hamstercore.level.PlayerLevelUpEvent(player, cap.getPlayerLevel()));
        });
    }
}