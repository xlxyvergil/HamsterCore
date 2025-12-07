package com.xlxyvergil.hamstercore.content.capability;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.entity.*;
import com.xlxyvergil.hamstercore.faction.Faction;
import com.xlxyvergil.hamstercore.level.LevelSystem;
import com.xlxyvergil.hamstercore.network.EntityFactionSyncToClient;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityCapabilityAttacher {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity livingEntity) {
            // 附加所有能力
            EntityFactionCapabilityProvider provider = new EntityFactionCapabilityProvider();
            provider.getCapability(EntityFactionCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                cap.setEntityType(livingEntity.getType());
            });
            event.addCapability(EntityFactionCapability.ID, provider);
            event.addCapability(EntityLevelCapability.ID, new EntityLevelCapabilityProvider());
            event.addCapability(EntityArmorCapability.ID, new EntityArmorCapabilityProvider());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        // 只处理服务端
        if (!(event.getLevel() instanceof ServerLevel)) return;
        
        // 获取实体
        Mob entity = event.getEntity();
        
        initializeEntityCapabilities(entity);
    }
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof LivingEntity livingEntity && !(livingEntity instanceof Mob)) {
            initializeEntityCapabilities(livingEntity);
        }
    }
    
    /**
     * 按照正确的顺序初始化实体的所有能力：
     * 1. 派系
     * 2. 等级
     * 3. 护甲（基于派系和等级）
     */
    private static void initializeEntityCapabilities(LivingEntity entity) {
        // 1. 初始化派系
        entity.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
                .ifPresent(factionCap -> {
                    // 确保派系被初始化
                    factionCap.getFaction();
                    // 同步到跟踪该实体的客户端
                    EntityFactionSyncToClient.sync(entity);
                });
        
        // 2. 初始化等级
        int level = LevelSystem.calculateEntityLevel(entity);
        entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY)
                .ifPresent(levelCap -> {
                    levelCap.setLevel(level);
                });
        
        // 记录调试信息
        LOGGER.debug("Entity: " + entity.getType().getDescriptionId() + " at (" + entity.getX() + ", " + entity.getZ() + ") calculated level: " + level);
        
        // 3. 初始化护甲（基于派系和等级）
        entity.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
                .ifPresent(armorCap -> {
                    // 获取实体的基础护甲值
                    double baseArmor = EntityArmorCapability.getBaseArmorForEntity(entity);
                    LOGGER.debug("Entity base armor: " + baseArmor);
                    // 计算并设置实际护甲值
                    armorCap.calculateAndSetArmor(baseArmor, level, 20); // 基础等级为20
                });
    }
}