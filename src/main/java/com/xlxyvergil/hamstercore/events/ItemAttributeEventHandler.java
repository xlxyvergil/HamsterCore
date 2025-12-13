package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.handler.ElementModifierEventHandler;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.TradeWithVillagerEvent;
// ChunkLoadEvent导入已移除，因为未使用
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 物品属性事件处理器
 * 处理各种情况下的物品属性修饰符应用
 */
@Mod.EventBusSubscriber(modid = "hamstercore")
public class ItemAttributeEventHandler {

    /**
     * 处理实体加入世界事件
     * 确保物品实体能正确应用元素修饰符
     */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // 处理掉落物实体
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getItem();
            if (shouldApplyElementModifiers(stack)) {
                // 触发一次属性修饰符计算，确保物品有正确的属性
                stack.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
        }
    }

    /**
     * 处理玩家获取物品事件
     * 包括从创造模式物品栏、其他来源获取物品
     */
    @SubscribeEvent
    public static void onItemPickup(PlayerEvent.ItemPickupEvent event) {
        ItemStack stack = event.getStack();
        if (shouldApplyElementModifiers(stack)) {
            // 触发一次属性修饰符计算
            stack.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
    }

    /**
     * 处理村民交易事件
     * 确保通过村民交易获得的物品能正确应用元素修饰符
     */
    @SubscribeEvent
    public static void onTradeWithVillager(TradeWithVillagerEvent event) {
        ItemStack result = event.getMerchantOffer().getResult();
        if (shouldApplyElementModifiers(result)) {
            // 触发一次属性修饰符计算
            result.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
    }

    /*
     * 处理玩家打开物品栏事件已移除
     * 当玩家打开物品栏时重新计算物品属性
     */
    /*
    @SubscribeEvent
    public static void onPlayerOpenContainer(PlayerEvent.OpenContainer event) {
        Player player = event.getEntity();
        // 遍历玩家背包中的物品
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (shouldApplyElementModifiers(stack)) {
                // 触发一次属性修饰符计算
                stack.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
            }
        }
    }
    */

    /**
     * 处理物品合成事件
     * 确保工作台合成的物品能正确应用元素修饰符
     */
    @SubscribeEvent
    public static void onItemCrafted(PlayerEvent.ItemCraftedEvent event) {
        ItemStack result = event.getCrafting();
        if (shouldApplyElementModifiers(result)) {
            // 触发一次属性修饰符计算
            result.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
    }

    /**
     * 处理附魔事件
     * 确保附魔后的物品能正确应用元素修饰符
     */
    @SubscribeEvent
    public static void onEnchantmentLevelSet(net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent event) {
        ItemStack itemStack = event.getItem();
        if (shouldApplyElementModifiers(itemStack)) {
            // 触发一次属性修饰符计算
            itemStack.getAttributeModifiers(net.minecraft.world.entity.EquipmentSlot.MAINHAND);
        }
    }

    /**
     * 判断是否应该应用元素修饰符
     * @param stack 物品堆
     * @return 是否应该应用元素修饰符
     */
    private static boolean shouldApplyElementModifiers(ItemStack stack) {
        return stack.hasTag() && stack.getTag() != null && stack.getTag().contains("element_data");
    }
}