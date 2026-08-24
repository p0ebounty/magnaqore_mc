# ROADMAP

Статусы: ✅ готово · 🔄 в работе · ⏳ впереди

## Фаза 0 — Фундамент ✅ (2026-08-24)
- ✅ Аудит VPS (Ubuntu 24.04, 2 vCPU, 8 GB RAM + 4 GB swap)
- ✅ Окружение: OpenJDK 21, Gradle 9.7, PostgreSQL 16 (базы `magnaqore_dev`/`magnaqore_prod`), Chromium (Playwright)
- ✅ Репозиторий: структура, CLAUDE.md, .gitignore, ADR, скрипты (download/dev/backup)
- ✅ Ресёрч: рынок и форматы (docs/RESEARCH.md), тех. стек

## Фаза 1 — Dev-ядро 🔄 (почти готово)
Цель: рабочая dev-сеть, в которую можно зайти и играть в выживание.
- ✅ Манифест версий (Paper 26.2-116, Velocity 4.0.0, Java 25) + скачивание по манифесту
- ✅ Конфигурация: Velocity (modern forwarding) + NanoLimbo (авторизация) + Paper survival (только localhost)
- ✅ Кроссплей: Geyser + Floodgate на proxy (UDP 19133), Floodgate и на backend
- ✅ Авторизация: LibreLoginProd — offline-регистрация, автовход premium (/premium), Bedrock без пароля; Postgres
- ✅ База плагинов: LuckPerms (Postgres storage+messaging), EssentialsX (dev-сборка), Vault(Unlocked), PlaceholderAPI, WorldEdit, WorldGuard, GriefPrevention, ViaVersion/Backwards, Chunky (CoreProtect выпал — ждёт 26.2-сборки)
- ✅ Защита с первого дня: GrimAC (античит), Sonar (антибот), EpicGuard (гео/VPN), AntiCrasher — см. RESEARCH-SECURITY.md
- ✅ Сеть запущена и видна из интернета (внешний пинг ОК)
- ⏳ Вход юзера с Java-клиента (72.61.185.5:25566) и/или Bedrock (порт 19133) — критерий приёмки

## Фаза 1.5 — Наполнение выживания ⏳
- ⏳ Сид с красивым спавном (проверенный для 26.2), worldborder 10000, прегенерация Chunky r=5000 (+Nether r=625)
- ⏳ Спавн: бесплатная схематика (PMC/BuiltByBit, с указанием автора) через WorldEdit
- ⏳ Первый свой Java-плагин **MagnaQoreCore** (Gradle, paper-api): приветствия, MOTD, /spawn-логика, задел под кланы/статистику в Postgres — сразу двуязычный (RU/EN по локали клиента)
- ⏳ **Двуязычность RU/EN** (требование юзера): свои плагины — по локали клиента; чужие — изучить Triton-подход vs ручные локали (Sonar/LibreLogin/Essentials имеют файлы сообщений)
- ⏳ Контент: Jobs Reborn, могилы (AngelChest/GravesX), анти-комбатлог, стартовый квест (BetonQuest)
- ⏳ Балансировка прав LuckPerms (группы: default, vip, moder, admin)
- ⏳ Вернуть CoreProtect (когда выйдет 26.2-сборка) — до этого нет роллбека гриферства; пересмотреть FAWE
- ⏳ Донастройка Sonar (база verified-игроков, сообщения)

## Фаза 2 — Prod и публичный запуск ⏳
- ⏳ Prod-контур: systemd-юниты, автозапуск, рестарты, непривилегированный юзер вместо root
- ⏳ Firewall: nftables (synproxy TCP, pps-лимиты UDP 19132 — референсы в RESEARCH-SECURITY.md), fail2ban, SSH-харденинг
- ⏳ TCPShield Free для Java-порта при первой серьёзной атаке (+haproxy-protocol на Velocity)
- ⏳ DNS: сказать пользователю, какие записи создать (A `play.<домен>`; SRV для dev)
- ⏳ Бэкапы по крону + ротация, мониторинг (spark, метрики хоста)
- ⏳ GitHub: пользователь создаёт репозиторий и выдаёт ключ → push, дальше регулярно
- ⏳ Тестовый публичный запуск (софт-ланч для друзей)

## Фаза 3 — Комьюнити и сайт ⏳
- ⏳ Discord-сервер + DiscordSRV (чат-мост), VK
- ⏳ Веб-карта Pl3xMap (наружу через reverse proxy на поддомене)
- ⏳ Сайт на Next.js: prod- и dev-домены (dev с хот-релоадом наружу), живой онлайн, IP-копирование, «как зайти» (Java/Bedrock/лаунчеры), правила RU/EN, новости
- ⏳ Голосования: NuVotifier + VotingPlugin + регистрация на RU-мониторингах
- ⏳ Продвижение: TikTok/YouTube-клипы, интеграции

## Фаза 4 — Расширение режимов ⏳ (по онлайну, не по календарю)
- ⏳ ~30–50 CCU: Duels + BoxPvP-зона у спавна → отдельное лобби (стало 2+ направления)
- ⏳ ~100+ CCU: BedWars (MBedwars или ScreamingBedWars), паркур в лобби
- ⏳ Дальше: BentoBox OneBlock/SkyBlock, сезонная анархия, креатив-плоты как бонус
- ⏳ Монетизация: только EULA-чистая косметика/титулы (доверие против P2W-рынка)

## Принцип
Запускаемся одним отполированным режимом (выживание с приватом), сеть Velocity готова к росту с первого дня, каждый новый режим добавляется когда онлайн его прокормит (см. docs/RESEARCH.md).
