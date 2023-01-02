package org.zeith.onlinedisplays.blocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.*;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.zeith.hammerlib.api.blocks.IItemGroupBlock;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.net.PacketOpenDisplayConfig;
import org.zeith.onlinedisplays.tiles.TileDisplay;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockDisplay
		extends ContainerBlock
		implements IItemGroupBlock
{
	public BlockDisplay(Properties props)
	{
		super(props);
	}
	
	VoxelShape SHAPE = box(2, 2, 2, 14, 14, 14);
	
	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_, ISelectionContext p_220053_4_)
	{
		return SHAPE;
	}
	
	@Override
	public VoxelShape getVisualShape(BlockState p_230322_1_, IBlockReader reader, BlockPos p_230322_3_, ISelectionContext p_230322_4_)
	{
		if(reader instanceof World && ((World) reader).isClientSide)
		{
			if(!OnlineDisplays.PROXY.isCreative())
				return VoxelShapes.empty();
		}
		
		return super.getVisualShape(p_230322_1_, reader, p_230322_3_, p_230322_4_);
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
	public ItemGroup getItemGroup()
	{
		return ItemGroup.TAB_REDSTONE;
	}
}