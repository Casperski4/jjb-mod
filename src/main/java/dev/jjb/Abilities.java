package dev.jjb;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.LightningEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** Сбалансированные способности оригинальных стендов и клинков. */
public final class Abilities {
    private Abilities() {}

    public record Action(int cost, int cooldownTicks, Consumer<ServerPlayerEntity> run) {}

    /** primary = ПКМ (стенд / шикай), secondary = Shift+ПКМ (банкай), может быть null. */
    public record Def(String id, int color, Action primary, Action secondary) {}

    public static final Map<String, Def> ALL = new LinkedHashMap<>();

    private static void reg(String id, int color, Action primary, Action secondary) {
        ALL.put(id, new Def(id, color, primary, secondary));
    }

    private static int sec(int s) {
        return s * 20;
    }

    // ---------- помощники ----------

    private static ServerWorld w(ServerPlayerEntity p) {
        return p.getServerWorld();
    }

    private static List<LivingEntity> near(ServerPlayerEntity p, double r) {
        return w(p).getEntitiesByClass(LivingEntity.class, p.getBoundingBox().expand(r),
                e -> e != p && e.isAlive() && e.squaredDistanceTo(p) <= r * r);
    }

    private static List<LivingEntity> inFront(ServerPlayerEntity p, double r, double minDot) {
        Vec3d look = p.getRotationVec(1f);
        List<LivingEntity> out = new ArrayList<>();
        for (LivingEntity e : near(p, r)) {
            Vec3d to = e.getPos().add(0, e.getHeight() / 2, 0).subtract(p.getEyePos()).normalize();
            if (to.dotProduct(look) >= minDot) out.add(e);
        }
        return out;
    }

    private static LivingEntity target(ServerPlayerEntity p, double range) {
        return inFront(p, range, 0.93).stream()
                .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(p))).orElse(null);
    }

    private static void hit(ServerPlayerEntity p, LivingEntity e, float dmg) {
        e.damage(p.getDamageSources().playerAttack(p), dmg * Balance.DAMAGE_MULT);
    }

    private static void fx(ServerPlayerEntity p, ParticleEffect type, int count, double spread) {
        w(p).spawnParticles(type, p.getX(), p.getY() + 1, p.getZ(), count, spread, 0.5, spread, 0.05);
    }

    private static void msg(ServerPlayerEntity p, String s) {
        p.sendMessage(net.minecraft.text.Text.literal(s), true);
    }

    private static void lightning(ServerPlayerEntity p, LivingEntity t, float dmg) {
        LightningEntity bolt = EntityType.LIGHTNING_BOLT.create(w(p));
        if (bolt != null) {
            bolt.refreshPositionAfterTeleport(t.getX(), t.getY(), t.getZ());
            bolt.setCosmetic(true);
            w(p).spawnEntity(bolt);
        }
        hit(p, t, dmg);
    }

    private static void effect(LivingEntity e, net.minecraft.entity.effect.StatusEffect fx, int ticks, int amp) {
        e.addStatusEffect(new StatusEffectInstance(fx, ticks, amp));
    }

    // ---------- регистрация ----------

    public static void init() {
        // ===== СТЕНДЫ =====

        reg("stand_crimson_echo", 0xB3202A, new Action(25, sec(12), p -> {
            fx(p, ParticleTypes.CRIT, 40, 1.2);
            for (LivingEntity e : inFront(p, 4, 0.5)) hit(p, e, 9f);
            msg(p, "Багровое Эхо: шквал ударов");
        }), null);

        reg("stand_silent_hourglass", 0xD8C27A, new Action(40, sec(40), p -> {
            fx(p, ParticleTypes.ENCHANT, 60, 3);
            for (LivingEntity e : near(p, 8)) {
                effect(e, StatusEffects.SLOWNESS, sec(4), 3);
                effect(e, StatusEffects.WEAKNESS, sec(4), 0);
            }
            effect(p, StatusEffects.SPEED, sec(4), 1);
            msg(p, "Тихие Часы: время вокруг замедлилось");
        }), null);

        reg("stand_iron_tide", 0x5A6B7C, new Action(30, sec(20), p -> {
            fx(p, ParticleTypes.CLOUD, 50, 2.5);
            for (LivingEntity e : near(p, 5)) {
                hit(p, e, 4f);
                Vec3d push = e.getPos().subtract(p.getPos()).normalize().multiply(1.4).add(0, 0.4, 0);
                e.setVelocity(push);
                e.velocityModified = true;
            }
            effect(p, StatusEffects.RESISTANCE, sec(5), 0);
            msg(p, "Железный Прилив: волна");
        }), null);

        reg("stand_paper_moon", 0xE8E8F0, new Action(35, sec(30), p -> {
            fx(p, ParticleTypes.HAPPY_VILLAGER, 30, 1);
            p.heal(8f);
            effect(p, StatusEffects.REGENERATION, sec(5), 1);
            msg(p, "Бумажная Луна: исцеление");
        }), null);

        reg("stand_gale_marionette", 0x3FB8A5, new Action(20, sec(14), p -> {
            LivingEntity t = target(p, 14);
            if (t == null) {
                msg(p, "Нет цели");
                return;
            }
            Vec3d pull = p.getPos().subtract(t.getPos()).normalize().multiply(1.6).add(0, 0.3, 0);
            t.setVelocity(pull);
            t.velocityModified = true;
            hit(p, t, 3f);
            effect(t, StatusEffects.SLOWNESS, sec(2), 1);
            msg(p, "Ветреная Марионетка: притяжение");
        }), null);

        // ===== КЛИНКИ: шикай (ПКМ) и банкай (Shift+ПКМ) =====

        reg("blade_frostveil", 0x8FD3FF,
                new Action(25, sec(10), p -> {
                    fx(p, ParticleTypes.SNOWFLAKE, 60, 3);
                    for (LivingEntity e : near(p, 6)) {
                        hit(p, e, 5f);
                        effect(e, StatusEffects.SLOWNESS, sec(3), 1);
                    }
                    msg(p, "Шикай — Ледяная Вуаль");
                }),
                new Action(60, sec(120), p -> {
                    fx(p, ParticleTypes.SNOWFLAKE, 200, 6);
                    for (LivingEntity e : near(p, 10)) {
                        hit(p, e, 10f);
                        effect(e, StatusEffects.SLOWNESS, sec(5), 3);
                        e.setFrozenTicks(e.getMinFreezeDamageTicks() + 100);
                    }
                    effect(p, StatusEffects.RESISTANCE, Balance.BANKAI_BUFF_TICKS, 0);
                    msg(p, "БАНКАЙ — Ледяная Вуаль: Полный Зимний Покров");
                }));

        reg("blade_ember_sovereign", 0xFF6A1A,
                new Action(25, sec(10), p -> {
                    LivingEntity t = target(p, 12);
                    if (t == null) {
                        msg(p, "Нет цели");
                        return;
                    }
                    fx(p, ParticleTypes.FLAME, 40, 0.6);
                    hit(p, t, 7f);
                    t.setOnFireFor(4);
                    msg(p, "Шикай — Пепельный Владыка");
                }),
                new Action(60, sec(120), p -> {
                    fx(p, ParticleTypes.LAVA, 120, 4);
                    for (LivingEntity e : near(p, 8)) {
                        hit(p, e, 8f);
                        e.setOnFireFor(6);
                    }
                    effect(p, StatusEffects.FIRE_RESISTANCE, Balance.BANKAI_BUFF_TICKS, 0);
                    msg(p, "БАНКАЙ — Пепельный Владыка: Огненный Шторм");
                }));

        reg("blade_hollow_lantern", 0x9A5CE0,
                new Action(25, sec(10), p -> {
                    LivingEntity t = target(p, 4);
                    if (t == null) {
                        msg(p, "Нет цели вблизи");
                        return;
                    }
                    fx(p, ParticleTypes.SOUL, 25, 0.6);
                    hit(p, t, 6f);
                    p.heal(3f);
                    msg(p, "Шикай — Пустой Фонарь: поглощение");
                }),
                new Action(60, sec(120), p -> {
                    fx(p, ParticleTypes.SOUL_FIRE_FLAME, 100, 2);
                    effect(p, StatusEffects.SPEED, Balance.BANKAI_BUFF_TICKS, 0);
                    effect(p, StatusEffects.STRENGTH, Balance.BANKAI_BUFF_TICKS, 0);
                    msg(p, "БАНКАЙ — Пустой Фонарь: Пробуждённая Тень");
                }));

        reg("blade_thunder_regent", 0xF2E14B,
                new Action(25, sec(10), p -> {
                    LivingEntity t = target(p, 16);
                    if (t == null) {
                        msg(p, "Нет цели");
                        return;
                    }
                    lightning(p, t, 8f);
                    msg(p, "Шикай — Громовой Регент");
                }),
                new Action(60, sec(120), p -> {
                    List<LivingEntity> list = near(p, 12);
                    list.sort(Comparator.comparingDouble(e -> e.squaredDistanceTo(p)));
                    for (int i = 0; i < Math.min(5, list.size()); i++) lightning(p, list.get(i), 7f);
                    effect(p, StatusEffects.SPEED, Balance.BANKAI_BUFF_TICKS, 0);
                    msg(p, "БАНКАЙ — Громовой Регент: Небесный Суд");
                }));
    }
}
