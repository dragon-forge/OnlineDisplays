package org.zeith.onlinedisplays.init;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.blocks.BlockDisplay;

@SimplyRegister
public interface BlocksOD
{
	@RegistryName("display")
	BlockDisplay DISPLAY = new BlockDisplay(
			Block.Properties.of(Material.STONE)
					.strength(OnlineDisplays.getModSettings().survivalMode ? 4 : -1F)
					.dynamicShape()
	);
}