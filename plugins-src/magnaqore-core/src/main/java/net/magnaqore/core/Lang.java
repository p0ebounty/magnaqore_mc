package net.magnaqore.core;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/**
 * RU/EN localization by client locale. All strings are MiniMessage (docs/STYLE.md).
 * Files live in the plugin data folder (lang/ru.yml, lang/en.yml) so they can be
 * tuned without a rebuild; jar resources are the committed source of truth.
 */
public final class Lang {

    /** Brand mark prepended by {@link #msg}: MQ › */
    public static final String PREFIX =
            "<gradient:#8A2BE2:#00CED1><b>MQ</b></gradient> <dark_gray>›</dark_gray> ";

    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<String, YamlConfiguration> bundles;

    public Lang(MagnaQoreCore plugin) {
        this.bundles = Map.of(
                "ru", load(plugin, "ru"),
                "en", load(plugin, "en")
        );
    }

    private static YamlConfiguration load(MagnaQoreCore plugin, String code) {
        File file = new File(plugin.getDataFolder(), "lang/" + code + ".yml");
        if (!file.exists()) {
            plugin.saveResource("lang/" + code + ".yml", false);
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        // jar resource is the fallback for keys missing on disk (older extracted file)
        var res = plugin.getResource("lang/" + code + ".yml");
        if (res != null) {
            cfg.setDefaults(YamlConfiguration.loadConfiguration(
                    new java.io.InputStreamReader(res, java.nio.charset.StandardCharsets.UTF_8)));
        }
        return cfg;
    }

    private String code(CommandSender sender) {
        if (sender instanceof Player p) {
            Locale l = p.locale();
            if (l != null && "ru".equalsIgnoreCase(l.getLanguage())) {
                return "ru";
            }
        }
        return "en";
    }

    private String raw(String code, String key) {
        YamlConfiguration bundle = bundles.get(code);
        String value = bundle != null ? bundle.getString(key) : null;
        if (value == null) {
            YamlConfiguration en = bundles.get("en");
            value = en != null ? en.getString(key) : null;
        }
        return value != null ? value : key;
    }

    /** Branded message: MQ › text */
    public Component msg(CommandSender viewer, String key, TagResolver... resolvers) {
        return mm.deserialize(PREFIX + raw(code(viewer), key), resolvers);
    }

    /** Bare localized line, no brand mark (join/quit lines, subtitles). */
    public Component line(CommandSender viewer, String key, TagResolver... resolvers) {
        return mm.deserialize(raw(code(viewer), key), resolvers);
    }

    public MiniMessage mini() {
        return mm;
    }

    /** Parses text that may be MiniMessage or legacy &-codes (e.g. LuckPerms meta). */
    public Component anyFormat(String text) {
        if (text == null || text.isEmpty()) {
            return Component.empty();
        }
        if (text.indexOf('&') >= 0 || text.indexOf('§') >= 0) {
            return LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(text.replace('§', '&'));
        }
        return mm.deserialize(text);
    }
}
