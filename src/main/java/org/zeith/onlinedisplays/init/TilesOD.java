package org.zeith.onlinedisplays.init;

import net.minecraft.world.level.block.entity.BlockEntityType;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.hammerlib.annotations.client.TileRenderer;
import org.zeith.hammerlib.api.forge.BlockAPI;
import org.zeith.onlinedisplays.client.render.tile.TileRenderDisplay;
import org.zeith.onlinedisplays.tiles.TileDisplay;

@SimplyRegister
public interface TilesOD
{
	@RegistryName("display")
	@TileRenderer(TileRenderDisplay.class)
	BlockEntityType<TileDisplay> DISPLAY = BlockAPI.createBlockEntityType(TileDisplay::new, BlocksOD.DISPLAY);
}