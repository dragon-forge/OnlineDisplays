package org.zeith.onlinedisplays.client.gui;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.zeith.hammerlib.client.utils.FXUtils;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.net.lft.TransportSessionBuilder;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.client.texture.IDisplayableTexture;
import org.zeith.onlinedisplays.init.BlocksOD;
import org.zeith.onlinedisplays.mixins.ImageButtonAccessor;
import org.zeith.onlinedisplays.net.*;
import org.zeith.onlinedisplays.tiles.TileDisplay;

import java.util.*;
import java.util.function.Predicate;

public class GuiDisplayConfig
		extends Screen
{
	public static final Predicate<String> URL_MATCHER = ((Predicate<String>) Objects::nonNull)
			.and(
					OnlineDisplays.URL_TEST
							.or((str) -> str.startsWith("local/"))
			);
	
	protected final int
			xSize = 176,
			ySize = 166;
	
	protected int guiLeft, guiTop;
	
	public final ResourceLocation texture = OnlineDisplays.id("textures/gui/display.png");
	
	public TileDisplay display;
	
	public GuiDisplayConfig(TileDisplay display)
	{
		super(BlocksOD.DISPLAY.getName());
		this.display = display;
	}
	
	EditBox url;
	
	// Offsets
	EditBox tx, ty, tz;
	
	// Rotations
	EditBox rx, ry, rz;
	
	// Scale
	EditBox sx, sy;
	
	ImageButtonAccessor emissiveToggle;
	
	public void setURL(String value)
	{
		if(url != null)
			url.setValue(value);
	}
	
	@Override
	protected void init()
	{
		this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
		
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		super.init();
		
		String pv = url != null ? url.getValue() : display.imageURL.get();
		url = addWidget(new EditBox(font, guiLeft + 7, guiTop + 19, 142, 18, OnlineDisplays.gui("url")));
		url.setMaxLength(1024);
		url.setValue(pv);
		if(pv != null)
		{
			boolean validURL = URL_MATCHER.test(pv);
			url.setTextColor(validURL ? 0x22FF11 : 0xFF2211);
		}
		
		{
			int xBase = 7;
			int yBase = 52;
			
			pv = tx != null ? tx.getValue() : Float.toString(display.matrix.translateX);
			tx = addWidget(new EditBox(font, guiLeft + xBase, guiTop + yBase, 40, 18, OnlineDisplays.gui("translate_x")));
			tx.setMaxLength(10);
			tx.setValue(pv);
			tx.moveCursorToStart();
			
			pv = ty != null ? ty.getValue() : Float.toString(display.matrix.translateY);
			ty = addWidget(new EditBox(font, guiLeft + xBase + 60, guiTop + yBase, 40, 18, OnlineDisplays.gui("translate_y")));
			ty.setMaxLength(10);
			ty.setValue(pv);
			ty.moveCursorToStart();
			
			pv = tz != null ? tz.getValue() : Float.toString(display.matrix.translateZ);
			tz = addWidget(new EditBox(font, guiLeft + xBase + 120, guiTop + yBase, 40, 18, OnlineDisplays.gui("translate_z")));
			tz.setMaxLength(10);
			tz.setValue(pv);
			tz.moveCursorToStart();
		}
		
		{
			int xBase = 7;
			int yBase = 85;
			
			pv = rx != null ? rx.getValue() : Float.toString(display.matrix.rotateX);
			rx = addWidget(new EditBox(font, guiLeft + xBase, guiTop + yBase, 40, 18, OnlineDisplays.gui("rotate_x")));
			rx.setMaxLength(10);
			rx.setValue(pv);
			rx.moveCursorToStart();
			
			pv = ry != null ? ry.getValue() : Float.toString(display.matrix.rotateY);
			ry = addWidget(new EditBox(font, guiLeft + xBase + 60, guiTop + yBase, 40, 18, OnlineDisplays.gui("rotate_y")));
			ry.setMaxLength(10);
			ry.setValue(pv);
			ry.moveCursorToStart();
			
			pv = rz != null ? rz.getValue() : Float.toString(display.matrix.rotateZ);
			rz = addWidget(new EditBox(font, guiLeft + xBase + 120, guiTop + yBase, 40, 18, OnlineDisplays.gui("rotate_z")));
			rz.setMaxLength(10);
			rz.setValue(pv);
			rz.moveCursorToStart();
		}
		
		{
			int xBase = 67;
			int yBase = 118;
			
			pv = sx != null ? sx.getValue() : Float.toString(display.matrix.scaleX);
			sx = addWidget(new EditBox(font, guiLeft + xBase, guiTop + yBase, 40, 18, OnlineDisplays.gui("scale_x")));
			sx.setMaxLength(10);
			sx.setValue(pv);
			sx.moveCursorToStart();
			
			pv = sy != null ? sy.getValue() : Float.toString(display.matrix.scaleY);
			sy = addWidget(new EditBox(font, guiLeft + xBase + 60, guiTop + yBase, 40, 18, OnlineDisplays.gui("scale_y")));
			sy.setMaxLength(10);
			sy.setValue(pv);
			sy.moveCursorToStart();
		}
		
		addWidget(new Button(guiLeft + xSize - 80 - 6, guiTop + ySize - 26, 80, 20, OnlineDisplays.gui("apply"), btn -> applyChanges()));
		addWidget(new Button(guiLeft + 6, guiTop + ySize - 26, 80, 20, OnlineDisplays.gui("aspect"), btn -> applyAspectRatio()));
		
		addWidget(new ImageButton(guiLeft + 7 + 143, guiTop + 18, 20, 20,
				0, 0,
				20,
				OnlineDisplays.id("textures/gui/choose_file.png"),
				40,
				40,
				btn -> beginSelectingLocalFile(), (btn, mat, mouseX, mouseY) ->
		{
			renderTooltip(mat, Arrays.asList(OnlineDisplays.gui("local_file").getVisualOrderText()), mouseX, mouseY, font);
		}, OnlineDisplays.EMPTY_TXT
		));
		
		emissiveToggle = (ImageButtonAccessor) addWidget(new ImageButton(guiLeft + 7, guiTop + 117, 20, 20,
				0, 0,
				20,
				OnlineDisplays.id("textures/gui/emissive.png"),
				40,
				40,
				btn -> toggleEmissive(), (btn, mat, mouseX, mouseY) ->
		{
			renderTooltip(mat, Arrays.asList(OnlineDisplays.gui("emissive").getVisualOrderText()), mouseX, mouseY, font);
		}, OnlineDisplays.EMPTY_TXT
		));
		updateEmissive();
	}
	
	public void updateEmissive()
	{
		this.emissiveToggle.setXTexStart(display.isEmissive.get() ? 0 : 20);
	}
	
	private void toggleEmissive()
	{
		boolean nv = !display.isEmissive.get();
		display.isEmissive.set(nv);
		Network.sendToServer(new PacketSetEmissiveFlag(display, nv));
		updateEmissive();
	}
	
	private void beginSelectingLocalFile()
	{
		FileBrowserScreen scr = new FileBrowserScreen(file ->
		{
			new TransportSessionBuilder()
					.addData(UploadLocalFileSession.generate(file, display))
					.setAcceptor(UploadLocalFileSession.class)
					.build()
					.sendToServer();
		}, null);
		
		minecraft.pushGuiLayer(scr);
	}
	
	private void applyChanges()
	{
		String cachedURL = url.getValue();
		Network.sendToServer(new PacketUpdateTransforms(display));
		if(!Objects.equals(cachedURL, display.imageURL.get()))
		{
			display.updateURL(cachedURL);
			Network.sendToServer(new PacketUpdateURL(display));
		}
	}
	
	private void applyAspectRatio()
	{
		IDisplayableTexture tex = display.image;
		if(tex == null) return;
		
		TileDisplay.DisplayMatrix matrix = display.matrix;
		
		float curAR = matrix.scaleX / matrix.scaleY;
		float desiredAR = tex.getWidth() / (float) tex.getHeight();
		
		if(Math.abs(curAR - desiredAR) < 0.0001)
			return;
		
		if(matrix.scaleX > matrix.scaleY) // X-centric aspect ratio
		{
			float ratioW = tex.getHeight() / (float) tex.getWidth();
			sy.setValue(Float.toString(ratioW * matrix.scaleX));
		} else // Y-centric aspect ratio
		{
			float ratioH = tex.getWidth() / (float) tex.getHeight();
			sx.setValue(Float.toString(ratioH * matrix.scaleY));
		}
	}
	
	@Override
	public void removed()
	{
		this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
		Network.sendToServer(new PacketRequestDisplaySync(display));
		super.removed();
	}
	
	public Optional<Float> tryParseAndColorize(EditBox tf)
	{
		try
		{
			float v = Float.parseFloat(tf.getValue());
			if(!Float.isFinite(v)) throw new NumberFormatException();
			tf.setTextColor(0x22FF11);
			return Optional.of(v);
		} catch(Throwable e)
		{
			tf.setTextColor(0xFF2211);
		}
		return Optional.empty();
	}
	
	@Override
	public void tick()
	{
		super.tick();
		display.matrix.translateX = tryParseAndColorize(tx).orElse(display.matrix.translateX);
		display.matrix.translateY = tryParseAndColorize(ty).orElse(display.matrix.translateY);
		display.matrix.translateZ = tryParseAndColorize(tz).orElse(display.matrix.translateZ);
		display.matrix.rotateX = tryParseAndColorize(rx).orElse(display.matrix.rotateX);
		display.matrix.rotateY = tryParseAndColorize(ry).orElse(display.matrix.rotateY);
		display.matrix.rotateZ = tryParseAndColorize(rz).orElse(display.matrix.rotateZ);
		display.matrix.scaleX = tryParseAndColorize(sx).orElse(display.matrix.scaleX);
		display.matrix.scaleY = tryParseAndColorize(sy).orElse(display.matrix.scaleY);
		
		boolean validURL = URL_MATCHER.test(url.getValue());
		url.setTextColor(validURL ? 0x22FF11 : 0xFF2211);
	}
	
	@Override
	public void render(PoseStack mat, int mouseX, int mouseY, float partial)
	{
		renderBackground(mat);
		
		FXUtils.bindTexture(texture);
		blit(mat, guiLeft, guiTop, 0, 0, xSize, ySize);
		font.draw(mat, getTitle(), guiLeft + 8, guiTop + 6, 0x222222);
		
		for(GuiEventListener w : children())
			if(w instanceof Widget ww)
				ww.render(mat, mouseX, mouseY, partial);
		
		for(GuiEventListener w : children())
			if(w instanceof AbstractWidget wg && w.isMouseOver(mouseX, mouseY))
				renderTooltip(mat, Collections.singletonList(wg.getMessage().getVisualOrderText()), mouseX, mouseY, font);
		
		super.render(mat, mouseX, mouseY, partial);
	}
	
	@Override
	public boolean mouseClicked(double x, double y, int btn)
	{
		return super.mouseClicked(x, y, btn);
	}
	
	@Override
	public boolean keyPressed(int k1, int k2, int wtf)
	{
		InputConstants.Key key = InputConstants.getKey(k1, k2);
		
		if(this.minecraft.options.keyInventory.isActiveAndMatches(key) && !url.isFocused())
		{
			this.onClose();
			return true;
		}
		
		boolean f = super.keyPressed(k1, k2, wtf);
		
		if(url.isFocused())
		{
			String cachedURL = url.getValue();
			boolean validURL = URL_MATCHER.test(cachedURL);
			url.setTextColor(validURL ? 0x22FF11 : 0xFF2211);
		}
		
		return f;
	}
	
	@Override
	public boolean isPauseScreen()
	{
		return false;
	}
}