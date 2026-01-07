package com.xlxyvergil.hamstercore.modification.client;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.modification.Modification;
import com.xlxyvergil.hamstercore.modification.ModificationItem;
import com.xlxyvergil.hamstercore.modification.ModificationRegistry;

import dev.shadowsoffire.placebo.reload.DynamicHolder;
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
 * 改装件模型类
 * 根据改装件ID动态加载对应的JSON模型
 */
@OnlyIn(Dist.CLIENT)
public class ModificationModel implements BakedModel {
    private final BakedModel original;
    private final ItemOverrides overrides;

    @SuppressWarnings("deprecation")
    public ModificationModel(BakedModel original, ModelBakery loader) {
        this.original = original;
        this.overrides = new ItemOverrides(){
            @Override
            public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
                BakedModel specific = ModificationModel.this.resolve(original, stack, world, entity, seed);
                return specific == original ? specific : specific.getOverrides().resolve(specific, stack, world, entity, seed);
            }
        };
    }

    public BakedModel resolve(BakedModel original, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
        String id = ModificationItem.getModificationId(stack);
        if (id != null && !id.isEmpty()) {
            DynamicHolder<Modification> holder = ModificationRegistry.INSTANCE.holder(ResourceLocation.parse(id));
            if (holder.isBound()) {
                // 加载对应的改装件模型
                return Minecraft.getInstance().getModelManager().getModel(ResourceLocation.parse(HamsterCore.MODID + ":item/modification/" + holder.getId().getPath()));
            }
        }
        return original;
    }

    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    @Override
    @Deprecated
    public List<BakedQuad> getQuads(BlockState pState, Direction pDirection, RandomSource pRandom) {
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
    @Deprecated
    public TextureAtlasSprite getParticleIcon() {
        return this.original.getParticleIcon();
    }

    @Override
    @Deprecated
    public ItemTransforms getTransforms() {
        return this.original.getTransforms();
    }
}
