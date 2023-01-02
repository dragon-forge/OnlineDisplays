package org.zeith.onlinedisplays.client.texture;

import java.awt.image.BufferedImage;

public interface ITextureFactory
{
	IDisplayableTexture createStaticImage(String internalHash, BufferedImage image);
}