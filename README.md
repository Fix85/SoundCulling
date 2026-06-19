# Sound Culling

[![Build](https://github.com/Fix85/SoundCulling/actions/workflows/build.yml/badge.svg)](https://github.com/Fix85/SoundCulling/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
![Fabric](https://img.shields.io/badge/Loader-Fabric-blue)

A client-side Fabric mod for Minecraft that lets you block individual sound events
in-game. Mute annoying, spammy, or unnecessary sounds (specific mobs, blocks, or UI sounds)
without touching your resource packs or volume sliders.

> [🇬🇧 English](#sound-culling) · [🇷🇺 Русский](#ru)

## Features
- **Dynamic sound registry** — lists every registered sound event, including ones added by other mods.
- **Localized names** — each sound shows its in-game subtitle in your selected language (English, Russian, …), with the raw id underneath.
- **Preview** — click ▶ to play any sound before deciding.
- **Quick controls** — block or unblock everything (or just your current search results) in one click.
- **Master switch** — instantly mute all sounds without losing your per-sound settings.
- **Search** — filter the list by name or id.
- **Lightweight** — blocked sounds are cancelled before they ever reach the sound engine.

## Requirements
- **Minecraft** (supported versions list on Modrinth)
- **Fabric Loader** (compatible version)
- **Fabric API**
- **Cloth Config API** — bundled inside the jar.
- **Mod Menu** — recommended, used to open the config screen.

## Installation
1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft.
2. Drop the following into your `mods` folder:
   - `soundculling-*.jar` (from [Releases](https://github.com/Fix85/SoundCulling/releases) or Modrinth)
   - [Fabric API](https://modrinth.com/mod/fabric-api)
   - [Mod Menu](https://modrinth.com/mod/modmenu) *(recommended)*
3. Launch the game and open **Mods → Sound Culling → Config**.

Settings are stored in `config/soundculling.json`.

## Building from source
```bash
git clone https://github.com/Fix85/SoundCulling.git
cd SoundCulling
./gradlew build
```
The built jar is written to `build/libs/`. Requires JDK 21.

## License
Released under the [MIT License](LICENSE).

---

## RU / Русский

Клиентский мод на Fabric для Minecraft, позволяющий блокировать отдельные звуки
прямо в игре. Заглушайте раздражающие или ненужные звуки (мобов, блоков, интерфейса)
без правки ресурс-паков и ползунков громкости.

### Возможности
- **Динамический список звуков** — отображаются все зарегистрированные звуки, в том числе из других модов.
- **Локализованные названия** — для каждого звука показывается его внутриигровой субтитр на выбранном языке, а под ним — технический id.
- **Предпрослушивание** — нажмите ▶, чтобы воспроизвести звук перед решением.
- **Быстрые действия** — заблокировать или разблокировать всё (или только результаты поиска) одной кнопкой.
- **Главный переключатель** — мгновенно отключить все звуки, не теряя индивидуальные настройки.
- **Поиск** — фильтрация списка по названию или id.
- **Лёгкость** — заблокированные звуки отменяются до попадания в звуковой движок.

### Требования
- **Minecraft** (список поддерживаемых версий смотрите в разделе «Версии» на Modrinth)
- **Fabric Loader** (совместимая версия)
- **Fabric API**
- **Cloth Config API** — встроен в jar.
- **Mod Menu** — рекомендуется, открывает экран настроек.

### Установка
1. Установите [Fabric Loader](https://fabricmc.net/use/) for Minecraft.
2. Поместите в папку `mods`:
   - `soundculling-*.jar` (из раздела [Releases](https://github.com/Fix85/SoundCulling/releases) или с Modrinth)
   - [Fabric API](https://modrinth.com/mod/fabric-api)
   - [Mod Menu](https://modrinth.com/mod/modmenu) *(рекомендуется)*
3. Запустите игру и откройте **Моды → Sound Culling → Настройки**.

Настройки хранятся в `config/soundculling.json`.

### Сборка из исходников
```bash
git clone https://github.com/Fix85/SoundCulling.git
cd SoundCulling
./gradlew build
```
Готовый jar появится в `build/libs/`. Требуется JDK 21.

### Лицензия
Распространяется по лицензии [MIT](LICENSE).
