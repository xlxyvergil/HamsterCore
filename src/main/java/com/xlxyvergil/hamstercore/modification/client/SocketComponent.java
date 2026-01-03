package com.xlxyvergil.hamstercore.modification.client;

import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record SocketComponent(ItemStack socketed, List<ModificationInstance> modifications) implements TooltipComponent {
}
