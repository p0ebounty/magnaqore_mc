# STATE — контрольная точка проекта

> **Правило**: этот файл читается ПЕРВЫМ при продолжении работы (после сжатия контекста или новой сессии) и обновляется после каждого значимого шага. Подробности решений — docs/DECISIONS.md, план — docs/ROADMAP.md.

**Обновлено**: 2026-08-24, вторая сессия (Фаза 1 ✅ принята юзером; Фаза 1.5 в разгаре)

## Свежие изменения (Фаза 1.5, сессия 3)
- **Опыт новичка** (юзер делегировал UX полностью): стартовый кит `start` (инструменты+еда+факелы+брёвна, одноразовый, авто-выдача на первый вход), `/rtp` (алиас к /tpr, 300–4500 блоков от спавна, вода исключена), стартовый баланс 100, приветствие с гайдом (rtp/кит/лопата/jobs), GP: 256 стартовых блоков привата.
- Правила мира: difficulty normal, ночь пропускается 30% спящих, фантомы выключены (26.x: gamerule `insomnia`; doInsomnia/commandModificationBlockLimit в 26.2 НЕ существуют).
- Essentials messages: обе локали целиком перекрашены автозаменой палитры (gold→gray, red→aqua, dark_red→red, yellow→white), файлы в plugins/Essentials/messages_{en,ru}.properties.
- Контент: GravesX 2026.4.9.1 (могилы), AntiRelog 3.1.4.1 (анти-комбатлог, RU), Jobs 5.2.6.3 + CMILib 1.5.9.6 (работы; версионная логика в CMILib — потому годовалый Jobs работает на 26.2).
- **Спавн = German Style Castle** (скачанная ручная постройка, см. docs/CREDITS.md): замок на скале 217×213×140 в точке (-48,64), baseY 142. Мой процедурный замок снесён. Запасные схематики: economy-spawn.schem, warzone-spawn.schem (в plugins/MagnaQoreCore/schematics).
- Швы врезки залечены `regen` (8 зон вернулись к рельефу сида). Бэкап регионов до вставки: /backups/pre-castle-regions/.
- **Зона замка**: биом → cherry_grove («лето», снег невозможен), снег счищен (20k слоёв), WG-регион `spawn` (-163..-46 → 67..174, весь Y): строить нельзя, pvp/мобы/взрывы deny, **двери/кнопки работают** (use: allow), сундуки закрыты (chest-access deny).
- Инструментарий /mqspawn расширен: paste (WE API), flatten, **regen** (реген к сиду), **biome**, **desnow**, setpoint (с авто-Y "~"), list. Все правки мира скриптуемы из консоли.
- Точка спавна: двор замка (-29, ~, 91). Второй аккаунт юзера (Bedrock, .TweeOrange54935) тоже dev+op.
- Известный глюк: первая консольная команда в первые ~20 сек после Done глотается с NPE (CommandSourceStack.getLevel null) — просто повторить позже.

## Свежие изменения (Фаза 1.5, сессия 2)
- **Мир пересоздан**: сид `-7203507603979108244` (Cherry Blossom Valley) — спавн в вишнёвой роще (32 блока) в кольце гор, деревня в ~970 блоках. Подтверждено скриншотом карты.
- Worldborder: овер 10000, незер 1250 (центр 0,0). **Chunky прегенерирует овер r=5000 — идёт часами в фоне**; после завершения запустить незер: `chunky world world_nether; chunky radius 625; chunky start`. После рестарта survival преген НЕ продолжается сам — нужно `chunky continue`.
- **MagnaQoreCore 0.1.0** (plugins-src/magnaqore-core, Gradle, paper-api 26.2): локализация RU/EN по локали клиента, фирменный чат (префикс LP + «ник » текст»), кастомные вход/выход [+]/[-], титул-приветствие, первый вход, /spawn. Сборка+деплой: `infra/build-core.sh`.
- **Дизайн-система docs/STYLE.md** (требование юзера-перфекциониста: никаких дефолтов, всё в едином стиле). Применено: MOTD, лимбо, TAB (хедер/футер), LibreLogin (32 сообщения: титулы/промпты/ошибки, MiniMessage, RU·EN), префикс админа ✦.
- **TAB 6.1.2** (таб-лист в стиле, сортировка admin>moder>vip>default), **Pl3xMap 26.2-554** (веб-карта на 127.0.0.1:8080 — смотреть скриншотом playwright; наружу вынесем на prod-этапе за reverse proxy).
- LP-группа admin (вес 100, `*`), юзер p0ebounty — админ (op 4 + группа).

## Что работает прямо сейчас

Dev-сеть ЗАПУЩЕНА и видна из интернета (проверено внешним пингом api.mcsrvstat.us):

| Процесс | tmux-сессия | Порт | Статус |
|---|---|---|---|
| Velocity 4.0.0-6 | mq-dev-velocity | 0.0.0.0:25566 TCP | ✅ работает |
| Geyser (Bedrock) | (плагин velocity) | 0.0.0.0:19133 UDP | ✅ работает |
| NanoLimbo 1.13.0 (авторизация) | mq-dev-limbo | 127.0.0.1:25590 | ✅ работает |
| Paper 26.2-116 survival | mq-dev-survival | 127.0.0.1:25592 | ✅ работает |

- Управление: `infra/dev.sh start|stop|status|cmd|console`
- Java: Temurin 25 (`/usr/lib/jvm/temurin-25-jdk-amd64/bin/java`), путь зашит в dev.sh
- PostgreSQL: LuckPerms (storage+messaging) и LibreLogin пишут в `magnaqore_dev` — проверено (таблицы luckperms_*, librepremium_data)
- Velocity-плагины: Geyser, Floodgate, LibreLoginProd 0.25.11 (auth), LuckPerms, Sonar 2.1.50 (антибот), EpicGuard 7.6.1 (гео/VPN), AntiCrasher 2.0.11, spark
- Survival-плагины: LuckPerms, EssentialsX 2.22.1-dev (сборка 1825), WorldEdit 7.4.5, WorldGuard 7.0.18, GrimAC 2.3.74 (античит), GriefPrevention 16.18.7, ViaVersion+ViaBackwards 5.11.0 (клиенты 1.7.2–26.2), Chunky, Vault(Unlocked), PlaceholderAPI, floodgate-spigot
- Поток игрока: вход через 25566 → Sonar/EpicGuard-проверки → NanoLimbo (пока не авторизован, /register /login; premium — автовход; Bedrock — без пароля) → survival

## Известные проблемы / флаги
1. **CoreProtect 24.0 не работает на 26.2** (сам отключился) — убран; вернуть, когда выйдет совместимая сборка (роллбек-логирование гриферства сейчас ОТСУТСТВУЕТ).
2. **FAWE 2.15.4 падал на инициализации мира** на 26.2 — заменён на обычный WorldEdit 7.4.5; FAWE пересмотреть позже (для больших вставок схематик).
3. GrimAC предупреждает: ViaBackwards на 26.2 — возможны ложные срабатывания у СТАРЫХ клиентов в транспорте (лодки/лошади). Наблюдать.
4. EssentialsX — dev-сборка (стабильная 2.22.0 не поддерживает 26.2); перепиновать на релиз 2.23.0, когда выйдет.
5. Velocity 4 выбран вынужденно (свежий Geyser требует его adventure-библиотеку) — экосистема плагинов местами ещё на 3.x; при добавлении velocity-плагинов проверять совместимость.
6. Sonar: не настроена база verified-игроков (warning в логе), язык EN. `transfer.enabled: false` — НЕ включать (ломает Geyser).
7. Firewall (ufw) ВЫКЛЮЧЕН — сознательно до prod-этапа (наружу торчат только ssh/25566/19133).
8. В git не закоммичены jar-ы и миры (by design, см. .gitignore); секреты в infra/secrets/.

## Следующие шаги (по приоритету)
1. Дождаться конца прегена овера → запустить незер (см. выше), проверить `chunky continue` после рестартов.
2. Спавн-схематика: ⚠️ требование юзера — БЕЗ указания чужих авторов в игре. Искать только свободные лицензии без обязательного кредита (CC0 / «no credit required»), иначе строим/генерируем сами. Вставить WorldEdit у спавна, setworldspawn, WG-регион спавна.
3. Контент: Jobs Reborn, могилы (AngelChest/GravesX), AntiRelog, LP-группы vip/moder (+prefix ✦-стиль).
4. Стиль дальше: Essentials messages (игровые команды), Sonar kick-экраны, GriefPrevention messages — чек-лист в docs/STYLE.md.
5. MagnaQoreCore v0.2: /help замена (брендовое меню команд), задел статистики в Postgres.
6. Prod-этап по ROADMAP (systemd, nftables, DNS, домен от юзера).

Справка: GitHub — `git@github.com:p0ebounty/magnaqore_mc.git`, пуш через SSH-алиас `github-magnaqore` (на VPS чужой deploy key под `Host github.com` — не трогать!). Юзер принял Фазу 1 заходом с Java 26.2.
3. Фаза 1.5 (см. ROADMAP): сид+прегенерация мира, спавн-схематика, MagnaQoreCore (свой плагин), Jobs/могилы/анти-комбатлог, группы LuckPerms.
4. Двуязычность RU/EN (требование юзера): исследовать Triton vs свои переводы в плагинах + языки Sonar/LibreLogin/Essentials.
5. Prod-этап: systemd, nftables, fail2ban, DNS (юзер даст домен), TCPShield Free.

## Как проверить, что всё живо
```bash
cd /projects/server && ./infra/dev.sh status
ss -tulpn | grep -E "25566|19133"
curl -s "https://api.mcsrvstat.us/3/72.61.185.5:25566" | jq .online
tail -20 minecraft/dev/velocity/logs/latest.log
```
