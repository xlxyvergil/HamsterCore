package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.util.ElementNBTUtils;
import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.faction.Faction;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * 派系修饰符计算器
 * 计算针对特定派系的伤害修饰符
 */
public class FactionModifierCalculator {
    
    /**
     * 计算派系伤害修饰符
     * @param attacker 攻击者
     * @param target 目标实体
     * @param data 武器元素数据
     * @param targetFaction 目标派系
     * @return 派系伤害修饰符
     */
    public static double calculateFactionModifier(LivingEntity attacker, LivingEntity target, WeaponElementData data, String targetFaction) {
        // 计算克制系数
        double modifier = 0.0;
        
        // 添加武器上的extra层派系修饰符
        // 如果目标实体派系和extra层中的派系相同，则extra层中的数据生效
        if (data != null) {
            // 检查Extra层是否有指定派系的数据
            double extraFactionModifier = data.getExtraFaction(targetFaction) != null ? 
                data.getExtraFaction(targetFaction).getValue() : 0.0;
            modifier += extraFactionModifier;
        }
        
        return modifier;
    }
}