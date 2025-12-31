package com.xlxyvergil.hamstercore.modification.event;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;

/**
 * Event fired when {@link com.xlxyvergil.hamstercore.modification.ModificationHelper#getSockets(ItemStack)} is called.<br>
 * This event allows other mods to modify the number of sockets on an item.
 */
public class GetItemSocketsEvent extends Event {
    private final ItemStack stack;
    private int sockets;

    public GetItemSocketsEvent(ItemStack stack, int sockets) {
        this.stack = stack;
        this.sockets = sockets;
    }

    /**
     * Gets the item stack being queried.
     * @return The item stack
     */
    public ItemStack getStack() {
        return this.stack;
    }

    /**
     * Gets the current number of sockets.
     * @return The current number of sockets
     */
    public int getSockets() {
        return this.sockets;
    }

    /**
     * Sets the number of sockets.
     * @param sockets The new number of sockets
     */
    public void setSockets(int sockets) {
        this.sockets = sockets;
    }
}
