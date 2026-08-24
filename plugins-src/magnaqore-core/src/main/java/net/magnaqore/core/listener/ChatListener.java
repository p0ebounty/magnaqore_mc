package net.magnaqore.core.listener;

import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.magnaqore.core.MagnaQoreCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Unified chat look (docs/STYLE.md): [lp-prefix] name » message
 * No brackets, no default <name> wrapping.
 */
public final class ChatListener implements Listener {

    private final MagnaQoreCore plugin;
    private LuckPerms luckPerms;

    public ChatListener(MagnaQoreCore plugin) {
        this.plugin = plugin;
    }

    private LuckPerms lp() {
        if (luckPerms == null && plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
            try {
                luckPerms = LuckPermsProvider.get();
            } catch (IllegalStateException ignored) {
                // LuckPerms not ready yet
            }
        }
        return luckPerms;
    }

    private Component prefixOf(Player player) {
        LuckPerms lp = lp();
        if (lp == null) {
            return Component.empty();
        }
        var user = lp.getUserManager().getUser(player.getUniqueId());
        if (user == null) {
            return Component.empty();
        }
        String prefix = user.getCachedData().getMetaData().getPrefix();
        if (prefix == null || prefix.isBlank()) {
            return Component.empty();
        }
        return plugin.lang().anyFormat(prefix).append(Component.space());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onChat(AsyncChatEvent event) {
        event.renderer(ChatRenderer.viewerUnaware((source, displayName, message) ->
                Component.empty()
                        .append(prefixOf(source))
                        .append(Component.text(source.getName(), NamedTextColor.WHITE))
                        .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                        .append(message.colorIfAbsent(NamedTextColor.GRAY))
        ));
    }
}
