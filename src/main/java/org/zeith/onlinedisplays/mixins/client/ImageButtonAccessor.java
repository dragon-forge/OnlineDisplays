package org.zeith.onlinedisplays.mixins.client;

import net.minecraft.client.gui.components.ImageButton;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ImageButton.class)
public interface ImageButtonAccessor
{
	@Accessor
	int getXTexStart();
	
	@Mutable
	@Accessor
	void setXTexStart(int xTexStart);
}
