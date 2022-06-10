package party.lemons.biomemakeover.crafting.witch;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.architectury.networking.NetworkManager;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.ShufflingList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import party.lemons.biomemakeover.crafting.witch.data.QuestCategories;
import party.lemons.biomemakeover.init.BMBlocks;
import party.lemons.biomemakeover.init.BMItems;
import party.lemons.biomemakeover.network.S2C_HandleWitchQuests;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class WitchQuestHandler
{
    public static void init()
    {
        /*
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(BMItems.GLOWFISH.get(), 10, 3));
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(BMBlocks.GLOWSHROOM_STEM.get(), 10, 3));
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(BMBlocks.GREEN_GLOWSHROOM_BLOCK.get(), 10, 3));
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(BMBlocks.ORANGE_GLOWSHROOM_BLOCK.get(), 13, 3));
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(BMBlocks.PURPLE_GLOWSHROOM_BLOCK.get(), 10, 3));
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(BMBlocks.GREEN_GLOWSHROOM.get(), 5, 1));
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(BMBlocks.ORANGE_GLOWSHROOM.get(), 8, 1));
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(BMBlocks.PURPLE_GLOWSHROOM.get(), 5, 1));
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(BMBlocks.MYCELIUM_ROOTS.get(), 3, 2));
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(BMBlocks.MYCELIUM_SPROUTS.get(), 3, 2));
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(BMBlocks.TALL_BROWN_MUSHROOM.get(), 5, 2));
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(BMBlocks.TALL_RED_MUSHROOM.get(), 5, 2));
        addQuestItem(QuestCategory.MUSHROOM, QuestItem.of(Blocks.MYCELIUM, 10, 1));

        addQuestItem(QuestCategory.MESA, QuestItem.of(BMItems.SCUTTLER_TAIL.get(), 5, 5));
        addQuestItem(QuestCategory.MESA, QuestItem.of(BMItems.ECTOPLASM.get(), 8, 3));
        addQuestItem(QuestCategory.MESA, QuestItem.of(BMItems.PINK_PETALS.get(), 12, 1));
        addQuestItem(QuestCategory.MESA, QuestItem.of(BMBlocks.SAGUARO_CACTUS.get(), 1, 10));

        addQuestItem(QuestCategory.DARK_FOREST, QuestItem.of(BMItems.MOTH_SCALES.get(), 5, 6));
        addQuestItem(QuestCategory.DARK_FOREST, QuestItem.of(BMItems.BULBUS_ROOT.get(), 5, 5));
        addQuestItem(QuestCategory.DARK_FOREST, QuestItem.of(BMItems.ILLUNITE_SHARD.get(), 10, 2));

        addQuestItem(QuestCategory.DARK_FOREST, QuestItem.of(BMBlocks.IVY.get(), 3, 10));
        addQuestItem(QuestCategory.DARK_FOREST, QuestItem.of(BMBlocks.MOTH_BLOSSOM.get(), 10, 2));

        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.DANDELION, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.POPPY, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.BLUE_ORCHID, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.ALLIUM, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.AZURE_BLUET, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.ORANGE_TULIP, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.RED_TULIP, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.PINK_TULIP, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.WHITE_TULIP, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.OXEYE_DAISY, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.CORNFLOWER, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.LILY_OF_THE_VALLEY, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.SUNFLOWER, 2, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.LILAC, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.ROSE_BUSH, 1, 4));
        addQuestItem(QuestCategory.FLOWER, QuestItem.of(Blocks.PEONY, 1, 4));

        addQuestItem(QuestCategory.SWAMP, QuestItem.of(BMBlocks.SMALL_LILY_PAD.get(), 1.25F, 4));
        addQuestItem(QuestCategory.SWAMP, QuestItem.of(BMBlocks.CATTAIL.get(), 1.25F, 4));
        addQuestItem(QuestCategory.SWAMP, QuestItem.of(BMBlocks.REED.get(), 0.75F, 4));
        addQuestItem(QuestCategory.SWAMP, QuestItem.of(BMBlocks.WILLOWING_BRANCHES.get(), 1F, 4));
        addQuestItem(QuestCategory.SWAMP, QuestItem.of(Blocks.LILY_PAD, 1, 4));
        addQuestItem(QuestCategory.SWAMP, QuestItem.of(Items.SLIME_BALL, 2, 4));
        addQuestItem(QuestCategory.SWAMP, QuestItem.of(BMItems.MAGENTA_PETALS.get(), 3, 4));

        addQuestItem(QuestCategory.OCEAN, QuestItem.of(Blocks.KELP, 0.8F, 5));
        addQuestItem(QuestCategory.OCEAN, QuestItem.of(Blocks.SEAGRASS, 0.8F, 5));
        addQuestItem(QuestCategory.OCEAN, QuestItem.of(Items.SALMON, 1, 2));
        addQuestItem(QuestCategory.OCEAN, QuestItem.of(Items.COD, 1, 2));
        addQuestItem(QuestCategory.OCEAN, QuestItem.of(Items.INK_SAC, 2, 4));
        addQuestItem(QuestCategory.OCEAN, QuestItem.of(Items.SEA_PICKLE, 4, 4));
        addQuestItem(QuestCategory.OCEAN, QuestItem.of(Items.TROPICAL_FISH, 5, 2));
        addQuestItem(QuestCategory.OCEAN, QuestItem.of(Items.PUFFERFISH, 5, 2));

        addQuestItem(QuestCategory.JUNGLE, QuestItem.of(Items.COCOA_BEANS, 1.25F, 10));
        addQuestItem(QuestCategory.JUNGLE, QuestItem.of(Blocks.VINE, 1, 4));
        addQuestItem(QuestCategory.JUNGLE, QuestItem.of(Blocks.MELON, 4, 4));
        addQuestItem(QuestCategory.JUNGLE, QuestItem.of(Items.GLISTERING_MELON_SLICE, 6, 2));
        addQuestItem(QuestCategory.JUNGLE, QuestItem.of(Items.MELON_SLICE, 1.25F, 4));
        addQuestItem(QuestCategory.JUNGLE, QuestItem.of(Blocks.BAMBOO, 0.5F, 10));

        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.WARPED_FUNGUS, 5, 5));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.CRIMSON_FUNGUS, 5, 5));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Items.NETHER_WART, 2.25F, 10));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.TWISTING_VINES, 1F, 3));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.WEEPING_VINES, 1, 3));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.WARPED_ROOTS, 3, 3));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.CRIMSON_ROOTS, 3, 3));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.WARPED_WART_BLOCK, 6, 2));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.NETHER_WART_BLOCK, 5, 2));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.NETHER_SPROUTS, 2, 2));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.CRIMSON_NYLIUM, 4, 1));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.WARPED_NYLIUM, 4, 1));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.GLOWSTONE, 5, 3));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Items.BLAZE_ROD, 10, 3));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Items.BLAZE_POWDER, 6, 3));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Items.MAGMA_CREAM, 7, 3));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Items.GHAST_TEAR, 14, 2));
        addQuestItem(QuestCategory.NETHER, QuestItem.of(Blocks.SHROOMLIGHT, 8, 2));

        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.APPLE, 5, 4));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.WHEAT, 1F, 10));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.TURTLE_EGG, 10F, 1));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.TALL_GRASS, 2F, 4));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.SWEET_BERRIES, 1F, 4));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.SUGAR_CANE, 0.5F, 8));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.SUGAR, 0.8F, 8));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.PAPER, 0.8F, 8));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.REDSTONE, 2F, 8));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.GLOWSTONE, 4F, 2));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.SPIDER_EYE, 2F, 2));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.FERMENTED_SPIDER_EYE, 8F, 1));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.ENDER_PEARL, 5F, 5));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.SCUTE, 8F, 1));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.GUNPOWDER, 3.5F, 4));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.RABBIT_FOOT, 5F, 2));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.PHANTOM_MEMBRANE, 7F, 4));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.GOLDEN_CARROT, 6F, 4));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.HONEY_BOTTLE, 7F, 1));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.HONEYCOMB_BLOCK, 7F, 1));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.EGG, 1.25F, 5));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.POISONOUS_POTATO, 3F, 5));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.BONE, 0.4F, 10));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Items.LAPIS_LAZULI, 0.5F, 10));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Blocks.PUMPKIN, 3.5F, 4));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Blocks.LARGE_FERN, 0.75F, 4));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Blocks.GRASS, 0.5F, 4));
        addQuestItem(QuestCategory.COMMON, QuestItem.of(Blocks.CAULDRON, 4F, 1));

        addQuestItem(QuestCategory.RARE, QuestItem.of(Blocks.WITHER_ROSE, 10, 1));
        addQuestItem(QuestCategory.RARE, QuestItem.of(Items.TOTEM_OF_UNDYING, 15, 1));
        addQuestItem(QuestCategory.RARE, QuestItem.of(Blocks.WITHER_SKELETON_SKULL, 50, 1));
        addQuestItem(QuestCategory.RARE, QuestItem.of(Items.NETHER_STAR, 175, 1));
        addQuestItem(QuestCategory.RARE, QuestItem.of(Items.EXPERIENCE_BOTTLE, 10, 1));
        addQuestItem(QuestCategory.RARE, QuestItem.of(Items.ENCHANTED_GOLDEN_APPLE, 40, 1));
        addQuestItem(QuestCategory.RARE, QuestItem.of(Items.GOLDEN_APPLE, 5, 1));
        addQuestItem(QuestCategory.RARE, QuestItem.of(Items.CREEPER_HEAD, 30, 1));
        addQuestItem(QuestCategory.RARE, QuestItem.of(Items.SKELETON_SKULL, 30, 1));
        addQuestItem(QuestCategory.RARE, QuestItem.of(Items.ZOMBIE_HEAD, 30, 1));
        */

    }

    public static ItemStack getRewardFor(WitchQuest quest, RandomSource random)
    {
        QuestRarity rarity = QuestRarity.getRarityFromQuest(quest);

        return rarity.rewards.sample().pickRandom(random);
    }

    public static WitchQuest createQuest(RandomSource random)
    {
        List<Integer> counts = ITEM_COUNT_SELECTOR.shuffle().stream().toList();

        int count = counts.get(random.nextInt(counts.size()));
        List<QuestItem> questItems = Lists.newArrayList();

        int safetyCount = count * 2;    //If there's not enough items to select, it infinite loops

        while(questItems.size() < count && safetyCount > 0)
        {
            safetyCount--;
            QuestCategory category = QuestCategories.choose();
            List<QuestItem> itemPool = category.getRequestedItemPool();

            QuestItem item = itemPool.get(random.nextInt(itemPool.size()));
            if(!questItems.contains(item)) questItems.add(item);
        }

        WitchQuest quest = new WitchQuest(random, questItems);

        return quest;
    }

    public static void sendQuests(Player player, int index, WitchQuestList quests)
    {
        if(player.level.isClientSide()) return;

        new S2C_HandleWitchQuests(index, quests).sendTo((ServerPlayer) player);
    }

    private static final ShufflingList<Integer> ITEM_COUNT_SELECTOR = new ShufflingList<>();

    static
    {
        ITEM_COUNT_SELECTOR.add(1, 5);
        ITEM_COUNT_SELECTOR.add(2, 8);
        ITEM_COUNT_SELECTOR.add(3, 4);
        ITEM_COUNT_SELECTOR.add(4, 3);
        ITEM_COUNT_SELECTOR.add(5, 1);
    }
}