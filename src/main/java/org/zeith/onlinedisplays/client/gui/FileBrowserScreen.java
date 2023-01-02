package org.zeith.onlinedisplays.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.gui.widget.list.ExtendedList;
import org.zeith.hammerlib.client.utils.*;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.client.texture.IDisplayableTexture;
import org.zeith.onlinedisplays.client.texture.OnlineTextureParser;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

public class FileBrowserScreen
		extends Screen
{
	private File selectedFile, rootDirectory = new File(".");
	private FileListWidget fileList;
	private Consumer<File> onChosen;
	private Runnable onCancelled;
	
	public FileBrowserScreen(Consumer<File> onChosen, Runnable onCancelled)
	{
		super(OnlineDisplays.EMPTY_TXT);
		this.onChosen = onChosen;
		this.onCancelled = onCancelled;
	}
	
	public FileBrowserScreen withRootDirectory(File dir)
	{
		if(dir.isDirectory())
			rootDirectory = dir;
		return this;
	}
	
	@Override
	public void init()
	{
		int x = width / 2 - 100;
		int y = height / 4 - 10;
		
		this.fileList = new FileListWidget(this, x, y, 200, 160, rootDirectory);
		this.children.add(this.fileList);
		this.setFocused(this.fileList);
		
		y += 162;
		
		this.addButton(new Button(x, y, 99, 20, OnlineDisplays.gui("cancel"), (button) ->
		{
			this.cancel();
		}));
		
		x += 101;
		
		this.addButton(new Button(x, y, 99, 20, OnlineDisplays.gui("select"), (button) ->
		{
			this.selectFile();
		}));
	}
	
	private void selectFile()
	{
		this.minecraft.popGuiLayer();
		if(selectedFile != null)
			onChosen.accept(selectedFile);
		else if(onCancelled != null)
			onCancelled.run();
	}
	
	private void cancel()
	{
		if(onCancelled != null)
			onCancelled.run();
		this.minecraft.popGuiLayer();
	}
	
	@Override
	public void render(MatrixStack mat, int mouseX, int mouseY, float partialTicks)
	{
		int x = width / 2 - 100;
		int y = height / 4 - 10;
		
		this.renderBackground(mat);
		Scissors.begin();
		Scissors.scissor(x, y, 200, 160);
		this.fileList.render(mat, mouseX, mouseY, partialTicks);
		Scissors.end();
		
		drawCenteredString(mat, font, OnlineDisplays.gui("select_file"), width / 2, 20, 0xFFFFFF);
		super.render(mat, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public boolean isPauseScreen()
	{
		return false;
	}
	
	private class FileListWidget
			extends ExtendedList<FileListEntry>
	{
		private final FileBrowserScreen parentScreen;
		private File rootDirectory;
		
		public FileListWidget(FileBrowserScreen screen, int x, int y, int width, int height, File rootDirectory)
		{
			super(screen.minecraft, width, height, y, y + height, screen.font.lineHeight + 2);
			setLeftPos(x);
			this.parentScreen = screen;
			this.rootDirectory = rootDirectory;
			this.refreshEntries();
			headerHeight = 0;
			setRenderTopAndBottom(false);
		}
		
		@Override
		protected int getScrollbarPosition()
		{
			return x0 + this.width - 6;
		}
		
		private void refreshEntries()
		{
			setScrollAmount(0);
			
			clearEntries();
			
			boolean listingRoots = this.rootDirectory == null;
			File[] files = listingRoots ? File.listRoots() : this.rootDirectory.listFiles();
			
			if(this.rootDirectory != null)
			{
				File parent = new File(this.rootDirectory, "..");
				if(parent.isDirectory())
				{
					addEntry(new FileListEntry(parent, parentScreen.font, this, false));
				}
			}
			
			if(files != null)
			{
				for(File file : files)
					if(file.isDirectory() && (!file.isHidden() || this.rootDirectory == null))
						addEntry(new FileListEntry(file, parentScreen.font, this, listingRoots));
				for(File file : files)
					if(file.isFile() && (!file.isHidden() || this.rootDirectory == null))
						addEntry(new FileListEntry(file, parentScreen.font, this, listingRoots));
			}
		}
		
		@Override
		public int getRowWidth()
		{
			return this.width;
		}
	}
	
	private class FileListEntry
			extends ExtendedList.AbstractListEntry<FileListEntry>
	{
		private final File file;
		private final FontRenderer font;
		private final FileListWidget widget;
		private boolean isRoot;
		
		public FileListEntry(File file, FontRenderer font, FileListWidget widget, boolean isRoot)
		{
			this.file = file;
			this.font = font;
			this.widget = widget;
			this.isRoot = isRoot;
		}
		
		public String getName()
		{
			return isRoot ? this.file.toString() : this.file.getName();
		}
		
		@Override
		public void render(MatrixStack mat, int id, int y, int x, int width, int heightPadded, int p_230432_7_, int p_230432_8_, boolean hovered, float p_230432_10_)
		{
			boolean isSelected = widget.getSelected() == this;
			String name = getName();
			
			try
			{
				Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
				
				if(icon instanceof ImageIcon)
				{
					ImageIcon ii = (ImageIcon) icon;
					IDisplayableTexture tx = OnlineTextureParser.getTextureFromIcon(ii);
					if(tx != null)
					{
						FXUtils.bindTexture(tx.getPath(System.currentTimeMillis()));
						RenderSystem.enableBlend();
						RenderSystem.color4f(1F, 1F, 1F, 1F);
						RenderUtils.drawFullTexturedModalRect(x - 0.5F, y - 0.5F, 8, 8);
					}
				}
			} catch(Throwable e)
			{
				e.printStackTrace();
			}
			
			font.draw(mat, name, x + 10, y, hovered ? 0xFF5500 : (isSelected ? 0xFFAA00 : 0xFFFFFF));
		}
		
		private long lastClickTime;
		
		@Override
		public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int btn)
		{
			if(btn == 0)
			{
				if(System.currentTimeMillis() - lastClickTime < 500L && file.isDirectory())
				{
					try
					{
						rootDirectory = file.getCanonicalFile();
						
						if(file.getName().equals("..") &&
								rootDirectory.getAbsolutePath().equals(new File(rootDirectory, "..").getCanonicalPath())
								&& file.getParentFile().toPath().getNameCount() == 0)
						{
							rootDirectory = null;
							widget.rootDirectory = null;
							widget.refreshEntries();
							return true;
						}
					} catch(IOException e)
					{
						rootDirectory = file.toPath().normalize().toFile();
					}
					
					widget.rootDirectory = rootDirectory;
					widget.refreshEntries();
				} else
				{
					lastClickTime = System.currentTimeMillis();
					widget.setSelected(this);
					if(file.isFile())
						selectedFile = file;
				}
				return true;
			}
			return false;
		}
	}
}
