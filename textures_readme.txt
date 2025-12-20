护盾条纹理文件说明
==================

为了使用纹理图片渲染护盾条，您需要创建以下四个纹理文件：

1. shield_bar.png (护盾条填充纹理)
   - 路径: src/main/resources/assets/hamstercore/textures/gui/shield_bar.png
   - 大小: 81x4 像素
   - 内容: 蓝色渐变纹理，用于显示护盾条的当前值
   - 示例颜色: 从浅蓝 (0, 0, 255, 200) 到深蓝 (0, 0, 180, 200) 的水平渐变

2. shield_frame.png (护盾条边框纹理)
   - 路径: src/main/resources/assets/hamstercore/textures/gui/shield_frame.png
   - 大小: 82x6 像素
   - 内容: 护盾条的边框，外围蓝色，内部深灰色
   - 示例设计:
     * 外围边框 (像素0和81, 0和5): 蓝色 (0, 0, 255, 255)
     * 内部边框 (像素1和80, 1和4): 深灰色 (64, 64, 64, 255)
     * 内部区域 (像素2-79, 2-3): 透明或深灰色背景

3. shield_bar_gating.png (护盾保险状态下的填充纹理)
   - 路径: src/main/resources/assets/hamstercore/textures/gui/shield_bar_gating.png
   - 大小: 81x4 像素
   - 内容: 护盾保险状态下的填充纹理，可以使用金色或其他醒目的颜色

4. shield_frame_gating.png (护盾保险状态下的边框纹理)
   - 路径: src/main/resources/assets/hamstercore/textures/gui/shield_frame_gating.png
   - 大小: 82x6 像素
   - 内容: 护盾保险状态下的边框纹理，可以使用金色边框

注意：只有玩家才有护盾保险机制，实体头顶的护盾显示不会使用_gating.png纹理文件。

您可以使用任何图像编辑软件（如 Photoshop、GIMP 或在线工具）创建这些纹理文件。