package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.api.element.ElementComputedAPI;
import com.xlxyvergil.hamstercore.enchantment.ElementEnchantment;
import com.xlxyvergil.hamstercore.enchantment.ModEnchantments;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.util.DebugLogger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class EnchantmentEventHandler {

    // 存储物品附魔信息的映射，用于在物品从砂轮取出时移除对应的数据
    private static final Map<Integer, List<ElementEnchantmentInfo>> storedEnchantmentInfo = new HashMap<>();
    
    // 存储物品本身，用于在移除时准确找到对应的NBT数据
    private static final Map<Integer, ItemStack> storedItems = new HashMap<>();

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();
        
        // 检查是否已经有输出物品，如果有则不处理
        if (!event.getOutput().isEmpty()) {
            return;
        }

        // 检查右侧是否是附魔书
        if (right.getItem() instanceof EnchantedBookItem) {
            // 获取附魔信息
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(right);
            
            // 遍历所有附魔
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                Enchantment enchantment = entry.getKey();
                int level = entry.getValue();
                
                // 检查是否是我们自定义的元素附魔
                if (enchantment instanceof ElementEnchantment) {
                    ElementEnchantment elementEnchantment = (ElementEnchantment) enchantment;
                    
                    // 检查是否已经存在相同的附魔，避免重复附魔
                    if (hasExistingEnchantment(left, elementEnchantment)) {
                        DebugLogger.log("物品已存在相同附魔，跳过处理: %s", elementEnchantment.getElementType().getName());
                        return; // 如果已经存在相同类型的附魔，则不处理
                    }
                    
                    // 创建输出物品（复制左侧物品）
                    ItemStack output = left.copy();
                    
                    // 直接使用附魔预定义的ID
                    String enchantmentId = elementEnchantment.getEnchantmentId();
                    DebugLogger.log("准备添加元素附魔: %s, 等级: %d, 附魔ID: %s", elementEnchantment.getElementType().getName(), level, enchantmentId);
                    
                    // 根据元素类型决定添加到哪个部分
                    // 对于不同等级，我们可以设置不同的值
                    double value = 0.5 * level; // 示例：每级增加0.5
                    
                    String elementTypeName = elementEnchantment.getElementType().getName();
                    DebugLogger.log("处理元素附魔，元素类型名称: %s", elementTypeName);
                    
                    if (isFactionElement(elementEnchantment.getElementType())) {
                        // 派系元素添加到extra
                        DebugLogger.log("添加派系元素到extra层: %s", elementTypeName);
                        ElementComputedAPI.addExtraFactionModifierWithSpecificSource(output, elementTypeName, value, "add", enchantmentId);
                        DebugLogger.log("添加派系元素到extra: %s, ID: %s", elementTypeName, enchantmentId);
                    } else {
                        // 其他元素添加到computed
                        DebugLogger.log("添加普通元素到computed层: %s", elementTypeName);
                        ElementComputedAPI.addComputedElementWithSpecificSource(output, elementTypeName, value, "add", enchantmentId);
                        DebugLogger.log("添加元素到computed: %s, ID: %s", elementTypeName, enchantmentId);
                    }
                    
                    // 将附魔添加到物品的附魔列表中（标准Minecraft附魔系统）
                    Map<Enchantment, Integer> outputEnchantments = EnchantmentHelper.getEnchantments(output);
                    outputEnchantments.put(elementEnchantment, level);
                    EnchantmentHelper.setEnchantments(outputEnchantments, output);
                    
                    // 设置输出物品
                    event.setOutput(output);
                    // 设置消耗经验值
                    event.setCost(level);
                    
                    DebugLogger.log("成功设置附魔输出物品");
                    
                    // 找到第一个元素附魔就退出循环
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public static void onGrindstonePlace(GrindstoneEvent.OnPlaceItem event) {
        // 当物品放入砂轮时，记录物品上的元素附魔信息
        ItemStack topItem = event.getTopItem();
        ItemStack bottomItem = event.getBottomItem();
        
        // 处理顶部物品
        processElementEnchantmentsOnPlace(topItem, 1);
        
        // 处理底部物品
        processElementEnchantmentsOnPlace(bottomItem, 2);
    }
    
    @SubscribeEvent
    public static void onGrindstoneTake(GrindstoneEvent.OnTakeItem event) {
        // 当物品从砂轮中取出时，处理附魔数据的移除
        ItemStack topItem = event.getTopItem();
        ItemStack bottomItem = event.getBottomItem();
        
        // 移除顶部物品的元素附魔数据
        removeElementEnchantmentData(topItem, 1);
        
        // 移除底部物品的元素附魔数据
        removeElementEnchantmentData(bottomItem, 2);
        
        // 清理存储的信息
        storedEnchantmentInfo.remove(1);
        storedEnchantmentInfo.remove(2);
        storedItems.remove(1);
        storedItems.remove(2);
    }
    
    /**
     * 在物品放入砂轮时处理元素附魔
     * @param stack 物品堆
     * @param slotIndex 物品槽位索引 (1 或 2)
     */
    private static void processElementEnchantmentsOnPlace(ItemStack stack, int slotIndex) {
        // 获取物品的所有附魔
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
        List<ElementEnchantmentInfo> enchantmentInfos = new ArrayList<>();
        
        // 遍历所有附魔，检查是否有元素附魔，并记录相关信息
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();
            if (enchantment instanceof ElementEnchantment) {
                ElementEnchantment elementEnchantment = (ElementEnchantment) enchantment;
                
                // 直接使用附魔预定义的ID
                String enchantmentId = elementEnchantment.getEnchantmentId();
                
                // 存储附魔信息
                String elementTypeName = elementEnchantment.getElementType().getName();
                DebugLogger.log("存储元素附魔信息，元素类型名称: %s", elementTypeName);
                enchantmentInfos.add(new ElementEnchantmentInfo(
                    elementTypeName,
                    enchantmentId,
                    isFactionElement(elementEnchantment.getElementType())
                ));
                
                // 记录日志表明我们识别到了这个元素附魔
                DebugLogger.log("检测到物品上的元素附魔: %s, 等级: %d, ID: %s", 
                               elementEnchantment.getElementType().getName(), level, enchantmentId);
            }
        }
        
        // 存储信息以备后续使用
        if (!enchantmentInfos.isEmpty()) {
            storedEnchantmentInfo.put(slotIndex, enchantmentInfos);
            // 同时存储物品本身，以便在移除时使用
            storedItems.put(slotIndex, stack.copy());
        }
    }
    
    /**
     * 移除物品上所有元素附魔相关数据
     * @param stack 物品堆
     * @param slotIndex 物品槽位索引 (1 或 2)
     */
    private static void removeElementEnchantmentData(ItemStack stack, int slotIndex) {
        // 从存储的信息中获取附魔数据，因为在物品取出时原始附魔可能已被移除
        List<ElementEnchantmentInfo> enchantmentInfos = storedEnchantmentInfo.get(slotIndex);
        ItemStack storedItem = storedItems.get(slotIndex); // 获取之前存储的物品
        
        if (enchantmentInfos != null && storedItem != null) {
            // 使用预先存储的信息来移除元素数据
            for (ElementEnchantmentInfo info : enchantmentInfos) {
                DebugLogger.log("处理移除元素附魔，元素类型名称: %s, 是否为派系元素: %s", info.elementType, info.isFactionElement);
                if (info.isFactionElement) {
                    // 从extra层移除派系元素
                    ElementComputedAPI.removeExtraFactionModifierBySpecificSource(storedItem, info.elementType, info.enchantmentId);
                    DebugLogger.log("从物品中移除派系元素: %s, SpecificSource: %s", info.elementType, info.enchantmentId);
                } else {
                    // 从computed层移除元素
                    ElementComputedAPI.removeComputedElementBySpecificSource(storedItem, info.elementType, info.enchantmentId);
                    DebugLogger.log("从物品中移除元素: %s, SpecificSource: %s", info.elementType, info.enchantmentId);
                }
            }
            
            // 重新计算物品的元素数据以确保一致性
            WeaponElementData data = WeaponDataManager.loadElementData(storedItem);
            WeaponDataManager.computeUsageData(storedItem, data);
            WeaponDataManager.saveElementData(storedItem, data);
            
            // 更新砂轮中物品的状态
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(storedItem);
            for (ElementEnchantmentInfo info : enchantmentInfos) {
                // 查找并移除对应的附魔
                Enchantment enchantmentToRemove = null;
                for (Enchantment enchantment : enchantments.keySet()) {
                    if (enchantment instanceof ElementEnchantment) {
                        ElementEnchantment elementEnchantment = (ElementEnchantment) enchantment;
                        if (elementEnchantment.getEnchantmentId().equals(info.enchantmentId)) {
                            enchantmentToRemove = enchantment;
                            break;
                        }
                    }
                }
                
                if (enchantmentToRemove != null) {
                    enchantments.remove(enchantmentToRemove);
                }
            }
            EnchantmentHelper.setEnchantments(enchantments, storedItem);
            
            // 将修改后的物品数据保存回原始物品中
            if (!stack.isEmpty() && ItemStack.matches(stack, storedItem)) {
                stack.setTag(storedItem.getTag());
            }
        }
    }
    
    /**
     * 元素附魔信息存储类
     */
    private static class ElementEnchantmentInfo {
        public final String elementType;
        public final String enchantmentId;
        public final boolean isFactionElement;
        
        public ElementEnchantmentInfo(String elementType, String enchantmentId, boolean isFactionElement) {
            this.elementType = elementType;
            this.enchantmentId = enchantmentId;
            this.isFactionElement = isFactionElement;
        }
    }
    
    /**
     * 检查物品是否已经存在指定的元素附魔
     * @param stack 物品堆
     * @param elementEnchantment 元素附魔
     * @return 是否已存在该附魔
     */
    private static boolean hasExistingEnchantment(ItemStack stack, ElementEnchantment elementEnchantment) {
        Map<Enchantment, Integer> existingEnchantments = EnchantmentHelper.getEnchantments(stack);
        return existingEnchantments.containsKey(elementEnchantment);
    }

    /**
     * 判断是否为派系元素
     * @param elementType 元素类型
     * @return 是否为派系元素
     */
    private static boolean isFactionElement(com.xlxyvergil.hamstercore.element.ElementType elementType) {
        return elementType == com.xlxyvergil.hamstercore.element.ElementType.GRINEER ||
               elementType == com.xlxyvergil.hamstercore.element.ElementType.INFESTED ||
               elementType == com.xlxyvergil.hamstercore.element.ElementType.CORPUS ||
               elementType == com.xlxyvergil.hamstercore.element.ElementType.OROKIN ||
               elementType == com.xlxyvergil.hamstercore.element.ElementType.SENTIENT ||
               elementType == com.xlxyvergil.hamstercore.element.ElementType.MURMUR;
    }
}