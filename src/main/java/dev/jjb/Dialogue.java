package dev.jjb;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Очередь диалогов для мультиплеера: реплики видят все игроки сразу,
 * идут по одной с паузой, не перекрывают друг друга.
 * В тексте можно писать %n — туда подставятся ники всех игроков онлайн.
 */
public final class Dialogue {
    private Dialogue() {}

    public record Line(String who, String text) {}

    public static Line L(String who, String text) {
        return new Line(who, text);
    }

    public static Line[] lines(Line... l) {
        return l;
    }

    public static final int LINE_GAP_TICKS = 70;

    private record Entry(long at, Runnable run) {}

    private static final List<Entry> QUEUE = new ArrayList<>();
    private static long cursor = 0;

    private static final Map<String, String> COLORS = Map.of(
            "Рэн", "§b",
            "Мира", "§a",
            "Тэцу", "§6",
            "Страж Разлома", "§c",
            "Хранитель Квартала", "§6",
            "Аэтер", "§4",
            "Система", "§7"
    );

    public static String names(MinecraftServer s) {
        List<ServerPlayerEntity> list = s.getPlayerManager().getPlayerList();
        if (list.isEmpty()) return "путники";
        return list.stream().map(p -> p.getName().getString()).collect(Collectors.joining(", "));
    }

    private static Text render(MinecraftServer s, Line l) {
        String color = COLORS.getOrDefault(l.who(), "§f");
        return Text.literal(color + "[" + l.who() + "] §f" + l.text().replace("%n", names(s)));
    }

    /** Поставить реплики в общую очередь. */
    public static void say(MinecraftServer s, Line... lines) {
        for (Line l : lines) {
            long at = Math.max(s.getTicks(), cursor);
            cursor = at + LINE_GAP_TICKS;
            QUEUE.add(new Entry(at, () -> s.getPlayerManager().broadcast(render(s, l), false)));
        }
    }

    /** Выполнить действие после всех уже поставленных реплик. */
    public static void later(MinecraftServer s, Runnable r) {
        long at = Math.max(s.getTicks(), cursor);
        cursor = at + 20;
        QUEUE.add(new Entry(at, r));
    }

    public static void tick(MinecraftServer s) {
        if (QUEUE.isEmpty()) return;
        long now = s.getTicks();
        for (Iterator<Entry> it = QUEUE.iterator(); it.hasNext(); ) {
            Entry e = it.next();
            if (e.at() <= now) {
                it.remove();
                e.run().run();
            }
        }
    }

    public static void clear() {
        QUEUE.clear();
        cursor = 0;
    }
}
