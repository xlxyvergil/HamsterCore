package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.element.ElementCalculationCoordinator;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "hamstercore", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ElementCalculationEventHandler {

    /**
     * 监听物品属性修改事件
     * 当物品属性被计算时触发，这是最关键的事件，确保NBT数据在属性计算前生效
     */
    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.isEmpty()) {
            // 获取或创建武器数据
            WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
            if (weaponData != null) {
                // 调用ElementCalculationCoordinator来计算元素值并放入缓存
                ElementCalculationCoordinator.INSTANCE.calculateAndCacheElements(stack, weaponData);
            }
        }
    }

    /**
     * 监听实体加入世界事件
     * 当物品实体、玩家等加入世界时触发
     */
    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof ItemEntity itemEntity) {
            processItemStack(itemEntity.getItem());
        }
    }

    /**
     * 监听实体掉落事件
     * 当实体死亡掉落物品时触发
     */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (event.getEntity() instanceof Monster) {
            event.getDrops().forEach(itemEntity -> {
                processItemStack(itemEntity.getItem());
            });
        }
    }

    /**
     * 监听玩家拾取物品事件
     * 当玩家拾取物品时触发
     */
    @SubscribeEvent
    public static void onEntityItemPickup(EntityItemPickupEvent event) {
        ItemStack stack = event.getItem().getItem();
        processItemStack(stack);
    }

    /**
     * 监听玩家加入游戏事件
     * 当玩家加入游戏时触发，确保玩家携带的物品NBT数据生效
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // 处理玩家所有装备和物品栏中的物品
        for (ItemStack stack : event.getEntity().getInventory().items) {
            processItemStack(stack);
        }
        for (ItemStack stack : event.getEntity().getInventory().armor) {
            processItemStack(stack);
        }
        for (ItemStack stack : event.getEntity().getInventory().offhand) {
            processItemStack(stack);
        }
    }

    /**
     * 处理物品堆的通用方法
     * 获取武器数据并调用ElementCalculationCoordinator计算元素值
     */
    private static void processItemStack(ItemStack stack) {
        if (!stack.isEmpty()) {
            // 获取或创建武器数据
            WeaponData weaponData = WeaponDataManager.getWeaponData(stack);
            if (weaponData != null) {
                // 调用ElementCalculationCoordinator来计算元素值并放入缓存
                ElementCalculationCoordinator.INSTANCE.calculateAndCacheElements(stack, weaponData);
            }
        }
    }
}