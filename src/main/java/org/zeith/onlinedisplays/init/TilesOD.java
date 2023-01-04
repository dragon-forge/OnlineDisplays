package org.zeith.onlinedisplays.init;

import net.minecraft.tileentity.TileEntityType;
import org.zeith.hammerlib.annotations.*;
import org.zeith.onlinedisplays.client.render.tile.TileRenderDisplay;
import org.zeith.onlinedisplays.tiles.TileDisplay;

@SimplyRegister
public interface TilesOD
{
	@RegistryName("display")
	@TileRenderer(TileRenderDisplay.class)
	TileEntityType<TileDisplay> DISPLAY = TileEntityType.Builder.of(TileDisplay::new, BlocksOD.DISPLAY).build(null);
}