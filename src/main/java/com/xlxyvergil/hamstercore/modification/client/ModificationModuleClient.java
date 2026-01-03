package com.xlxyvergil.hamstercore.modification.client;

import java.util.Set;

import com.xlxyvergil.hamstercore.HamsterCore;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 改装件客户端模块，参考Apotheosis的AdventureModuleClient实现
 */
public class ModificationModuleClient {

    @Mod.EventBusSubscriber(modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModBusSub {
        
        @SubscribeEvent
        public static void addModificationModels(ModelEvent.RegisterAdditional e) {
            // 自动注册所有models/item/modification/目录下的模型
            Set<ResourceLocation> locs = Minecraft.getInstance().getResourceManager().listResources("models", 
                loc -> HamsterCore.MODID.equals(loc.getNamespace()) && loc.getPath().contains("/modification/") && loc.getPath().endsWith(".json")
            ).keySet();
            
            for (ResourceLocation s : locs) {
                String path = s.getPath().substring("models/".length(), s.getPath().length() - ".json".length());
                e.register(new ResourceLocation(HamsterCore.MODID, path));
            }
        }
        
        @SubscribeEvent
        public static void replaceModificationModel(ModelEvent.ModifyBakingResult e) {
            // 替换默认的改装件模型为我们的自定义模型
            ModelResourceLocation key = new ModelResourceLocation(new ResourceLocation(HamsterCore.MODID, "modification"), "inventory");
            if (e.getModels().containsKey(key)) {
                e.getModels().put(key, new ModificationModel(e.getModels().get(key), e.getModelBakery()));
            }
        }
    }
}
