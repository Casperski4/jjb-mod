package dev.jjb;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Предмет-активатор способности: ПКМ — основная, Shift+ПКМ — банкай. */
public class AbilityItem extends Item {
    private final Abilities.Def def;

    public AbilityItem(Settings settings, Abilities.Def def) {
        super(settings);
        this.def = def;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (world.isClient || !(player instanceof ServerPlayerEntity p)) {
            return TypedActionResult.success(stack);
        }

        boolean bankai = p.isSneaking() && def.secondary() != null;
        Abilities.Action action = bankai ? def.secondary() : def.primary();
        ModData data = ModData.get(p.getServer());

        if (bankai && data.storyChapter < Balance.BANKAI_UNLOCK_CHAPTER) {
            p.sendMessage(Text.literal("Банкай ещё закрыт. Пройди сюжет до главы "
                    + (Balance.BANKAI_UNLOCK_CHAPTER + 1) + "."), true);
            return TypedActionResult.fail(stack);
        }
        if (p.getItemCooldownManager().isCoolingDown(this)) {
            return TypedActionResult.fail(stack);
        }
        int spirit = data.getSpirit(p.getUuid());
        if (spirit < action.cost()) {
            p.sendMessage(Text.literal("Не хватает Духа: " + spirit + "/" + action.cost()), true);
            return TypedActionResult.fail(stack);
        }

        data.setSpirit(p.getUuid(), spirit - action.cost());
        action.run().accept(p);
        p.getItemCooldownManager().set(this, action.cooldownTicks());
        return TypedActionResult.success(stack);
    }
}
