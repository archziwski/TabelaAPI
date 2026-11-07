package dev.tabelaapi.platform.paper;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUpdateSign;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerOpenSignEditor;
import dev.tabelaapi.api.TabelaPrompt;
import dev.tabelaapi.platform.SignTransport;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import net.kyori.adventure.text.Component;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/** Sends a per-player fake sign and consumes its update packet without changing world blocks. */
public final class PaperSignTransport implements SignTransport {
    private final Plugin plugin;
    private final ConcurrentMap<UUID, Editor> editors = new ConcurrentHashMap<>();
    private final PacketListenerAbstract listener;

    public PaperSignTransport(Plugin plugin) {
        this.plugin = plugin;
        listener = new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                if (event.getPacketType() != PacketType.Play.Client.UPDATE_SIGN
                    || !(event.getPlayer() instanceof Player player)) return;

                WrapperPlayClientUpdateSign packet = new WrapperPlayClientUpdateSign(event);
                Editor editor = editors.get(player.getUniqueId());
                if (editor == null || !editor.matches(packet.getBlockPosition().getX(),
                    packet.getBlockPosition().getY(), packet.getBlockPosition().getZ())) return;

                event.setCancelled(true);
                String[] lines = Arrays.copyOf(packet.getTextLines(), 4);
                if (editors.remove(player.getUniqueId(), editor)) {
                    player.getScheduler().run(plugin, task -> {
                        restore(player, editor.location());
                        editor.result().accept(lines);
                    }, () -> editors.remove(player.getUniqueId(), editor));
                }
            }
        };
        PacketEvents.getAPI().getEventManager().registerListener(listener);
    }

    @Override
    public void open(Player player, TabelaPrompt prompt, Consumer<String[]> result) {
        close(player);
        Location location = player.getLocation().getBlock().getLocation().add(0, 2, 0);
        if (location.getBlockY() >= location.getWorld().getMaxHeight()) {
            location.subtract(0, 3, 0);
        }

        Editor editor = new Editor(location, result);
        editors.put(player.getUniqueId(), editor);
        try {
            player.sendBlockChange(location, Material.OAK_SIGN.createBlockData());
            List<String> promptLines = prompt.lines();
            String[] signLines = new String[4];
            for (int i = 0; i < signLines.length; i++) {
                signLines[i] = i < promptLines.size() ? promptLines.get(i) : "";
            }
            BlockState virtualState = Material.OAK_SIGN.createBlockData().createBlockState();
            if (!(virtualState instanceof Sign virtualSign)) {
                throw new IllegalStateException("Oak sign did not create a sign state");
            }
            SignSide front = virtualSign.getSide(Side.FRONT);
            for (int i = 0; i < signLines.length; i++) front.line(i, Component.text(signLines[i]));
            player.sendBlockUpdate(location, virtualSign);
            if (!prompt.title().isBlank()) player.sendMessage(prompt.title());
            PacketEvents.getAPI().getPlayerManager().sendPacket(player,
                new WrapperPlayServerOpenSignEditor(new Vector3i(location.getBlockX(),
                    location.getBlockY(), location.getBlockZ()), true));
        } catch (RuntimeException exception) {
            editors.remove(player.getUniqueId(), editor);
            restore(player, location);
            throw exception;
        }
    }

    @Override
    public void close(Player player) {
        Editor editor = editors.remove(player.getUniqueId());
        if (editor != null) restore(player, editor.location());
    }

    public void shutdown() {
        PacketEvents.getAPI().getEventManager().unregisterListener(listener);
        for (UUID playerId : editors.keySet()) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) player.getScheduler().run(plugin, task -> close(player), () -> editors.remove(playerId));
        }
    }

    private void restore(Player player, Location location) {
        Block block = location.getBlock();
        BlockData currentData = block.getBlockData();
        BlockState state = block.getState();
        player.sendBlockChange(location, currentData);
        if (state instanceof Sign sign) {
            player.sendBlockUpdate(location, sign);
        }
    }

    private record Editor(Location location, Consumer<String[]> result) {
        private boolean matches(int x, int y, int z) {
            return location.getBlockX() == x && location.getBlockY() == y && location.getBlockZ() == z;
        }
    }
}
