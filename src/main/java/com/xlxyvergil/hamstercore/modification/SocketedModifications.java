package com.xlxyvergil.hamstercore.modification;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * 已安装改装件集合
 */
public class SocketedModifications {
    private final List<ModificationInstance> modifications;

    public SocketedModifications(List<ModificationInstance> modifications) {
        this.modifications = modifications;
    }

    /**
     * 获取改装件列表
     */
    public List<ModificationInstance> modifications() {
        return modifications;
    }

    /**
     * 获取改装件数量
     */
    public int size() {
        return modifications.size();
    }

    /**
     * 获取有效的改装件流
     */
    public java.util.stream.Stream<ModificationInstance> streamValidModifications() {
        return modifications.stream().filter(ModificationInstance::isValid);
    }
    
    /**
     * 获取改装件流
     */
    public java.util.stream.Stream<ModificationInstance> stream() {
        return modifications.stream();
    }

    /**
     * 获取指定索引的改装件
     */
    public ModificationInstance get(int index) {
        if (index < 0 || index >= modifications.size()) {
            return ModificationInstance.EMPTY;
        }
        return modifications.get(index);
    }

    /**
     * 设置指定索引的改装件
     */
    public SocketedModifications set(int index, ModificationInstance instance) {
        List<ModificationInstance> newMods = new java.util.ArrayList<>(this.modifications);
        newMods.set(index, instance);
        return new SocketedModifications(newMods);
    }

    /**
     * 应用所有改装件的词缀
     */
    public void applyAllModifications(ItemStack stack, net.minecraft.world.entity.player.Player player) {
        for (ModificationInstance inst : modifications) {
            if (inst.isValid()) {
                inst.applyAffixes(stack, player);
            }
        }
    }

    /**
     * 移除所有改装件的词缀
     */
    public void removeAllAffixes(ItemStack stack) {
        for (ModificationInstance inst : modifications) {
            if (inst.isValid()) {
                inst.removeAffixes(stack);
            }
        }
    }
}
