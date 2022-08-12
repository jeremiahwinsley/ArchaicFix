package org.embeddedt.archaicfix.mixins.client.threadedupdates;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.client.ForgeHooksClient;
import org.embeddedt.archaicfix.helpers.ThreadedChunkUpdateHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderBlocks.class)
public class MixinRenderBlocks {

    @Inject(method = "renderBlockByRenderType", at = @At("HEAD"), cancellable = true)
    private void cancelRenderDelegatedToDifferentThread(Block block, int x, int y, int z, CallbackInfoReturnable<Boolean> cir) {
        boolean offThreadBlock = ThreadedChunkUpdateHelper.canBlockBeRenderedOffThread(block, ForgeHooksClient.getWorldRenderPass());
        if(Thread.currentThread() == ThreadedChunkUpdateHelper.MAIN_THREAD ? offThreadBlock : !offThreadBlock) {
            // Cancel rendering block if it's delegated to a different thread.
            cir.setReturnValue(ThreadedChunkUpdateHelper.lastUpdateResult.renderedSomething);
        }
    }

    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/Tessellator;instance:Lnet/minecraft/client/renderer/Tessellator;"))
    private Tessellator modifyTessellatorAccess() {
        return Thread.currentThread() == ThreadedChunkUpdateHelper.MAIN_THREAD ? Tessellator.instance : ThreadedChunkUpdateHelper.instance.threadTessellator;
    }

}
