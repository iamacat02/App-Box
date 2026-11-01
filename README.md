# AppBox (App Launcher)

<p align="center">
  <b>A clean, list-style Android launcher inspired by Greenify.</b><br/>
  Organise your installed apps into custom categories and sections without bloating your home screen.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-green.svg?style=flat-square&logo=android" alt="Platform"/>
  <img src="https://img.shields.io/badge/Min%20SDK-24%20(Android%207.0)-brightgreen.svg?style=flat-square" alt="Min SDK"/>
  <img src="https://img.shields.io/badge/Language-Kotlin%202.2-blue.svg?style=flat-square&logo=kotlin" alt="Language"/>
  <img src="https://img.shields.io/badge/UI-Jetpack%20Compose-purple.svg?style=flat-square&logo=jetpackcompose" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Database-Room-orange.svg?style=flat-square" alt="Room"/>
</p>

---

## 📱 Screenshots

<table align="center">
  <tr>
    <th align="center">Main Screen</th>
    <th align="center">Add Apps</th>
    <th align="center">Manage Sections</th>
    <th align="center">Settings</th>
  </tr>
  <tr>
    <td align="center"><img src="screenshots/main_screen.png" width="200" alt="Main Screen"/></td>
    <td align="center"><img src="screenshots/add_apps.png" width="200" alt="Add Apps"/></td>
    <td align="center"><img src="screenshots/manage_sections.png" width="200" alt="Manage Sections"/></td>
    <td align="center"><img src="screenshots/settings.png" width="200" alt="Settings"/></td>
  </tr>
</table>

---

## ✨ Features

- **Categorized Sections (Tabs):** Organise installed applications into default or custom tabs such as *Apps*, *Games*, *Social Media*, *Tools*, *Media*, and *Creative*.
- **Safe App Removal:** Removing an app from a section only clears its shortcut from that section—it **never uninstalls** the app from your device.
- **Fast Multi-Select & Search:** Easily search through installed apps and add multiple apps to any section in one tap.
- **Drag & Drop Section Management:** Reorder, edit, hide, set defaults, or batch-delete sections effortlessly.
- **Gesture Support:** Swipe left/right over the app list to quickly switch between sections.
- **Flexible Sorting:** Sort apps alphabetically (A–Z / Z–A) or by installation/update time.
- **Customization & Themes:** Material 3 design with light, dark, and system theme options.
- **Performance Optimized:** App icons are fetched on-demand directly from `PackageManager` via a byte-bounded LRU cache.

---

## 🏗️ Architecture

AppBox is built with modern Android development practices adhering to **Clean Architecture** principles and **Unidirectional Data Flow (UDF)**:

```text
UI (Jetpack Compose) ──► UiEvent ──► ViewModel ──► UseCase ──► Repository ──► Room / PackageManager
                                        │                                           │
                                        └─────────── StateFlow<UiState> ◄───────────┘
```

- **`data/`**: Room database (`AppDatabase`), Entities, DAOs, `DataStore` settings repository, and `InstalledAppsRepository`.
- **`domain/`**: Use cases for section management, app sorting, name validation, and app memberships.
- **`ui/`**: Declarative UI built entirely in **Jetpack Compose** (M3), organized into screen states, sealed UI events, and custom composable components.
- **`di/`**: Light-weight manual dependency injection container (`AppContainer`).

---

## 🛠️ Requirements & Tech Stack

- **Min SDK:** 24 (Android 7.0 Nougat+)
- **Target / Compile SDK:** 35 / 37
- **Language:** Kotlin 2.2.10
- **UI Framework:** Jetpack Compose (Compose BOM 2026.02.01)
- **Database:** Room 2.8.5
- **Build Tool:** Gradle 9.6.0 with AGP 9.4.1

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 21+ (Gradle automatically manages its JDK toolchain)
- Android SDK Platform 37 installed

### Building from Source

1. **Clone the repository:**
   ```bash
   git clone https://github.com/iamacat/AppBox.git
   cd AppBox
   ```

2. **Build Debug APK:**
   ```bash
   ./gradlew :app:assembleDebug
   ```
   The APK will be generated at:
   `app/build/outputs/apk/debug/app-debug.apk`

3. **Install on connected device / emulator via ADB:**
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   adb shell am start -n com.iamacat.appbox/.MainActivity
   ```

---

## 🧪 Testing

Run unit tests covering use cases, section name validation, and sorting logic against in-memory fake DAOs:

```bash
./gradlew :app:testDebugUnitTest
```

---

## 🗺️ Navigation & Controls

- **Top Bar `+`**: Quick-select or create new sections.
- **Horizontal Swipe**: Swipe left or right across the app list to switch active sections.
- **FAB `+`**: Open the multi-select app picker for the active section.
- **Row Long-Press / `⋮`**: Context menu to Launch App, Move/Copy to another section, Remove, or view App Info.
- **Overflow Menu `⋮`**: Manage Sections, Sort Apps, Refresh App List, Settings, and About dialog.

---

## 📄 License

```text
Copyright 2026 iamacat

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations Canadian.
```
