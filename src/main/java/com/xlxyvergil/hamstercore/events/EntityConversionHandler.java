package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.content.capability.EntityCapabilityAttacher;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityHealthModifierCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import com.xlxyvergil.hamstercore.network.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.living.LivingConversionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "hamstercore")
public class EntityConversionHandler {

    @SubscribeEvent
    public static void onLivingConversion(LivingConversionEvent.Post event) {
        LivingEntity originalEntity = event.getEntity();
        LivingEntity convertedEntity = event.getOutcome();
        
        // 复制能力值到新实体
        copyCapabilities(originalEntity, convertedEntity);
    }
    
    @SubscribeEvent
    public static void onMobSpawn(MobSpawnEvent.FinalizeSpawn event) {
        // 检查是否是Mob实体并且不是从刷怪笼或传送门等来源生成的自然繁殖实体
        Mob mob = event.getEntity();
        if ((event.getSpawnType() == MobSpawnType.NATURAL || event.getSpawnType() == MobSpawnType.CHUNK_GENERATION)) {
            
            // 查找附近相同类型且已存在的实体作为父实体
            Entity parentEntity = mob.level().getEntitiesOfClass(mob.getClass(), 
                mob.getBoundingBox().inflate(16.0D)).stream()
                .filter(e -> e != mob && e instanceof LivingEntity)
                .findFirst()
                .orElse(null);
                
            if (parentEntity instanceof LivingEntity parentLiving) {
                // 复制能力值到新实体
                copyCapabilities(parentLiving, mob);
            }
        }
    }
    
    /**
     * 将原始实体的能力值复制到目标实体
     */
    private static void copyCapabilities(LivingEntity originalEntity, LivingEntity targetEntity) {
        // 复制等级
        originalEntity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(originalLevelCap -> {
            targetEntity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(targetLevelCap -> {
                targetLevelCap.setLevel(originalLevelCap.getLevel());
                // 同步到客户端
                EntityLevelSyncToClient.sync(targetEntity);
            });
        });
        
        // 复制派系
        originalEntity.getCapability(EntityFactionCapabilityProvider.CAPABILITY).ifPresent(originalFactionCap -> {
            targetEntity.getCapability(EntityFactionCapabilityProvider.CAPABILITY).ifPresent(targetFactionCap -> {
                targetFactionCap.setFaction(originalFactionCap.getFaction());
                // 同步到客户端
                EntityFactionSyncToClient.sync(targetEntity);
            });
        });
        
        // 复制护甲
        originalEntity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(originalArmorCap -> {
            targetEntity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(targetArmorCap -> {
                // 复制实际护甲值（Capability中现在只存储最终计算后的护甲值）
                targetArmorCap.setArmor(originalArmorCap.getArmor());
                // 同步到客户端
                EntityArmorSyncToClient.sync(targetEntity);
            });
        });
        
        // 复制护盾
        originalEntity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(originalShieldCap -> {
            targetEntity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(targetShieldCap -> {
                // 复制护盾相关值
                targetShieldCap.setMaxShield(originalShieldCap.getMaxShield());
                targetShieldCap.setCurrentShield(originalShieldCap.getCurrentShield());
                targetShieldCap.setRegenRate(originalShieldCap.getRegenRate());
                targetShieldCap.setRegenDelay(originalShieldCap.getRegenDelay());
                targetShieldCap.setRegenDelayDepleted(originalShieldCap.getRegenDelayDepleted());
                targetShieldCap.setImmunityTime(originalShieldCap.getImmunityTime());
                targetShieldCap.setInsuranceAvailable(originalShieldCap.isInsuranceAvailable());
                // 同步到客户端
                EntityShieldSyncToClient.sync(targetEntity);
            });
        });
        
        // 复制生命值修饰符
        originalEntity.getCapability(EntityHealthModifierCapabilityProvider.CAPABILITY).ifPresent(originalHealthCap -> {
            targetEntity.getCapability(EntityHealthModifierCapabilityProvider.CAPABILITY).ifPresent(targetHealthCap -> {
                targetHealthCap.setHealthModifier(originalHealthCap.getHealthModifier());
                // 生命值修饰符网络同步
                EntityHealthModifierSyncToClient.sync(targetEntity);
            });
        });
        
        // 使用新的Attribute修饰符系统重新初始化目标实体的能力
        EntityCapabilityAttacher.initializeEntityCapabilities(targetEntity);
        
        // 重新同步所有能力到客户端
        EntityArmorSyncToClient.sync(targetEntity);
        EntityShieldSyncToClient.sync(targetEntity);
        EntityLevelSyncToClient.sync(targetEntity);
        EntityFactionSyncToClient.sync(targetEntity);
        EntityHealthModifierSyncToClient.sync(targetEntity);
    }
}