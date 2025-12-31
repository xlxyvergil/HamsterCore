package com.xlxyvergil.hamstercore.modification.client;

import com.mojang.datafixers.util.Either;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.modification.ModificationHelper;
import com.xlxyvergil.hamstercore.modification.SocketedModifications;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = HamsterCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModificationClientEvents {

    @SubscribeEvent
    public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(SocketComponent.class, SocketTooltipRenderer::new);
    }

    @Mod.EventBusSubscriber(modid = HamsterCore.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeBusSub {

        @SubscribeEvent
        public static void gatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
            ItemStack stack = event.getItemStack();
            int sockets = ModificationHelper.getSockets(stack);
            if (sockets == 0) return;

            SocketedModifications modifications = ModificationHelper.getModifications(stack);
            
            // 直接使用SocketedModifications，它已经包含所有槽位
            event.getTooltipElements().add(event.getTooltipElements().size(), 
                Either.right(new SocketComponent(stack, modifications.modifications())));
        }
    }
}
