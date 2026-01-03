package com.xlxyvergil.hamstercore.element.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.xlxyvergil.hamstercore.element.effect.ElementEffectRegistry;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;


/**
 * 毒气效果管理器
 * 管理毒气云AoE效果，支持叠加和范围伤害
 */
public class GasManager {
    
    // 存储所有的毒气云实例 - 使用线程安全的集合避免并发修改异常
    private static final Map<UUID, GasCloud> gasClouds = new ConcurrentHashMap<>();
    
    /**
     * 毒气云类
     * 表示一个毒气云实例
     */
    public static class GasCloud {
        private final UUID cloudId;
        private final LivingEntity sourceEntity; // 原始目标实体（可能已死亡）
        private final double centerX, centerY, centerZ;
        private final int amplifier;
        private final DamageSource damageSource;
        private final float baseDamage; // 基础伤害值
        private final double baseRadius; // 基础半径（3米）
        private final double additionalRadius; // 额外半径（每层0.3米，最大3米）
        private final double totalRadius; // 总半径
        private int ticksRemaining; // 剩余时间（6秒 = 120 ticks）
        private int tickCounter; // 计数器
        
        public GasCloud(LivingEntity target, int amplifier, DamageSource damageSource, float baseDamage) {
            this.cloudId = UUID.randomUUID();
            this.sourceEntity = target;
            this.centerX = target.getX();
            this.centerY = target.getY();
            this.centerZ = target.getZ();
            this.amplifier = amplifier;
            this.damageSource = damageSource;
            this.baseDamage = baseDamage;
            this.baseRadius = 3.0; // 基础3米
            // 计算额外半径：每层0.3米，最大3米
            this.additionalRadius = Math.min(amplifier * 0.3, 3.0);
            this.totalRadius = baseRadius + additionalRadius; // 最大6米
            this.ticksRemaining = 120; // 6秒
            this.tickCounter = 0;
        }
        
        public UUID getCloudId() {
            return cloudId;
        }
        
        public int getTicksRemaining() {
            return ticksRemaining;
        }
        
        public void tick() {
            ticksRemaining--;
            tickCounter++;
            
            // 每tick都赋予毒气效果，持续6秒
            applyGasEffect();
        }
        
        public boolean isExpired() {
            return ticksRemaining <= 0;
        }
        
        /**
         * 为范围内的所有实体赋予毒气状态效果
         */
        private void applyGasEffect() {
            Level level = sourceEntity.level();
            if (level == null || !(level instanceof ServerLevel)) return;
            
            ServerLevel serverLevel = (ServerLevel) level;
            
            // 计算作用范围
            AABB boundingBox = new AABB(
                centerX - totalRadius,
                centerY - totalRadius,
                centerZ - totalRadius,
                centerX + totalRadius,
                centerY + totalRadius,
                centerZ + totalRadius
            );
            
            // 找到范围内的所有实体，创建副本避免并发修改异常
            List<LivingEntity> entities = new ArrayList<>(serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox));
            
            // 为范围内的所有实体赋予毒气状态效果（排除玩家）
            for (LivingEntity livingEntity : entities) {
                // 排除玩家实体
                if (livingEntity instanceof Player) {
                    continue;
                }
                
                double distance = Math.sqrt(
                    Math.pow(livingEntity.getX() - centerX, 2) +
                    Math.pow(livingEntity.getY() - centerY, 2) +
                    Math.pow(livingEntity.getZ() - centerZ, 2)
                );
                
                // 检查是否在范围内
                if (distance <= totalRadius) {
                    // 计算毒气DoT伤害：基础伤害 * 10% * (1 + 等级/10)
                    float gasDamage = baseDamage * 0.10F * (1.0F + amplifier * 0.1F);

                    // 给实体添加GasEffect状态效果，持续120 ticks（6秒）
                    // 等级为amplifier
                    MobEffectInstance effectInstance = new MobEffectInstance(
                            (ElementEffect) ElementEffectRegistry.Effects.GAS.get(), 120, amplifier);
                    livingEntity.addEffect(effectInstance);
                    // 存储伤害数据
                    ElementEffectDataHelper.setEffectDamage(livingEntity, (ElementEffect) ElementEffectRegistry.Effects.GAS.get(), gasDamage);
                }
            }
        }
        

    }
    
    /**
     * 为实体创建毒气云
     * @param target 目标实体
     * @param amplifier 效果等级
     * @param damageSource 伤害源
     * @param baseDamage 基础伤害值
     */
    public static void addGasCloud(LivingEntity target, int amplifier, DamageSource damageSource, float baseDamage) {
        GasCloud cloud = new GasCloud(target, amplifier, damageSource, baseDamage);
        gasClouds.put(cloud.getCloudId(), cloud);
        
    }
    
    /**
     * 更新所有毒气云
     */
    public static void updateAllGasClouds() {
        // 创建一个副本以避免并发修改异常
        List<UUID> expiredClouds = new ArrayList<>();
        
        // 遍历所有毒气云 - ConcurrentHashMap确保安全迭代
        for (Map.Entry<UUID, GasCloud> entry : gasClouds.entrySet()) {
            GasCloud cloud = entry.getValue();
            cloud.tick();
            
            // 记录过期的毒气云
            if (cloud.isExpired()) {
                expiredClouds.add(entry.getKey());
            }
        }
        
        // 移除所有过期的毒气云 - ConcurrentHashMap确保安全删除
        for (UUID cloudId : expiredClouds) {
            gasClouds.remove(cloudId);
        }
    }
    
    /**
     * 清理所有毒气云
     */
    public static void clearAllGasClouds() {
        gasClouds.clear();
    }
    
    /**
     * 获取活跃的毒气云数量
     */
    public static int getActiveGasCloudCount() {
        return gasClouds.size();
    }
    
    /**
     * 获取指定实体相关的毒气云
     */
    public static List<GasCloud> getGasCloudsForEntity(LivingEntity entity) {
        List<GasCloud> clouds = new ArrayList<>();
        // ConcurrentHashMap确保安全迭代
        for (GasCloud cloud : gasClouds.values()) {
            if (cloud.sourceEntity.equals(entity)) {
                clouds.add(cloud);
            }
        }
        return clouds;
    }
}