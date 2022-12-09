package party.lemons.biomemakeover.forge;

import dev.architectury.platform.Platform;
import dev.architectury.platform.forge.EventBuses;
import dev.architectury.utils.Env;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import party.lemons.biomemakeover.BiomeMakeover;
import party.lemons.biomemakeover.BiomeMakeoverClient;
import party.lemons.biomemakeover.Constants;
import party.lemons.biomemakeover.init.BMEffects;
import party.lemons.biomemakeover.level.particle.BlossomParticle;
import party.lemons.biomemakeover.level.particle.LightningSparkParticle;
import party.lemons.biomemakeover.level.particle.PoltergeistParticle;
import party.lemons.biomemakeover.level.particle.TeleportParticle;
import party.lemons.biomemakeover.util.loot.BMLootTableInjection;

@Mod(Constants.MOD_ID)
@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BMForge
{
    public BMForge()
    {
        EventBuses.registerModEventBus(Constants.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        FMLJavaModLoadingContext.get().getModEventBus().addListener(BMForge::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(BMForge::commonSetup);

        BiomeMakeover.init();
        if (Platform.getEnvironment() == Env.CLIENT) {
            FMLJavaModLoadingContext.get().getModEventBus().addListener(BMForge::particleSetup);
            BiomeMakeoverClient.init();
        }
    }

    @SubscribeEvent
    public static void lootLoad(LootTableLoadEvent event)
    {
        for(BMLootTableInjection.InjectedItem item : BMLootTableInjection.getInsertedEntries())
        {
            if(event.getName().equals(item.table()))
            {
                event.getTable().addPool(LootPool.lootPool()
                        .setRolls(item.rolls())
                        .add(LootItem.lootTableItem(item.itemLike())
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0f, 2.0f)))).when(LootItemKilledByPlayerCondition.killedByPlayer()).build());

            }
        }
    }

    public static void commonSetup(FMLCommonSetupEvent event)
    {
    }

    public static void clientSetup(FMLClientSetupEvent event)
    {
        if (Platform.getEnvironment() == Env.CLIENT) {
            BiomeMakeoverClient.init();
        }
    }

    public static void particleSetup(RegisterParticleProvidersEvent event)
    {
        event.register(BMEffects.LIGHTNING_SPARK.get(), LightningSparkParticle.Provider::new);
        event.register(BMEffects.POLTERGEIST.get(), PoltergeistParticle.Provider::new);
        event.register(BMEffects.BLOSSOM.get(), BlossomParticle.Provider::new);
        event.register(BMEffects.TELEPORT.get(), TeleportParticle.Provider::new);
        //TODO: Look into why arch method crashes?
    }
}
