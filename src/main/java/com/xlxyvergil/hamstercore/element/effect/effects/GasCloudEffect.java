package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.GasManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectInstance;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectRegistry;
import com.xlxyvergil.hamstercore.handler.ElementTriggerHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.server.level.ServerLevel;
import java.util.ArrayList;
import java.util.List;

/**
 * 毒云元素效果
 * 负责毒气的AoE传播，为周围实体赋予毒气状态效果
 */
public class GasCloudEffect extends ElementEffect {
    
    // 毒云效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    // 毒云效果持续时间（tick）：6秒 = 120 ticks
    private static final int CLOUD_DURATION = 120;
    
    // 毒云影响范围（米）：5米
    private static final double AOE_RANGE = 5.0;
    
    // 毒云传播间隔（tick）：每秒传播一次 = 20 ticks
    private static final int SPREAD_INTERVAL = 20;
    
    public GasCloudEffect() {
        super(MobEffectCategory.HARMFUL, 0x8B008B); // 深绿色
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每秒触发一次传播
        return duration % SPREAD_INTERVAL == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 只在服务器端处理
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }
        
        ServerLevel serverLevel = (ServerLevel) entity.level();
        
        // 计算影响范围
        AABB boundingBox = new AABB(
            entity.getX() - AOE_RANGE, entity.getY() - AOE_RANGE, entity.getZ() - AOE_RANGE,
            entity.getX() + AOE_RANGE, entity.getY() + AOE_RANGE, entity.getZ() + AOE_RANGE);
        
        // 找到范围内的所有实体，创建副本避免并发修改异常
        List<LivingEntity> entities = new ArrayList<>(serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox));
        
        // 从当前云效果获取原始伤害值
        ElementEffectInstance gasEffectInstance = getElementEffectInstance(entity);
        float baseDamage = gasEffectInstance != null ? gasEffectInstance.getFinalDamage() : 1.0F;
        DamageSource damageSource = gasEffectInstance != null ? gasEffectInstance.getDamageSource() : entity.damageSources().generic();
        
        // 为范围内的所有实体赋予毒气状态效果（排除玩家）
        for (LivingEntity livingEntity : entities) {
            // 排除玩家实体和中心实体
            if (livingEntity instanceof Player || livingEntity == entity) {
                continue;
            }
            
            // 计算传播的伤害值：基础伤害的100%（保持原始伤害）
            float spreadDamage = baseDamage;
            
            // 为目标实体添加毒气状态效果（DoT效果）
            ElementEffectInstance effectInstance = 
                new ElementEffectInstance(
                    (ElementEffect) ElementEffectRegistry.GAS.get(), // GasEffect
                    CLOUD_DURATION, // 持续时间
                    amplifier, // 保持原始等级
                    spreadDamage, // 传播伤害
                    damageSource // 伤害源
                );
            livingEntity.addEffect(effectInstance);
        }
    }
}