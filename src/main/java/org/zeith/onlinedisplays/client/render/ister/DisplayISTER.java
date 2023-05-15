package org.zeith.onlinedisplays.client.render.ister;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import org.zeith.onlinedisplays.client.resources.data.DrawableAreaMetadataSection;
import org.zeith.onlinedisplays.client.texture.*;
import org.zeith.onlinedisplays.mixins.AtlasTextureAccessor;
import org.zeith.onlinedisplays.mixins.BakedOverrideAccessor;

import java.util.*;

public class DisplayISTER
		extends BlockEntityWithoutLevelRenderer
{
	public static final BlockEntityWithoutLevelRenderer INSTANCE = new DisplayISTER();
	
	private Map<ResourceLocation, DrawableAreaMetadataSection> metadataSectionMap = new HashMap<>();
	
	public DisplayISTER()
	{
		super(null, null);
		((ReloadableResourceManager) Minecraft.getInstance().getResourceManager()).registerReloadListener(this);
	}
	
	@Override
	public void onResourceManagerReload(ResourceManager p_172555_)
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
			meta = Minecraft.getInstance().getResourceManager().getResourceOrThrow(rl).metadata().getSection(DrawableAreaMetadataSection.SERIALIZER).orElseThrow();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		
		metadataSectionMap.put(rl, meta);
		
		return Optional.ofNullable(meta);
	}
	
	public TextureAtlasSprite renderAllOverrides(ItemStack stack, PoseStack pose, MultiBufferSource bufferSource, int uv2, int overlay)
	{
		Minecraft mc = Minecraft.getInstance();
		ItemRenderer ir = mc.getItemRenderer();
		
		BakedModel isterModel = ir.getModel(stack, mc.level, mc.player, 0);
		ImmutableList<ItemOverrides.BakedOverride> overrides = isterModel.getOverrides().getOverrides();
		
		for(var override : overrides)
		{
			var overridenModel = ((BakedOverrideAccessor) override).getModel();
			if(overridenModel != null)
				for(var model : overridenModel.getRenderPasses(stack, true))
					for(var pass : model.getRenderTypes(stack, true))
					{
						var buf = bufferSource.getBuffer(pass);
						ir.renderModelLists(overridenModel, stack, uv2, overlay, pose, buf);
					}
		}
		
		return isterModel.getParticleIcon(ModelData.EMPTY);
	}
	
	@Override
	public void renderByItem(ItemStack stack, ItemTransforms.TransformType tt, PoseStack mat, MultiBufferSource src, int uv2, int overlay)
	{
		TextureAtlasSprite tex = renderAllOverrides(stack, mat, src, uv2, overlay);
		
		if(tex == null)
			return;
		
		DrawableAreaMetadataSection meta = getSectionFrom(tex).orElse(null);
		if(meta == null) return;
		
		CompoundTag beTag = stack.getTagElement("BlockEntityTag");
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
			var builder = src.getBuffer(type);
			
			boolean gui = tt == ItemTransforms.TransformType.GUI;
			boolean thirdPerson = tt == ItemTransforms.TransformType.THIRD_PERSON_LEFT_HAND || tt == ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND;
			
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
			
			PoseStack.Pose matrixstack$entry = mat.last();
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