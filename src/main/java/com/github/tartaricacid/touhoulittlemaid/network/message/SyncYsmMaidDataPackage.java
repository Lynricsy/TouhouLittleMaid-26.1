package com.github.tartaricacid.touhoulittlemaid.network.message;

import com.github.tartaricacid.touhoulittlemaid.network.client.SyncYsmMaidDataPackageProxy;
import com.github.tartaricacid.touhoulittlemaid.util.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import static com.github.tartaricacid.touhoulittlemaid.util.IdentifierUtil.modLoc;

/**
 * 同步 YSM 女仆的轮盘动画与漫游变量，仅安装 YSM 后才会发送
 */
public record SyncYsmMaidDataPackage(int entityId, String rouletteAnim, boolean isRouletteAnimPlaying,
                                     Object2FloatOpenHashMap<String> roamingVars) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncYsmMaidDataPackage> TYPE = new CustomPacketPayload.Type<>(modLoc("sync_ysm_maid_data"));
    public static final StreamCodec<ByteBuf, SyncYsmMaidDataPackage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SyncYsmMaidDataPackage::entityId,
            ByteBufCodecs.STRING_UTF8, SyncYsmMaidDataPackage::rouletteAnim,
            ByteBufCodecs.BOOL, SyncYsmMaidDataPackage::isRouletteAnimPlaying,
            ByteBufUtils.OBJECT_2_FLOAT_OPEN_HASH_MAP_CODEC, SyncYsmMaidDataPackage::roamingVars,
            SyncYsmMaidDataPackage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncYsmMaidDataPackage message, IPayloadContext context) {
        if (context.flow().isClientbound()) {
            context.enqueueWork(() -> SyncYsmMaidDataPackageProxy.handle(message));
        }
    }
}
