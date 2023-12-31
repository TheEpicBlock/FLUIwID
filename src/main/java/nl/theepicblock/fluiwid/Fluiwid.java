package nl.theepicblock.fluiwid;

import net.fabricmc.api.ModInitializer;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Fluiwid implements ModInitializer {
	private static final String MOD_ID = "fluiwid";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final StatusEffect WATER_EFFECT = Registry.register(Registries.STATUS_EFFECT, id("water"), new WaterStatusEffect(StatusEffectCategory.BENEFICIAL, 0x9654f9));
	public static final Potion WATER_POTION = Registry.register(Registries.POTION, id("fluiwid"), new Potion(new StatusEffectInstance(WATER_EFFECT, 3600, 0, false, false, true)));
	public static final Potion LONG_WATER_POTION = Registry.register(Registries.POTION, id("long_fluiwid"), new Potion("fluiwid", new StatusEffectInstance(WATER_EFFECT, 9600*2, 0, false, false, true)));

	@Override
	public void onInitialize() {
		BrewingRecipeRegistry.registerPotionRecipe(Potions.AWKWARD, Items.WATER_BUCKET, WATER_POTION);
		BrewingRecipeRegistry.registerPotionRecipe(WATER_POTION, Items.GUNPOWDER, LONG_WATER_POTION);
	}

	public static Identifier id(String path) {
		return new Identifier(MOD_ID, path);
	}
}