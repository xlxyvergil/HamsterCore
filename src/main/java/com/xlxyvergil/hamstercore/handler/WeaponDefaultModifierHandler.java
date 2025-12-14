package com.xlxyvergil.hamstercore.handler;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.modifier.ElementCombinationModifier;
import com.xlxyvergil.hamstercore.util.ElementModifierManager;
import com.xlxyvergil.hamstercore.util.ForgeAttributeValueReader;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 武器默认修饰符处理器
 * 负责完整的元素修饰符处理流程：
 * 1. 在onItemAttributeModifier事件触发
 * 2. 调用ElementModifierEventHandler把NBT内的InitialModifier层数据转换为ElementModifierManager所需的修饰符数据
 * 3. 调用ElementModifierManager把数据作为修饰符应用进道具
 * 4. 调用ForgeAttributeValueReader读取经过Forge属性系统计算后的实际元素修饰符数值
 * 5. 把数值和元素类型，以及basic层数据传入ElementCombinationModifier计算
 * 6. ElementCombinationModifier把计算后的结果放入usage层
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
            if (isProcessed(stack)) {
                return;
            }
            
            // 3. 从物品的NBT中加载武器数据
            WeaponData weaponData = WeaponDataManager.loadElementData(stack);
            if (weaponData == null || weaponData.getInitialModifiers().isEmpty()) {
                return;
            }
            
            // 标记为开始处理
            markAsProcessing(stack);
            
            // 4. 调用ElementModifierEventHandler把NBT内的InitialModifier层数据转换为ElementModifierManager所需的修饰符数据
            // 4.1 转换InitialModifiers层数据为ElementModifierManager所需的格式
            List<ElementModifierManager.ElementModifierData> elementModifiers = 
                ElementModifierEventHandler.convertToElementModifierData(weaponData.getInitialModifiers());
            
            // 4.2 使用ElementModifierManager应用转换后的修饰符到物品上
            if (!elementModifiers.isEmpty()) {
                ElementModifierManager.applyElementModifiers(stack, elementModifiers, EquipmentSlot.MAINHAND);
            }
            
            // 5. 调用ForgeAttributeValueReader进行修饰符计算
            // 5.1 验证修饰符应用成功
            System.out.println("Debug: Applied " + elementModifiers.size() + " element modifiers to item");
            
            // 5.2 强制刷新物品属性以确保Forge系统已处理修饰符
            // 这里通过重新获取物品修饰符来触发Forge的计算过程
            var appliedModifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
            System.out.println("Debug: Total modifiers on item after application: " + 
                             (appliedModifiers != null ? appliedModifiers.size() : 0));
            
            // 6. 使用ForgeAttributeValueReader获取分类后的元素数值
            ForgeAttributeValueReader.ElementCategoryData categoryData = 
                ForgeAttributeValueReader.getAllElementValuesByCategory(stack);
            
            // 6.1 显示计算结果
            System.out.println("Debug: ForgeAttributeValueReader分类计算结果:");
            System.out.println("  - 基础元素: " + categoryData.getBasicValues());
            System.out.println("  - 复合元素: " + categoryData.getComplexValues());
            System.out.println("  - 特殊元素: " + categoryData.getSpecialValues());
            System.out.println("  - 派系元素: " + categoryData.getFactionValues());
            
            // 7. 传递预分类的基础元素和复合元素数据到ElementCombinationModifier
            Map<String, Double> basicAndComplexValues = categoryData.getBasicAndComplexValues();
            
            // 7.1 显示传入的数据
            System.out.println("Debug: 传入ElementCombinationModifier的预分类数据:");
            System.out.println("  - basicAndComplexValues: " + basicAndComplexValues);
            System.out.println("  - basicElements: " + weaponData.getBasicElements().keySet());
            
            // 7.2 使用预分类的数值进行元素组合计算
            ElementCombinationModifier.computeElementCombinationsWithValues(weaponData, basicAndComplexValues);
            
            System.out.println("Debug: ElementCombinationModifier计算完成，结果已保存到Usage层");
            
            // 8. 保存更新后的武器数据
            WeaponDataManager.saveElementData(stack, weaponData);
            
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
        stack.getOrCreateTag().putBoolean(PROCESSING_MARK, true);
        stack.getOrCreateTag().remove(PROCESSING_MARK + "_Processing");
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