package com.github.tartaricacid.touhoulittlemaid.datagen;

import com.github.tartaricacid.touhoulittlemaid.util.IdentifierUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.decoration.painting.PaintingVariant;

import java.util.Optional;

public final class RegistryDataGenerator {
    private RegistryDataGenerator() {
    }

    static void genPainting(BootstrapContext<PaintingVariant> ctx) {
        var id = IdentifierUtil.modLoc("wine_fox");
        ctx.register(ResourceKey.create(Registries.PAINTING_VARIANT, id),
                new PaintingVariant(2, 3, id, Optional.empty(), Optional.empty()));
    }
}
