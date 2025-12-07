package com.xlxyvergil.hamstercore.faction;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = "hamstercore")
public class FactionDamageHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    
    // 基础等级
    private static final int BASE_LEVEL = 20;
    
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        // 获取被攻击的实体
        LivingEntity target = event.getEntity();
        
        // 获取攻击源
        Entity sourceEntity = event.getSource().getEntity();
        
        // 确保攻击者也是生物实体
        if (sourceEntity instanceof LivingEntity) {
            // 使用公式 ID = BD × (1-AM)
            // BD = 基础伤害
            float baseDamage = event.getAmount();
            
            // 获取我们自定义的护甲值
            double customArmor = target.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
                .map(armorCap -> {
                    LOGGER.debug("Getting armor from capability for entity: " + target.getType().getDescriptionId());
                    double armor = armorCap.getArmor();
                    LOGGER.debug("Got armor value: " + armor);
                    return armor;
                })
                .orElse(0.0);
            
            // 计算AM = 0.9 × √(AR/2700)
            double AM = 0.9 * Math.sqrt(customArmor / 2700.0);
            
            // 应用公式: ID = BD × (1-AM)
            float inflictedDamage = (float) (baseDamage * (1.0 - AM));
            
            // 确保伤害不会小于0
            if (inflictedDamage < 0) {
                inflictedDamage = 0;
            }
            
            // 设置最终伤害
            event.setAmount(inflictedDamage);
        }
    }
}