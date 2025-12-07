package com.xlxyvergil.hamstercore.element;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;

/**
 * 元素伤害处理器
 * 负责处理武器元素触发和伤害计算
 */
@Mod.EventBusSubscriber(modid = "hamstercore")
public class ElementDamageHandler {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingAttack(LivingAttackEvent event) {
        // 只处理玩家攻击事件
        if (event.getSource().getEntity() instanceof Player player) {
            ItemStack weapon = player.getMainHandItem();
            
            // 检查武器是否有元素属性
            if (ElementHelper.hasElementAttributes(weapon)) {
                // 处理元素触发（可能触发多个元素）
                List<ElementType> triggeredElements = ElementHelper.processWeaponTrigger(weapon);
                
                for (ElementType triggeredElement : triggeredElements) {
                    // 记录触发的元素类型，可以在伤害计算中使用
                    LOGGER.debug("Triggered element: " + triggeredElement.getName());
                    
                    // 在这里可以添加元素效果的具体实现
                    // 比如施加状态效果、特殊效果等
                    applyElementEffect(event, triggeredElement, weapon, player, event.getEntity());
                }
            }
        }
    }
    
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingHurt(LivingHurtEvent event) {
        // 只处理玩家攻击事件
        if (event.getSource().getEntity() instanceof Player player) {
            LivingEntity target = event.getEntity();
            ItemStack weapon = player.getMainHandItem();
            
            // 检查武器是否有元素属性
            if (ElementHelper.hasElementAttributes(weapon)) {
                // 计算元素伤害
                float elementDamage = calculateElementDamage(weapon, event.getAmount());
                
                // 应用元素伤害
                if (elementDamage > 0) {
                    event.setAmount(event.getAmount() + elementDamage);
                    
                    // 可以在这里添加额外的伤害处理逻辑
                    LOGGER.debug("Applied element damage: " + elementDamage);
                }
            }
        }
    }
    
    /**
     * 应用元素效果
     * @param event 攻击事件
     * @param elementType 触发的元素类型
     * @param weapon 武器
     * @param attacker 攻击者
     * @param target 目标
     */
    private static void applyElementEffect(LivingAttackEvent event, ElementType elementType, ItemStack weapon, Player attacker, LivingEntity target) {
        // 获取元素属性
        ElementAttribute attribute = ElementRegistry.getAttribute(elementType);
        if (attribute == null) {
            return;
        }
        
        // 获取元素实例
        Map<ElementType, ElementInstance> elements = ElementHelper.getElementAttributes(weapon);
        ElementInstance element = elements.get(elementType);
        if (element == null) {
            return;
        }
        
        // 根据元素类型应用不同的效果
        switch (elementType) {
            case IMPACT:
                // 冲击效果：可能造成击退或眩晕
                applyImpactEffect(target, element);
                break;
                
            case PUNCTURE:
                // 穿刺效果：可能无视部分护甲
                applyPunctureEffect(event, element);
                break;
                
            case SLASH:
                // 切割效果：可能造成流血
                applySlashEffect(target, element);
                break;
                
            case COLD:
                // 冰冻效果：可能减速
                applyColdEffect(target, element);
                break;
                
            case ELECTRICITY:
                // 电击效果：可能造成连锁伤害
                applyElectricityEffect(target, element);
                break;
                
            case HEAT:
                // 火焰效果：可能造成燃烧
                applyHeatEffect(target, element);
                break;
                
            case TOXIN:
                // 毒素效果：可能造成中毒
                applyToxinEffect(target, element);
                break;
                
            // 复合元素效果
            case BLAST:
                applyBlastEffect(target, element);
                break;
                
            case CORROSIVE:
                applyCorrosiveEffect(target, element);
                break;
                
            case GAS:
                applyGasEffect(target, element);
                break;
                
            case MAGNETIC:
                applyMagneticEffect(target, element);
                break;
                
            case RADIATION:
                applyRadiationEffect(target, element);
                break;
                
            case VIRAL:
                applyViralEffect(target, element);
                break;
                
            default:
                break;
        }
    }
    
    /**
     * 计算元素伤害
     * @param weapon 武器
     * @param baseDamage 基础伤害
     * @return 元素伤害
     */
    private static float calculateElementDamage(ItemStack weapon, float baseDamage) {
        Map<ElementType, ElementInstance> elements = ElementHelper.getElementAttributes(weapon);
        float totalElementDamage = 0.0f;
        
        for (Map.Entry<ElementType, ElementInstance> entry : elements.entrySet()) {
            ElementType type = entry.getKey();
            ElementInstance element = entry.getValue();
            
            // 排除特殊属性，它们不直接增加伤害
            if (type == ElementType.TRIGGER_CHANCE || type == ElementType.CRITICAL_CHANCE || type == ElementType.CRITICAL_DAMAGE) {
                continue;
            }
            
            // 计算元素伤害（这里使用简单的加法，可以根据需要修改）
            totalElementDamage += (float) element.value();
        }
        
        return totalElementDamage;
    }
    
    /**
     * 应用冲击效果
     */
    private static void applyImpactEffect(LivingEntity target, ElementInstance element) {
        // 冲击效果：击退目标
        double knockbackStrength = 0.5 + element.value() * 0.1;
        target.knockback((float) knockbackStrength, target.getX() - target.getX(), target.getZ() - target.getZ());
    }
    
    /**
     * 应用穿刺效果
     */
    private static void applyPunctureEffect(LivingAttackEvent event, ElementInstance element) {
        // 穿刺效果：忽略部分护甲
        // 这里可以在伤害计算中实现
    }
    
    /**
     * 应用切割效果
     */
    private static void applySlashEffect(LivingEntity target, ElementInstance element) {
        // 切割效果：造成流血（持续伤害）
        // 这里可以实现流血效果
    }
    
    /**
     * 应用冰冻效果
     */
    private static void applyColdEffect(LivingEntity target, ElementInstance element) {
        // 冰冻效果：减速目标
        // 这里可以实现减速效果
    }
    
    /**
     * 应用电击效果
     */
    private static void applyElectricityEffect(LivingEntity target, ElementInstance element) {
        // 电击效果：可能造成连锁伤害
        // 这里可以实现电击效果
    }
    
    /**
     * 应用火焰效果
     */
    private static void applyHeatEffect(LivingEntity target, ElementInstance element) {
        // 火焰效果：燃烧目标
        int fireTicks = (int) (element.value() * 20); // 每点伤害燃烧1秒
        target.setSecondsOnFire(fireTicks / 20);
    }
    
    /**
     * 应用毒素效果
     */
    private static void applyToxinEffect(LivingEntity target, ElementInstance element) {
        // 毒素效果：中毒
        // 这里可以实现中毒效果
    }
    
    /**
     * 应用爆炸效果
     */
    private static void applyBlastEffect(LivingEntity target, ElementInstance element) {
        // 爆炸效果：范围伤害
        // 这里可以实现范围伤害
    }
    
    /**
     * 应用腐蚀效果
     */
    private static void applyCorrosiveEffect(LivingEntity target, ElementInstance element) {
        // 腐蚀效果：降低护甲
        // 这里可以实现降甲效果
    }
    
    /**
     * 应用毒气效果
     */
    private static void applyGasEffect(LivingEntity target, ElementInstance element) {
        // 毒气效果：范围持续伤害
        // 这里可以实现范围持续伤害
    }
    
    /**
     * 应用磁力效果
     */
    private static void applyMagneticEffect(LivingEntity target, ElementInstance element) {
        // 磁力效果：可能影响移动或使用物品
        // 这里可以实现磁力效果
    }
    
    /**
     * 应用辐射效果
     */
    private static void applyRadiationEffect(LivingEntity target, ElementInstance element) {
        // 辐射效果：可能造成持续伤害或降低属性
        // 这里可以实现辐射效果
    }
    
    /**
     * 应用病毒效果
     */
    private static void applyViralEffect(LivingEntity target, ElementInstance element) {
        // 病毒效果：可能降低最大生命值或传播
        // 这里可以实现病毒效果
    }
}