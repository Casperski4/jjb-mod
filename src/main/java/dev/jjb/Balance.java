package dev.jjb;

/** Все цифры баланса в одном месте. Меняй здесь, остальной код не трогай. */
public final class Balance {
    private Balance() {}

    // Ресурс "Дух": тратится на способности стендов и клинков
    public static final int SPIRIT_MAX = 100;
    public static final int SPIRIT_REGEN_PER_SEC = 2;

    // Общий множитель урона всех способностей
    public static final float DAMAGE_MULT = 1.0f;

    // Банкай открывается, когда пройдено столько глав
    public static final int BANKAI_UNLOCK_CHAPTER = 8;

    // Длительность баффов банкая, тики (20 тиков = 1 сек)
    public static final int BANKAI_BUFF_TICKS = 20 * 20;

    // Волны мобов в сюжете
    public static final int WAVE_INTERVAL_TICKS = 100;
    public static final int WAVE_MAX_ALIVE = 6;
    public static final int WAVE_SPAWN_PER_TICK = 3;

    // Мультиплеер: масштаб на каждого игрока сверх первого
    public static final double KILLS_PER_EXTRA_PLAYER = 0.5;   // +50% целей убийства
    public static final double BOSS_HP_PER_EXTRA_PLAYER = 0.6; // +60% HP босса
    public static final int WAVE_ALIVE_PER_EXTRA_PLAYER = 3;   // +3 моба в волне
    public static final int MAX_SCALED_PLAYERS = 8;            // выше не масштабируем
}
