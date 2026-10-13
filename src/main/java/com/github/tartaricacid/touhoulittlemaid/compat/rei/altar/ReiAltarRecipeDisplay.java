package com.github.tartaricacid.touhoulittlemaid.compat.rei.altar;

import com.github.tartaricacid.touhoulittlemaid.TouhouLittleMaid;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

/**
 * 祭坛配方在 REI 中的展示数据
 * <p>
 * 该类会在服务端（REI 通用插件）生成并通过 {@link #SERIALIZER} 同步到客户端，因此不能引用任何客户端类
 */
public class ReiAltarRecipeDisplay implements Display {
    public static final CategoryIdentifier<ReiAltarRecipeDisplay> ALTAR = CategoryIdentifier.of(TouhouLittleMaid.MOD_ID, "plugin/altar");

    private static final MapCodec<ReiAltarRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("id").forGetter(ReiAltarRecipeDisplay::getId),
            EntryIngredient.codec().listOf().fieldOf("inputs").forGetter(ReiAltarRecipeDisplay::getInputEntries),
            EntryIngredient.codec().listOf().fieldOf("outputs").forGetter(ReiAltarRecipeDisplay::getOutputEntries),
            Codec.FLOAT.fieldOf("power").forGetter(ReiAltarRecipeDisplay::getPowerCost),
            Codec.STRING.fieldOf("lang_key").forGetter(ReiAltarRecipeDisplay::getLangKey)
    ).apply(instance, ReiAltarRecipeDisplay::new));

    private static final StreamCodec<RegistryFriendlyByteBuf, ReiAltarRecipeDisplay> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, ReiAltarRecipeDisplay::getId,
            EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), ReiAltarRecipeDisplay::getInputEntries,
            EntryIngredient.streamCodec().apply(ByteBufCodecs.list()), ReiAltarRecipeDisplay::getOutputEntries,
            ByteBufCodecs.FLOAT, ReiAltarRecipeDisplay::getPowerCost,
            ByteBufCodecs.STRING_UTF8, ReiAltarRecipeDisplay::getLangKey,
            ReiAltarRecipeDisplay::new
    );

    public static final DisplaySerializer<ReiAltarRecipeDisplay> SERIALIZER = DisplaySerializer.of(CODEC, STREAM_CODEC);

    private final Identifier id;
    private final List<EntryIngredient> inputs;
    private final List<EntryIngredient> outputs;
    private final float powerCost;
    private final String langKey;

    public ReiAltarRecipeDisplay(Identifier id, List<EntryIngredient> inputs, List<EntryIngredient> outputs, float powerCost, String langKey) {
        this.id = id;
        this.inputs = inputs;
        this.outputs = outputs;
        this.powerCost = powerCost;
        this.langKey = langKey;
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    public Identifier getId() {
        return id;
    }

    public float getPowerCost() {
        return powerCost;
    }

    public String getLangKey() {
        return langKey;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return ALTAR;
    }

    @Override
    public Optional<Identifier> getDisplayLocation() {
        return Optional.of(id);
    }

    @Override
    public DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
