package com.xlxyvergil.hamstercore.element.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.effect.DoTManager;
import com.xlxyvergil.hamstercore.element.effect.effects.GasEffect;

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
        private final float finalDamage;
        private final int amplifier;
        private final DamageSource damageSource;
        private final double baseRadius; // 基础半径（3米）
        private final double additionalRadius; // 额外半径（每层0.3米，最大3米）
        private final double totalRadius; // 总半径
        private int ticksRemaining; // 剩余时间（6秒 = 120 ticks）
        private int tickCounter; // 计数器
        
        public GasCloud(LivingEntity target, float finalDamage, int amplifier, DamageSource damageSource) {
            this.cloudId = UUID.randomUUID();
            this.sourceEntity = target;
            this.centerX = target.getX();
            this.centerY = target.getY();
            this.centerZ = target.getZ();
            this.finalDamage = finalDamage;
            this.amplifier = amplifier;
            this.damageSource = damageSource;
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
            
            // 施加瞬间和施加后5秒内（第0~5秒）赋予毒气效果
            if (tickCounter <= 100) { // 100 ticks = 5秒
                applyGasEffect();
            }
            
            // 生成粒子效果
            spawnParticles();
        }
        
        public boolean isExpired() {
            return ticksRemaining <= 0;
        }
        
        /**
         * 为范围内的所有实体赋予毒气状态效果
         */
        private void applyGasEffect() {
            Level level = sourceEntity.level();
            if (level == null) return;
            
            // 计算作用范围
            AABB boundingBox = new AABB(
                centerX - totalRadius,
                centerY - totalRadius,
                centerZ - totalRadius,
                centerX + totalRadius,
                centerY + totalRadius,
                centerZ + totalRadius
            );
            
            // 找到范围内的所有实体
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, boundingBox);
            
            // 为范围内的所有实体赋予毒气状态效果
            for (LivingEntity livingEntity : entities) {
                double distance = Math.sqrt(
                    Math.pow(livingEntity.getX() - centerX, 2) +
                    Math.pow(livingEntity.getY() - centerY, 2) +
                    Math.pow(livingEntity.getZ() - centerZ, 2)
                );
                
                // 检查是否在范围内
                if (distance <= totalRadius) {
                    // 给实体添加GasEffect状态效果，持续120 ticks（6秒）
                    // 这里直接使用GasEffect实例，确保与元素系统一致
                    // 等级为amplifier，持续时间为120 ticks
                    
                    // 计算DoT伤害：最终伤害的50% * 效果等级
                    float dotDamage = finalDamage * 0.5f * (amplifier + 1);
                    
                    // 创建一个新的伤害源，标记为毒云伤害，防止再次触发毒气效果
                    DamageSource gasDamageSource = damageSource.getEntity() != null ? 
                        DamageSource.indirectMagic(livingEntity, damageSource.getEntity()) : 
                        DamageSource.MAGIC;
                    
                    // 通过DoTManager添加DoT效果，使用GasEffect状态效果
                    DoTManager.addDoT(livingEntity, ElementType.GAS, dotDamage, 120, amplifier, gasDamageSource);
                }
            }
        }
        
        /**
         * 生成毒气粒子效果
         */
        private void spawnParticles() {
            // 降低粒子生成频率，每5个tick生成一次
            if (tickCounter % 5 != 0) {
                return;
            }
            
            Level level = sourceEntity.level();
            if (level instanceof ServerLevel serverLevel) {
                // 使用原版喷溅药水粒子效果
                serverLevel.sendParticles(
                    ParticleTypes.ENTITY_EFFECT,
                    centerX,
                    centerY,
                    centerZ,
                    1,
                    0.5, 0.5, 0.5,
                    0.01
                );
            }
        }
    }
    
    /**
     * 为实体创建毒气云
     * @param target 目标实体
     * @param finalDamage 最终伤害值
     * @param amplifier 效果等级
     * @param damageSource 伤害源
     */
    public static void addGasCloud(LivingEntity target, float finalDamage, int amplifier, DamageSource damageSource) {
        GasCloud cloud = new GasCloud(target, finalDamage, amplifier, damageSource);
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