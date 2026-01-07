package com.xlxyvergil.hamstercore.modification.event;

import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

/**
 * 改装件安装事件
 */
public class ItemModificationSocketingEvent extends Event {

    private final ItemStack baseStack;
    private final ItemStack modificationStack;
    private final List<ModificationInstance> newModifications;
    private final ItemStack resultStack;

    public ItemModificationSocketingEvent(ItemStack baseStack, ItemStack modificationStack, 
                                    List<ModificationInstance> newModifications, ItemStack resultStack) {
        this.baseStack = baseStack;
        this.modificationStack = modificationStack;
        this.newModifications = newModifications;
        this.resultStack = resultStack;
    }

    public ItemStack getBaseStack() {
        return baseStack;
    }

    public ItemStack getModificationStack() {
        return modificationStack;
    }

    public List<ModificationInstance> getNewModifications() {
        return newModifications;
    }

    public ItemStack getResultStack() {
        return resultStack;
    }

    /**
     * 是否允许安装
     */
    public boolean isAllowed() {
        return true;
    }

    /**
     * 是否被拒绝
     */
    public boolean isDenied() {
        return false;
    }
}
