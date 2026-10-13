package com.github.tartaricacid.touhoulittlemaid.entity.data;

import com.github.tartaricacid.touhoulittlemaid.util.IdentifierUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.apache.commons.lang3.StringUtils;

/**
 * YSM（Yes Steve Model）女仆模型数据，由 YSM 模组调用写入
 *
 * @param isYsmModel 是否使用 YSM 模型渲染
 * @param modelId    YSM 模型 ID
 * @param textureId  YSM 模型材质 ID
 * @param name       YSM 模型显示名称
 */
public record YsmData(
        boolean isYsmModel,
        String modelId,
        String textureId,
        Component name
) {
    public static final YsmData EMPTY = new YsmData(false, StringUtils.EMPTY, StringUtils.EMPTY, Component.empty());
    private static final String KEY = IdentifierUtil.modLoc("ysm").toString();

    public static final MapCodec<YsmData> CODEC = RecordCodecBuilder.mapCodec(ins -> ins.group(
            Codec.BOOL.optionalFieldOf("is_ysm_model", false).forGetter(YsmData::isYsmModel),
            Codec.STRING.optionalFieldOf("model_id", StringUtils.EMPTY).forGetter(YsmData::modelId),
            Codec.STRING.optionalFieldOf("texture_id", StringUtils.EMPTY).forGetter(YsmData::textureId),
            ComponentSerialization.CODEC.optionalFieldOf("name", Component.empty()).forGetter(YsmData::name)
    ).apply(ins, YsmData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, YsmData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, YsmData::isYsmModel,
            ByteBufCodecs.STRING_UTF8, YsmData::modelId,
            ByteBufCodecs.STRING_UTF8, YsmData::textureId,
            ComponentSerialization.STREAM_CODEC, YsmData::name,
            YsmData::new
    );

    public static final AttachmentType<YsmData> TYPE = AttachmentType
            .builder(() -> EMPTY)
            .serialize(CODEC)
            .sync(STREAM_CODEC)
            .build();

    /**
     * 直接从女仆实体 NBT（物品存储的女仆数据）里读取 YSM 数据，没有则返回 {@link #EMPTY}
     */
    public static YsmData directGet(CompoundTag data) {
        return data.getCompound(AttachmentHolder.ATTACHMENTS_NBT_KEY)
                .flatMap(attachment -> attachment.getCompound(KEY))
                .flatMap(tag -> CODEC.codec().parse(NbtOps.INSTANCE, tag).result())
                .orElse(EMPTY);
    }

    /**
     * 直接移除女仆实体 NBT 中的 YSM 数据，用于强制使用女仆模组自带模型渲染
     */
    public static void directRemove(CompoundTag data) {
        data.getCompound(AttachmentHolder.ATTACHMENTS_NBT_KEY).ifPresent(attachment -> attachment.remove(KEY));
    }

    public YsmData withYsmModel(boolean isYsmModel) {
        return new YsmData(isYsmModel, this.modelId, this.textureId, this.name);
    }

    public YsmData withModel(String modelId, String textureId, Component name) {
        return new YsmData(this.isYsmModel, modelId, textureId, name);
    }
}
