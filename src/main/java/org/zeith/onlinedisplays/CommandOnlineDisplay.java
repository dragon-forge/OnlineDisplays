package org.zeith.onlinedisplays;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.zeith.hammerlib.util.java.Cast;
import org.zeith.onlinedisplays.level.LevelImageStorage;
import org.zeith.onlinedisplays.tiles.TileDisplay;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.*;

public class CommandOnlineDisplay
{
	private static <T> LiteralArgumentBuilder<CommandSourceStack> tweakDisplayParam(String name, ArgumentType<T> arg, BiFunction<CommandContext<CommandSourceStack>, String, T> getArg, @Nullable Function<TileDisplay, Float> get, BiConsumer<TileDisplay, T> handler)
	{
		boolean isNumber = arg instanceof FloatArgumentType && get != null;
		
		var pArg = Commands.argument("pos", BlockPosArgument.blockPos())
				.then(Commands.literal("set")
						.then(Commands.argument("value", arg)
								.executes(s ->
								{
									var dim = DimensionArgument.getDimension(s, "dimension");
									var pos = BlockPosArgument.getSpawnablePos(s, "pos");
									var td = Cast.cast(dim.getBlockEntity(pos), TileDisplay.class);
									if(td != null)
									{
										handler.accept(td, getArg.apply(s, "value"));
										for(var player : dim.getChunkSource().chunkMap.getPlayers(new ChunkPos(td.getBlockPos()), false))
											player.connection.send(td.getUpdatePacket());
										return Command.SINGLE_SUCCESS;
									}
									return 0;
								})
						)
				);
		
		if(isNumber)
		{
			pArg.then(Commands.literal("plus")
					.then(Commands.argument("value", arg)
							.executes(s ->
							{
								var dim = DimensionArgument.getDimension(s, "dimension");
								var pos = BlockPosArgument.getSpawnablePos(s, "pos");
								var td = Cast.cast(dim.getBlockEntity(pos), TileDisplay.class);
								var val = getArg.apply(s, "value");
								if(td != null && val instanceof Float fv)
								{
									handler.accept(td, Cast.cast(get.apply(td) +/*PLUS*/ fv));
									for(var player : dim.getChunkSource().chunkMap.getPlayers(new ChunkPos(td.getBlockPos()), false))
										player.connection.send(td.getUpdatePacket());
									return Command.SINGLE_SUCCESS;
								}
								return 0;
							})
					)
			);
			
			pArg.then(Commands.literal("minus")
					.then(Commands.argument("value", arg)
							.executes(s ->
							{
								var dim = DimensionArgument.getDimension(s, "dimension");
								var pos = BlockPosArgument.getSpawnablePos(s, "pos");
								var td = Cast.cast(dim.getBlockEntity(pos), TileDisplay.class);
								var val = getArg.apply(s, "value");
								if(td != null && val instanceof Float fv)
								{
									handler.accept(td, Cast.cast(get.apply(td) - /*MINUS*/ fv));
									for(var player : dim.getChunkSource().chunkMap.getPlayers(new ChunkPos(td.getBlockPos()), false))
										player.connection.send(td.getUpdatePacket());
									return Command.SINGLE_SUCCESS;
								}
								return 0;
							})
					)
			);
			
			pArg.then(Commands.literal("multiply")
					.then(Commands.argument("value", arg)
							.executes(s ->
							{
								var dim = DimensionArgument.getDimension(s, "dimension");
								var pos = BlockPosArgument.getSpawnablePos(s, "pos");
								var td = Cast.cast(dim.getBlockEntity(pos), TileDisplay.class);
								var val = getArg.apply(s, "value");
								if(td != null && val instanceof Float fv)
								{
									handler.accept(td, Cast.cast(get.apply(td) * /*MUL*/ fv));
									for(var player : dim.getChunkSource().chunkMap.getPlayers(new ChunkPos(td.getBlockPos()), false))
										player.connection.send(td.getUpdatePacket());
									return Command.SINGLE_SUCCESS;
								}
								return 0;
							})
					)
			);
			
			pArg.then(Commands.literal("divide")
					.then(Commands.argument("value", arg)
							.executes(s ->
							{
								var dim = DimensionArgument.getDimension(s, "dimension");
								var pos = BlockPosArgument.getSpawnablePos(s, "pos");
								var td = Cast.cast(dim.getBlockEntity(pos), TileDisplay.class);
								var val = getArg.apply(s, "value");
								if(td != null && val instanceof Float fv && Math.abs(fv) > 0.00001F)
								{
									handler.accept(td, Cast.cast(get.apply(td) / /*DIV*/ fv));
									for(var player : dim.getChunkSource().chunkMap.getPlayers(new ChunkPos(td.getBlockPos()), false))
										player.connection.send(td.getUpdatePacket());
									return Command.SINGLE_SUCCESS;
								}
								return 0;
							})
					)
			);
		}
		
		return Commands.literal(name).then(Commands.argument("dimension", DimensionArgument.dimension()).then(pArg));
	}
	
	public static LiteralArgumentBuilder<CommandSourceStack> create()
	{
		return Commands.literal(OnlineDisplays.MOD_ID)
				.requires(s -> s.hasPermission(2))
				.then(Commands.literal("preload")
						.then(Commands.argument("url", StringArgumentType.string())
								.then(Commands.argument("cache_for_clients", EntityArgument.players())
										.executes(s ->
										{
											String url = StringArgumentType.getString(s, "url");
											var cacheForClients = EntityArgument.getPlayers(s, "cache_for_clients")
													.stream()
													.map(ServerPlayer::getGameProfile)
													.map(GameProfile::getId)
													.toArray(UUID[]::new);
											
											var server = s.getSource().getServer();
											LevelImageStorage storage = LevelImageStorage.get(server.overworld());
											
											storage.queueDownload(url, cacheForClients);
											
											return 0;
										})
								)
						)
				)
				.then(Commands.literal("configure")
						.then(tweakDisplayParam("offset_x", FloatArgumentType.floatArg(), FloatArgumentType::getFloat, td -> td.matrix.translateX, (td, val) -> td.matrix.translateX = val))
						.then(tweakDisplayParam("offset_y", FloatArgumentType.floatArg(), FloatArgumentType::getFloat, td -> td.matrix.translateY, (td, val) -> td.matrix.translateY = val))
						.then(tweakDisplayParam("offset_z", FloatArgumentType.floatArg(), FloatArgumentType::getFloat, td -> td.matrix.translateZ, (td, val) -> td.matrix.translateZ = val))
						
						.then(tweakDisplayParam("rotation_x", FloatArgumentType.floatArg(), FloatArgumentType::getFloat, td -> td.matrix.rotateX, (td, val) -> td.matrix.rotateX = val))
						.then(tweakDisplayParam("rotation_y", FloatArgumentType.floatArg(), FloatArgumentType::getFloat, td -> td.matrix.rotateY, (td, val) -> td.matrix.rotateY = val))
						.then(tweakDisplayParam("rotation_z", FloatArgumentType.floatArg(), FloatArgumentType::getFloat, td -> td.matrix.rotateZ, (td, val) -> td.matrix.rotateZ = val))
						
						.then(tweakDisplayParam("scale_x", FloatArgumentType.floatArg(), FloatArgumentType::getFloat, td -> td.matrix.scaleX, (td, val) -> td.matrix.scaleX = val))
						.then(tweakDisplayParam("scale_y", FloatArgumentType.floatArg(), FloatArgumentType::getFloat, td -> td.matrix.scaleY, (td, val) -> td.matrix.scaleY = val))
						
						.then(tweakDisplayParam("emissive", BoolArgumentType.bool(), BoolArgumentType::getBool, null, (td, val) -> td.isEmissive.setBool(val)))
						.then(tweakDisplayParam("url", StringArgumentType.string(), StringArgumentType::getString, null, TileDisplay::updateURL))
				);
	}
}