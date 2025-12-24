package net.misemise.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.Leashable;
import net.misemise.LeashConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Leashable.class)
public interface LeashMixin {

        // /**
        // * リードが切れる処理をキャンセルして、カスタム距離で管理
        // */
        // @Inject(
        // method = "tickLeash",
        // at = @At(
        // value = "INVOKE",
        // target = "Lnet/minecraft/entity/Leashable;detachLeash(ZZ)V"
        // ),
        // cancellable = true
        // )
        // private void onLeashBreak(CallbackInfo ci) {
        // // Implementation commented out due to mapping issues in 1.21.11
        // }

        // /**
        // * リードの引き寄せ力をカスタマイズ
        // */
        // @Inject(
        // method = "applyLeashElasticity",
        // at = @At("HEAD"),
        // cancellable = true
        // )
        // private void modifyLeashPull(CallbackInfo ci) {
        // // Implementation commented out due to mapping issues in 1.21.11
        // }
}