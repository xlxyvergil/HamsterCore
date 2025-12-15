package com.xlxyvergil.hamstercore.handler;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.modifier.ElementAttributeModifierEntry;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * 武器默认修饰符处理器
 * 负责完整的元素修饰符处理流程：
 * 1. 在onItemAttributeModifier事件触发
 * 2. 调用ElementModifierEventHandler把NBT内的InitialModifier层数据转换为ElementAttributeModifierEntry格式
 * 3. 调用ElementAttributeModifierEntry把数据作为修饰符应用进道具
 * 4. 把InitialModifier层数据按照数据需求放入usage层
 */
@Mod.EventBusSubscriber
public class WeaponDefaultModifierHandler {
    
    // 防止重复处理的标记
    private static final String PROCESSING_MARK = "HamsterCore_ProcessingApplied";
    
    /**
     * 物品属性修饰符事件
     * 完整的元素修饰符处理流程
     */
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        try {
            ItemStack stack = event.getItemStack();
            
            // 1. 基础检查
            if (stack == null || stack.isEmpty()) {
                return;
            }
            
            // 2. 防止重复处理 - 同一个物品在同一事件中只处理一次
            if (isProcessed(stack) || isProcessing(stack)) {
                return;
            }
            
            // 3. 从物品的NBT中加载武器数据
            WeaponData weaponData = WeaponDataManager.loadElementData(stack);
            if (weaponData == null || weaponData.getInitialModifiers().isEmpty()) {
                return;
            }
            
            // 标记为开始处理
            markAsProcessing(stack);
            
            // 4. 调用ElementModifierEventHandler把NBT内的InitialModifier层数据转换为ElementAttributeModifierEntry格式
            // 4.1 转换InitialModifiers层数据为ElementAttributeModifierEntry格式
            List<ElementAttributeModifierEntry> elementModifiers = 
                ElementModifierEventHandler.convertToElementModifierEntries(weaponData.getInitialModifiers());
            
            // 4.2 使用ElementAttributeModifierEntry应用转换后的修饰符到物品上
            if (!elementModifiers.isEmpty()) {
                ElementAttributeModifierEntry.applyElementModifiers(stack, elementModifiers, event.getSlotType(), (attr, mod) -> event.addModifier(attr, mod));
            }
            
            // 5. 应用修饰符后，保存初始数据到NBT（不进行复合计算）
            System.out.println("Debug: Applied " + elementModifiers.size() + " element modifiers to item");
            
            // 6. 保存初始武器数据到NBT，复合计算将在需要时动态进行
            WeaponDataManager.saveElementData(stack, weaponData);
            
            // 7. 标记需要同步附魔修饰符，让 ElementEnchantmentEventHandler 在低优先级时处理
            // 这样可以避免在同一事件周期中重复处理修饰符
            stack.getOrCreateTag().putBoolean("HamsterCore_NeedsEnchantmentSync", true);
            
            // 标记为处理完成
            markAsProcessed(stack);
            
        } catch (Exception e) {
            System.err.println("Error in WeaponDefaultModifierHandler.onItemAttributeModifier: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查物品是否已经被处理过
     */
    private static boolean isProcessed(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(PROCESSING_MARK);
    }
    
    /**
     * 检查物品是否正在处理中
     */
    private static boolean isProcessing(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(PROCESSING_MARK + "_Processing");
    }
    
    /**
     * 标记物品为正在处理中
     */
    private static void markAsProcessing(ItemStack stack) {
        stack.getOrCreateTag().putBoolean(PROCESSING_MARK + "_Processing", true);
    }
    
    /**
     * 标记物品为已处理
     */
    private static void markAsProcessed(ItemStack stack) {
        stack.getTag().putBoolean(PROCESSING_MARK, true);
        stack.getTag().remove(PROCESSING_MARK + "_Processing");
    }
    
    /**
     * 清除处理标记（用于测试或重置）
     */
    public static void clearProcessingMark(ItemStack stack) {
        if (stack.hasTag()) {
            stack.getTag().remove(PROCESSING_MARK);
            stack.getTag().remove(PROCESSING_MARK + "_Processing");
        }
    }
}