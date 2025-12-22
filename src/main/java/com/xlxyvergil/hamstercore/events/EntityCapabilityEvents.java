package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.EntityCapabilityAttacher;
import com.xlxyvergil.hamstercore.content.capability.entity.*;
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
public class EntityCapabilityEvents {
    
    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity livingEntity && !(livingEntity instanceof Player)) {
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
            
            // 检查实体是否应该拥有护盾能力
            if (shouldHaveShieldCapability(livingEntity)) {
                // 附加实体护盾能力
                event.addCapability(EntityShieldCapability.ID, new EntityShieldCapabilityProvider());
            }
        }
    }
    
    /**
     * 检查实体是否应该拥有护盾能力
     * 注意：这个方法在AttachCapabilitiesEvent阶段调用，此时派系能力可能还未完全初始化
     * 因此这里只检查明确配置的实体，派系相关的检查延迟到initializeShieldCapability方法中
     * @param entity 实体
     * @return 如果实体应该拥有护盾能力则返回true，否则返回false
     */
    private static boolean shouldHaveShieldCapability(LivingEntity entity) {
        // 加载护盾配置
        com.xlxyvergil.hamstercore.config.ShieldConfig shieldConfig = com.xlxyvergil.hamstercore.config.ShieldConfig.load();
        
        // 检查实体是否在entityBaseShields中明确配置了护盾值
        float baseShield = shieldConfig.getBaseShieldForEntity(entity.getType());
        if (baseShield >= 0) { // 找到了明确配置
            return baseShield > 0; // 护盾值大于0才赋予护盾能力
        }
        
        // 如果实体没有明确配置，则默认附加护盾能力，后续在initializeShieldCapability中再判断是否启用
        return true;
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        // 只处理服务端
        if (!(event.getLevel() instanceof ServerLevel)) return;
        
        // 获取实体
        LivingEntity entity = event.getEntity();
        
        // 排除玩家实体
        if (entity instanceof Player) {
            return;
        }
        
        // 使用新的Attribute修饰符系统初始化实体能力
        EntityCapabilityAttacher.initializeEntityCapabilities(entity);
        
        // 立即同步到客户端，确保客户端能获取到正确的数据
        EntityCapabilityAttacher.syncEntityCapabilitiesToClients(entity);
    }
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity) {
            // 排除玩家实体，玩家应该由PlayerCapabilityEvents处理
            if (livingEntity instanceof Player) {
                return;
            }
            
            if (!event.getLevel().isClientSide()) {
                // 非玩家实体处理
                EntityCapabilityAttacher.initializeEntityCapabilities(livingEntity);
                // 立即同步到客户端，确保客户端能获取到正确的数据
                EntityCapabilityAttacher.syncEntityCapabilitiesToClients(livingEntity);
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
                        PacketDistributor.PLAYER.with(() -> player),
                        new EntityShieldSyncToClient(entity.getId(), shieldCap.getCurrentShield(), shieldCap.getMaxShield())
                    );
                }
            });
        }
    }
}