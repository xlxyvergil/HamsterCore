package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * 冲击元素效果
 * 用于管理击退的状态效果
 */
public class ImpactEffect extends ElementEffect {
    
    // 冲击效果的最大等级
    public static final int MAX_LEVEL = 1;    
    public ImpactEffect() {
        super(MobEffectCategory.HARMFUL, 0x808080); // 灰色
    }
    
    /**
     * 应用冲击效果，实现击退效果
     * 直接向后击退一格
     * @param entity 实体
     * @param amplifier 效果等级（冲击效果只有1级）
     */
    public void applyEffect(LivingEntity entity, int amplifier) {
        // 实现击退效果，直接向后击退一格
        // 获取实体当前朝向的反方向
        Vec3 lookVec = entity.getLookAngle();
        // 向后击退一格，忽略Y轴，增加一点垂直动量
        entity.push(-lookVec.x, 0.1, -lookVec.z);
        entity.hurtMarked = true;
    }}