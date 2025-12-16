package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.modifier.ElementAttributeModifierEntry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 元素附魔事件处理器
 * 负责在附魔应用和移除时同步ElementAttributeModifierEntry的属性修饰符
 * 参考EnchantingInfuser 的实现模式
 */
@Mod.EventBusSubscriber
public class ElementEnchantmentEventHandler {
    
    // 用于缓存上次处理的附魔状态
    private static final String LAST_ENCHANTMENT_CACHE = "HamsterCore_LastEnchantmentCache";
    
    /**
     * 使用 ItemAttributeModifierEvent 直接添加元素修饰符
     * 参考WeaponDefaultModifierHandler实现模式
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        
        // 只处理主手槽位的物品
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        
        // 直接应用当前附魔的修饰符，忽略WeaponDefaultModifierHandler的处理标记
        // 因为我们处理的是附魔相关的修饰符，与基础修饰符是不同的
        applyCurrentElementEnchantments(stack, event);
    }
    

    

    

    
    /**
     * 应用当前元素附魔的修饰符
     * 使用事件API来处理修饰符，避免直接修改NBT标签导致原有武器属性丢失
     */
    private static void applyCurrentElementEnchantments(ItemStack stack, ItemAttributeModifierEvent event) {
        // 确保stack不为null
        if (stack == null) {
            return;
        }
        
        List<ElementAttributeModifierEntry> allModifiers = new ArrayList<>();
        
        // 收集所有需要应用的新修饰符
        for (Map.Entry<Enchantment, Integer> entry : stack.getAllEnchantments().entrySet()) {
            // 确保entry、key和value不为null
            if (entry == null || entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            
            if (entry.getKey() instanceof ElementEnchantment elementEnchantment && elementEnchantment.getElementType() != null) {
                int level = entry.getValue();
                
                // 直接使用getElementAttributeModifiers方法获取修饰符
                List<ElementAttributeModifierEntry> enchantmentModifiers = 
                    elementEnchantment.getElementAttributeModifiers(stack, EquipmentSlot.MAINHAND, level);
                
                if (!enchantmentModifiers.isEmpty()) {
                    allModifiers.addAll(enchantmentModifiers);
                }
            }
        }
        
        // 使用事件API应用修饰符，不直接修改NBT标签
        if (!allModifiers.isEmpty()) {
            // 直接使用ElementAttributeModifierEntry的applyElementModifiers方法，传入event::addModifier作为回调
            ElementAttributeModifierEntry.applyElementModifiers(stack, allModifiers, EquipmentSlot.MAINHAND, event::addModifier);
        }
    }
}