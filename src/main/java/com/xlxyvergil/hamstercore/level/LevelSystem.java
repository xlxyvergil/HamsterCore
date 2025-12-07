package com.xlxyvergil.hamstercore.level;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.LevelConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber(modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelSystem {
    
    private static LevelConfig levelConfig;
    private static Map<Level, Integer> cachedDaysMap = new HashMap<>();
    
    public static void init() {
        levelConfig = LevelConfig.load();
    }
    
    public static int calculateEntityLevel(LivingEntity entity) {
        if (levelConfig == null) {
            return 20; // 返回默认等级20
        }
        
        Level level = entity.level();
        if (level.isClientSide()) {
            return 20; // 客户端返回默认等级20
        }
        
        ServerLevel serverLevel = (ServerLevel) level;
        MinecraftServer server = serverLevel.getServer();
        
        // 计算基于天数的等级部分
        int days = 0;
        if (levelConfig.isUsePlayerDays()) {
            // 使用玩家生存天数
            days = getHighestPlayerDays(server);
        } else {
            // 使用服务器游戏天数
            days = (int) (serverLevel.getDayTime() / 24000L);
        }
        
        int dayBasedLevel = days * levelConfig.getBaseLevelPerDay();
        
        // 计算基于距离的等级部分
        Vec3 position = entity.position();
        // 使用世界坐标计算距离，而不是仅仅使用X和Z坐标
        double distance = Math.sqrt(position.x * position.x + position.z * position.z);
        
        int distanceBasedLevel = levelConfig.getDistanceBaseLevel();
        if (distance > levelConfig.getDistanceThreshold()) {
            double extraDistance = distance - levelConfig.getDistanceThreshold();
            // 改进计算逻辑：每100格增加指定等级
            int extraLevels = (int) (extraDistance / 100) * levelConfig.getDistanceLevelPer100Blocks();
            distanceBasedLevel += extraLevels;
        }
        
        // 确保等级至少为基础等级
        return Math.max(levelConfig.getDistanceBaseLevel(), dayBasedLevel + distanceBasedLevel);
    }
    
    private static int getHighestPlayerDays(MinecraftServer server) {
        int maxDays = 0;
        
        for (ServerLevel level : server.getAllLevels()) {
            // 检查缓存
            if (cachedDaysMap.containsKey(level) && level.getGameTime() % 1200 != 0) { // 每分钟更新一次缓存
                maxDays = Math.max(maxDays, cachedDaysMap.get(level));
                continue;
            }
            
            int levelMaxDays = 0;
            for (ServerPlayer player : level.players()) {
                // 使用PLAY_TIME统计来获取玩家游戏时间
                int playerDays = (int) (player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME)) / 24000L);
                levelMaxDays = Math.max(levelMaxDays, playerDays);
            }
            
            cachedDaysMap.put(level, levelMaxDays);
            maxDays = Math.max(maxDays, levelMaxDays);
        }
        
        return maxDays;
    }
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onFinalizeSpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getLevel().isClientSide()) return;
        
        LivingEntity entity = event.getEntity();
        // 为实体设置等级Capability
        entity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
            int level = calculateEntityLevel(entity);
            cap.setLevel(level);
        });
    }
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity && !event.getEntity().level().isClientSide() && !(livingEntity instanceof net.minecraft.world.entity.Mob)) {
            // 为非Mob实体设置等级Capability
            livingEntity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                int level = calculateEntityLevel(livingEntity);
                cap.setLevel(level);
            });
        }
    }
}