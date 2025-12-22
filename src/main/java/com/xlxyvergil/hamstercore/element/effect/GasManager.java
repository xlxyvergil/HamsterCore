package com.xlxyvergil.hamstercore.element.effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 毒气效果管理器
 * 管理毒气云AoE效果，支持叠加和范围伤害
 */
public class GasManager {
    
    // 存储所有的毒气云实例
    private static final Map<UUID, GasCloud> gasClouds = new HashMap<>();
    
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
            
            // 施加瞬间和施加后5秒内（第0~5秒）造成伤害
            if (tickCounter <= 100) { // 100 ticks = 5秒
                applyDamage();
            }
            
            // 生成粒子效果
            spawnParticles();
        }
        
        public boolean isExpired() {
            return ticksRemaining <= 0;
        }
        
        /**
         * 应用伤害到范围内的所有实体
         */
        private void applyDamage() {
            Level level = sourceEntity.level();
            if (level == null) return;
            
            // 计算伤害范围
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
            
            // 对范围内的所有实体造成伤害
            for (LivingEntity livingEntity : entities) {
                double distance = Math.sqrt(
                    Math.pow(livingEntity.getX() - centerX, 2) +
                    Math.pow(livingEntity.getY() - centerY, 2) +
                    Math.pow(livingEntity.getZ() - centerZ, 2)
                );
                
                // 检查是否在范围内
                if (distance <= totalRadius) {
                    // 计算伤害：最终伤害的50% * 效果等级
                    float gasDamage = finalDamage * 0.5f * (amplifier + 1);
                    livingEntity.hurt(damageSource, gasDamage);
                }
            }
        }
        
        /**
         * 生成毒气粒子效果
         */
        private void spawnParticles() {
            Level level = sourceEntity.level();
            if (level instanceof ServerLevel serverLevel) {
                // 在毒气云范围内随机生成毒气粒子
                for (int i = 0; i < 5; i++) {
                    double offsetX = (Math.random() - 0.5) * totalRadius * 2;
                    double offsetY = (Math.random() - 0.5) * totalRadius * 0.5;
                    double offsetZ = (Math.random() - 0.5) * totalRadius * 2;
                    
                    serverLevel.sendParticles(
                        ParticleTypes.EFFECT,
                        centerX + offsetX,
                        centerY + offsetY,
                        centerZ + offsetZ,
                        1,
                        0, 0, 0,
                        0.0f
                    );
                }
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
        
        // 播放毒气生成音效
        Level level = target.level();
        if (level instanceof ServerLevel) {
            level.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.WITCH_DRINK, SoundSource.HOSTILE, 1.0F, 1.0F);
        }
    }
    
    /**
     * 更新所有毒气云
     */
    public static void updateAllGasClouds() {
        Iterator<Map.Entry<UUID, GasCloud>> iterator = gasClouds.entrySet().iterator();
        
        while (iterator.hasNext()) {
            Map.Entry<UUID, GasCloud> entry = iterator.next();
            GasCloud cloud = entry.getValue();
            
            cloud.tick();
            
            // 如果云已过期，移除它
            if (cloud.isExpired()) {
                iterator.remove();
            }
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
        for (GasCloud cloud : gasClouds.values()) {
            if (cloud.sourceEntity.equals(entity)) {
                clouds.add(cloud);
            }
        }
        return clouds;
    }
}