# InitialsBox

**InitialsBox** is a modern, feature-rich Android application built with **Jetpack Compose** and **Material 3**. It allows users to create, customize, and manage beautiful custom initials, monograms, and avatars with ease.

---

## ✨ Features

- **🎨 Initials & Monogram Generation**: Generate stylish initials with advanced customization options (colors, typography styles, background shapes, borders, and effects).
- **📋 Presets & Templates**: Quickly apply pre-defined aesthetic styles and presets.
- **📜 History Management**: Save generated initials to history, view past creations, and reuse or export them anytime.
- **📤 Export & Sharing**: Export your customized initials in high-quality formats to share across social media or save to your device.
- **🌓 Theming & Customization**: Supports Light/Dark themes and Android Dynamic Color (Material You).
- **✨ Smooth Navigation & UI**: Built entirely with Jetpack Compose, offering a fluid user experience, splash screen, and responsive layouts.

---

## 🛠️ Tech Stack & Architecture

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) & [Material 3](https://m3.material.io/)
- **Architecture**: MVVM (Model-View-ViewModel) with unidirectional data flow (StateFlow)
- **Navigation**: Jetpack Navigation Compose
- **Async Operations**: Kotlin Coroutines & Flow
- **Min SDK**: 26 (Android 8.0 Oreo)
- **Target / Compile SDK**: 37

---

## 📂 Project Structure

```
app/src/main/java/com/example/initialsbox/
├── data/           # Preferences and local data management
├── engine/         # Initials generation algorithm, rendering, and export formatting
├── model/          # Data models, enums, settings, and presets
├── ui/
│   ├── components/ # Reusable Compose components and dialogs
│   ├── navigation/ # App navigation routes and graph
│   ├── screens/    # Screen composables (Home, History, Settings, About, Splash)
│   └── theme/      # Material 3 theme, colors, and typography
└── MainActivity.kt # Entry point hosting NavHost and Theme setup
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Koala or newer
- JDK 11 or higher
- Android SDK (Compile SDK 37, Min SDK 26)

### Building and Running
1. Clone the repository:
   ```bash
   git clone https://github.com/abdullah-ateeq/InitialsBox.git
   ```
2. Open the project in **Android Studio**.
3. Sync project with Gradle files.
4. Run the app on an Android Emulator or physical device (API 26+).

---

## 🧪 Testing

The project includes unit tests for core generation logic and UI instrumented tests.
To run unit tests:
```bash
./gradlew test
```

---

## 📄 License

This project is open-source and available under the [MIT License](LICENSE).
