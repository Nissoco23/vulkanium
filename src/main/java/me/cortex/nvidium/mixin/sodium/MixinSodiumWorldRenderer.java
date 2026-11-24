package me.cortex.nvidium.mixin.sodium;

import com.mojang.renderpearl.api.textures.GpuSampler;
import me.cortex.nvidium.Nvidium;
import me.cortex.nvidium.NvidiumWorldRenderer;
import me.cortex.nvidium.sodiumCompat.INvidiumWorldRendererGetter;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.render.chunk.ChunkRenderMatrices;
import net.caffeinemc.mods.sodium.client.render.chunk.RenderSectionManager;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.caffeinemc.mods.sodium.client.render.viewport.Viewport;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.oit.OitStage;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class MixinSodiumWorldRenderer implements INvidiumWorldRendererGetter {
    @Shadow
    private RenderSectionManager renderSectionManager;
    @Unique
    private Viewport viewport;

    @Override
    public NvidiumWorldRenderer getRenderer() {
        if (Nvidium.IS_ENABLED) {
            return ((INvidiumWorldRendererGetter) renderSectionManager).getRenderer();
        } else {
            return null;
        }
    }

    @Inject(method = "setupTerrain", at = @At(value = "HEAD"))
    public void trackViewport(Camera camera, Viewport viewport, FogParameters fogParameters, boolean spectator, boolean updateChunksImmediately, Matrix4f cullMatrix, CallbackInfo ci) {
        this.viewport = viewport;
    }

    @Inject(method = "renderLayer", at = @At(value = "HEAD"), cancellable = true)
    public void renderLayer(ChunkRenderMatrices matrices, TerrainRenderPass pass, double x, double y, double z, FogParameters fogParameters, GpuSampler terrainSampler, RenderPass renderPass, OitStage stage, CallbackInfo ci) {
        if (Nvidium.IS_ENABLED) {
            ci.cancel();

            // CUTOUT IS WITH SOLID
            if (pass != DefaultTerrainRenderPasses.SOLID && pass != DefaultTerrainRenderPasses.TRANSLUCENT) {
                return;
            }

            // TODO 26.3 order independent transparency (Improved Transparency, stage != null) is not implemented on
            //  the Vulkan path yet, translucent terrain is only drawn by the traditional blended pass.
            if (stage != null) {
                return;
            }

            // Minecraft 26.3 opens a render pass before calling us. Nvidium needs to run compute dispatches, barriers
            // and its own render passes (occlusion depth buffer), none of which are allowed while a pass is open and
            // a new pass can't be created before the current one is closed. Closing is idempotent
            // (FrontendRenderPass.isClosed), so the caller closing it again afterwards is a no-op.
            renderPass.close();

            if (pass == DefaultTerrainRenderPasses.SOLID) {
                this.getRenderer().renderFrame(pass, viewport, fogParameters, matrices, x, y, z, terrainSampler);
            } else {
                this.getRenderer().renderTranslucent(pass, terrainSampler);
            }
        }
    }
}
