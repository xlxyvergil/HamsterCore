package com.xlxyvergil.hamstercore.element.effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.xlxyvergil.hamstercore.element.effect.effects.BlastEffect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * 爆炸效果管理器
 * 管理延迟爆炸伤害效果，支持叠加和范围伤害
 */
public class BlastManager {
    
    // 存储实体身上的爆炸效果
    private static final Map<LivingEntity, List<BlastEntry>> entityBlasts = new HashMap<>();
    
    /**
     * 爆炸效果条目类
     * 表示一个爆炸效果条目
     */
    public static class BlastEntry {
        private final float damage;
        private final int amplifier;
        private final DamageSource damageSource;
        private int delayTicks; // 延迟tick数（1.5秒 = 30 ticks）
        private final int maxDuration; // 最大持续时间（6秒 = 120 ticks）
        private int ticksLived; // 已存在的ticks
        
        public BlastEntry(float damage, int amplifier, DamageSource damageSource) {
            this.damage = damage;
            this.amplifier = amplifier;
            this.damageSource = damageSource;
            this.delayTicks = 30; // 1.5秒延迟
            this.maxDuration = 120; // 6秒持续时间
            this.ticksLived = 0;
        }
        
        public float getDamage() {
            return damage;
        }
        
        public int getAmplifier() {
            return amplifier;
        }
        
        public DamageSource getDamageSource() {
            return damageSource;
        }
        
        public boolean shouldExplode() {
            return delayTicks <= 0;
        }
        
        public boolean isExpired() {
            return ticksLived >= maxDuration;
        }
        
        public void tick() {
            if (delayTicks > 0) {
                delayTicks--;
            }
            ticksLived++;
        }
        
        public int getTicksLived() {
            return ticksLived;
        }
    }
    
    /**
     * 为实体添加爆炸效果
     * @param entity 实体
     * @param damage 爆炸伤害
     * @param amplifier 效果等级
     * @param damageSource 伤害源
     */
    public static void addBlast(LivingEntity entity, float damage, int amplifier, DamageSource damageSource) {
        BlastEntry entry = new BlastEntry(damage, amplifier, damageSource);
        entityBlasts.computeIfAbsent(entity, k -> new ArrayList<>()).add(entry);
    }
    
    /**
     * 更新实体身上的所有爆炸效果
     * @param entity 实体
     */
    public static void updateBlasts(LivingEntity entity) {
        List<BlastEntry> blasts = entityBlasts.get(entity);
        if (blasts != null) {
            Iterator<BlastEntry> iterator = blasts.iterator();
            List<BlastEntry> readyToExplode = new ArrayList<>();
            int totalAmplifier = 0;
            
            // 检查是否达到最大叠加层数（10级）
            boolean maxStackReached = blasts.size() >= BlastEffect.MAX_LEVEL;
            
            while (iterator.hasNext()) {
                BlastEntry entry = iterator.next();
                entry.tick();
                totalAmplifier += entry.getAmplifier();
                
                // 检查是否应该爆炸
                if (entry.shouldExplode()) {
                    readyToExplode.add(entry);
                    iterator.remove();
                } else if (entry.isExpired()) {
                    iterator.remove();
                }
            }
            
            // 如果达到最大叠加层数或在1.5秒内累积了多层数，立即结算所有伤害
            if (maxStackReached || (readyToExplode.size() > 1 && totalAmplifier >= BlastEffect.MAX_LEVEL)) {
                // 立即结算所有待爆炸的效果
                for (BlastEntry entry : blasts) {
                    readyToExplode.add(entry);
                }
                blasts.clear();
                
                // 使用5米范围进行结算
                for (BlastEntry entry : readyToExplode) {
                    explode(entity, entry.getDamage(), entry.getAmplifier(), entry.getDamageSource(), 5.0);
                }
            } else {
                // 正常爆炸
                for (BlastEntry entry : readyToExplode) {
                    explode(entity, entry.getDamage(), entry.getAmplifier(), entry.getDamageSource(), 2.0);
                }
            }
            
            // 如果该实体没有任何爆炸效果了，清理map
            if (blasts.isEmpty()) {
                entityBlasts.remove(entity);
            }
        }
    }
    
    /**
     * 执行爆炸
     * @param center 爆炸中心实体
     * @param damage 爆炸伤害
     * @param amplifier 等级
     * @param damageSource 伤害源
     * @param range 爆炸范围（米）
     */
    private static void explode(LivingEntity center, float damage, int amplifier, DamageSource damageSource, double range) {
        Level level = center.level();
        AABB boundingBox = center.getBoundingBox().inflate(range);
        
        // 找到范围内的所有实体
        List<Entity> entities = level.getEntities(center, boundingBox, 
            entity -> entity instanceof LivingEntity && entity != center);
        
        // 对范围内的所有实体造成伤害
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.hurt(damageSource, damage);
            }
        }
        
        // 中心实体也受到伤害
        center.hurt(damageSource, damage);
        
        // 播放爆炸效果
        if (level instanceof ServerLevel serverLevel) {
            // 添加爆炸粒子效果
            serverLevel.sendParticles(ParticleTypes.EXPLOSION, 
                center.getX(), center.getY(), center.getZ(), 
                10, 0.5, 0.5, 0.5, 0.1);
            
            // 播放爆炸声音
            level.playSound(null, center.getX(), center.getY(), center.getZ(),
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
    
    /**
     * 移除实体身上的所有爆炸效果
     * @param entity 实体
     */
    public static void clearBlasts(LivingEntity entity) {
        entityBlasts.remove(entity);
    }
    
    /**
     * 获取实体身上的爆炸效果数量
     * @param entity 实体
     * @return 爆炸效果的数量
     */
    public static int getBlastCount(LivingEntity entity) {
        List<BlastEntry> blasts = entityBlasts.get(entity);
        return blasts != null ? blasts.size() : 0;
    }
}