package com.github.tartaricacid.touhoulittlemaid.datagen;

import com.github.tartaricacid.touhoulittlemaid.advancements.rewards.GiveSmartSlabConfigTrigger;
import com.github.tartaricacid.touhoulittlemaid.datagen.advancement.BaseAdvancement;
import com.github.tartaricacid.touhoulittlemaid.datagen.advancement.ChallengeAdvancement;
import com.github.tartaricacid.touhoulittlemaid.datagen.advancement.FavorabilityAdvancement;
import com.github.tartaricacid.touhoulittlemaid.datagen.advancement.MaidBaseAdvancement;
import com.github.tartaricacid.touhoulittlemaid.util.IdentifierUtil;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.worldgen.BootstrapContext;

import java.util.List;

public class AdvancementDataGen extends AdvancementProvider {
    public AdvancementDataGen() {
        super(List.of(MainAdvancement::new, GiveSmartSlab::new));
    }

    public static final class GiveSmartSlab extends AdvancementSubProvider {
        public GiveSmartSlab(BootstrapContext<Advancement> output) {
            super(output);
        }

        @Override
        public void generate() {
            Advancement.Builder.advancement()
                    .addCriterion("tick", GiveSmartSlabConfigTrigger.create())
                    .rewards(AdvancementRewards.Builder.loot(this.output.lookup(Registries.LOOT_TABLE).getOrThrow(LootTableGenerator.GIVE_SMART_SLAB)))
                    .save(this.output, IdentifierUtil.modLoc("give_smart_slab").toString());
        }
    }

    public static final class MainAdvancement extends AdvancementSubProvider {
        public MainAdvancement(BootstrapContext<Advancement> output) {
            super(output);
        }

        @Override
        public void generate() {
            BaseAdvancement.generate(this.output);
            MaidBaseAdvancement.generate(this.output);
            FavorabilityAdvancement.generate(this.output);
            ChallengeAdvancement.generate(this.output);
        }
    }
}
