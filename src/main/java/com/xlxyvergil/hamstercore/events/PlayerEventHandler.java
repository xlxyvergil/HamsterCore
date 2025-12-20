package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.EntityCapabilityAttacher;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HamsterCore.MODID)
public class PlayerEventHandler {

    /**
     * 监听玩家登录事件
     * 玩家首次加入世界时需要初始化能力值
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            LivingEntity player = event.getEntity();
            // 初始化玩家能力
            EntityCapabilityAttacher.initializeEntityCapabilities(player);
            // 同步到客户端
            EntityCapabilityAttacher.syncEntityCapabilitiesToClients(player);
        }
    }

    /**
     * 监听玩家重生事件
     * 玩家死亡后重生时需要重新同步能力值
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            LivingEntity player = event.getEntity();
            // 重新初始化玩家能力
            EntityCapabilityAttacher.initializeEntityCapabilities(player);
            // 同步到客户端
            EntityCapabilityAttacher.syncEntityCapabilitiesToClients(player);
        }
    }

    /**
     * 监听玩家维度传送事件
     * 玩家在不同维度间传送时需要重新同步数据
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            LivingEntity player = event.getEntity();
            // 重新同步玩家能力
            EntityCapabilityAttacher.syncEntityCapabilitiesToClients(player);
        }
    }
}