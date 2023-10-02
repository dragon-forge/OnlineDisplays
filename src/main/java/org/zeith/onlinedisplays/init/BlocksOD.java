package org.zeith.onlinedisplays.init;

import net.minecraft.world.level.block.Block;
import org.zeith.hammerlib.annotations.*;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.blocks.BlockDisplay;

@SimplyRegister
public interface BlocksOD
{
	@RegistryName("display")
	BlockDisplay DISPLAY = new BlockDisplay(
			Block.Properties.of()
					.strength(OnlineDisplays.getModSettings().survivalMode ? 4 : -1F)
					.dynamicShape()
	);
}