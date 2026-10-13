package com.github.tartaricacid.touhoulittlemaid.datagen.advancement;

import com.github.tartaricacid.touhoulittlemaid.util.IdentifierUtil;
import com.github.tartaricacid.touhoulittlemaid.advancements.maid.MaidEventTrigger;
import com.github.tartaricacid.touhoulittlemaid.advancements.maid.TriggerType;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ItemLike;



public class FavorabilityAdvancement {
    public static void generate(BootstrapContext<Advancement> output) {
        AdvancementHolder root = makeRoot(InitItems.BOOKSHELF.get(), "maid_sit_joy")
                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.MAID_SIT_JOY))
                .rewards(AdvancementRewards.Builder.experience(50))
                .save(output, id("favorability/maid_sit_joy").toString());

        generateFavorability(output, root);

        generateJoy(output, root);
    }

    private static void generateJoy(BootstrapContext<Advancement> output, AdvancementHolder root) {
        AdvancementHolder joy = make(InitItems.PICNIC_BASKET.get(), "maid_picnic_eat").parent(root)
                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.MAID_PICNIC_EAT))
                .save(output, id("favorability/maid_picnic_eat").toString());

        AdvancementHolder gomoku = makeGoal(InitItems.GOMOKU.get(), "win_gomoku").parent(joy)
                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.WIN_GOMOKU))
                .save(output, id("favorability/win_gomoku").toString());

        AdvancementHolder cchess = makeGoal(InitItems.CCHESS.get(), "win_cchess").parent(gomoku)
                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.WIN_CCHESS))
                .save(output, id("favorability/win_cchess").toString());

        makeGoal(InitItems.WCHESS.get(), "win_wchess").parent(cchess)
                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.WIN_WCHESS))
                .save(output, id("favorability/win_wchess").toString());

        make(InitItems.PINK_MAID_BED.get(), "maid_sleep").parent(joy)
                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.MAID_SLEEP))
                .save(output, id("favorability/maid_sleep").toString());
    }

    private static void generateFavorability(BootstrapContext<Advancement> output, AdvancementHolder root) {
        AdvancementHolder increased = make(InitItems.FAVORABILITY_TOOL_ADD.get(), "favorability_increased").parent(root)
                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.FAVORABILITY_INCREASED))
                .save(output, id("favorability/favorability_increased").toString());

        makeGoal(InitItems.FAVORABILITY_TOOL_FULL.get(), "favorability_increased_max").parent(increased)
                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.FAVORABILITY_INCREASED_MAX))
                .save(output, id("favorability/favorability_increased_max").toString());
    }

    /**
     * 根进度：只有根节点需要背景图
     */
    private static Advancement.Builder makeRoot(ItemLike item, String key) {
        MutableComponent title = Component.translatable(String.format("advancements.touhou_little_maid.favorability.%s.title", key));
        MutableComponent desc = Component.translatable(String.format("advancements.touhou_little_maid.favorability.%s.description", key));

        return Advancement.Builder.advancement().rootDisplay(item.asItem(), title, desc,
                IdentifierUtil.modLoc("advancements/backgrounds/stone"),
                AdvancementType.TASK, true, true, false);
    }

    private static Advancement.Builder make(ItemLike item, String key) {
        MutableComponent title = Component.translatable(String.format("advancements.touhou_little_maid.favorability.%s.title", key));
        MutableComponent desc = Component.translatable(String.format("advancements.touhou_little_maid.favorability.%s.description", key));

        return Advancement.Builder.advancement().display(item.asItem(), title, desc,
                AdvancementType.TASK, true, true, false);
    }

    private static Advancement.Builder makeGoal(ItemLike item, String key) {
        MutableComponent title = Component.translatable(String.format("advancements.touhou_little_maid.favorability.%s.title", key));
        MutableComponent desc = Component.translatable(String.format("advancements.touhou_little_maid.favorability.%s.description", key));

        return Advancement.Builder.advancement().display(item.asItem(), title, desc,
                AdvancementType.GOAL, true, true, false);
    }

    private static Identifier id(String id) {
        return IdentifierUtil.modLoc(id);
    }
}
