package net.wurstclient.hacks;

import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClickWindowC2SPacket;
import net.wurstclient.Category;
import net.wurstclient.events.PacketOutputListener;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.CheckboxSetting;
import net.wurstclient.settings.SliderSetting;

public class AutoTotemHack extends Hack
        implements UpdateListener, PacketOutputListener {

    private final CheckboxSetting swapWhileMoving = new CheckboxSetting(
            "Swap while moving",
            "Whether or not to swap totem of undying\n"
                    + "while the player is moving.\n\n"
                    + "\u00a7c\u00a7lWARNING:\u00a7r" + " This would not be possible\n"
                    + "without cheats. It may raise suspicion.",
            false);

    private final SliderSetting delay =
            new SliderSetting("Delay",
                    "Amount of ticks to wait before swapping\n"
                            + "the next totem of undying.",
                    2, 0, 20, 1, SliderSetting.ValueDisplay.INTEGER);

    private int timer;

    public AutoTotemHack() {
        super("AutoTotem", "Refill your totem of undying automatically.");
        setCategory(Category.COMBAT);
        addSetting(swapWhileMoving);
        addSetting(delay);
    }

    @Override
    public void onEnable() {
        timer = 0;
        EVENTS.add(UpdateListener.class, this);
        EVENTS.add(PacketOutputListener.class, this);
    }

    @Override
    public void onDisable() {
        EVENTS.remove(UpdateListener.class, this);
        EVENTS.remove(PacketOutputListener.class, this);
    }

    @Override
    public void onUpdate() {
        // wait for timer
        if (timer > 0) {
            timer--;
            return;
        }

        // check screen
        if (MC.currentScreen instanceof ContainerScreen
                && !(MC.currentScreen instanceof InventoryScreen))
            return;

        ClientPlayerEntity player = MC.player;
        PlayerInventory inventory = player.inventory;

        if (!swapWhileMoving.isChecked() && (player.input.movementForward != 0
                || player.input.movementSideways != 0))
            return;

        ItemStack offHandStack = inventory.offHand.get(0);
        if (!offHandStack.isEmpty())
            return;

        // search inventory for totem of undying
        int target_slot = -1;
        // search hotbar lastly
        for (int slot = 35; slot >= 0; slot--) {
            ItemStack stack = inventory.getInvStack(slot);

            if (stack.isEmpty())
                continue;

            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                target_slot = slot;
                break;
            }
        }

        // no available totem of undying
        if (target_slot == -1)
            return;

        // hotbar fix
        if (target_slot < 9)
            target_slot += 36;

        if (!offHandStack.isEmpty())
            IMC.getInteractionManager().windowClick_QUICK_MOVE(45); // should not happen!
        IMC.getInteractionManager().windowClick_PICKUP(target_slot);
        IMC.getInteractionManager().windowClick_PICKUP(45);
    }

    @Override
    public void onSentPacket(PacketOutputEvent event) {
        if (event.getPacket() instanceof ClickWindowC2SPacket)
            timer = delay.getValueI();
    }
}