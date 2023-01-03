package org.zeith.onlinedisplays.init;

import net.minecraft.tileentity.TileEntityType;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.onlinedisplays.tiles.TileDisplay;

@SimplyRegister
public interface TilesOD
{
	@RegistryName("display")
	TileEntityType<TileDisplay> DISPLAY = TileEntityType.Builder.of(TileDisplay::new, BlocksOD.DISPLAY).build(null);
}