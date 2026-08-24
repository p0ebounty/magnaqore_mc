package net.magnaqore.core.listener;

import java.time.Duration;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import net.magnaqore.core.MagnaQoreCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Branded join/quit lines (localized per viewer) + first-join welcome sequence.
 * Default Minecraft messages are suppressed entirely (docs/STYLE.md).
 */
public final class JoinQuitListener implements Listener {

    private final MagnaQoreCore plugin;

    public JoinQuitListener(MagnaQoreCore plugin) {
        this.plugin = plugin;
    }

    private void broadcast(String key, Player about) {
        TagResolver name = Placeholder.unparsed("name", about.getName());
        for (Player viewer : plugin.getServer().getOnlinePlayers()) {
            viewer.sendMessage(plugin.lang().line(viewer, key, name));
        }
        plugin.getServer().getConsoleSender()
                .sendMessage(plugin.lang().line(plugin.getServer().getConsoleSender(), key, name));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);
        Player player = event.getPlayer();
        boolean firstJoin = !player.hasPlayedBefore();

        broadcast("join", player);
        if (firstJoin) {
            broadcast("first-join", player);
        }

        TagResolver name = Placeholder.unparsed("name", player.getName());
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) {
                return;
            }
            player.showTitle(Title.title(
                    plugin.lang().line(player, "welcome-title"),
                    plugin.lang().line(player, "welcome-subtitle", name),
                    Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofSeconds(1))
            ));
            if (firstJoin) {
                player.sendMessage(Component.empty());
                player.sendMessage(plugin.lang().msg(player, "welcome-line"));
                player.sendMessage(Component.empty());
            }
        }, 30L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);
        broadcast("quit", event.getPlayer());
    }
}
