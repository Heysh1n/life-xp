# Архитектура сборки: LIFE-XP Challenge (Stonecutter + Architectury)

**Tags:** `#MinecraftModding`, `#Gradle`, `#Stonecutter`, `#Architectury`, `#Java_Toolchain`, `#MultiVersion`

---

## 1. Обзор архитектуры

Проект использует монорепозиторную структуру с единым исходным кодом для поддержки нескольких версий Minecraft (**1.21.1, 1.21.4, 26.1, 26.2, 26.3**) и двух основных платформ (**Fabric** и **NeoForge**) со 100% паритетом функционала.

Контроль версий, конфигурации зависимостей и условная компиляция осуществляются связкой **Stonecutter v0.5.1** и **Architectury Plugin/Loom**:
* **Единый общий код (Single Source of Truth):** `common/src/main/java`.
* **Платформо-зависимый код:** `fabric/` и `neoforge/`.
* **Управление свойствами версий:** изолировано в директории `versions/<version>/gradle.properties`.
* **Препроцессинг комментариев:** кастомный препроцессор `gradle/stonecutter-processor.gradle` с поддержкой операторов сравнения (`>=`, `<=`, `>`, `<`, `==`, `!=`) и логических связок (`&&`, `||`).

---

## 2. Динамический Java Toolchain

Среда сборки Gradle автоматически настраивает Java Toolchain и флаг компилятора `--release` в зависимости от целевой версии Minecraft:
* **Java 21:** Ветка `1.21.x` (`1.21.1`, `1.21.4`).
* **Java 25:** Ветка `26.x` (`26.1`, `26.2`, `26.3`).

Определение происходит на этапе конфигурации Gradle:
```groovy
def mcVersion = rootProject.findProperty("minecraft_version") ?: ""
def targetJavaVersion = mcVersion.startsWith("26.") ? 25 : 21

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
}
tasks.withType(JavaCompile).configureEach {
    options.release = targetJavaVersion
}
```

---

## 3. Матричный билд-пайплайн и CLI команды

### 3.1. Основные команды разработчика

| Команда | Описание |
|---|---|
| `./gradlew build` | Запуск автоматической **мульти-сборки всех 5 версий подряд** с формированием итогового консольного отчёта. |
| `./gradlew -to <версия>` | Переключение рабочей среды IDE и активного контекста на указанную версию (например, `./gradlew -to 1.21.1`). |
| `./gradlew to <версия>` | Альтернативный синтаксис переключения версии (например, `./gradlew to 26.2`). |
| `./gradlew -PsingleBuild=true build` | Сборка артефактов **только для текущей активной версии**. |

### 3.2. Логика работы мульти-билда (`runMultiBuild`)
1. Сборщик последовательно переключает свойства проекта через `to <ver>`.
2. Компилирует проект под нужной версией Java Toolchain.
3. Применяет правильные маппинги через Loom:
   - Для обфусцированных версий (`1.21.x`): `apply plugin: "dev.architectury.loom"`, `mappings loom.officialMojangMappings()`, `remapJar`.
   - Для необфусцированных версий (`26.x`): `apply plugin: "dev.architectury.loom-no-remap"`, `shadowJar`.
4. **Экспорт и роутинг артефактов:**
   Готовые `.jar` файлы автоматически переименовываются и сохраняются по маске:
   ```text
   releases/<minecraft_version>/<platform>/LifeXPChallenge-<mod_version>-<platform>-<minecraft_version>.jar
   ```
5. **Отказоустойчивость:** Падение сборки одной из версий не прерывает сборку остальных; в конце выводится сводная таблица статусов (`[✓] УСПЕШНО` / `[✗] ОШИБКА`).
6. **Восстановление окружения:** После завершения цикла сборки рабочее окружение возвращается к исходной активной версии.

---

## 4. Карта миграции API Mojang & Known Differences

При написании единого кода через препроцессор Stonecutter `//? if` учитываются следующие ключевые различия API между `1.21.x` и `26.x`:

| Аспект API | 1.21.1 | 1.21.4 | 26.x (26.1 / 26.2 / 26.3) |
|---|---|---|---|
| **Идентификаторы** | `ResourceLocation` | `ResourceLocation` | `Identifier` (в 26.x класс `Identifier` заменяет `ResourceLocation`) |
| **Взаимодействие предметов (`use`)** | `InteractionResultHolder<ItemStack>` | `InteractionResult` | `InteractionResult` |
| **Анимация использования** | `UseAnim` | `ItemUseAnimation` | `ItemUseAnimation` |
| **Тултипы (`appendHoverText`)** | `(ItemStack, TooltipContext, List<Component>, TooltipFlag)` | `(ItemStack, TooltipContext, List<Component>, TooltipFlag)` | `(ItemStack, TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag)` |
| **Зажатие клавиши Shift** | `Screen.hasShiftDown()` | `Screen.hasShiftDown()` | `Minecraft.getInstance().hasShiftDown()` |
| **Высота мира** | `level.getMinBuildHeight()` | `level.getMinY()` | `level.getMinY()` |
| **GameRules** | `level.getGameRules().getBoolean(...)` | `serverLevel.getGameRules().getBoolean(...)` | `serverLevel.getGameRules().get(GameRules.KEEP_INVENTORY)` |
| **Отправка сообщений чата** | `player.displayClientMessage(..., false)` | `player.displayClientMessage(..., false)` | `player.sendSystemMessage(...)` |
| **Рендеринг виньетки GUI** | `RenderSystem.enableBlend()` + `guiGraphics.blit(...)` | `guiGraphics.blit(RenderType::guiTextured, ...)` | `guiGraphics.blit(RenderPipelines.GUI_TEXTURED, ...)` |
| **Параметры LootContext** | `context.getParamOrNull(...)` | `context.getOptionalParameter(...)` | `getOptionalParameter` (26.1/26.2), `getOptional` (>= 26.3) |
| **Loot Type / Codec** | `getType()` (`LootItemConditionType`) | `getType()` (`LootItemConditionType`) | `codec()` (`MapCodec`) |
| **Architectury RIGHT_CLICK_BLOCK** | Возвращает `EventResult` | Возвращает `InteractionResult` (`.asMinecraft()`) | Возвращает `EventResult` |

---

## 5. Правила работы с препроцессором Stonecutter

1. **Без вложенных условий:** Stonecutter и встроенный парсер обрабатывают код построчно. Вложенные блоки `//? if` ломают границы комментариев. Любые вариативные вызовы оформляются отдельными плоскими вспомогательными методами (`isShiftDown()`, `getLootParam()`, `renderVignette()`).
2. **Поддержка связок:** Разрешены условия вида `//? if >= 1.21.4 && < 26.3 {` благодаря обновлённому обработчику `StonecutterProcessor.evalCondition`.
3. **Строгая независимость веток:** Все ветки комментариев (`/* ... */`) должны быть синтаксически валидными для своей целевой версии Java/Minecraft.
