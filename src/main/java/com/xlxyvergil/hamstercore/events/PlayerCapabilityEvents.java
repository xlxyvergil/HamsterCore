package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapability;
import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.level.PlayerLevelUpEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerCapabilityEvents {
    
    @SubscribeEvent
    public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Player> event) {
        Player player = event.getObject();
        event.addCapability(
            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hamstercore", "player_level"),
            new PlayerLevelCapabilityProvider()
        );
    }
    
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // 玩家复活时保持玩家等级数据
        if (event.isWasDeath()) {
            event.getOriginal().getCapability(PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(oldCap -> {
                event.getEntity().getCapability(PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(newCap -> {
                    newCap.deserializeNBT(oldCap.serializeNBT());
                });
            });
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        // 玩家登录时触发等级更新事件
        player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            // 发布等级更新事件，触发PlayerCapabilityAttacher重新初始化数据
            MinecraftForge.EVENT_BUS.post(new PlayerLevelUpEvent(player, cap.getPlayerLevel()));
        });
    }
    
    // 添加玩家数据保存事件处理
    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        Player player = event.getEntity();
        player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            CompoundTag data = cap.serializeNBT();
            event.getPlayerFile("hamstercore_player_level").getParentFile().mkdirs();
            try {
                net.minecraft.nbt.NbtIo.writeCompressed(data, event.getPlayerFile("hamstercore_player_level"));
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    // 添加玩家数据加载事件处理
    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        Player player = event.getEntity();
        java.io.File file = event.getPlayerFile("hamstercore_player_level");
        if (file.exists()) {
            try {
                CompoundTag data = net.minecraft.nbt.NbtIo.readCompressed(file);
                player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    cap.deserializeNBT(data);
                });
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}