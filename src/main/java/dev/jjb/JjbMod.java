package dev.jjb;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class JjbMod implements ModInitializer {
    public static final String ID = "jojobleach";

    @Override
    public void onInitialize() {
        Abilities.init();
        ModItems.init();

        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) ->
                JjbCommands.register(dispatcher));

        // Убийство любого игрока идёт в общий счётчик группы
        ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, entity, killed) -> {
            if (entity instanceof ServerPlayerEntity p) Story.onKill(p, killed);
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) ->
                Story.onJoin(handler.getPlayer()));

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) ->
                Story.onRespawn(newPlayer));

        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }

    private void tick(MinecraftServer server) {
        Dialogue.tick(server);
        int t = server.getTicks();
        if (t % 20 == 0) {
            ModData data = ModData.get(server);
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                data.setSpirit(p.getUuid(), data.getSpirit(p.getUuid()) + Balance.SPIRIT_REGEN_PER_SEC);
            }
            Story.tickSecond(server);
        }
        if (t % Balance.WAVE_INTERVAL_TICKS == 0) {
            Story.tickWaves(server);
        }
    }
}
