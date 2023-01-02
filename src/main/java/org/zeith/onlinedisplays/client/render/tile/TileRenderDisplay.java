package org.zeith.onlinedisplays.client.render.tile;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import org.zeith.hammerlib.client.render.tile.ITESR;
import org.zeith.onlinedisplays.tiles.TileDisplay;

public class TileRenderDisplay
		implements ITESR<TileDisplay>
{
	private static void addVertex(Matrix4f pose, Matrix3f normal, IVertexBuilder buf, float r, float g, float b, float a, float x, float y, float z, float u, float v)
	{
		buf.vertex(pose, x, y, z)
				.color(r, g, b, a)
				.uv(u, v)
				.overlayCoords(OverlayTexture.NO_OVERLAY)
				.uv2(15728880)
				.normal(normal, 0.0F, 1.0F, 0.0F)
				.endVertex();
	}
	
	@Override
	public void render(TileDisplay tile, float partial, MatrixStack matrix, IRenderTypeBuffer buf, int lighting, int overlay, TileEntityRendererDispatcher renderer)
	{
		if(tile.image == null) return;
		
		long msExisted = (long) ((tile.ticksExisted + partial) * 50D);
		
		if(tile.isEmissive.get())
			lighting = 15 << 20 | 15 << 4;
		
		RenderType type = RenderType.entityTranslucent(tile.image.getPath(msExisted));
		IVertexBuilder builder = buf.getBuffer(type);
		
		tile.matrix.apply(matrix);
		
		MatrixStack.Entry matrixstack$entry = matrix.last();
		Matrix4f pose = matrixstack$entry.pose();
		Matrix3f normal = matrixstack$entry.normal();
		
		builder.vertex(pose, 0, 0, 0)
				.color(1F, 1F, 1F, 1F)
				.uv(1, 1)
				.overlayCoords(overlay)
				.uv2(lighting)
				.normal(normal, 0.0F, 1.0F, 0.0F)
				.endVertex();
		
		builder.vertex(pose, 0, 1, 0)
				.color(1F, 1F, 1F, 1F)
				.uv(1, 0)
				.overlayCoords(overlay)
				.uv2(lighting)
				.normal(normal, 0.0F, 1.0F, 0.0F)
				.endVertex();
		
		builder.vertex(pose, 1, 1, 0)
				.color(1F, 1F, 1F, 1F)
				.uv(0, 0)
				.overlayCoords(overlay)
				.uv2(lighting)
				.normal(normal, 0.0F, 1.0F, 0.0F)
				.endVertex();
		
		builder.vertex(pose, 1, 0, 0)
				.color(1F, 1F, 1F, 1F)
				.uv(0, 1)
				.overlayCoords(overlay)
				.uv2(lighting)
				.normal(normal, 0.0F, 1.0F, 0.0F)
				.endVertex();
	}
	
	@Override
	public boolean shouldRenderOffScreen(TileDisplay tile)
	{
		return true;
	}
}