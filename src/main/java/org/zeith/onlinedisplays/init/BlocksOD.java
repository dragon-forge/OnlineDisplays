package org.zeith.onlinedisplays.init;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.material.Material;
import net.minecraftforge.common.ToolType;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.blocks.BlockDisplay;
import org.zeith.onlinedisplays.tiles.TileDisplay;

@SimplyRegister
public interface BlocksOD
{
	@RegistryName("display")
	BlockDisplay DISPLAY = new BlockDisplay(
			AbstractBlock.Properties.of(Material.STONE)
					.harvestTool(ToolType.PICKAXE)
					.harvestLevel(2)
					.strength(OnlineDisplays.getModSettings().survivalMode ? 4 : -1F)
					.dynamicShape()
	);
}