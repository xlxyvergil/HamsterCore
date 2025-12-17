package com.xlxyvergil.hamstercore.handler;


import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.element.ElementCalculationCoordinator;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
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
            
            // 获取武器数据并计算缓存
            AffixCacheManager.AffixCacheData cacheData = null;
            if (!weapon.isEmpty()) {
                // 获取武器数据
                WeaponData weaponData = WeaponDataManager.loadElementData(weapon);
                if (weaponData != null) {
                    // 使用ElementCalculationCoordinator计算并缓存元素数据
                    ElementCalculationCoordinator.INSTANCE.calculateAndCacheElements(weapon, weaponData);
                    // 获取缓存的数据
                    cacheData = AffixCacheManager.getOrCreateCache(weapon);
                }
            }
            
            // 使用元素伤害管理器计算最终伤害，传递缓存数据
            ElementDamageManager.ElementDamageData damageData = 
                ElementDamageManager.calculateElementDamage(livingAttacker, target, baseDamage, weapon, targetFaction, targetArmor, cacheData);
            
            // 设置最终伤害
            event.setAmount(damageData.getFinalDamage());
            
            // 处理元素触发效果，传递缓存数据
            ElementTriggerHandler.handleElementTriggers(livingAttacker, target, cacheData);
        }
    }
}