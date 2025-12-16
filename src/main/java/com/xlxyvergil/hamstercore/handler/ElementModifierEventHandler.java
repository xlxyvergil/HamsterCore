package com.xlxyvergil.hamstercore.handler;

import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.InitialModifierEntry;
import com.xlxyvergil.hamstercore.element.modifier.ElementAttributeModifierEntry;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

import java.util.ArrayList;
import java.util.List;

/**
 * 元素修饰符事件处理器
 * 读取道具InitialModifier层数据，然后转换为ElementAttributeModifierEntry
 */
@Mod.EventBusSubscriber(modid = "hamstercore")
public class ElementModifierEventHandler {
    
    /**
     * 将InitialModifier层数据转换为ElementAttributeModifierEntry格式
     * @param initialModifiers InitialModifier层数据
     * @return ElementAttributeModifierEntry列表
     */
    public static List<ElementAttributeModifierEntry> convertToElementModifierEntries(
            List<InitialModifierEntry> initialModifiers) {
        List<ElementAttributeModifierEntry> elementModifiers = new ArrayList<>();
        
        if (initialModifiers == null || initialModifiers.isEmpty()) {
            return elementModifiers;
        }
        
        for (InitialModifierEntry entry : initialModifiers) {
            try {
                if (entry == null) {
                    continue;
                }
                
                // 从InitialModifierEntry中提取数据
                String elementName = entry.getName();
                net.minecraft.world.entity.ai.attributes.AttributeModifier originalModifier = entry.getModifier();
                
                // 根据名称获取元素类型
                ElementType elementType = ElementType.byName(elementName);
                if (elementType == null) {
                    System.err.println("Unknown element type: " + elementName);
                    continue;
                }
                
                // 如果修饰符为null，创建默认的修饰符
                if (originalModifier == null) {
                    // 创建默认的UUID
                    UUID modifierUuid = UUID.nameUUIDFromBytes(("hamstercore:" + elementName).getBytes());
                    
                    // 创建默认的修饰符
                    originalModifier = new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        modifierUuid, 
                        elementName, 
                        1.0, // 默认值
                        net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION
                    );
                }
                
                // 创建ElementAttributeModifierEntry
                ElementAttributeModifierEntry modifierEntry = new ElementAttributeModifierEntry(
                    elementType,
                    originalModifier.getId(),
                    originalModifier.getAmount(),
                    originalModifier.getOperation()
                );
                
                elementModifiers.add(modifierEntry);
                
            } catch (Exception e) {
                System.err.println("Error converting InitialModifierEntry for " + entry.getName() + ": " + e.getMessage());
            }
        }
        
        return elementModifiers;
    }
    
    /**
     * 通过事件应用转换后的元素修饰符
     * @param event ItemAttributeModifierEvent事件
     * @param elementModifiers ElementAttributeModifierEntry列表
     */
    private static void applyElementModifiersViaEvent(
            ItemAttributeModifierEvent event, List<ElementAttributeModifierEntry> elementModifiers) {
        
        for (ElementAttributeModifierEntry modifierEntry : elementModifiers) {
            try {
                // 从修饰符数据获取元素类型
                ElementType elementType = modifierEntry.getElementType();
                if (elementType == null) {
                    continue;
                }
                
                // 获取对应的元素属性
                var attributeRegistry = ElementRegistry.getAttribute(elementType);
                if (attributeRegistry == null || !attributeRegistry.isPresent()) {
                    continue;
                }
                
                // 创建Minecraft AttributeModifier
                net.minecraft.world.entity.ai.attributes.AttributeModifier minecraftModifier = 
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        modifierEntry.getId(),
                        modifierEntry.getName(),
                        modifierEntry.getAmount(),
                        modifierEntry.getOperation()
                    );
                
                // 通过事件应用修饰符
                event.addModifier(attributeRegistry.get(), minecraftModifier);
                
            } catch (Exception e) {
                System.err.println("Error applying element modifier for " + modifierEntry.getElementType().getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * 监听物品属性修饰符事件
     * 读取道具InitialModifier层数据，然后转换为修饰符并应用
     */
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        try {
            ItemStack stack = event.getItemStack();
            
            // 防止空指针异常
            if (stack == null || stack.isEmpty()) {
                return;
            }
            
            // 从物品本身的NBT中加载武器数据
            WeaponData weaponData = WeaponDataManager.loadElementData(stack);
            
            // 如果找到了武器数据，则处理元素修饰符
            if (weaponData != null && weaponData.getInitialModifiers() != null) {
                
                // 1. 读取InitialModifier层数据
                List<InitialModifierEntry> initialModifiers = weaponData.getInitialModifiers();
                
                // 2. 转换为ElementAttributeModifierEntry格式
                List<ElementAttributeModifierEntry> elementModifiers = 
                    convertToElementModifierEntries(initialModifiers);
                
                // 3. 通过事件应用转换后的修饰符
                if (!elementModifiers.isEmpty()) {
                    applyElementModifiersViaEvent(event, elementModifiers);
                }
            }
            
        } catch (Exception e) {
            // 记录错误但不抛出异常，避免崩溃
            System.err.println("Error processing element modifiers in onItemAttributeModifier: " + e.getMessage());
            e.printStackTrace();
        }
    }
}