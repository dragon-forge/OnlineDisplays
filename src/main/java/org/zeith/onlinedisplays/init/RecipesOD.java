package org.zeith.onlinedisplays.init;


import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraftforge.common.Tags;
import org.zeith.hammerlib.annotations.ProvideRecipes;
import org.zeith.hammerlib.api.IRecipeProvider;
import org.zeith.hammerlib.event.recipe.RegisterRecipesEvent;
import org.zeith.onlinedisplays.OnlineDisplays;

@ProvideRecipes
public class RecipesOD
		implements IRecipeProvider
{
	@Override
	public void provideRecipes(RegisterRecipesEvent event)
	{
		if(OnlineDisplays.getModSettings().survivalMode)
		{
			event.shaped()
					.shape("ggg", "rpr", "ggg")
					.map('g', Tags.Items.INGOTS_GOLD)
					.map('r', Items.REDSTONE_LAMP)
					.map('p', Items.PAINTING)
					.result(new ItemStack(BlocksOD.DISPLAY))
					.register();
			
			event.shapeless()
					.add(BlocksOD.DISPLAY)
					.result(BlocksOD.DISPLAY)
					.register();
		}
	}
}