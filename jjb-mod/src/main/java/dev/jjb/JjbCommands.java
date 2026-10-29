package dev.jjb;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public final class JjbCommands {
    private JjbCommands() {}

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("jjb")
                .then(literal("story")
                        // любой игрок может запустить общий сюжет
                        .then(literal("start").executes(c -> {
                            Story.start(c.getSource().getPlayerOrThrow());
                            return 1;
                        }))
                        .then(literal("status").executes(c -> {
                            MinecraftServer s = c.getSource().getServer();
                            c.getSource().sendFeedback(() -> Text.literal(Story.status(s)), false);
                            return 1;
                        }))
                        // повтор главы: любой игрок (если босс пропал или группа застряла)
                        .then(literal("retry").executes(c -> {
                            Story.retry(c.getSource().getServer());
                            return 1;
                        }))
                        // следующие команды только для оператора
                        .then(literal("skip").requires(s -> s.hasPermissionLevel(2)).executes(c -> {
                            Story.skip(c.getSource().getServer());
                            return 1;
                        }))
                        .then(literal("reset").requires(s -> s.hasPermissionLevel(2)).executes(c -> {
                            Story.reset(c.getSource().getServer());
                            c.getSource().sendFeedback(() -> Text.literal("Сюжет сброшен."), true);
                            return 1;
                        })))
                .then(literal("spirit").executes(c -> {
                    ServerPlayerEntity p = c.getSource().getPlayerOrThrow();
                    int v = ModData.get(c.getSource().getServer()).getSpirit(p.getUuid());
                    p.sendMessage(Text.literal("Дух: " + v + "/" + Balance.SPIRIT_MAX));
                    return v;
                }))
                .then(literal("map").requires(s -> s.hasPermissionLevel(2)).executes(c -> {
                    ModData d = ModData.get(c.getSource().getServer());
                    MapBuilder.buildAll(c.getSource().getServer().getOverworld());
                    d.mapBuilt = true;
                    d.markDirty();
                    c.getSource().sendFeedback(() -> Text.literal("Карта построена (4 региона на Y=" + MapBuilder.Y + ")."), true);
                    return 1;
                }))
                .then(literal("give").requires(s -> s.hasPermissionLevel(2))
                        .then(argument("id", StringArgumentType.word())
                                .suggests((ctx, b) -> {
                                    Abilities.ALL.keySet().forEach(b::suggest);
                                    return b.buildFuture();
                                })
                                .executes(c -> {
                                    ServerPlayerEntity p = c.getSource().getPlayerOrThrow();
                                    ItemStack st = ModItems.stack(StringArgumentType.getString(c, "id"));
                                    if (st.isEmpty()) {
                                        c.getSource().sendError(Text.literal("Нет такого предмета"));
                                        return 0;
                                    }
                                    p.giveItemStack(st);
                                    return 1;
                                })))
                .then(literal("info").executes(c -> {
                    c.getSource().sendFeedback(() -> Text.literal("Кооп-сюжет: " + Story.CHAPTERS.size()
                            + " глав, целевое время ~" + Story.totalMinutes() / 60.0 + " ч. Играть лучше 2-4 игрокам."), false);
                    return 1;
                })));
    }
}
