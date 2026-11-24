package me.cortex.nvidium.mixin.minecraft;

import com.mojang.renderpearl.backend.vulkan.VulkanBackend;
import com.mojang.renderpearl.backend.vulkan.VulkanFeatureSets;
import com.mojang.renderpearl.backend.vulkan.VulkanPhysicalDevice;
import com.mojang.renderpearl.backend.vulkan.init.FeatureSet;
import com.mojang.renderpearl.backend.vulkan.init.VulkanFeature;
import com.mojang.renderpearl.backend.vulkan.init.VulkanPNextStruct;
import me.cortex.nvidium.Nvidium;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.vulkan.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.util.vma.Vma.VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT;

/**
 * Nvidium integrates with Minecraft 26.3's FeatureSet based device creation: the mesh shader requirements are
 * registered as an *optional* FeatureSet (see {@link MixinVulkanFeatureSets}), so GPUs without mesh shader support
 * keep working as normal and simply run without Nvidium. Here we find out which of our sets were actually enabled.
 */
@Mixin(VulkanBackend.class)
public class MixinVulkanBackend {
    @Inject(
            method = "createDevice(Lcom/mojang/renderpearl/backend/vulkan/init/FeatureSet;Lcom/mojang/renderpearl/backend/vulkan/VulkanPhysicalDevice;)Lorg/lwjgl/vulkan/VkDevice;",
            at = @At("HEAD")
    )
    private static void nvidium$detectEnabledFeatures(FeatureSet enabled, VulkanPhysicalDevice physicalDevice, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<VkDevice> cir) {
        Nvidium.IS_COMPATIBLE = enabled.contains(MixinVulkanFeatureSets.NVIDIUM_MESH_SHADER);
        Nvidium.IS_ENABLED = Nvidium.IS_COMPATIBLE;
        if (!Nvidium.IS_COMPATIBLE) {
            Nvidium.LOGGER.info("Mesh shader FeatureSet not enabled on this device, disabling Nvidium");
            return;
        }

        Nvidium.SUPPORT_VK_KHR_fragment_shader_barycentric = enabled.contains(MixinVulkanFeatureSets.NVIDIUM_BARYCENTRIC);
        Nvidium.SUPPORT_NV_representative_fragment_test = enabled.contains(MixinVulkanFeatureSets.NVIDIUM_REPRESENTATIVE_FRAGMENT_TEST);
        Nvidium.LOGGER.info("Enabling Nvidium: VK_EXT_mesh_shader, barycentric={}, representativeFragmentTest={}",
                Nvidium.SUPPORT_VK_KHR_fragment_shader_barycentric, Nvidium.SUPPORT_NV_representative_fragment_test);

        Nvidium.SUBGROUP_SIZE = physicalDevice.vkPhysicalDeviceVulkan11Properties().subgroupSize();
        Nvidium.LOGGER.info("SubGroup size is {}", Nvidium.SUBGROUP_SIZE);
        Nvidium.config.automatic_memory = false; // TODO not supported on the Vulkan backend yet
    }

    @ModifyArg(method = "createVma(Lorg/lwjgl/vulkan/VkDevice;)J", at = @At(value = "INVOKE", target = "Lorg/lwjgl/util/vma/Vma;vmaCreateAllocator(Lorg/lwjgl/util/vma/VmaAllocatorCreateInfo;Lorg/lwjgl/PointerBuffer;)I"), index = 0)
    private static VmaAllocatorCreateInfo nvidium$enableVMABDA(VmaAllocatorCreateInfo createInfo) {
        if (Nvidium.IS_ENABLED) {
            Nvidium.LOGGER.info("Enabling Vulkan VMA buffer device address");
            createInfo.flags(createInfo.flags() | VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT);
        }
        return createInfo;
    }
}
