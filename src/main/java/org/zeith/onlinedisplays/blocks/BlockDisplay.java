package org.zeith.onlinedisplays.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.*;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.*;
import net.minecraft.util.text.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.hammerlib.api.blocks.ICustomBlockItem;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.client.render.ister.DisplayISTER;
import org.zeith.onlinedisplays.net.PacketOpenDisplayConfig;
import org.zeith.onlinedisplays.tiles.TileDisplay;
import org.zeith.onlinedisplays.util.ImageData;

import javax.annotation.Nullable;
import java.util.*;

public class BlockDisplay
		extends ContainerBlock
		implements ICustomBlockItem
{
	public BlockDisplay(Properties props)
	{
		super(props);
	}
	
	VoxelShape SHAPE = box(2, 2, 2, 14, 14, 14);
	
	public ItemStack save(TileDisplay display)
	{
		ItemStack st = new ItemStack(this);
		
		if(display != null
				&& !StringUtils.isNullOrEmpty(display.imageHash.get()))
		{
			
			CompoundNBT nbt = new CompoundNBT();
			nbt.put("HL", display.writeNBT(new CompoundNBT()));
			st.addTagElement("BlockEntityTag", nbt);
			
			String hash = display.imageHash.get();
			ImageData id = OnlineDisplays.PROXY.getImageContainer(display.getLevel())
					.load(hash);
			
			if(id != null)
			{
				CompoundNBT dsp = new CompoundNBT();
				ListNBT lore = new ListNBT();
				
				lore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(
						new StringTextComponent(id.getFileName())
								.withStyle(Style.EMPTY
										.withColor(Color.fromRgb(0x22FFFF))
								)
				)));
				
				lore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(
						OnlineDisplays.info("emissive." + display.isEmissive.get())
								.withStyle(Style.EMPTY
										.withColor(Color.fromRgb(display.isEmissive.get() ? 0xFFFF11 : 0x666666))
								)
				)));
				
				dsp.put("Lore", lore);
				st.addTagElement("display", dsp);
			}
		}
		
		return st;
	}
	
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder)
	{
		TileDisplay display = Cast.cast(builder.getParameter(LootParameters.BLOCK_ENTITY), TileDisplay.class);
		return Collections.singletonList(save(display));
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx)
	{
		if(reader instanceof World && !OnlineDisplays.getModSettings().survivalMode && ((World) reader).isClientSide && !OnlineDisplays.PROXY.isCreative())
			return VoxelShapes.empty();
		return SHAPE;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx)
	{
		return VoxelShapes.empty();
	}
	
	@Override
	public VoxelShape getVisualShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext ctx)
	{
		return SHAPE;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World level, BlockPos pos, Random rng)
	{
		ClientPlayerEntity player = Minecraft.getInstance().player;
		boolean flag = false;
		if(player != null)
			for(ItemStack st : player.getHandSlots())
				if(st.getItem() == asItem())
				{
					flag = true;
					break;
				}
		if(flag)
			level.addParticle(ParticleTypes.BARRIER, (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
	}
	
	@Override
	public ActionResultType use(BlockState state, World level, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult res)
	{
		if(!level.isClientSide)
		{
			TileDisplay tile = Cast.cast(level.getBlockEntity(pos), TileDisplay.class);
			if(tile != null)
				Network.sendTo(player, new PacketOpenDisplayConfig(tile));
		}
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state)
	{
		return super.hasTileEntity(state);
	}
	
	@Nullable
	@Override
	public TileEntity newBlockEntity(IBlockReader world)
	{
		return new TileDisplay();
	}
	
	@Override
	public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, PlayerEntity player)
	{
		if(player == null || player.isShiftKeyDown())
			return new ItemStack(this);
		return save(Cast.cast(world.getBlockEntity(pos), TileDisplay.class));
	}
	
	@Override
	public BlockItem createBlockItem()
	{
		return new BlockItem(this, new Item.Properties()
				.tab(ItemGroup.TAB_REDSTONE)
				.setISTER(() -> DisplayISTER::new)
		);
	}
}