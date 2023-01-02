package org.zeith.onlinedisplays.init;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.material.Material;
import org.zeith.hammerlib.annotations.RegistryName;
import org.zeith.hammerlib.annotations.SimplyRegister;
import org.zeith.onlinedisplays.blocks.BlockDisplay;

@SimplyRegister
public interface BlocksOD
{
	@RegistryName("display")
	BlockDisplay DISPLAY = new BlockDisplay(AbstractBlock.Properties.of(Material.STONE).strength(-1F).dynamicShape());
}