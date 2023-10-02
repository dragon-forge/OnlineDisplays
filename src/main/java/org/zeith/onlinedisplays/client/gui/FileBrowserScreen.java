package org.zeith.onlinedisplays.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import org.zeith.hammerlib.client.utils.*;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.client.texture.*;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.*;
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
		
		this.fileList = addWidget(new FileListWidget(this, x, y, 200, 160, rootDirectory));
		this.setFocused(this.fileList);
		
		y += 162;
		
		this.addRenderableWidget(Button.builder(OnlineDisplays.gui("cancel"), (button) ->
		{
			this.cancel();
		}).pos(x, y).size(99, 20).build());
		
		x += 101;
		
		this.addRenderableWidget(Button.builder(OnlineDisplays.gui("select"), (button) ->
		{
			this.selectFile();
		}).pos(x, y).size(99, 20).build());
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
	public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTicks)
	{
		int x = width / 2 - 100;
		int y = height / 4 - 10;
		
		this.renderBackground(gfx);
		Scissors.begin(x, y, 200, 160);
		this.fileList.render(gfx, mouseX, mouseY, partialTicks);
		Scissors.end();
		
		gfx.drawCenteredString(font, OnlineDisplays.gui("select_file"), width / 2, 20, 0xFFFFFF);
		super.render(gfx, mouseX, mouseY, partialTicks);
	}
	
	@Override
	public boolean isPauseScreen()
	{
		return false;
	}
	
	private class FileListWidget
			extends AbstractSelectionList<FileListWidget.FileListEntry>
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
		
		@Override
		public void updateNarration(NarrationElementOutput out)
		{
		}
		
		private class FileListEntry
				extends AbstractSelectionList.Entry<FileListEntry>
		{
			private final File file;
			private final Font font;
			private final FileListWidget widget;
			private boolean isRoot;
			
			public FileListEntry(File file, Font font, FileListWidget widget, boolean isRoot)
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
			public void render(GuiGraphics gfx, int id, int y, int x, int width, int heightPadded, int p_93529_, int p_93530_, boolean hovered, float p_93532_)
			{
				boolean isSelected = widget.getSelected() == this;
				String name = getName();
				
				try
				{
					Icon icon = FileSystemView.getFileSystemView().getSystemIcon(file);
					
					if(icon instanceof ImageIcon ii)
					{
						IDisplayableTexture tx = OnlineTextureParser.getTextureFromIcon(ii);
						if(tx != null)
						{
							FXUtils.bindTexture(tx.getPath(System.currentTimeMillis()));
							RenderSystem.enableBlend();
							RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
							RenderUtils.drawFullTexturedModalRect(gfx, x - 0.5F, y - 0.5F, 8, 8);
						}
					}
				} catch(Throwable e)
				{
					e.printStackTrace();
				}
				
				gfx.drawString(font, name, x + 10, y, hovered ? 0xFF5500 : (isSelected ? 0xFFAA00 : 0xFFFFFF));
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
									rootDirectory.getAbsolutePath()
											.equals(new File(rootDirectory, "..").getCanonicalPath())
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
}