package com.xlxyvergil.hamstercore.level;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.LevelConfig;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityLevelCapabilityProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class LevelSystem {
    
    /**
     * 计算基于距离的等级部分
     * @param entity 实体
     * @return 基于距离计算的等级增加值
     */
    private static int calculateDistanceBasedLevel(LivingEntity entity) {
        if (levelConfig == null) return 0;
        
        // 获取实体与出生点的距离
        double distance = getDistanceToSpawn(entity);
        
        // 1000格内的基础等级为配置值
        int distanceBasedLevel = levelConfig.getDistanceBaseLevel();
        
        if (distance > levelConfig.getDistanceThreshold()) {
            double extraDistance = distance - levelConfig.getDistanceThreshold();
            // 每100格增加指定等级
            int extraLevels = (int) (extraDistance / 100) * levelConfig.getDistanceLevelPer100Blocks();
            distanceBasedLevel += extraLevels;
        }
        
        return distanceBasedLevel;
    }
    
    /**
     * 获取实体与出生点的距离
     * @param entity 实体
     * @return 实体与出生点的水平距离
     */
    private static double getDistanceToSpawn(LivingEntity entity) {
        // 获取世界的出生点位置
        BlockPos spawnPoint = entity.level().getSharedSpawnPos();
        // 如果出生点坐标为0，则尝试从世界数据中获取
        if (spawnPoint.getX() == 0 && spawnPoint.getZ() == 0) {
            spawnPoint = new BlockPos(entity.level().getLevelData().getXSpawn(), 
                                      entity.level().getLevelData().getYSpawn(), 
                                      entity.level().getLevelData().getZSpawn());
        }
        BlockPos entityPos = entity.blockPosition();
        
        // 输出调试日志
        
        // 计算实体与出生点的水平距离（忽略Y轴）
        double distance = Math.sqrt(entityPos.distSqr(spawnPoint));
        
        return distance;
    }
    
    /**
     * 获取服务器游戏天数
     * @param server 服务器对象
     * @return 天数
     */
    private static int getDays(MinecraftServer server) {
        if (levelConfig == null) return 0;
        
        // 使用主世界游戏天数
        ServerLevel overworld = server.getLevel(Level.OVERWORLD);
        return overworld != null ? (int) (overworld.getDayTime() / 24000L) : 0;
    }
    
    private static LevelConfig levelConfig;
    // 移除不需要的缓存变量
    
    public static void init() {
        levelConfig = LevelConfig.load();
        // 初始化生命值修饰符系统
        HealthModifierSystem.init(levelConfig);
    }
    
    public static int calculateEntityLevel(LivingEntity entity) {
        if (levelConfig == null) {
            return 20; // 返回默认等级20
        }
        
        // 玩家固定等级为30
        if (entity instanceof Player) {
            return 30;
        }
        
        Level level = entity.level();
        if (level.isClientSide()) {
            return 20; // 客户端返回默认等级20
        }
        
        ServerLevel serverLevel = (ServerLevel) level;
        MinecraftServer server = serverLevel.getServer();
        
        // 计算基于天数的等级部分
        int days = getDays(server);
        int dayBasedLevel = days * levelConfig.getBaseLevelPerDay();
        
        // 计算基于距离的等级部分
        int distanceBasedLevel = calculateDistanceBasedLevel(entity);
        
        // 总等级 = 基础等级 + 天数等级 + 距离等级
        int totalLevel = levelConfig.getDistanceBaseLevel() + dayBasedLevel + distanceBasedLevel;
        
        return totalLevel;
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
        
        // 为非玩家实体应用生命值修饰符
        if (!(entity instanceof net.minecraft.world.entity.player.Player)) {
            HealthModifierSystem.applyHealthModifier(entity);
        }
    }
    
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity livingEntity && 
            !event.getEntity().level().isClientSide() && 
            !(livingEntity instanceof net.minecraft.world.entity.player.Player)) {
            
            // 获取实体等级并设置到Capability
            livingEntity.getCapability(EntityLevelCapabilityProvider.CAPABILITY).ifPresent(cap -> {
                int level = calculateEntityLevel(livingEntity);
                cap.setLevel(level);
            });
            
            // 为非玩家实体应用生命值修饰符（包括Mob）
            HealthModifierSystem.applyHealthModifier(livingEntity);
        }
    }
}