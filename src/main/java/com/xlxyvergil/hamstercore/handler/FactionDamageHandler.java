package com.xlxyvergil.hamstercore.handler;

import java.util.List;
import java.util.Map;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.faction.Faction;
import com.xlxyvergil.hamstercore.util.ForgeAttributeValueReader;

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
    
    // 添加静态变量存储最近一次战斗信息
    public static boolean lastAttackWasCritical = false;
    public static int lastCriticalLevel = 0;
    public static double lastCriticalMultiplier = 1.0;
    public static List<ElementType> lastTriggeredElements = null;
    
    
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
            
            // 从Forge属性系统获取特殊元素和派系元素值
            Map<String, Double> specialAndFactionValues = ForgeAttributeValueReader.getAllSpecialAndFactionValues(weapon);
            
            // 使用元素伤害管理器计算最终伤害
            ElementDamageManager.ElementDamageData damageData = 
                ElementDamageManager.calculateElementDamage(livingAttacker, target, baseDamage, weapon, targetFaction, targetArmor, specialAndFactionValues);
            
            // 设置最终伤害
            event.setAmount(damageData.getFinalDamage());
            
            // 处理元素触发效果，使用新的ElementTriggerHandler类
            ElementTriggerHandler.handleElementTriggers(livingAttacker, target);
        }
    }
}