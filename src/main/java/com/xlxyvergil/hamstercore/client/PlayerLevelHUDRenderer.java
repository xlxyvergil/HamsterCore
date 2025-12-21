package com.xlxyvergil.hamstercore.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.content.capability.PlayerLevelCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class PlayerLevelHUDRenderer {
    
    // 经验条纹理资源位置
    private static final ResourceLocation EXPERIENCE_BAR = 
        ResourceLocation.fromNamespaceAndPath(HamsterCore.MOD_ID, "textures/gui/experience_bar.png");
    private static final ResourceLocation EXPERIENCE_FRAME = 
        ResourceLocation.fromNamespaceAndPath(HamsterCore.MOD_ID, "textures/gui/experience_frame.png");
    
    @SubscribeEvent
    public static void onRenderGameOverlay(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.renderDebug || mc.screen != null) {
            return;
        }
        
        Player player = mc.player;
        if (player == null) return;
        
        // 获取玩家等级
        player.getCapability(PlayerLevelCapability.CAPABILITY).ifPresent(cap -> {
            int playerLevel = cap.getPlayerLevel();
            
            // 设置渲染位置（快捷道具栏左侧，距离1像素）
            int x = event.getWindow().getGuiScaledWidth() / 2 - 91 - 1 - 5; // 91是快捷道具栏左侧到屏幕中心的距离，再减去经验条宽度和间距
            int y = event.getWindow().getGuiScaledHeight() - 29 - 22; // 与快捷道具栏顶部对齐，22是物品栏高度
            
            // 渲染等级文本
            String levelText = "Lv." + playerLevel + "/30";
            Font font = mc.font;
            
            // 绘制文本（在经验条上方）
            GuiGraphics guiGraphics = event.getGuiGraphics();
            guiGraphics.drawString(font, Component.literal(levelText), x - font.width(levelText) / 2 + 2, y - 12, 0xFFFFFF, false);
            
            // 渲染经验进度条（竖直）
            renderExperienceBar(guiGraphics, cap, x, y);
        });
    }
    
    private static void renderExperienceBar(GuiGraphics guiGraphics, PlayerLevelCapability cap, int x, int y) {
        // 获取当前等级经验和升级所需经验
        int currentExp = cap.getCurrentLevelExperience();
        int expToNext = cap.getNextLevelExperience();
        
        // 计算经验条的填充比例
        float fillRatio = expToNext > 0 ? (float) currentExp / expToNext : 0;
        
        // 经验条背景框架（竖直放置，5x22像素）
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, EXPERIENCE_FRAME);
        guiGraphics.blit(EXPERIENCE_FRAME, x, y, 0, 0, 5, 22, 10, 22);
        
        // 经验条填充（从下往上填充）
        int fillHeight = (int) (22 * fillRatio);
        if (fillHeight > 0) {
            RenderSystem.setShaderTexture(0, EXPERIENCE_BAR);
            // 从底部开始绘制，所以需要调整y坐标
            guiGraphics.blit(EXPERIENCE_BAR, x, y + 22 - fillHeight, 5, 22 - fillHeight, 5, fillHeight, 10, 22);
        }
    }
}