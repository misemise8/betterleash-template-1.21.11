package net.misemise;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ConfigSyncPayload(double maxDistance, double pullStrength) implements CustomPayload {
    public static final CustomPayload.Id<ConfigSyncPayload> ID =
            new CustomPayload.Id<>(Identifier.of(BetterLeash.MOD_ID, "config_sync"));

    public static final PacketCodec<RegistryByteBuf, ConfigSyncPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.DOUBLE, ConfigSyncPayload::maxDistance,
            PacketCodecs.DOUBLE, ConfigSyncPayload::pullStrength,
            ConfigSyncPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}