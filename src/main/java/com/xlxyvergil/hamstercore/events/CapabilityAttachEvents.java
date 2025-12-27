package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityEffectCapability;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CapabilityAttachEvents {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<net.minecraft.world.entity.Entity> event) {
        // 为实体附加状态效果Capability
        EntityEffectCapability.attachEntityCapability(event);
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        // 当玩家开始追踪实体时，同步状态效果Capability
        EntityEffectCapability.syncEntityCapability(event);
    }
}