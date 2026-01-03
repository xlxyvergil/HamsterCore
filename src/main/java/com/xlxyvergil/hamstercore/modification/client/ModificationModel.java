package com.xlxyvergil.hamstercore.modification.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.modification.ModificationHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * 改装件模型，参考Apotheosis的GemModel实现
 */
@OnlyIn(Dist.CLIENT)
public class ModificationModel implements BakedModel {
    private final BakedModel original;
    private final ItemOverrides overrides;

    public ModificationModel(BakedModel original, ModelBakery loader) {
        this.original = original;
        this.overrides = new ItemOverrides() {
            @Override
            public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
                BakedModel specific = ModificationModel.this.resolve(original, stack, world, entity, seed);
                return specific == original ? specific : specific.getOverrides().resolve(specific, stack, world, entity, seed);
            }
        };
    }

    public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
        // 从ItemStack中获取改装件ID
        String modificationId = ModificationHelper.getModificationId(stack);
        if (modificationId != null) {
            // 构建模型资源位置
            // 模型路径格式：hamstercore:item/modification/{modelPath}
            ResourceLocation modelLocation = new ResourceLocation(
                HamsterCore.MODID,
                "item/modification/" + modificationId
            );
            return Minecraft.getInstance().getModelManager().getModel(modelLocation);
        }
        return original;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pDirection, RandomSource pRandom) {
        return this.original.getQuads(pState, pDirection, pRandom);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.original.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return this.original.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return this.original.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return this.original.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return this.original.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return this.original.getTransforms();
    }
}
