package dev.jjb;

import dev.jjb.Dialogue.Line;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static dev.jjb.Dialogue.L;
import static dev.jjb.Dialogue.lines;

/**
 * Кооперативный сюжет: 12 глав по ~20 минут = ~4 часа для всей группы.
 * Глава и прогресс общие. Убийства любого игрока идут в общий счётчик.
 * Рекомендуется играть вдвоём-четверых; цели и боссы масштабируются (см. Balance).
 *
 * Время в главах целевое, не измеренное в игре. Подгоняй need и bossHp под свой темп.
 */
public final class Story {
    private Story() {}

    public enum Type { KILL, REACH, BOSS }

    public record Chapter(String title, int minutes, int region, Type type, String arg,
                          int need, int bossHp, String[] rewards,
                          Line[] intro, Line[] mid, Line[] outro) {}

    private static final String[] NONE = {};

    private static String[] r(String... s) {
        return s;
    }

    public static final List<Chapter> CHAPTERS = List.of(

            // 1
            new Chapter("Пробуждение", 20, 0, Type.KILL, "husk", 20, 0, r("stand_crimson_echo"),
                    lines(
                            L("Рэн", "Эй, вы! Живы? Не стойте столбом, они идут на ваш дух!"),
                            L("Мира", "Я Мира, это Рэн. А вы, значит, %n. Вокруг вас светится Резонанс, вы резонаторы."),
                            L("Рэн", "Резонанс даёт стенд, вторую душу, которая дерётся рядом с вами. Держитесь вместе и бейте мёртвых из песка.")),
                    lines(L("Мира", "Половина! Не разбредайтесь, одиночек они давят первыми.")),
                    lines(
                            L("Рэн", "Багровое Эхо ваше. ПКМ, чтобы ударить. Не забывайте про Дух, он не бесконечный."),
                            L("Мира", "Нам нужно в город. Пока он ещё стоит."))),

            // 2
            new Chapter("Первый клинок", 20, 0, Type.KILL, "stray", 25, 0, r("blade_frostveil"),
                    lines(
                            L("Рэн", "У меня тоже есть клинок. Тот, что у вас в руке, просит имени."),
                            L("Мира", "Ледяные тени это осколки разлома. Их тут будет много. Прикрывайте друг друга.")),
                    lines(L("Рэн", "Чувствуете холод в рукояти? Клинок вас слушает.")),
                    lines(
                            L("Рэн", "Ледяная Вуаль. Шикай на ПКМ, это только первая форма."),
                            L("Рэн", "Банкай откроется позже, когда докажете, что дойдёте до конца."))),

            // 3
            new Chapter("Разлом", 20, 1, Type.REACH, "", 1, 0, r("stand_iron_tide"),
                    lines(
                            L("Мира", "Небо над городом трескается каждый день. Маяк на краю Пустоши держит трещину."),
                            L("Рэн", "Идём к маяку. Кто отстанет, кричите, подождём.")),
                    NONE_LINES(),
                    lines(
                            L("Мира", "Маяк горит. Но трещина шире, чем я думала."),
                            L("Рэн", "Железный Прилив ваш. Отбрасывает толпу, самое то для разлома."))),

            // 4
            new Chapter("Страж Разлома", 20, 1, Type.BOSS, "Страж Разлома", 1, 120, r("blade_ember_sovereign"),
                    lines(
                            L("Страж Разлома", "Вы пахнете Пустотой. Назад."),
                            L("Рэн", "Он не злой, он на посту. Но живыми он нас не пропустит."),
                            L("Мира", "%n, вперёд! Я прикрою!")),
                    lines(L("Рэн", "Он слабеет, добивайте вместе!")),
                    lines(
                            L("Страж Разлома", "Он идёт... Аэтер... собирает осколки..."),
                            L("Мира", "Аэтер? Владыка Цитадели? Это же сказка."),
                            L("Рэн", "Сказки так не умирают. Пепельный Владыка теперь ваш."))),

            // 5
            new Chapter("Охотники", 20, 1, Type.KILL, "vindicator", 25, 0, r("stand_paper_moon"),
                    lines(
                            L("Мира", "Охотники Аэтера. Они уводят резонаторов в Цитадель живыми."),
                            L("Рэн", "Не дадим. Держите строй!")),
                    lines(L("Мира", "Поддержка за мной. %n, прикрывайте!")),
                    lines(
                            L("Рэн", "Слышали? Один из них проговорился: Тэцу ещё держит Белый Квартал."),
                            L("Мира", "Тэцу? Мой учитель! Я думала, его нет в живых."),
                            L("Мира", "Бумажная Луна лечит. Берите, пригодится."))),

            // 6
            new Chapter("Белый Квартал", 20, 2, Type.REACH, "", 1, 0, NONE,
                    lines(
                            L("Мира", "Белый Квартал. Тэцу закрыл его пять лет назад и никого не впускал."),
                            L("Рэн", "Вход у дальней колонны.")),
                    NONE_LINES(),
                    lines(
                            L("Тэцу", "Кто идёт? Назад, пока не поздно."),
                            L("Мира", "Учитель, это я!"),
                            L("Тэцу", "Мира? Нет. Ты уже не та, что я учил."))),

            // 7
            new Chapter("Тихие Стражи", 20, 2, Type.KILL, "pillager", 30, 0, r("stand_silent_hourglass"),
                    lines(
                            L("Тэцу", "Стражи, не подпускать."),
                            L("Рэн", "Они стреляют без предупреждения. Рассредоточьтесь!")),
                    lines(L("Мира", "Слышу его голос в их тишине. Он ведёт их, но не хочет убивать.")),
                    lines(
                            L("Рэн", "Тихие Часы замедляют время. Один кулдаун, и вся группа успевает выдохнуть."),
                            L("Рэн", "Идём на арену. Он ждёт."))),

            // 8
            new Chapter("Хранитель Квартала", 20, 2, Type.BOSS, "Хранитель Квартала", 1, 250, r("blade_hollow_lantern"),
                    lines(
                            L("Хранитель Квартала", "Я закрыл Квартал не от вас. От него. Но Аэтер не остановится, пока не соберёт все клинки."),
                            L("Рэн", "Тогда мы остановим его вместе."),
                            L("Хранитель Квартала", "Докажите.")),
                    lines(L("Мира", "Он сдерживается! Бейте, пока он не передумал!")),
                    lines(
                            L("Тэцу", "Достойны. Берите Фонарь, он слышит Пустоту."),
                            L("Тэцу", "Идите в Цитадель и не доверяйте шёпоту."),
                            L("Мира", "Учитель..."))),

            // 9
            new Chapter("Пробуждение Банкая", 20, 2, Type.KILL, "spider", 35, 0, NONE,
                    lines(
                            L("Тэцу", "Шикай это слова. Банкай это имя. Назовите клинок вслух."),
                            L("Рэн", "Shift+ПКМ клинком. Пауки-отголоски будут вашей тренировкой.")),
                    lines(L("Рэн", "Слышите звон? Это весь клинок отзывается!")),
                    lines(
                            L("Мира", "БАНКАЙ ваш. Берегите Дух, банкай тратит много."),
                            L("Рэн", "Теперь вы настоящие резонаторы. Идём в Цитадель."))),

            // 10
            new Chapter("Цитадель", 20, 3, Type.REACH, "", 1, 0, r("stand_gale_marionette"),
                    lines(
                            L("Рэн", "Врата Цитадели. Дальше я иду только с вами."),
                            L("Мира", "Он знает, что мы идём."),
                            L("Аэтер", "Добро пожаловать, осколки. Я ждал.")),
                    NONE_LINES(),
                    lines(
                            L("Мира", "Ветер тянет внутрь. Берите Марионетку, она вытянет врага к вам."),
                            L("Рэн", "Держимся вместе. Что бы он ни говорил."))),

            // 11
            new Chapter("Эхо Пустоты", 20, 3, Type.KILL, "wither_skeleton", 30, 0, r("blade_thunder_regent"),
                    lines(
                            L("Аэтер", "Каждый скелет мой гость. Некогда резонатор, как вы."),
                            L("Рэн", "Не жалейте. Они уже не они.")),
                    lines(L("Мира", "Среди них есть стенды. Они помнят прежних хозяев.")),
                    lines(
                            L("Рэн", "Громовой Регент ваш. Его хозяин пал здесь, теперь клинок выбрал вас."),
                            L("Аэтер", "Последний шаг. Идите ко мне."))),

            // 12
            new Chapter("Владыка Цитадели", 20, 3, Type.BOSS, "Владыка Цитадели", 1, 500, NONE,
                    lines(
                            L("Аэтер", "Я собрал силу всех, кого вы победили. Стража. Хранителя. Каждого."),
                            L("Рэн", "Он врёт, это только эхо!"),
                            L("Мира", "%n, вместе! Всё, что у нас есть!")),
                    lines(
                            L("Аэтер", "Невозможно... вы же осколки..."),
                            L("Рэн", "Он дрогнул! Добивайте!")),
                    lines(
                            L("Аэтер", "Резонанс... един..."),
                            L("Рэн", "Разлом закрывается."),
                            L("Мира", "Мы сделали это. %n, спасибо вам."),
                            L("Система", "Сюжет пройден. Спасибо за игру!")))
    );

    private static Line[] NONE_LINES() {
        return new Line[0];
    }

    /** Между главами: пока true, убийства и волны не считаются. */
    private static boolean transition = false;

    public static int totalMinutes() {
        return CHAPTERS.stream().mapToInt(Chapter::minutes).sum();
    }

    // ---------- масштаб под число игроков ----------

    private static int scaledPlayers(MinecraftServer s) {
        int n = s.getPlayerManager().getPlayerList().size();
        return Math.max(1, Math.min(n, Balance.MAX_SCALED_PLAYERS));
    }

    public static int effectiveNeed(MinecraftServer s, Chapter c) {
        if (c.type() != Type.KILL) return c.need();
        double k = 1.0 + Balance.KILLS_PER_EXTRA_PLAYER * (scaledPlayers(s) - 1);
        return (int) Math.ceil(c.need() * k);
    }

    private static int effectiveBossHp(MinecraftServer s, Chapter c) {
        double k = 1.0 + Balance.BOSS_HP_PER_EXTRA_PLAYER * (scaledPlayers(s) - 1);
        return (int) Math.round(c.bossHp() * k);
    }

    // ---------- управление ----------

    /** Любой игрок может запустить общий сюжет. */
    public static void start(ServerPlayerEntity starter) {
        MinecraftServer s = starter.getServer();
        ModData d = ModData.get(s);
        if (d.storyStarted) {
            starter.sendMessage(Text.literal("Сюжет уже идёт. " + status(s)));
            return;
        }
        ensureMap(s, d);
        d.resetStory();
        d.storyStarted = true;
        d.markDirty();
        Dialogue.clear();
        s.getPlayerManager().broadcast(Text.literal("§6" + starter.getName().getString()
                + " запустил сюжет для всей группы!"), false);
        begin(s);
    }

    public static void reset(MinecraftServer s) {
        ModData.get(s).resetStory();
        transition = false;
        Dialogue.clear();
    }

    public static void ensureMap(MinecraftServer s, ModData d) {
        if (!d.mapBuilt) {
            MapBuilder.buildAll(s.getOverworld());
            d.mapBuilt = true;
            d.markDirty();
        }
    }

    /** Телепорт группы в регион текущей главы + реплики + цель. */
    public static void begin(MinecraftServer s) {
        ModData d = ModData.get(s);
        if (d.storyChapter >= CHAPTERS.size()) {
            s.getPlayerManager().broadcast(Text.literal("§6Сюжет пройден!"), false);
            transition = false;
            return;
        }
        int ch = d.storyChapter;
        Chapter c = CHAPTERS.get(ch);
        ServerWorld w = s.getOverworld();

        int i = 0;
        for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) {
            teleportToRegion(p, w, c.region(), i++);
        }

        s.getPlayerManager().broadcast(Text.literal("§6=== Глава " + (ch + 1) + "/" + CHAPTERS.size()
                + ": " + c.title() + " ==="), false);

        Dialogue.say(s, c.intro());
        Dialogue.later(s, () -> {
            transition = false;
            s.getPlayerManager().broadcast(Text.literal("§eЦель: " + describe(s, c)), false);
            if (c.type() == Type.BOSS) spawnBoss(s, c, ch);
        });
    }

    private static void teleportToRegion(ServerPlayerEntity p, ServerWorld w, int region, int index) {
        BlockPos pos = MapBuilder.center(region);
        double ox = (index % 4) - 1.5;
        double oz = (index / 4) - 0.5;
        p.teleport(w, pos.getX() + 0.5 + ox, pos.getY(), pos.getZ() + 0.5 + oz, 0f, 0f);
    }

    public static String describe(MinecraftServer s, Chapter c) {
        return switch (c.type()) {
            case KILL -> "убить всей группой " + effectiveNeed(s, c) + "× " + c.arg();
            case REACH -> "всем вместе дойти до маяка в регионе «" + MapBuilder.NAMES[c.region()] + "»";
            case BOSS -> "победить босса «" + c.arg() + "»";
        };
    }

    public static String status(MinecraftServer s) {
        ModData d = ModData.get(s);
        if (!d.storyStarted) return "Сюжет не начат: /jjb story start";
        if (d.storyChapter >= CHAPTERS.size()) return "Сюжет пройден.";
        Chapter c = CHAPTERS.get(d.storyChapter);
        return "Глава " + (d.storyChapter + 1) + "/" + CHAPTERS.size() + " «" + c.title() + "»: "
                + describe(s, c) + " (" + d.storyProgress + "/" + effectiveNeed(s, c) + ")";
    }

    /** Повторить текущую главу (например, если босс исчез). */
    public static void retry(MinecraftServer s) {
        ModData d = ModData.get(s);
        if (!d.storyStarted || d.storyChapter >= CHAPTERS.size()) return;
        d.setStory(d.storyChapter, 0);
        transition = true;
        Dialogue.clear();
        begin(s);
    }

    public static void skip(MinecraftServer s) {
        ModData d = ModData.get(s);
        if (d.storyStarted && d.storyChapter < CHAPTERS.size()) complete(s);
    }

    // ---------- вход игрока ----------

    /** Зашёл на сервер или возродился: вернуть в текущий регион, выдать пропущенное. */
    public static void onJoin(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        ModData d = ModData.get(s);
        if (!d.storyStarted) {
            p.sendMessage(Text.literal("§6[Stands & Bankai]§r Напиши §e/jjb story start§r, чтобы начать сюжет для всей группы."));
            return;
        }
        giveMissedRewards(p, d);
        if (d.storyChapter >= CHAPTERS.size()) return;
        Chapter c = CHAPTERS.get(d.storyChapter);
        teleportToRegion(p, s.getOverworld(), c.region(), s.getPlayerManager().getPlayerList().size() - 1);
        p.sendMessage(Text.literal("§6Ты присоединился к группе. " + status(s)));
    }

    public static void onRespawn(ServerPlayerEntity p) {
        MinecraftServer s = p.getServer();
        ModData d = ModData.get(s);
        if (!d.storyStarted || d.storyChapter >= CHAPTERS.size()) return;
        Chapter c = CHAPTERS.get(d.storyChapter);
        teleportToRegion(p, s.getOverworld(), c.region(), 0);
    }

    private static void giveMissedRewards(ServerPlayerEntity p, ModData d) {
        int done = Math.min(d.storyChapter, CHAPTERS.size());
        for (int i = d.getRewarded(p.getUuid()); i < done; i++) {
            for (String r : CHAPTERS.get(i).rewards()) {
                ItemStack st = ModItems.stack(r);
                if (!st.isEmpty()) p.giveItemStack(st);
            }
        }
        d.setRewarded(p.getUuid(), done);
    }

    // ---------- события ----------

    public static void onKill(ServerPlayerEntity killer, LivingEntity dead) {
        MinecraftServer s = killer.getServer();
        ModData d = ModData.get(s);
        if (!d.storyStarted || transition || d.storyChapter >= CHAPTERS.size()) return;
        int ch = d.storyChapter;
        Chapter c = CHAPTERS.get(ch);

        boolean count = switch (c.type()) {
            case KILL -> Registries.ENTITY_TYPE.getId(dead.getType()).getPath().equals(c.arg());
            case BOSS -> dead.getCommandTags().contains("jjb_boss_" + ch);
            default -> false;
        };
        if (!count) return;

        int need = effectiveNeed(s, c);
        int prog = d.storyProgress + 1;
        d.setStory(ch, prog);

        if (prog >= need) {
            complete(s);
            return;
        }
        s.getPlayerManager().broadcast(Text.literal("§7" + prog + "/" + need + " (" + killer.getName().getString() + ")"), true);
        if (c.type() == Type.KILL && prog == need / 2) {
            Dialogue.say(s, c.mid());
        }
    }

    /** Раз в секунду: проверка REACH, нужно, чтобы дошли ВСЕ игроки онлайн. */
    public static void tickSecond(MinecraftServer s) {
        ModData d = ModData.get(s);
        if (!d.storyStarted || transition || d.storyChapter >= CHAPTERS.size()) return;
        Chapter c = CHAPTERS.get(d.storyChapter);
        if (c.type() != Type.REACH) return;

        List<ServerPlayerEntity> players = s.getPlayerManager().getPlayerList();
        if (players.isEmpty()) return;
        BlockPos lm = MapBuilder.landmark(c.region());
        int there = 0;
        for (ServerPlayerEntity p : players) {
            if (p.getBlockPos().isWithinDistance(lm, 6)) there++;
        }
        if (there == players.size()) {
            complete(s);
        } else if (there > 0) {
            s.getPlayerManager().broadcast(Text.literal("§7У маяка " + there + "/" + players.size()
                    + ". Ждём остальных."), true);
        }
    }

    /** Каждые 5 секунд: подспавн мобов для глав KILL. */
    public static void tickWaves(MinecraftServer s) {
        ModData d = ModData.get(s);
        if (!d.storyStarted || transition || d.storyChapter >= CHAPTERS.size()) return;
        Chapter c = CHAPTERS.get(d.storyChapter);
        if (c.type() != Type.KILL) return;

        ServerWorld w = s.getOverworld();
        EntityType<?> type = Registries.ENTITY_TYPE.get(new Identifier("minecraft", c.arg()));
        int cap = Balance.WAVE_MAX_ALIVE + Balance.WAVE_ALIVE_PER_EXTRA_PLAYER * (scaledPlayers(s) - 1);

        for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) {
            if (p.getWorld() != w || p.getY() < MapBuilder.Y) continue;
            long alive = w.getEntitiesByType(type, p.getBoundingBox().expand(40), Entity::isAlive).size();
            int toSpawn = (int) Math.min(Balance.WAVE_SPAWN_PER_TICK, cap - alive);
            for (int i = 0; i < toSpawn; i++) {
                Entity e = type.create(w);
                if (e == null) continue;
                double ang = w.random.nextDouble() * Math.PI * 2;
                double rad = 10 + w.random.nextDouble() * 6;
                e.refreshPositionAndAngles(p.getX() + Math.cos(ang) * rad, MapBuilder.Y + 1,
                        p.getZ() + Math.sin(ang) * rad, 0f, 0f);
                if (e instanceof MobEntity m) m.setPersistent();
                w.spawnEntity(e);
            }
        }
    }

    // ---------- внутреннее ----------

    private static void complete(MinecraftServer s) {
        ModData d = ModData.get(s);
        int ch = d.storyChapter;
        Chapter c = CHAPTERS.get(ch);
        transition = true;

        s.getPlayerManager().broadcast(Text.literal("§aГлава пройдена: " + c.title()), false);
        for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) {
            for (String r : c.rewards()) {
                ItemStack st = ModItems.stack(r);
                if (!st.isEmpty()) {
                    p.giveItemStack(st);
                    p.sendMessage(Text.literal("§bНаграда: " + r));
                }
            }
            d.setRewarded(p.getUuid(), ch + 1);
        }
        d.setStory(ch + 1, 0);
        Dialogue.say(s, c.outro());
        Dialogue.later(s, () -> begin(s));
    }

    private static void spawnBoss(MinecraftServer s, Chapter c, int ch) {
        ServerWorld w = s.getOverworld();
        Entity e = EntityType.WITHER_SKELETON.create(w);
        if (!(e instanceof MobEntity boss)) return;
        BlockPos pos = MapBuilder.center(c.region());
        int hp = effectiveBossHp(s, c);
        boss.refreshPositionAndAngles(pos.getX() + 8.5, pos.getY(), pos.getZ() + 8.5, 0f, 0f);
        boss.setCustomName(Text.literal("§c" + c.arg()));
        boss.setCustomNameVisible(true);
        boss.setPersistent();
        boss.addCommandTag("jjb_boss_" + ch);
        boss.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(hp);
        boss.setHealth((float) hp);
        w.spawnEntity(boss);
    }
}
