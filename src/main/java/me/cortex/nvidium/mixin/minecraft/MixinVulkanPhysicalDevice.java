package me.cortex.nvidium.mixin.minecraft;

import com.mojang.renderpearl.backend.vulkan.VulkanPhysicalDevice;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import me.cortex.nvidium.vk.IGraphicsQueueSparse;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.lwjgl.vulkan.VkQueueFamilyProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.lwjgl.vulkan.VK10.VK_QUEUE_SPARSE_BINDING_BIT;
import static org.lwjgl.vulkan.VK10.vkGetPhysicalDeviceQueueFamilyProperties;

/**
 * Nvidium binds sparse buffers on the graphics queue. Instead of forcing Minecraft to only pick queue families with
 * sparse binding (which could make Minecraft itself unable to start on some GPUs), we just record whether the queue
 * family that was picked supports it and disable Nvidium otherwise.
 */
@Mixin(VulkanPhysicalDevice.class)
public abstract class MixinVulkanPhysicalDevice implements IGraphicsQueueSparse {
    @Shadow
    public abstract VkPhysicalDevice vkPhysicalDevice();

    @Shadow
    public abstract IntIntPair graphicsQueueFamilyAndIndex();

    @Unique
    private boolean nvidium$graphicsQueueSparse;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void nvidium$checkGraphicsQueueSparse(VkPhysicalDevice device, CallbackInfo ci) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int[] count = new int[1];
            vkGetPhysicalDeviceQueueFamilyProperties(device, count, null);
            VkQueueFamilyProperties.Buffer families = VkQueueFamilyProperties.calloc(count[0], stack);
            vkGetPhysicalDeviceQueueFamilyProperties(device, count, families);
            int family = this.graphicsQueueFamilyAndIndex().firstInt();
            this.nvidium$graphicsQueueSparse = family >= 0 && family < count[0]
                    && (families.get(family).queueFlags() & VK_QUEUE_SPARSE_BINDING_BIT) != 0;
        }
    }

    @Override
    public boolean nvidium$graphicsQueueSupportsSparse() {
        return this.nvidium$graphicsQueueSparse;
    }
}
