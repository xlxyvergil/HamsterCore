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
import net.minecraft.network.chat.ComponentContents;
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
    public static void modificationTooltips(net.minecraftforge.event.entity.player.ItemTooltipEvent e) {
        ItemStack stack = e.getItemStack();
        
        // 显示改装件槽位信息
        int sockets = SocketHelper.getSockets(stack);
        if (sockets > 0) {
            SocketedModifications modifications = SocketHelper.getModifications(stack);
            
            // 添加槽位数量提示
            int installed = (int) modifications.streamValidModifications().count();
            
            e.getToolTip().add(Component.literal("Sockets: " + installed + "/" + sockets)
                .withStyle(net.minecraft.ChatFormatting.GRAY));
            
            // 添加改装件信息
            for (ModificationInstance inst : modifications.modifications()) {
                if (inst.isValid()) {
                    Component desc = inst.getDescription();
                    if (desc.getContents() != ComponentContents.EMPTY) {
                        e.getToolTip().add(Component.literal("  ").append(desc)
                            .withStyle(net.minecraft.ChatFormatting.YELLOW));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void gatherModificationComponents(RenderTooltipEvent.GatherComponents e) {
        int sockets = SocketHelper.getSockets(e.getItemStack());
        if (sockets == 0) return;
    }

    @SubscribeEvent
    public static void modifyTooltipComponents(RenderTooltipEvent.GatherComponents e) {
        int sockets = SocketHelper.getSockets(e.getItemStack());
        if (sockets == 0) return;
        
        // 移除标记并添加改装件组件
        List<Either<FormattedText, TooltipComponent>> list = e.getTooltipElements();
        
        // 直接添加改装件组件
        list.add(Either.right(new ModificationTooltipRenderer.ModificationComponent(
            e.getItemStack(), 
            SocketHelper.getModifications(e.getItemStack())
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
                       loc.getPath().contains("/modifications/") && 
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
