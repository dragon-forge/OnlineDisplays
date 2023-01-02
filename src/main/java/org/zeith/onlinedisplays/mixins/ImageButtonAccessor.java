package org.zeith.onlinedisplays.mixins;

import net.minecraft.client.gui.widget.button.ImageButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
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
