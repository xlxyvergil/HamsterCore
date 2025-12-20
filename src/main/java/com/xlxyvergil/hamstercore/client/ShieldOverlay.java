package com.xlxyvergil.hamstercore.client;

import com.xlxyvergil.hamstercore.HamsterCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = HamsterCore.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ShieldOverlay {
    
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("shield_hud", (gui, guiGraphics, partialTick, width, height) -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (!minecraft.options.hideGui && gui.shouldDrawSurvivalElements()) {
                LocalPlayer player = minecraft.player;
                if (player != null && !player.isCreative() && !player.isSpectator()) {
                    ShieldHUDUpdater.renderShieldHUD(gui, guiGraphics, player, width, height);
                }
            }
        });
    }
}