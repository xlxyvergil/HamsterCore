package com.xlxyvergil.hamstercore.content.capability.entity;

import com.xlxyvergil.hamstercore.content.capability.EntityEffectSyncHandler;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.xlxyvergil.hamstercore.HamsterCore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体状态效果管理器 - 用于缓存实体的状态效果，避免每次渲染都从实体获取
 * 使用弱引用和定期清理来避免内存泄漏
 */
@Mod.EventBusSubscriber(modid = HamsterCore.MODID)
public class EntityEffectManager {
    
    // 存储实体ID到状态效果列表的映射
    private static final Map<Integer, List<MobEffectInstance>> entityEffectsCache = new ConcurrentHashMap<>();
    
    /**
     * 获取实体的状态效果缓存
     */
    public static List<MobEffectInstance> getEntityEffects(int entityId) {
        return entityEffectsCache.getOrDefault(entityId, Collections.emptyList());
    }
    
    /**
     * 更新实体的状态效果缓存（服务端）
     */
    public static void updateEntityEffects(LivingEntity entity) {
        List<MobEffectInstance> effects = new ArrayList<>(entity.getActiveEffects());
        entityEffectsCache.put(entity.getId(), effects);
    }
    
    /**
     * 更新实体的状态效果缓存（客户端，从网络包接收）
     */
    public static void updateEntityEffectsClientSide(int entityId, List<MobEffectInstance> effects) {
        entityEffectsCache.put(entityId, new ArrayList<>(effects));
    }
    
    /**
     * 移除实体的状态效果缓存
     */
    public static void removeEntityEffects(int entityId) {
        entityEffectsCache.remove(entityId);
    }
    
    /**
     * 清理已不存在的实体的缓存
     */
    public static void cleanupCache(Set<Integer> validEntityIds) {
        entityEffectsCache.entrySet().removeIf(entry -> !validEntityIds.contains(entry.getKey()));
    }
    
    @SubscribeEvent
    public static void onMobEffectAdded(MobEffectEvent.Added event) {
        if (!event.getEntity().level().isClientSide()) {
            // 更新缓存并同步到客户端
            updateEntityEffects(event.getEntity());
            EntityEffectSyncHandler.syncEntityEffects(event.getEntity());
        }
    }
    
    @SubscribeEvent
    public static void onMobEffectRemoved(MobEffectEvent.Remove event) {
        if (!event.getEntity().level().isClientSide()) {
            // 更新缓存并同步到客户端
            updateEntityEffects(event.getEntity());
            EntityEffectSyncHandler.syncEntityEffects(event.getEntity());
        }
    }
    
    @SubscribeEvent
    public static void onMobEffectExpired(MobEffectEvent.Expired event) {
        if (!event.getEntity().level().isClientSide()) {
            // 更新缓存并同步到客户端
            updateEntityEffects(event.getEntity());
            EntityEffectSyncHandler.syncEntityEffects(event.getEntity());
        }
    }
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity && !event.getLevel().isClientSide()) {
            // 实体进入世界时，同步其状态效果
            updateEntityEffects(livingEntity);
            EntityEffectSyncHandler.syncEntityEffects(livingEntity);
        }
    }
    
    @SubscribeEvent
    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity && !event.getLevel().isClientSide()) {
            // 实体离开世界时，清理缓存
            removeEntityEffects(event.getEntity().getId());
        }
    }
}