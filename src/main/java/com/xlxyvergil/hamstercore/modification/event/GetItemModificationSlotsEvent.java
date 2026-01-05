package com.xlxyvergil.hamstercore.modification.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;


/**
 * 获取物品改装槽位事件 - 模仿 Apotheosis 的 GetItemSocketsEvent
 */
public class GetItemModificationSlotsEvent extends Event {

    private final ItemStack stack;
    private int sockets;

    public GetItemModificationSlotsEvent(ItemStack stack, int sockets) {
        this.stack = stack;
        this.sockets = sockets;
    }

    public ItemStack getStack() {
        return stack;
    }

    public int getSockets() {
        return sockets;
    }

    public void setSockets(int sockets) {
        this.sockets = sockets;
    }
}
