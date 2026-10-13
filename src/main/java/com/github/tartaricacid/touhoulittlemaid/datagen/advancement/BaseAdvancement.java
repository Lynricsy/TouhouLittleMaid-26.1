package com.github.tartaricacid.touhoulittlemaid.datagen.advancement;

import com.github.tartaricacid.touhoulittlemaid.util.IdentifierUtil;
import com.github.tartaricacid.touhoulittlemaid.advancements.maid.MaidEventTrigger;
import com.github.tartaricacid.touhoulittlemaid.advancements.maid.TriggerType;
import com.github.tartaricacid.touhoulittlemaid.datagen.LootTableGenerator;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.github.tartaricacid.touhoulittlemaid.init.InitItems;
import net.minecraft.core.HolderSet;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.advancements.*;
import net.minecraft.advancements.predicates.entity.EntityPredicate;
import net.minecraft.advancements.triggers.KilledTrigger;
import net.minecraft.advancements.triggers.RecipeCraftedTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;



public class BaseAdvancement {
    public static void generate(BootstrapContext<Advancement> output) {
        AdvancementHolder root = makeRoot(InitItems.HAKUREI_GOHEI.get(), "craft_gohei")
                .requirements(AdvancementRequirements.Strategy.OR)
                .addCriterion("craft_hakurei_gohei", RecipeCraftedTrigger.TriggerInstance.craftedItem(recipe(output, "hakurei_gohei")))
                .addCriterion("craft_sanae_gohei", RecipeCraftedTrigger.TriggerInstance.craftedItem(recipe(output, "sanae_gohei")))
                .rewards(AdvancementRewards.Builder.experience(50))
                .save(output, id("base/craft_gohei").toString());

        generateAltar(output, root);

        generateMaid(output, root);

        generateChair(output, root);
    }

    private static void generateChair(BootstrapContext<Advancement> output, AdvancementHolder root) {
        AdvancementHolder chair = make(InitItems.CHAIR.get(), "craft_chair").parent(root)
                .addCriterion("craft_chair", RecipeCraftedTrigger.TriggerInstance.craftedItem(recipe(output, "chair")))
                .save(output, id("base/craft_chair").toString());

        make(InitItems.CHANGE_CHAIR_MODEL.get(), "change_chair_model").parent(chair)
                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.CHANGE_CHAIR_MODEL))
                .save(output, id("base/change_chair_model").toString());
    }

    private static void generateMaid(BootstrapContext<Advancement> output, AdvancementHolder root) {
//        ItemStack stack = ItemEntityPlaceholder.setRecipeId(new ItemStack(InitItems.ENTITY_PLACEHOLDER.get()), "spawn_box");
//        AdvancementHolder spawnMaid = make(stack, "spawn_maid").parent(root)
//                .addCriterion("altar_craft", AltarCraftTrigger.Instance.recipe(id("altar_recipe/spawn_box")))
//                .rewards(AdvancementRewards.Builder.loot(output.lookup(Registries.LOOT_TABLE).getOrThrow(LootTableGenerator.CAKE)))
//                .save(output, id("base/spawn_maid").toString());
//
//        makeGoal(Items.CAKE, "tamed_maid").parent(spawnMaid)
//                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.TAMED_MAID))
//                .save(output, id("base/tamed_maid").toString());
//
//        make(InitItems.CHANGE_MAID_MODEL.get(), "change_maid_model").parent(spawnMaid)
//                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.CHANGE_MAID_MODEL))
//                .save(output, id("base/change_maid_model").toString());
//
//        make(Items.JUKEBOX, "change_maid_sound").parent(spawnMaid)
//                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.CHANGE_MAID_SOUND))
//                .save(output, id("base/change_maid_sound").toString());
    }

    private static void generateAltar(BootstrapContext<Advancement> output, AdvancementHolder root) {
        HolderGetter<EntityType<?>> entityTypes = output.lookup(Registries.ENTITY_TYPE);

        AdvancementHolder altar = make(Items.WOOL.red(), "build_altar").parent(root)
                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.BUILD_ALTAR))
                .rewards(AdvancementRewards.Builder.loot(output.lookup(Registries.LOOT_TABLE).getOrThrow(LootTableGenerator.ADVANCEMENT_POWER_POINT)))
                .save(output, id("base/build_altar").toString());

        EntityPredicate.Builder predicate = EntityPredicate.Builder.entity().of(entityTypes, InitEntities.FAIRY.get());
        make(InitItems.FAIRY_SPAWN_EGG.get(), "kill_maid_fairy").parent(altar)
                .addCriterion("killed_entity", KilledTrigger.TriggerInstance.playerKilledEntity(predicate))
                .save(output, id("base/kill_maid_fairy").toString());

        make(InitItems.POWER_POINT.get(), "pickup_power_point").parent(altar)
                .addCriterion("maid_event", MaidEventTrigger.create(TriggerType.PICKUP_POWER_POINT))
                .save(output, id("base/pickup_power_point").toString());
    }

    /**
     * 根进度：只有根节点需要背景图
     */
    private static Advancement.Builder makeRoot(ItemLike item, String key) {
        MutableComponent title = Component.translatable(String.format("advancements.touhou_little_maid.base.%s.title", key));
        MutableComponent desc = Component.translatable(String.format("advancements.touhou_little_maid.base.%s.description", key));

        return Advancement.Builder.advancement().rootDisplay(item.asItem(), title, desc,
                IdentifierUtil.modLoc("advancements/backgrounds/stone"),
                AdvancementType.TASK, true, true, false);
    }

    private static Advancement.Builder make(ItemLike item, String key) {
        MutableComponent title = Component.translatable(String.format("advancements.touhou_little_maid.base.%s.title", key));
        MutableComponent desc = Component.translatable(String.format("advancements.touhou_little_maid.base.%s.description", key));

        return Advancement.Builder.advancement().display(item.asItem(), title, desc,
                AdvancementType.TASK, true, true, false);
    }

    private static Advancement.Builder make(ItemStack item, String key) {
        MutableComponent title = Component.translatable(String.format("advancements.touhou_little_maid.base.%s.title", key));
        MutableComponent desc = Component.translatable(String.format("advancements.touhou_little_maid.base.%s.description", key));

        return Advancement.Builder.advancement().display(ItemStackTemplate.fromNonEmptyStack(item), title, desc,
                AdvancementType.TASK, true, true, false);
    }

    private static Advancement.Builder makeGoal(ItemLike item, String key) {
        MutableComponent title = Component.translatable(String.format("advancements.touhou_little_maid.base.%s.title", key));
        MutableComponent desc = Component.translatable(String.format("advancements.touhou_little_maid.base.%s.description", key));

        return Advancement.Builder.advancement().display(item.asItem(), title, desc,
                AdvancementType.GOAL, true, true, false);
    }

    private static Identifier id(String id) {
        return IdentifierUtil.modLoc(id);
    }

    private static HolderSet<Recipe<?>> recipe(BootstrapContext<Advancement> output, String id) {
        return HolderSet.direct(output.lookup(Registries.RECIPE).getOrThrow(recipeKey(id)));
    }

    private static ResourceKey<Recipe<?>> recipeKey(String id) {
        return ResourceKey.create(Registries.RECIPE, id(id));
    }
}
