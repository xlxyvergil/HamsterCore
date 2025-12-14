package com.xlxyvergil.hamstercore.handler;

import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.InitialModifierEntry;
import com.xlxyvergil.hamstercore.util.ElementModifierManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * 元素修饰符事件处理器
 * 读取道具InitialModifier层数据，然后转换为符合ElementModifierManager要求的修饰符
 */
@Mod.EventBusSubscriber(modid = "hamstercore")
public class ElementModifierEventHandler {
    
    /**
     * 将InitialModifier层数据转换为ElementModifierData格式
     * @param initialModifiers InitialModifier层数据
     * @return 符合ElementModifierManager要求的修饰符数据列表
     */
    public static List<ElementModifierManager.ElementModifierData> convertToElementModifierData(
            List<InitialModifierEntry> initialModifiers) {
        List<ElementModifierManager.ElementModifierData> elementModifiers = new ArrayList<>();
        
        if (initialModifiers == null || initialModifiers.isEmpty()) {
            return elementModifiers;
        }
        
        for (InitialModifierEntry entry : initialModifiers) {
            try {
                if (entry == null || entry.getModifier() == null) {
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
                
                // 验证元素属性是否已注册
                ElementAttribute elementAttribute = ElementRegistry.getAttribute(elementType);
                if (elementAttribute == null) {
                    System.err.println("Unregistered element attribute: " + elementName);
                    continue;
                }
                
                // 创建符合ElementModifierManager要求的修饰符数据
                ResourceLocation attributeLocation = new ResourceLocation("hamstercore", elementType.getName());
                
                ElementModifierManager.ElementModifierData modifierData = new ElementModifierManager.ElementModifierData(
                    attributeLocation,                           // 属性标识符
                    originalModifier.getId(),                   // 修饰符ID
                    originalModifier.getAmount(),              // 修饰符数值
                    originalModifier.getName(),                // 修饰符名称
                    originalModifier.getOperation()           // 运算模式
                );
                
                elementModifiers.add(modifierData);
                
            } catch (Exception e) {
                System.err.println("Error converting InitialModifierEntry for " + entry.getName() + ": " + e.getMessage());
            }
        }
        
        return elementModifiers;
    }
    
    /**
     * 通过事件应用转换后的元素修饰符
     * @param event ItemAttributeModifierEvent事件
     * @param elementModifiers 转换后的元素修饰符数据
     */
    private static void applyElementModifiersViaEvent(
            ItemAttributeModifierEvent event,
            List<ElementModifierManager.ElementModifierData> elementModifiers) {
        
        for (ElementModifierManager.ElementModifierData modifierData : elementModifiers) {
            try {
                // 从属性标识符获取元素类型
                String elementName = modifierData.getAttribute().getPath();
                ElementType elementType = ElementType.byName(elementName);
                if (elementType == null) {
                    continue;
                }
                
                // 获取对应的元素属性
                ElementAttribute elementAttribute = ElementRegistry.getAttribute(elementType);
                if (elementAttribute == null) {
                    continue;
                }
                
                // 创建Minecraft AttributeModifier
                net.minecraft.world.entity.ai.attributes.AttributeModifier minecraftModifier = 
                    new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                        modifierData.getId(),
                        modifierData.getName(),
                        modifierData.getAmount(),
                        modifierData.getOperation()
                    );
                
                // 通过事件应用修饰符
                event.addModifier(elementAttribute, minecraftModifier);
                
            } catch (Exception e) {
                System.err.println("Error applying element modifier for " + modifierData.getAttribute() + ": " + e.getMessage());
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
                
                // 2. 转换为符合ElementModifierManager要求的格式
                List<ElementModifierManager.ElementModifierData> elementModifiers = 
                    convertToElementModifierData(initialModifiers);
                
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