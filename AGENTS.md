# AGENTS.md

Android app (Anywhere-). Two Gradle modules: `:app` (the app, `com.absinthe.anywhere_`) and `:color-picker` (vendored `com.flask.colorpicker` library — leave alone unless fixing the picker itself).

## Build / verify

- Requires JDK 17 (CI uses Zulu 17), Android SDK (compileSdk 35), NDK `25.0.8775105`, CMake (native lib `izuko`, see `app/CMakeLists.txt` + `app/src/main/cpp/izuko.cpp`).
- Local build: `./gradlew assembleDebug` (use this for verification; no device/emulator needed). Release CI command: `bash ./gradlew -PappVerName=<sha> assembleRelease`.
- There is no real test suite — only `ExampleUnitTest` / `ExampleInstrumentedTest` stubs. Do not add test infra; verify with `assembleDebug`.
- Release-only pipeline, do not touch unless asked: `optimizeReleaseRes` task auto-finalizes `optimizeReleaseResources` (uses `app/aapt2-resources.cfg` + `aapt2 optimize --collapse-resource-names`), R8 minify + `shrinkResources`, APK rename in `app/build.gradle.kts` (`Anywhere-<ver>-<code>-<variant>.apk`), signing/App Center/Telegram upload (upstream repo + secrets only, skipped on PRs/forks).

## Project specifics

- Toolchain is pinned and fragile: AGP `8.7.3`, Gradle `8.9`, Kotlin `1.9.21`, KSP `1.9.0-1.0.13` (root `build.gradle.kts`). Do not bump one without the others. (AGP 8.6+ is required: material `1.14.0` pulls `androidx.core:1.16.0` whose AAR metadata demands it.)
- `app/build.gradle.kts` quirks that look like mistakes but are intentional:
  - `configurations.all { exclude(appcompat, kotlin-stdlib-jdk7/jdk8) }` — appcompat is excluded globally, so do not add `androidx.appcompat:appcompat` to `:app` (only `:color-picker` uses it).
  - `buildConfigField APP_CENTER_SECRET` reads `System.getenv("APP_CENTER_SECRET")` and is empty locally — expected, don't "fix".
  - `debug` variant uses `applicationIdSuffix ".debug"` and app name `Anywhere-β`; `BETA` BuildConfig flag is per-buildType.
  - Room KSP args point at `$projectDir/schemas`, which does not exist in the repo — generated at build time.
  - Prebuilt binaries in `app/libs/` (`IceBox-SDK-1.0.6.aar`, `arm64-v8a`/`armeabi-v7a` .so) and JitPack deps (`zhaobozhen.libraries:me/utils:1.1.4`) — prefer keeping over replacing.
  - `materialThemeBuilder { primaryColor "#8BC34A" ... }` generates `Theme.Material3Expressive.{Light,Dark}.Anywhere` + palette — don't hand-edit generated theme resources; change the block instead. Base parents are stock expressive `NoActionBar` (Rikka has no expressive base); `PreferenceThemeOverlay.Rikka.Material3` is intentionally kept.
  - M3 Expressive (material `1.14.0`, Views): theme defaults in `values/themes.xml` pin verified expressive styles only (`Button`, `OutlinedButton`, `TonalButton`, `TextButton`, `FloatingActionButton`, `ExtendedFloatingActionButton.Medium`, `MaterialButtonToggleGroup`). Cards use `Widget.Material3.CardView.Filled` (standalone) / `.Outlined` (list rows) with no explicit elevation (tonal elevation instead). Toolbars are `MaterialToolbar` + `Widget.Material3Expressive.Toolbar.Surface`. Progress uses `Widget.Material3Expressive.LinearProgressIndicator`. Menu `SearchView`s are still AppCompat (migrating to `SearchBar`+`SearchView` is a behavior change — do separately).
  - `R.attr.awColorPrimary` (own attr, aliased to `?attr/colorPrimary` in `Base.AppTheme`) exists because material 1.14's R is non-transitive: `com.google.android.material.R.attr.colorPrimary` no longer resolves. Do not "simplify" it back to the material R ref.
  - `compileSdk 35` makes `PackageInfo.applicationInfo` nullable (`ApplicationInfo?`) — handle null, don't `!!` in list paths.
- Entrypoints: `AnywhereApplication.kt` (AppCenter/Once/MMKV/Sui init), Room stack in `database/` (`AnywhereRoomDatabase`, `AnywhereDao`, `AnywhereRepository`), per-feature UI under `ui/` (`main`, `editor`, `list`, `shortcuts`, `qrcode`, `backup`, `settings`, …). `minSdk 26`, `targetSdk 35`, `resourceConfigurations` limited to `en`, `zh-rCN/TW/HK`.
- CI (`.github/workflows/android.yml`): runs on push to `master` + PRs; commit message starting `[skip ci]` skips the build job. Actions are pinned working: checkout v4, setup-java v4, upload-artifact v4. The AppCenter mapping-upload step was removed (action repo gone, blocks job setup; TODO with restore command in the file). Signing/mapping-telegram steps are gated on `zhaobozhen/Anywhere-` non-PR builds, so on forks only build + mappings-artifact run. A fallback `apk-unsigned` artifact uploads whenever the sign step is skipped (forks/PRs) — unsigned, for inspection only. An `apk-debug` artifact is also built for skipped-sign runs: debug skips the release anti-tamper checks (`checkSignature`/PoliceMan in `AnywhereApplication`), so it is the installable test build on forks. `optimizeReleaseRes` locates `resources-release-optimize.ap_` dynamically (AGP moves it) and warn-skips if absent — don't re-hardcode the path.

## Conventions

- `.editorconfig`: 2-space indent, UTF-8, single-class imports (`ij_java_use_single_class_imports`). Kotlin code style `official` (`gradle.properties`).
