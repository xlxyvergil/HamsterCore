package com.xlxyvergil.hamstercore.content.capability;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.entity.*;
import com.xlxyvergil.hamstercore.level.HealthModifierSystem;
import com.xlxyvergil.hamstercore.network.EntityArmorSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityFactionSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityHealthModifierSyncToClient;
import com.xlxyvergil.hamstercore.network.EntityLevelSyncToClient;
import com.xlxyvergil.hamstercore.network.PacketHandler;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = HamsterCore.MODID)
public class EntityCapabilityAttacher {
    
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity livingEntity) {
            // 附加实体等级能力
            event.addCapability(EntityLevelCapability.ID, new EntityLevelCapabilityProvider());
            
            // 附加实体护甲能力
            EntityArmorCapabilityProvider armorProvider = new EntityArmorCapabilityProvider();
            armorProvider.setEntityType(livingEntity.getType());
            event.addCapability(EntityArmorCapability.ID, armorProvider);
            
            // 附加实体派系能力
            EntityFactionCapabilityProvider factionProvider = new EntityFactionCapabilityProvider();
            factionProvider.setEntityType(livingEntity.getType());
            event.addCapability(EntityFactionCapability.ID, factionProvider);
            
            // 附加实体生命值修饰符能力
            event.addCapability(EntityHealthModifierCapability.ID, new EntityHealthModifierCapabilityProvider());
        }
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        // 只处理服务端
        if (!(event.getLevel() instanceof ServerLevel)) return;
        
        // 获取实体
        LivingEntity entity = event.getEntity();
        
        initializeEntityCapabilities(entity);
        
        // 立即同步到客户端，确保客户端能获取到正确的数据
        syncEntityCapabilitiesToClients(entity);
    }
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            if (!event.getLevel().isClientSide()) {
                // 服务端：初始化实体能力
                initializeEntityCapabilities(livingEntity);
                
                // 立即同步到客户端，确保客户端能获取到正确的数据
                syncEntityCapabilitiesToClients(livingEntity);
            } else {
                // 客户端：如果实体是玩家，不需要做任何事
                // 如果是非玩家实体，等待服务端同步数据
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
        }
    }
    
    /**
     * 按照正确的顺序初始化实体的所有能力：
     * 1. 派系
     * 2. 等级
     * 3. 护甲（基于派系和等级）
     * 4. 生命值修饰符（基于等级）
     */
    private static void initializeEntityCapabilities(LivingEntity entity) {
        // 1. 初始化派系
        entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
                .ifPresent(factionCap -> {
                    // 确保派系被初始化
                    factionCap.getFaction();
                });
        
        // 2. 初始化等级
        entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY)
                .ifPresent(levelCap -> {
                    // 只有当等级尚未初始化时才进行初始化
                    if (!levelCap.isInitialized()) {
                        levelCap.initializeLevel(entity);
                    }
                });
        
        // 3. 初始化护甲（基于派系和等级）
        entity.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
                .ifPresent(armorCap -> {
                    // 获取实体的等级
                    int level = entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY)
                        .map(levelCap -> levelCap.getLevel())
                        .orElse(20); // 默认等级20
                    
                    // 初始化实体能力，传入基础等级20和当前等级
                    armorCap.initializeEntityCapabilities(20, level);
                });
        
        // 4. 初始化生命值修饰符（基于等级）
        HealthModifierSystem.applyHealthModifier(entity);
        
        // 在所有能力初始化完成后，同步到所有正在跟踪该实体的玩家
        syncEntityCapabilitiesToClients(entity);
    }
    
    /**
     * 同步实体的所有能力到跟踪该实体的所有客户端
     */
    private static void syncEntityCapabilitiesToClients(LivingEntity entity) {
        // 同步等级到客户端
        EntityLevelSyncToClient.sync(entity);
        
        // 同步派系到客户端
        EntityFactionSyncToClient.sync(entity);
        
        // 同步护甲到客户端
        EntityArmorSyncToClient.sync(entity);
        
        // 同步生命值修饰符到客户端
        EntityHealthModifierSyncToClient.sync(entity);
    }
}