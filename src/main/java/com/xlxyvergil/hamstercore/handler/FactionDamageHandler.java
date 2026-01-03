package com.xlxyvergil.hamstercore.handler;


import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectRegistry.Effects;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.faction.Faction;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "hamstercore")
public class FactionDamageHandler {
    
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        // 获取被攻击的实体
        LivingEntity target = event.getEntity();
        
        // 获取攻击源
        Entity sourceEntity = event.getSource().getEntity();
        
        // 确保攻击者也是生物实体
        if (sourceEntity instanceof LivingEntity livingAttacker) {
            // 获取攻击者使用的物品
            ItemStack weapon = livingAttacker instanceof Player ? 
                             ((Player) livingAttacker).getMainHandItem() : ItemStack.EMPTY;
            
            // 获取目标实体的派系
            String targetFaction = target.getCapability(EntityFactionCapabilityProvider.CAPABILITY)
                .map(factionCap -> {
                    Faction faction = factionCap.getFaction();
                    return faction != null ? faction.name() : "OROKIN";
                })
                .orElse("OROKIN");
            
            // 获取目标实体的护甲值
            Double targetArmor = target.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
                .map(armorCap -> armorCap.getArmor())
                .orElse(0.0);
            
            // 限制护甲值上限为2700
            targetArmor = Math.min(targetArmor, 2700.0);
            
            // 使用公式 ID = BD × (1+HM) × 元素总倍率 × 暴击伤害 × (1-AM)
            // BD = 基础伤害
            float baseDamage = event.getAmount();
            
            // 使用元素伤害管理器计算最终伤害，直接传递攻击者实体
            ElementDamageManager.ElementDamageData damageData = 
                ElementDamageManager.calculateElementDamage(livingAttacker, target, baseDamage, weapon, targetFaction, targetArmor);
            
            // 检查攻击者是否具有穿刺效果，如果有则降低其造成的伤害
            float finalDamage = damageData.getFinalDamage();
            // 穿刺效果现在通过PunctureEffect的addAttributeModifiers方法处理，不再在这里处理
            
            // 检查目标是否具有病毒效果，如果有则增加对目标的伤害
            // 病毒效果没有对应的原版属性，因此需要手动处理
            var viralEffect = target.getEffect(Effects.VIRAL.get());
            if (viralEffect != null) {
                // 计算病毒伤害增幅：第1级+100%，后续每级+25%，最大+325%
                int amplifier = viralEffect.getAmplifier();
                double viralDamageMultiplier = calculateViralDamageMultiplier(amplifier);
                // 对总伤害进行增伤，直接修改finalDamage
                finalDamage = finalDamage * (float)viralDamageMultiplier;
            }
            
            // 检查目标是否具有磁力效果，如果有则对总伤害进行增伤
            float damageWithMagneticBonus = finalDamage;
            var magneticEffect = target.getEffect(Effects.MAGNETIC.get());
            if (magneticEffect != null && hasShield(target)) {
                // 计算伤害增幅：第1级+100%，后续每级+25%，最大+325%
                int amplifier = magneticEffect.getAmplifier();
                double damageMultiplier = com.xlxyvergil.hamstercore.element.effect.effects.MagneticEffect.calculateShieldDamageMultiplier(amplifier);
                // 对总伤害进行增伤
                damageWithMagneticBonus = finalDamage * (float)damageMultiplier;
            }
            
            // 设置最终伤害（包含病毒和磁力增伤）
            event.setAmount(damageWithMagneticBonus);
            
            // 处理元素触发效果，传递原始基础伤害，而不是包含所有倍率的最终伤害
            ElementTriggerHandler.handleElementTriggers(livingAttacker, target, baseDamage, event.getSource());        
        }
    }
    
    /**
     * 检查实体当前是否有护盾值
     * @param entity 实体
     * @return 是否有护盾值
     */
    private static boolean hasShield(LivingEntity entity) {
        return entity.getCapability(com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider.CAPABILITY)
                .map(shieldCap -> shieldCap.getCurrentShield() > 0)
                .orElse(false);
    }
    
    /**
     * 计算病毒伤害增幅倍率
     * @param amplifier 效果等级 (0-9，对应1-10级)
     * @return 伤害增幅倍率
     */
    private static double calculateViralDamageMultiplier(int amplifier) {
        // 第1层提高100%，后续每级提高25%
        // 1级: 1.0 + 1.0 = 2.0 (100%增幅)
        // 2级: 1.0 + 1.0 + 0.25 = 2.25 (125%增幅)
        // ...
        // 10级: 1.0 + 1.0 + 9*0.25 = 4.25 (325%增幅)
        return 1.0 + 1.0 + (amplifier * 0.25);
    }
    

}