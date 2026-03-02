# Lokalize Gradle Plugin

Gradle plugin for Android/Kotlin Multiplatform internationalization that enforces translation completeness and provides AI-assisted translation.

## Features

- **Translation Validation**: Ensures all target locales have complete translations
- **AI-Powered Translation**: Automatically generates missing translations using LLMs (OpenAI, Google Gemini, Anthropic)
- **Smart Resource Handling**: Supports strings, plurals, and string-arrays
- **Build Integration**: Fails builds on missing translations (configurable)
- **Plural Support**: Properly translates all plural quantity variants (one, other, few, many)
- **Worker Isolation**: Uses Gradle Worker API for safe classpath isolation

## Installation

### Step 1: Add the plugin to your build

In your module's `build.gradle.kts`:

```kotlin
plugins {
    id("org.multipaz.lokalize")
}
```

### Step 2: Configure the plugin

```kotlin
import org.multipaz.lokalize.util.LLMProvider
import org.multipaz.lokalize.util.LLmModel

lokalize {
    defaultLocale = "en"
    targetLocales = listOf("es", "fr", "de")
    failOnMissing = true
    
    // Optional: Configure AI translation
    llmApiKey.set("your-api-key") // Or use environment variable
    llmProvider.set(LLMProvider.GOOGLE)  // GOOGLE, OPENAI, or ANTHROPIC
    llModel.set(LLmModel.GEMINI2_0_FLASH)
    
    // Optional: Custom resources directory
    resourcesDir.set("src/commonMain/composeResources")
}
```

### Step 3: Set up your API key (for AI translation)

Option 1 - Environment variable (recommended):
```bash
export LOKALIZE_API_KEY="your-api-key"
```

Option 2 - `local.properties` file:
```properties
lokalize.api.key=your-api-key
lokalize.provider=GOOGLE
lokalize.model=GEMINI2_0_FLASH
```

## Tasks

### `lokalizeCheck`

Validates that all target locales have complete translations.

```bash
./gradlew :module:lokalizeCheck
```

- Compares target locale files against the base (default) locale
- Reports missing strings, plurals, and arrays
- Fails the build if `failOnMissing = true` and translations are incomplete

### `lokalizeFix`

Generates missing translations using AI.

```bash
./gradlew :module:lokalizeFix
```

- Detects missing entries
- Sends them to the configured LLM for translation
- Updates the target locale files with translated content
- Fails if the API returns errors (does not silently use fallback text)

## Configuration Options

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `defaultLocale` | `String` | `"en"` | Base/source locale |
| `targetLocales` | `List<String>` | `[]` | Locales to validate/translate |
| `failOnMissing` | `Boolean` | `true` | Fail build on missing translations |
| `llmApiKey` | `Property<String>` | Environment lookup | API key for translation |
| `llmProvider` | `LLMProvider` | `GOOGLE` | LLM provider to use |
| `llModel` | `LLmModel` | `GEMINI2_0_FLASH` | Specific model |
| `resourcesDir` | `Property<String>` | `"src/commonMain/composeResources"` | Resources base path |

## Supported LLM Providers

### Google (Gemini)

Available models:
- `GEMINI2_0_FLASH` - Fast, efficient (recommended)
- `GEMINI2_0_FLASH_LITE` - Most efficient for low-latency
- `GEMINI2_5_PRO` - Advanced capabilities
- `GEMINI2_5_FLASH` - Balance of speed and capability

### OpenAI

Available models:
- `GPT4O` - Versatile flagship model
- `GPT4O_MINI` - Cost-effective version
- `GPT5` - Latest flagship
- `GPT5_MINI` / `GPT5_NANO` - Faster, cost-efficient

### Anthropic (Claude)

Available models:
- `CLAUDE_SONNET_4` - High-performance reasoning
- `CLAUDE_OPUS_4` - Most powerful for complex tasks
- `CLAUDE_HAIKU_4_5` - Fastest, most compact

## Supported Resource Types

### Simple Strings

```xml
<!-- values/strings.xml -->
<string name="app_name">My App</string>
<string name="welcome_message">Welcome, %1$s!</string>
```

### Plurals

All quantity variants are properly translated:

```xml
<!-- values/strings.xml -->
<plurals name="items_count">
    <item quantity="one">%d item</item>
    <item quantity="other">%d items</item>
</plurals>
```

The plugin will translate each quantity variant (one, other, few, many) separately based on the target locale's plural rules.

### String Arrays

```xml
<!-- values/strings.xml -->
<string-array name="months">
    <item>January</item>
    <item>February</item>
</string-array>
```

## How It Works

### Translation Flow

1. **Scan**: Reads base locale strings.xml and extracts all entries
2. **Compare**: Checks each target locale for missing or incomplete entries
3. **Batch**: Groups missing entries for efficient API usage
4. **Translate**: Sends to LLM with context-aware prompts
5. **Parse**: Extracts translations from API response
6. **Write**: Updates target locale files preserving existing content

### Plural Handling

Different locales have different plural rules:
- **English, Spanish**: `one`, `other`
- **French**: `one`, `other` (treats 0 as "one")
- **Russian, Polish**: `one`, `few`, `many`, `other`
- **Arabic**: `zero`, `one`, `two`, `few`, `many`, `other`
- **Japanese, Korean**: `only` (no plurals)

The plugin uses CLDR plural rules to determine required quantities for each locale.

## Troubleshooting

### API Rate Limits

If you see `429 Too Many Requests`:

**Google Gemini Free Tier**:
- Very limited daily quota
- Wait 24 hours for reset, or
- Upgrade to paid API key

**OpenAI**:
- Check your plan's rate limits
- Consider using `GPT4O_MINI` for cost savings

**Anthropic**:
- Claude has different rate limits per tier
- Check your API key's tier status

### Missing Translations Not Detected

Ensure your resource directory structure follows standard conventions:
```
src/commonMain/composeResources/
  values/strings.xml          (base/default)
  values-es/strings.xml       (Spanish)
  values-fr/strings.xml       (French)
```

### Build Fails with Classpath Issues

The plugin uses Gradle Worker API with classpath isolation. If you see Koog/Kotlin version conflicts:
```bash
./gradlew clean
./gradlew --stop  # Stop Gradle daemon
./gradlew :module:lokalizeFix
```

## Best Practices

1. **Commit base locale first**: Always ensure `values/strings.xml` is complete before running `lokalizeFix`

2. **Review AI translations**: While AI is accurate, review translations for:
   - Brand-specific terminology
   - Cultural context
   - UI space constraints

3. **Use environment variables for API keys**: Don't commit API keys to version control
   ```bash
   export LOKALIZE_API_KEY="your-key"
   ```

4. **Run check before commit**: Add to pre-commit hooks:
   ```bash
   ./gradlew :module:lokalizeCheck
   ```

5. **Batch translations**: For cost efficiency, accumulate several missing strings before running `lokalizeFix`

## Development Workflow (multipaz-compose)

When adding strings to `multipaz-compose`:

1. **Add to base locale**: Edit `values/strings.xml`
2. **Check**: `./gradlew :multipaz-compose:lokalizeCheck`
3. **Translate**: `./gradlew :multipaz-compose:lokalizeFix` (requires API key)

**Recommended**: Use environment variable (never commit keys):
```bash
export LOKALIZE_API_KEY="your-key"
./gradlew :multipaz-compose:lokalizeFix
```

**Alternative**: Configure in `build.gradle.kts` (⚠️ **DO NOT COMMIT API KEYS**):
```kotlin
lokalize {
    llmApiKey.set("your-api-key")
}
```

## CI/CD Integration
lokalize.provider=GOOGLE
lokalize.model=GEMINI2_0_FLASH
```

### Step 4: Review and commit
Review the generated translations in the `values-*/strings.xml` files, then commit both the base and translated strings.

### Important Notes
- **Always run `lokalizeCheck` first** - it validates without making changes
- **`lokalizeFix` requires an API key** - it calls LLM APIs (Google, OpenAI, or Anthropic)
- **Without API key**: The check will pass/fail based on translation completeness, but fix won't translate
- **Review AI translations** - AI is good but may need human review for brand terminology

## CI/CD Integration

### GitHub Actions

```yaml
- name: Check Translations
  run: ./gradlew :module:lokalizeCheck
  env:
    LOKALIZE_API_KEY: ${{ secrets.LOKALIZE_API_KEY }}
```

### GitLab CI

```yaml
translation-check:
  script:
    - ./gradlew :module:lokalizeCheck
  variables:
    LOKALIZE_API_KEY: $LOKALIZE_API_KEY
```

## Architecture

The plugin uses a layered architecture:

- **ResourceScanner**: Parses Android XML resources
- **TranslationComparator**: Finds missing/incomplete translations
- **BatchTranslator**: Groups translations for efficient API usage
- **TranslationWorkAction**: Runs in isolated Gradle Worker process
- **ResourceWriter**: Merges translations preserving existing content

Worker isolation ensures Koog's Kotlin 2.2.x doesn't conflict with Gradle's embedded Kotlin 2.0.x.

## License

Copyright (c) 2024 Multipaz Contributors

Licensed under the Apache License, Version 2.0
