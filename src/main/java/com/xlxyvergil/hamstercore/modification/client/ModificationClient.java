package com.xlxyvergil.hamstercore.modification.client;

import java.util.List;
import java.util.Set;

import com.mojang.datafixers.util.Either;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.modification.ModificationInstance;
import com.xlxyvergil.hamstercore.modification.SocketHelper;
import com.xlxyvergil.hamstercore.modification.SocketedModifications;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 改装系统客户端类 - 模仿 Apotheosis 的 AdventureModuleClient
 * 处理改装系统的客户端事件注册和渲染
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModificationClient {

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void modificationTooltips(RenderTooltipEvent.GatherComponents e) {
    }
    
    @SubscribeEvent
    public static void gatherModificationComponents(RenderTooltipEvent.GatherComponents e) {
        int sockets = SocketHelper.getSockets(e.getItemStack());
        int specialSockets = SocketHelper.getSpecialSockets(e.getItemStack());
        if (sockets == 0 && specialSockets == 0) return;
    }

    @SubscribeEvent
    public static void modifyTooltipComponents(RenderTooltipEvent.GatherComponents e) {
        int sockets = SocketHelper.getSockets(e.getItemStack());
        int specialSockets = SocketHelper.getSpecialSockets(e.getItemStack());
        if (sockets == 0 && specialSockets == 0) return;
        
        // 移除标记并添加改装件组件
        List<Either<FormattedText, TooltipComponent>> list = e.getTooltipElements();
        
        // 合并通用槽位和特殊槽位的改装件
        SocketedModifications mods = SocketHelper.getModifications(e.getItemStack());
        java.util.List<ModificationInstance> allMods = new java.util.ArrayList<>(mods.modifications());
        allMods.addAll(SocketHelper.getSpecialModifications(e.getItemStack()));
        
        // 直接添加改装件组件
        list.add(Either.right(new ModificationTooltipRenderer.ModificationComponent(
            e.getItemStack(), 
            new SocketedModifications(allMods)
        )));
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT, modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusSub {
        
        @SubscribeEvent
        public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent e) {
            e.register(ModificationTooltipRenderer.ModificationComponent.class, ModificationTooltipRenderer::new);
        }

        @SubscribeEvent
        public static void addModificationModels(ModelEvent.RegisterAdditional e) {
            Set<ResourceLocation> locs = Minecraft.getInstance().getResourceManager().listResources(
            "models", 
            loc -> HamsterCore.MODID.equals(loc.getNamespace()) && 
                   loc.getPath().contains("/modification/") && 
                   loc.getPath().endsWith(".json")
        ).keySet();
            
            for (ResourceLocation s : locs) {
                String path = s.getPath().substring("models/".length(), s.getPath().length() - ".json".length());
                e.register(new ResourceLocation(HamsterCore.MODID, path));
            }
        }

        @SubscribeEvent
        public static void replaceModificationModel(ModelEvent.ModifyBakingResult e) {
            ModelResourceLocation key = new ModelResourceLocation(
                new ResourceLocation(HamsterCore.MODID, "modification"), 
                "inventory"
            );
            BakedModel oldModel = e.getModels().get(key);
            if (oldModel != null) {
                e.getModels().put(key, new ModificationModel(oldModel, e.getModelBakery()));
            }
        }
    }
}
