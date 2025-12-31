package com.xlxyvergil.hamstercore.modification.client;

import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record SocketComponent(ItemStack socketed, List<ModificationInstance> modifications) implements TooltipComponent {
}
