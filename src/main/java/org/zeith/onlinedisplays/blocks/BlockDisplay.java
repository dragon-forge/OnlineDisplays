package org.zeith.onlinedisplays.blocks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.zeith.hammerlib.api.blocks.ICustomBlockItem;
import org.zeith.hammerlib.api.forge.BlockAPI;
import org.zeith.hammerlib.net.Network;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.OnlineDisplays;
import org.zeith.onlinedisplays.client.render.ister.DisplayISTER;
import org.zeith.onlinedisplays.net.PacketOpenDisplayConfig;
import org.zeith.onlinedisplays.tiles.TileDisplay;
import org.zeith.onlinedisplays.util.ImageData;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class BlockDisplay
		extends BaseEntityBlock
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
				&& !StringUtil.isNullOrEmpty(display.imageHash.get()))
		{
			
			CompoundTag nbt = new CompoundTag();
			nbt.put("HL", display.writeNBT(new CompoundTag()));
			st.addTagElement("BlockEntityTag", nbt);
			
			String hash = display.imageHash.get();
			ImageData id = OnlineDisplays.PROXY.getImageContainer(display.getLevel())
					.load(hash);
			
			if(id != null)
			{
				CompoundTag dsp = new CompoundTag();
				ListTag lore = new ListTag();
				
				lore.add(StringTag.valueOf(Component.Serializer.toJson(
						Component.literal(id.getFileName())
								.withStyle(Style.EMPTY
										.withColor(0x22FFFF)
								)
				)));
				
				lore.add(StringTag.valueOf(Component.Serializer.toJson(
						OnlineDisplays.info("emissive." + display.isEmissive.get())
								.withStyle(Style.EMPTY
										.withColor(display.isEmissive.get() ? 0xFFFF11 : 0x666666)
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
		TileDisplay display = Cast.cast(builder.getParameter(LootContextParams.BLOCK_ENTITY), TileDisplay.class);
		return Collections.singletonList(save(display));
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx)
	{
		if(reader instanceof Level lvl && !OnlineDisplays.getModSettings().survivalMode && lvl.isClientSide && !OnlineDisplays.PROXY.isCreative())
			return Shapes.empty();
		return SHAPE;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx)
	{
		return Shapes.empty();
	}
	
	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter reader, BlockPos pos, CollisionContext ctx)
	{
		return SHAPE;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource rng)
	{
		Player player = Minecraft.getInstance().player;
		boolean flag = false;
		if(player != null)
			for(ItemStack st : player.getHandSlots())
				if(st.getItem() == asItem())
				{
					flag = true;
					break;
				}
		if(flag)
			level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK_MARKER, state), (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult res)
	{
		if(!level.isClientSide)
		{
			TileDisplay tile = Cast.cast(level.getBlockEntity(pos), TileDisplay.class);
			if(tile != null)
				Network.sendTo(player, new PacketOpenDisplayConfig(tile));
		}
		return InteractionResult.SUCCESS;
	}
	
	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return new TileDisplay(pos, state);
	}
	
	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153212_, BlockState p_153213_, BlockEntityType<T> p_153214_)
	{
		return BlockAPI.ticker();
	}
	
	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player)
	{
		if(player == null || player.isShiftKeyDown())
			return new ItemStack(this);
		return save(Cast.cast(level.getBlockEntity(pos), TileDisplay.class));
	}
	
	@Override
	public BlockItem createBlockItem()
	{
		return new BlockItem(this, new Item.Properties())
		{
			@Override
			public void initializeClient(Consumer<IClientItemExtensions> consumer)
			{
				consumer.accept(new IClientItemExtensions()
				{
					@Override
					public BlockEntityWithoutLevelRenderer getCustomRenderer()
					{
						return DisplayISTER.INSTANCE;
					}
				});
			}
		};
	}
}