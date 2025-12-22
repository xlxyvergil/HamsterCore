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
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
    }
    
    /**
     * 对实体施加击退效果
     * @param target 被击退的目标实体
     * @param attacker 攻击者实体
     */
    public void applyKnockback(LivingEntity target, LivingEntity attacker) {
        // 使用攻击者的面向方向来击退目标
        Vec3 lookVec = attacker.getLookAngle();
        // 将目标向攻击者面向的方向击退，增加一点垂直动量
        target.push(lookVec.x, 0.1, lookVec.z);
        target.hurtMarked = true;
    }
}