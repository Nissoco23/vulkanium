package me.cortex.nvidium.mixin.minecraft;

import com.mojang.renderpearl.backend.vulkan.VulkanFeatureSets;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import com.mojang.renderpearl.backend.vulkan.init.VulkanFeature;
import com.mojang.renderpearl.backend.vulkan.init.VulkanPNextStruct;
import org.lwjgl.vulkan.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

/** Registers Nvidium's Vulkan requirements as optional FeatureSets of Minecraft's Vulkan backend. */
@Mixin(VulkanFeatureSets.class)
public class MixinVulkanFeatureSets {
    private static final VulkanPNextStruct MESH_SHADER_STRUCT = new VulkanPNextStruct(VkPhysicalDeviceMeshShaderFeaturesEXT.class);
    private static final VulkanPNextStruct BARYCENTRIC_STRUCT = new VulkanPNextStruct(VkPhysicalDeviceFragmentShaderBarycentricFeaturesKHR.class);
    private static final VulkanPNextStruct REPRESENTATIVE_FRAGMENT_TEST_STRUCT = new VulkanPNextStruct(VkPhysicalDeviceRepresentativeFragmentTestFeaturesNV.class);

    public static final FeatureSet NVIDIUM_MESH_SHADER = new FeatureSet(
            "Nvidium mesh shaders",
            Set.of(EXTMeshShader.VK_EXT_MESH_SHADER_EXTENSION_NAME),
            Set.of(
                    new VulkanFeature(VulkanFeatureSets.VK10_FEATURES_STRUCT, "shaderInt64"),
                    new VulkanFeature(VulkanFeatureSets.VK10_FEATURES_STRUCT, "shaderInt16"),
                    new VulkanFeature(VulkanFeatureSets.VK10_FEATURES_STRUCT, "sparseBinding"),
                    new VulkanFeature(VulkanFeatureSets.VK10_FEATURES_STRUCT, "sparseResidencyBuffer"),
                    new VulkanFeature(VulkanFeatureSets.VK11_FEATURES_STRUCT, "storageBuffer16BitAccess"),
                    new VulkanFeature(VulkanFeatureSets.VK11_FEATURES_STRUCT, "uniformAndStorageBuffer16BitAccess"),
                    new VulkanFeature(VulkanFeatureSets.VK12_FEATURES_STRUCT, "shaderInt8"),
                    new VulkanFeature(VulkanFeatureSets.VK12_FEATURES_STRUCT, "storageBuffer8BitAccess"),
                    new VulkanFeature(VulkanFeatureSets.VK12_FEATURES_STRUCT, "uniformAndStorageBuffer8BitAccess"),
                    new VulkanFeature(VulkanFeatureSets.VK12_FEATURES_STRUCT, "bufferDeviceAddress"),
                    new VulkanFeature(MESH_SHADER_STRUCT, "meshShader"),
                    new VulkanFeature(MESH_SHADER_STRUCT, "taskShader")
            )
    );

    public static final FeatureSet NVIDIUM_BARYCENTRIC = new FeatureSet(
            "Nvidium fragment shader barycentric",
            Set.of(KHRFragmentShaderBarycentric.VK_KHR_FRAGMENT_SHADER_BARYCENTRIC_EXTENSION_NAME),
            Set.of(new VulkanFeature(BARYCENTRIC_STRUCT, "fragmentShaderBarycentric"))
    );

    public static final FeatureSet NVIDIUM_REPRESENTATIVE_FRAGMENT_TEST = new FeatureSet(
            "Nvidium representative fragment test",
            Set.of(NVRepresentativeFragmentTest.VK_NV_REPRESENTATIVE_FRAGMENT_TEST_EXTENSION_NAME),
            Set.of(new VulkanFeature(REPRESENTATIVE_FRAGMENT_TEST_STRUCT, "representativeFragmentTest"))
    );

    @Inject(method = "optionalFeatureSets", at = @At("RETURN"), cancellable = true)
    private static void nvidium$addOptionalFeatureSets(CallbackInfoReturnable<Set<FeatureSet>> cir) {
        Set<FeatureSet> sets = new HashSet<>(cir.getReturnValue());
        sets.add(NVIDIUM_MESH_SHADER);
        sets.add(NVIDIUM_BARYCENTRIC);
        sets.add(NVIDIUM_REPRESENTATIVE_FRAGMENT_TEST);
        cir.setReturnValue(sets);
    }
}
