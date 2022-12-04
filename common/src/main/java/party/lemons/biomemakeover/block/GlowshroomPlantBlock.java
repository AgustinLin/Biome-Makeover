package party.lemons.biomemakeover.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import party.lemons.taniwha.block.types.TMushroomPlantBlock;

import java.util.function.Supplier;

public class GlowshroomPlantBlock extends TMushroomPlantBlock
{

    public GlowshroomPlantBlock(ResourceKey<ConfiguredFeature<?, ?>> feature, Properties properties) {
        super(feature, properties);
    }

    @Override
    public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
        BlockPos downPos = blockPos.below();
        BlockState downState = levelReader.getBlockState(downPos);
        if (blockState.is(BlockTags.MUSHROOM_GROW_BLOCK)) {
            return true;
        } else {
            return this.mayPlaceOn(downState, levelReader, downPos);
        }
    }
}