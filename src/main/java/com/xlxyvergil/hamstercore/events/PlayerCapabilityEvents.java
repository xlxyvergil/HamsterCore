package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.content.capability.PlayerCapabilityAttacher;
import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapability;
import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.*;
import com.xlxyvergil.hamstercore.level.PlayerLevelUpEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PlayerCapabilityEvents {
    
    @SubscribeEvent
    public static void onAttachPlayerCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            // 附加玩家等级能力（我们的玩家等级系统）
            event.addCapability(
                new net.minecraft.resources.ResourceLocation("hamstercore", "player_level"),
                new PlayerLevelCapabilityProvider()
            );
            
            // 附加玩家护甲能力
            EntityArmorCapabilityProvider armorProvider = new EntityArmorCapabilityProvider();
            armorProvider.setEntityType(player.getType());
            event.addCapability(EntityArmorCapability.ID, armorProvider);
            
            // 附加玩家派系能力
            EntityFactionCapabilityProvider factionProvider = new EntityFactionCapabilityProvider();
            factionProvider.setEntityType(player.getType());
            event.addCapability(EntityFactionCapability.ID, factionProvider);
            
            // 附加玩家生命值修饰符能力
            event.addCapability(EntityHealthModifierCapability.ID, new EntityHealthModifierCapabilityProvider());
            
            // 玩家总是拥有护盾能力
            event.addCapability(EntityShieldCapability.ID, new EntityShieldCapabilityProvider());
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLevelUp(PlayerLevelUpEvent event) {
        Player player = event.getPlayer();
        int playerLevel = event.getPlayerLevel();
        
        // 根据玩家等级重新初始化玩家能力数据
        PlayerCapabilityAttacher.initializePlayerCapabilities(player, playerLevel);
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        // 只处理服务端
        if (!(event.getLevel() instanceof net.minecraft.server.level.ServerLevel)) return;
        
        // 获取实体
        net.minecraft.world.entity.LivingEntity entity = event.getEntity();
        
        // 只处理玩家
        if (entity instanceof Player player) {
            // 获取玩家等级
            int playerLevel = player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY)
                .map(PlayerLevelCapability::getPlayerLevel)
                .orElse(0);
                
            PlayerCapabilityAttacher.initializePlayerCapabilities(player, playerLevel);
            
            // 立即同步到客户端，确保客户端能获取到正确的数据
            PlayerCapabilityAttacher.syncPlayerCapabilitiesToClients(player);
        }
    }
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!event.getLevel().isClientSide()) {
                // 获取玩家等级
                int playerLevel = player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY)
                    .map(PlayerLevelCapability::getPlayerLevel)
                    .orElse(0);
                    
                // 服务端：初始化玩家能力
                PlayerCapabilityAttacher.initializePlayerCapabilities(player, playerLevel);
                
                // 立即同步到客户端，确保客户端能获取到正确的数据
                PlayerCapabilityAttacher.syncPlayerCapabilitiesToClients(player);
            } else {
                // 客户端：等待服务端同步数据
            }
        }
    }
    
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            Player player = event.getEntity();
            // 获取玩家等级
            int playerLevel = player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY)
                .map(PlayerLevelCapability::getPlayerLevel)
                .orElse(0);
                
            // 初始化玩家能力
            PlayerCapabilityAttacher.initializePlayerCapabilities(player, playerLevel);
            // 同步到客户端
            PlayerCapabilityAttacher.syncPlayerCapabilitiesToClients(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            Player player = event.getEntity();
                
            // 对于重生的玩家，我们需要从存档中恢复等级数据
            // 先确保能力被附加
            player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                // 尝试从存档文件加载数据
                // 重生事件中没有getPlayerFile方法，直接构建文件路径
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    java.io.File playerDataDir = serverPlayer.getServer().getWorldPath(net.minecraft.world.level.storage.LevelResource.PLAYER_DATA_DIR).toFile();
                    java.io.File customDir = new java.io.File(playerDataDir, "hamstercore");
                    java.io.File file = new java.io.File(customDir, serverPlayer.getStringUUID() + "_hamstercore_player_level.dat");
                    if (file.exists()) {
                        try {
                            CompoundTag data = net.minecraft.nbt.NbtIo.readCompressed(file);
                            cap.deserializeNBT(data);
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
                
            // 获取玩家等级
            int playerLevel = player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY)
                .map(PlayerLevelCapability::getPlayerLevel)
                .orElse(0);
                
            // 重新初始化玩家能力
            PlayerCapabilityAttacher.initializePlayerCapabilities(player, playerLevel);
            // 同步到客户端
            PlayerCapabilityAttacher.syncPlayerCapabilitiesToClients(player);
        }
    }
    
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            Player player = event.getEntity();
            
            // 维度切换时同步玩家等级和经验数据到客户端
            PlayerCapabilityAttacher.syncPlayerCapabilitiesToClients(player);
        }
    }
    
    // 添加玩家数据保存事件处理
    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        Player player = event.getEntity();
        player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            CompoundTag data = cap.serializeNBT();
            event.getPlayerFile("hamstercore_player_level.dat").getParentFile().mkdirs();
            try {
                net.minecraft.nbt.NbtIo.writeCompressed(data, event.getPlayerFile("hamstercore_player_level.dat"));
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        });
    }
    
    // 添加玩家数据加载事件处理
    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        Player player = event.getEntity();
        java.io.File file = event.getPlayerFile("hamstercore_player_level.dat");
        if (file.exists()) {
            try {
                CompoundTag data = net.minecraft.nbt.NbtIo.readCompressed(file);
                player.getCapability(PlayerLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                    cap.deserializeNBT(data);
                    
                    // 在加载数据后，重新初始化玩家能力
                    PlayerCapabilityAttacher.initializePlayerCapabilities(player, cap.getPlayerLevel());
                    // 同步到客户端
                    PlayerCapabilityAttacher.syncPlayerCapabilitiesToClients(player);
                });
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }
}