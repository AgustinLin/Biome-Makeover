package party.lemons.biomemakeover.item;

import dev.architectury.core.item.ArchitecturyMobBucketItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import party.lemons.biomemakeover.init.BMAdvancements;

import java.util.function.Supplier;

public class GlowfishBucketItem extends ArchitecturyMobBucketItem
{

    public GlowfishBucketItem(Supplier<? extends EntityType<?>>entityType, Supplier<Fluid> fluid, Supplier<SoundEvent> soundEvent, Properties properties) {
        super(entityType, fluid, soundEvent, properties);
    }

    public InteractionResultHolder<ItemStack> use(Level world, ServerPlayer user, InteractionHand hand)
    {
        InteractionResultHolder<ItemStack> res = super.use(world, user, hand);

        if(!world.isClientSide() && res.getResult().consumesAction())
        {
            BMAdvancements.GLOWFISH_SAVE.trigger(user);
        }

        return res;
    }
}
