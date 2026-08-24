package net.magnaqore.core;

import net.magnaqore.core.command.SpawnCommand;
import net.magnaqore.core.listener.ChatListener;
import net.magnaqore.core.listener.JoinQuitListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class MagnaQoreCore extends JavaPlugin {

    private Lang lang;

    @Override
    public void onEnable() {
        this.lang = new Lang(this);

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinQuitListener(this), this);

        var spawn = getCommand("spawn");
        if (spawn != null) {
            spawn.setExecutor(new SpawnCommand(this));
        }

        getLogger().info("MagnaQoreCore enabled (locales: ru, en)");
    }

    public Lang lang() {
        return lang;
    }
}
