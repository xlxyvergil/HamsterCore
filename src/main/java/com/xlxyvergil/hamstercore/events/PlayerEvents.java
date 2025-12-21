package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.PlayerCapabilityAttacher;
import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapability;
import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.*;
import com.xlxyvergil.hamstercore.level.PlayerLevelUpEvent;
import com.xlxyvergil.hamstercore.network.EntityArmorSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityFactionSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityHealthModifierSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityLevelSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityShieldSyncToClient;
import com.xlxyvergil.hamstercore.network.PacketHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = HamsterCore.MODID)
public class PlayerEvents {
    
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof Player player) {
            // 附加玩家等级能力（我们的玩家等级系统）
            event.addCapability(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("hamstercore", "player_level"),
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
        if (!(event.getLevel() instanceof ServerLevel)) return;
        
        // 获取实体
        LivingEntity entity = event.getEntity();
        
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
    
    // 当玩家开始跟踪实体时，同步实体数据到该玩家
    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getTarget() instanceof LivingEntity entity && event.getEntity() instanceof ServerPlayer player) {
            // 同步等级
            entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(levelCap -> {
                PacketHandler.NETWORK.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EntityLevelSyncToClient(entity.getId(), levelCap.getLevel())
                );
            });
            
            // 同步派系
            entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY).ifPresent(factionCap -> {
                PacketHandler.NETWORK.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EntityFactionSyncToClient(entity.getId(), factionCap.getFaction())
                );
            });
            
            // 同步护甲
            entity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(armorCap -> {
                PacketHandler.NETWORK.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EntityArmorSyncToClient(entity.getId(), armorCap.getArmor())
                );
            });
            
            // 同步生命值修饰符
            entity.getCapability(EntityHealthModifierCapabilityProvider.CAPABILITY).ifPresent(healthCap -> {
                PacketHandler.NETWORK.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new EntityHealthModifierSyncToClient(entity.getId(), healthCap.getHealthModifier(), healthCap.isInitialized())
                );
            });
            
            // 同步护盾（仅当实体拥有护盾能力时）
            entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(shieldCap -> {
                // 检查护盾值是否有效
                if (shieldCap.getMaxShield() >= 0) {
                    PacketHandler.NETWORK.send(
                        PacketDistributor.PLAYER.with(() -> (ServerPlayer) event.getEntity()),
                        new EntityShieldSyncToClient(entity.getId(), shieldCap.getCurrentShield(), shieldCap.getMaxShield())
                    );
                }
            });
        }
    }
    
    /**
     * 监听玩家登录事件
     * 玩家首次加入世界时需要初始化能力值
     */
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

    /**
     * 监听玩家重生事件
     * 玩家死亡后重生时需要重新同步能力值
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            Player player = event.getEntity();
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

    /**
     * 监听玩家维度传送事件
     * 玩家在不同维度间传送时需要重新同步数据
     */
    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            Player player = event.getEntity();
            // 重新同步玩家能力
            PlayerCapabilityAttacher.syncPlayerCapabilitiesToClients(player);
        }
    }
}