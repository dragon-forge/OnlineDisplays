package org.zeith.onlinedisplays.client.render.ister;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.MinecraftForge;
import org.zeith.hammerlib.event.ResourceManagerReloadEvent;
import org.zeith.onlinedisplays.client.resources.data.DrawableAreaMetadataSection;
import org.zeith.onlinedisplays.client.texture.*;
import org.zeith.onlinedisplays.mixins.AtlasTextureAccessor;

import java.util.*;

public class DisplayISTER
		extends ItemStackTileEntityRenderer
{
	public DisplayISTER()
	{
		MinecraftForge.EVENT_BUS.addListener(this::onResourcesReload);
	}
	
	private Map<ResourceLocation, DrawableAreaMetadataSection> metadataSectionMap = new HashMap<>();
	
	public void onResourcesReload(ResourceManagerReloadEvent e)
	{
		metadataSectionMap.clear();
	}
	
	public Optional<DrawableAreaMetadataSection> getSectionFrom(TextureAtlasSprite tex)
	{
		ResourceLocation rl = ((AtlasTextureAccessor) tex.atlas()).callGetResourceLocation(tex.getName());
		DrawableAreaMetadataSection meta = null;
		
		if(metadataSectionMap.containsKey(rl))
			return Optional.ofNullable(metadataSectionMap.get(rl));
		
		try
		{
			meta = Minecraft.getInstance().getResourceManager().getResource(rl).getMetadata(DrawableAreaMetadataSection.SERIALIZER);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
		metadataSectionMap.put(rl, meta);
		
		return Optional.ofNullable(meta);
	}
	
	public TextureAtlasSprite renderAllOverrides(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack pose, IRenderTypeBuffer bufferSource, int uv2, int overlay)
	{
		Minecraft mc = Minecraft.getInstance();
		ItemRenderer ir = mc.getItemRenderer();
		
		IBakedModel isterModel = ir.getModel(stack, mc.level, mc.player);
		ImmutableList<ItemOverride> overrides = isterModel.getOverrides().getOverrides();
		
		for(ItemOverride override : overrides)
		{
			ResourceLocation mod = override.getModel();
			IBakedModel overridenModel;
			if(mod != null && (overridenModel = mc.getModelManager().getModel(mod)) != null)
			{
				for(Pair<IBakedModel, RenderType> model : overridenModel.getLayerModels(stack, true))
				{
					IVertexBuilder buf = bufferSource.getBuffer(model.getSecond());
					ir.renderModelLists(overridenModel, stack, uv2, overlay, pose, buf);
				}
			}
		}
		
		return isterModel.getParticleTexture(EmptyModelData.INSTANCE);
	}
	
	@Override
	public void renderByItem(ItemStack stack, ItemCameraTransforms.TransformType tt, MatrixStack mat, IRenderTypeBuffer src, int uv2, int overlay)
	{
		TextureAtlasSprite tex = renderAllOverrides(stack, tt, mat, src, uv2, overlay);
		
		if(tex == null)
			return;
		
		DrawableAreaMetadataSection meta = getSectionFrom(tex).orElse(null);
		if(meta == null) return;
		
		CompoundNBT beTag = stack.getTagElement("BlockEntityTag");
		if(beTag == null) return;
		
		beTag = beTag.getCompound("HL");
		
		String hash = beTag.getString("dataHash");
		String url = beTag.getString("url");
		
		if(hash.isEmpty() || !ClientImageStorage.isHashedOrRequest(hash, url))
			return;
		
		boolean emissive = beTag.getBoolean("emissive");
		
		IDisplayableTexture tx = OnlineTextureParser.getTextureByHash(hash);
		if(tx != null)
		{
			RenderType type = RenderType.entityTranslucent(tx.getPath(System.currentTimeMillis()));
			IVertexBuilder builder = src.getBuffer(type);
			
			boolean gui = tt == ItemCameraTransforms.TransformType.GUI;
			boolean thirdPerson = tt == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND || tt == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
			
			float normalX = 0.0F;
			float normalY = gui ? -1.0F : 0.0F;
			float normalZ = gui ? 0.0F : (thirdPerson ? 1.0F : -1.0F);
			
			float w = meta.textureWidth, h = meta.textureHeight;
			
			float aspectRatio = tx.getWidth() / (float) tx.getHeight();
			
			mat.pushPose();
			
			float x = meta.x / w;
			float y = meta.y / h;
			float viewW = meta.width / w;
			float viewH = meta.height / h;
			
			final float xWRef = meta.width / w;
			final float yHRef = meta.height / h;
			
			float xW = xWRef;
			float yH = yHRef;
			
			float min = Math.min(xW, yH);
			xW = min;
			yH = min;
			
			if(aspectRatio > 1) // wide image
				yH /= aspectRatio;
			else // tall image
				xW *= aspectRatio;
			
			// Fix xW and yH
			if(xW < xWRef && yH < yHRef)
			{
				float upscaleRate = Math.min(xWRef / xW, yHRef / yH);
				xW *= upscaleRate;
				yH *= upscaleRate;
			}
			
			// Recenter image.
			x = x + (viewW - xW) / 2;
			y = y + (viewH - yH) / 2;
			
			mat.translate(x, y, 8.51 / 16D);
			mat.scale(xW, yH, xW);
			
			MatrixStack.Entry matrixstack$entry = mat.last();
			Matrix4f pose = matrixstack$entry.pose();
			Matrix3f normal = matrixstack$entry.normal();
			
			if(emissive)
				uv2 = 15 << 20 | 15 << 4;
			
			builder.vertex(pose, 0, 0, 0)
					.color(1F, 1F, 1F, 1F)
					.uv(0, 1)
					.overlayCoords(overlay)
					.uv2(uv2)
					.normal(normal, normalX, normalY, normalZ)
					.endVertex();
			
			builder.vertex(pose, 0, 1, 0)
					.color(1F, 1F, 1F, 1F)
					.uv(0, 0)
					.overlayCoords(overlay)
					.uv2(uv2)
					.normal(normal, normalX, normalY, normalZ)
					.endVertex();
			
			builder.vertex(pose, 1, 1, 0)
					.color(1F, 1F, 1F, 1F)
					.uv(1, 0)
					.overlayCoords(overlay)
					.uv2(uv2)
					.normal(normal, normalX, normalY, normalZ)
					.endVertex();
			
			builder.vertex(pose, 1, 0, 0)
					.color(1F, 1F, 1F, 1F)
					.uv(1, 1)
					.overlayCoords(overlay)
					.uv2(uv2)
					.normal(normal, normalX, normalY, normalZ)
					.endVertex();
			
			mat.popPose();
		}
	}
}