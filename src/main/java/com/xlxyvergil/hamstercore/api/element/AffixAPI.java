package com.xlxyvergil.hamstercore.api.element;

import com.xlxyvergil.hamstercore.element.AffixManager;
import com.xlxyvergil.hamstercore.element.InitialModifierEntry;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

/**
 * 词缀系统API接口
 * 允许其他mod添加和删除词缀
 */
public class AffixAPI {
    /**
     * 为物品添加词缀
     * @param stack 物品栈
     * @param name 词缀名称
     * @param elementType 词缀类型（需提前注册）
     * @param amount 词缀数值
     * @param operation 操作类型
     * @param uuid 唯一标识
     * @param source 来源（通常是modid）
     */
    public static void addAffix(ItemStack stack, String name, String elementType, double amount, String operation, UUID uuid, String source) {
        AffixManager.addAffix(stack, name, elementType, amount, operation, uuid, source);
    }

    /**
     * 从物品上删除词缀
     * @param stack 物品栈
     * @param uuid 词缀唯一标识
     */
    public static void removeAffix(ItemStack stack, UUID uuid) {
        AffixManager.removeAffix(stack, uuid);
    }

    /**
     * 批量为物品添加词缀
     * @param stack 物品栈
     * @param entries 词缀条目列表
     */
    public static void batchAddAffixes(ItemStack stack, List<InitialModifierEntry> entries) {
        AffixManager.batchAddAffixes(stack, entries);
    }
    
    /**
     * 批量为物品删除词缀
     * @param stack 物品栈
     * @param uuids 词缀唯一标识列表
     */
    public static void batchRemoveAffixes(ItemStack stack, List<UUID> uuids) {
        AffixManager.batchRemoveAffixes(stack, uuids);
    }
}