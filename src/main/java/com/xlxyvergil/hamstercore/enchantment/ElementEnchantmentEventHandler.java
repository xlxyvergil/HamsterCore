package com.xlxyvergil.hamstercore.enchantment;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.util.ElementModifierManager;
import com.xlxyvergil.hamstercore.util.ElementUUIDManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 元素附魔事件处理器
 * 负责在附魔应用和移除时同步ElementModifierManager的属性修饰符
 * 参考 EnchantingInfuser 的实现模式
 */
@Mod.EventBusSubscriber
public class ElementEnchantmentEventHandler {
    
    // 用于缓存上次处理的附魔状态
    private static final String LAST_ENCHANTMENT_CACHE = "HamsterCore_LastEnchantmentCache";
    
    /**
     * 使用 ItemAttributeModifierEvent 来检测附魔变化
     * 当附魔发生变化时，同步应用对应的元素修饰符
     */
    @SubscribeEvent(priority = EventPriority.LOW) // 低优先级，在主要修饰符处理完后执行
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack == null || stack.isEmpty()) {
            return;
        }
        
        // 只处理主手槽位的物品
        if (event.getSlotType() != EquipmentSlot.MAINHAND) {
            return;
        }
        
        // 检测附魔变化或需要同步标记
        boolean needsSync = hasEnchantmentChanged(stack);
        
        // 检查是否有其他处理器设置的同步标记
        if (stack.hasTag() && stack.getTag().contains("HamsterCore_NeedsEnchantmentSync")) {
            needsSync = true;
            stack.getTag().remove("HamsterCore_NeedsEnchantmentSync"); // 清除标记
        }
        
        if (needsSync) {
            syncEnchantmentModifiers(stack);
            updateEnchantmentCache(stack);
        }
    }
    
    /**
     * 检查附魔是否发生变化
     */
    private static boolean hasEnchantmentChanged(ItemStack stack) {
        if (!stack.hasTag()) {
            return false;
        }
        
        CompoundTag tag = stack.getTag();
        CompoundTag currentEnchantments = new CompoundTag();
        
        // 将当前附魔信息转换为可比较的格式
        for (Map.Entry<Enchantment, Integer> entry : stack.getAllEnchantments().entrySet()) {
            if (entry.getKey() instanceof ElementEnchantment elementEnchantment) {
                String enchantName = elementEnchantment.getEnchantmentId();
                currentEnchantments.putInt(enchantName, entry.getValue());
            }
        }
        
        // 与缓存的附魔信息比较
        CompoundTag cachedEnchantments = tag.getCompound(LAST_ENCHANTMENT_CACHE);
        return !currentEnchantments.equals(cachedEnchantments);
    }
    
    /**
     * 更新附魔缓存
     */
    private static void updateEnchantmentCache(ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }
        
        CompoundTag tag = stack.getTag();
        CompoundTag currentEnchantments = new CompoundTag();
        
        // 将当前附魔信息缓存
        for (Map.Entry<Enchantment, Integer> entry : stack.getAllEnchantments().entrySet()) {
            if (entry.getKey() instanceof ElementEnchantment elementEnchantment) {
                String enchantName = elementEnchantment.getEnchantmentId();
                currentEnchantments.putInt(enchantName, entry.getValue());
            }
        }
        
        tag.put(LAST_ENCHANTMENT_CACHE, currentEnchantments);
    }
    
    /**
     * 同步附魔修饰符
     * 参考 EnchantingInfuser 的 setNewEnchantments 模式
     */
    private static void syncEnchantmentModifiers(ItemStack stack) {
        // 1. 首先清除所有元素附魔修饰符（类似 EnchantingInfuser 的做法）
        clearElementEnchantmentModifiers(stack);
        
        // 2. 然后应用当前附魔对应的修饰符
        applyCurrentElementEnchantments(stack);
        
        System.out.println("Debug: Synced enchantment modifiers using EnchantingInfuser pattern");
    }
    
    /**
     * 清除所有来自元素附魔的修饰符
     * 通过修改 NBT 来清除元素属性修饰符
     */
    private static void clearElementEnchantmentModifiers(ItemStack stack) {
        if (!stack.hasTag()) {
            return;
        }
        
        CompoundTag tag = stack.getTag();
        
        // 移除 AttributeModifiers 标签（这会清除所有属性修饰符）
        tag.remove("AttributeModifiers");
        
        // 保存 NBT 变化
        stack.setTag(tag);
    }
    

    
    /**
     * 应用当前元素附魔的修饰符
     * 参考 EnchantingInfuser 的 applyEnchantments 模式
     */
    private static void applyCurrentElementEnchantments(ItemStack stack) {
        List<ElementModifierManager.ElementModifierData> elementModifiers = new ArrayList<>();
        
        for (Map.Entry<Enchantment, Integer> entry : stack.getAllEnchantments().entrySet()) {
            if (entry.getKey() instanceof ElementEnchantment elementEnchantment) {
                int level = entry.getValue();
                ElementType elementType = elementEnchantment.getElementType();
                
                ElementAttribute elementAttribute = ElementRegistry.getAttribute(elementType);
                if (elementAttribute != null) {
                    var modifiers = elementEnchantment.getEntityAttributes(stack, EquipmentSlot.MAINHAND, level);
                    
                    for (AttributeModifier modifier : modifiers) {
                        ElementModifierManager.ElementModifierData modifierData = new ElementModifierManager.ElementModifierData();
                        modifierData.setAttribute(new net.minecraft.resources.ResourceLocation("hamstercore", elementType.getName()));
                        modifierData.setId(modifier.getId());
                        modifierData.setAmount(modifier.getAmount());
                        modifierData.setName(modifier.getName());
                        modifierData.setOperation(modifier.getOperation());
                        
                        elementModifiers.add(modifierData);
                    }
                }
            }
        }
        
        if (!elementModifiers.isEmpty()) {
            // 使用 ElementModifierManager 应用修饰符（会重新创建 AttributeModifiers 标签）
            ElementModifierManager.applyElementModifiers(stack, elementModifiers, EquipmentSlot.MAINHAND);
        }
    }
    
    /**
     * 手动处理附魔移除的同步方法
     * 这个方法可以在其他需要强制同步的地方调用
     * @param stack 物品堆
     */
    public static void syncEnchantmentRemoval(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        
        // 直接调用同步方法，避免递归
        syncEnchantmentModifiers(stack);
        
        System.out.println("Debug: Force synced enchantment modifiers");
    }
    
    /**
     * 从物品的NBT中读取附魔信息并同步修饰符
     * 这个方法可以在物品加载时调用，确保修饰符同步
     * @param stack 物品堆
     */
    public static void syncFromNBT(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        
        // 直接调用同步方法，避免递归
        syncEnchantmentModifiers(stack);
    }
}