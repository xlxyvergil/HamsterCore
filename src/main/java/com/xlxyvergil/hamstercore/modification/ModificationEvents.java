package com.xlxyvergil.hamstercore.modification;

import com.xlxyvergil.hamstercore.modification.event.ItemModificationSocketingEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 改装件系统事件处理
 */
public class ModificationEvents {

    /**
     * 处理怪物掉落改装件 - 参考Apotheosis实现
     * 只对怪物(Monster)掉落，不包括村民等其他实体
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onMonsterDropModification(LivingDropsEvent event) {
        // 检查是否是玩家击杀的
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            // 检查击杀的实体是否是怪物
            if (event.getEntity() instanceof Monster) {
                // 获取掉落概率
                float chance = ModificationConfig.getModificationDropChance();

                // 检查是否是Apotheosis的Boss（添加额外概率）
                if (event.getEntity().getPersistentData().contains("apoth.boss")) {
                    chance += ModificationConfig.getBossBonus();
                }

                if (player.getRandom().nextFloat() <= chance) {
                    ItemStack modification = ModificationRegistry.createRandomModificationStack(
                        player.getRandom(),
                        (ServerLevel) event.getEntity().level(),
                        player.getLuck()
                    );

                    if (!modification.isEmpty()) {
                        event.getDrops().add(new ItemEntity(
                            event.getEntity().level(),
                            event.getEntity().getX(),
                            event.getEntity().getY(),
                            event.getEntity().getZ(),
                            modification
                        ));
                    }
                }
            }
        }
    }

    /**
     * 处理安装配方完成事件
     */
    @SubscribeEvent
    public void onItemSocketing(ItemModificationSocketingEvent event) {
        ItemStack baseStack = event.getBaseStack().copy();
        ItemStack modStack = event.getModificationStack().copy();

        // 获取基础物品的当前改装件
        SocketedModifications currentMods = SocketHelper.getModifications(baseStack);

        // 找到第一个空槽位
        int firstEmptySlot = SocketHelper.getFirstEmptySocket(baseStack);
        if (firstEmptySlot < 0) {
            return;
        }

        // 添加新改装件
        ModificationInstance newMod = ModificationItem.getModification(modStack);
        
        // 检查互斥组
        java.util.List<String> newModGroups = newMod.getMutualExclusionGroups();
        if (!newModGroups.isEmpty()) {
            // 检查现有改装件是否与新改装件在同一互斥组
            for (ModificationInstance existingMod : currentMods.modifications()) {
                if (existingMod.isValid()) {
                    java.util.List<String> existingGroups = existingMod.getMutualExclusionGroups();
                    // 检查是否有交集
                    for (String newGroup : newModGroups) {
                        if (existingGroups.contains(newGroup)) {
                            // 互斥组冲突，取消安装
                            return;
                        }
                    }
                }
            }
        }
        
        List<ModificationInstance> newModifications = new ArrayList<>(currentMods.modifications());
        
        // 确保列表大小足够
        while (newModifications.size() <= firstEmptySlot) {
            newModifications.add(ModificationInstance.EMPTY);
        }
        
        newModifications.set(firstEmptySlot, newMod);

        // 更新改装件列表
        SocketedModifications updatedMods = currentMods.set(firstEmptySlot, newMod);

        // 应用新改装件的词缀
        newMod.applyAffixes(event.getResultStack(), null);

        // 保存到 NBT
        SocketHelper.setModifications(event.getResultStack(), updatedMods.modifications());
    }
}
