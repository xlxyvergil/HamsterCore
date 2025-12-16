package com.xlxyvergil.hamstercore.events;

import com.xlxyvergil.hamstercore.element.EnchantmentAffixManager;
import dev.shadowsoffire.placebo.events.GetEnchantmentLevelEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "hamstercore", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EnchantmentEventHandler {

    /**
     * 监听铁砧更新事件
     * 当玩家在铁砧上操作物品时触发
     */
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        if (event.getOutput() != null) {
            EnchantmentAffixManager.updateEnchantmentAffixes(event.getOutput());
        }
    }

    /**
     * 监听铁砧修复事件
     * 当玩家在铁砧上修复物品时触发
     */
    @SubscribeEvent
    public static void onAnvilRepair(AnvilRepairEvent event) {
        ItemStack repairedStack = event.getOutput();
        if (!repairedStack.isEmpty()) {
            EnchantmentAffixManager.updateEnchantmentAffixes(repairedStack);
        }
    }

    /**
     * 监听玩家登录事件
     * 当玩家登录游戏时触发
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // 更新玩家所有物品的附魔词缀
        for (ItemStack stack : event.getEntity().getInventory().items) {
            if (!stack.isEmpty()) {
                EnchantmentAffixManager.updateEnchantmentAffixes(stack);
            }
        }
        for (ItemStack stack : event.getEntity().getInventory().armor) {
            if (!stack.isEmpty()) {
                EnchantmentAffixManager.updateEnchantmentAffixes(stack);
            }
        }
        for (ItemStack stack : event.getEntity().getInventory().offhand) {
            if (!stack.isEmpty()) {
                EnchantmentAffixManager.updateEnchantmentAffixes(stack);
            }
        }
    }
    
    /**
     * 监听Placebo的GetEnchantmentLevelEvent事件
     * 当请求物品的附魔等级时触发，用于确保物品上的附魔词缀始终是最新的
     */
    @SubscribeEvent
    public static void onGetEnchantmentLevel(GetEnchantmentLevelEvent event) {
        ItemStack stack = event.getStack();
        if (!stack.isEmpty()) {
            EnchantmentAffixManager.updateEnchantmentAffixes(stack);
        }
    }
}
