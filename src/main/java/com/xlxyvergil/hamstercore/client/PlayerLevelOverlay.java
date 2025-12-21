package com.xlxyvergil.hamstercore.client;

import com.xlxyvergil.hamstercore.HamsterCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class PlayerLevelOverlay {
    
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("player_level_hud", (gui, guiGraphics, partialTick, width, height) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.options.hideGui && gui.shouldDrawSurvivalElements()) {
                LocalPlayer player = minecraft.player;
                if (player != null) {
                    PlayerLevelHUDRenderer.renderPlayerLevelHUD(gui, guiGraphics, player, width, height);
                }
            }
        });
    }
}