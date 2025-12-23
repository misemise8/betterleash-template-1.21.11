package net.misemise.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.math.Vec3d;
import net.misemise.LeashConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class LeashMixin {

    /**
     * リードが切れる処理をキャンセルして、カスタム距離で管理
     */
    @Inject(
            method = "tickLeash",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/MobEntity;detachLeash(ZZ)V"
            ),
            cancellable = true
    )
    private void onLeashBreak(Entity leashHolder, float distance, CallbackInfo ci) {
        // カスタム最大距離を超えた場合のみ切れる
        if (distance <= LeashConfig.maxLeashDistance) {
            ci.cancel();
        }
    }

    /**
     * リードの引き寄せ力をカスタマイズ
     */
    @Inject(
            method = "tickLeash",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/mob/MobEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"
            ),
            cancellable = true
    )
    private void modifyLeashPull(Entity leashHolder, float distance, CallbackInfo ci) {
        MobEntity mob = (MobEntity) (Object) this;

        // カスタム引き寄せ強度を適用
        double dx = (leashHolder.getX() - mob.getX()) / distance;
        double dy = (leashHolder.getY() - mob.getY()) / distance;
        double dz = (leashHolder.getZ() - mob.getZ()) / distance;

        // 引き寄せ強度を適用（デフォルトは0.4、設定で変更可能）
        double strength = LeashConfig.pullStrength;
        Vec3d velocity = mob.getVelocity();
        Vec3d newVelocity = velocity.add(
                Math.copySign(dx * dx * strength, dx),
                Math.copySign(dy * dy * strength, dy),
                Math.copySign(dz * dz * strength, dz)
        );

        mob.setVelocity(newVelocity);
        ci.cancel();
    }
}