IAgo is a Kotlin Compose Multiplatform chatbot that targets Android and Web (JS/Wasm).
The Gemini key now lives only in a backend service (`backend` module), and the app calls this backend.

## Architecture

- **Frontend** (Web UI): `http://localhost:8080` - Built with Kotlin/JS/Wasm, served by Gradle dev server
- **Backend** (API): `http://localhost:8081` - Ktor server that proxies requests to Gemini

## Backend configuration (Gemini key)

- Set `GEMINI_API_KEY` in the backend environment.
- Optional: set `PORT` (default: `8080`).
- Optional: set `IAGO_ALLOWED_ORIGINS` (comma-separated). Default is `*` in development.
- The project does **not** implement rate limiting.

Examples:

```shell
export GEMINI_API_KEY="your-gemini-api-key"
./gradlew :backend:run
```

## Frontend to backend URL

- Android reads `IAGO_BACKEND_URL` from `BuildConfig` (default `http://10.0.2.2:8080`).
- Web reads `window.IAGO_BACKEND_URL` in `composeApp/src/webMain/resources/index.html`.

If you see `Fail to fetch` in Web, usually the backend is down or URL/CORS is wrong.

Quick checks:

- Start backend first.
- Confirm `window.IAGO_BACKEND_URL` points to the backend (example: `http://localhost:8080`).
- Open `http://localhost:8080/health` and verify it returns `ok`.

---

* [/composeApp](./composeApp/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
    - [commonMain](./composeApp/src/commonMain/kotlin) is for code that’s common for all targets.
    - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
      For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
      the [iosMain](./composeApp/src/iosMain/kotlin) folder would be the right place for such calls.
      Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./composeApp/src/jvmMain/kotlin)
      folder is the appropriate location.

### Build and Run Android Application

To build and run the development version of the Android app, use the run configuration from the run widget
in your IDE’s toolbar or build it directly from the terminal:

- on macOS/Linux
  ```shell
  export IAGO_BACKEND_URL="http://10.0.2.2:8080"
  ./gradlew :composeApp:assembleDebug
  ```
- on Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Web Application

To build and run the development version of the web app, use the run configuration from the run widget
in your IDE's toolbar or run it directly from the terminal:

- for the Wasm target (faster, modern browsers):
    - on macOS/Linux
      ```shell
      ./gradlew :composeApp:wasmJsBrowserDevelopmentRun
      ```
    - on Windows
      ```shell
      .\gradlew.bat :composeApp:wasmJsBrowserDevelopmentRun
      ```
- for the JS target (slower, supports older browsers):
    - on macOS/Linux
      ```shell
      ./gradlew :composeApp:jsBrowserDevelopmentRun
      ```
    - on Windows
      ```shell
      .\gradlew.bat :composeApp:jsBrowserDevelopmentRun
      ```

### Run Backend Service

- on macOS/Linux
  ```shell
  export GEMINI_API_KEY="your-gemini-api-key"
  export PORT=8080
  ./gradlew :backend:run
  ```

## Complete Development Setup (in order)

**Terminal 1** - Start Backend:
```shell
cd /home/kayque/Repos/IAgo
export GEMINI_API_KEY="your-gemini-api-key"
export PORT=8081
./gradlew :backend:run
```

**Terminal 2** - Start Frontend:
```shell
cd /home/kayque/Repos/IAgo
./gradlew :composeApp:jsBrowserDevelopmentRun
```

This will open frontend automatically at `http://localhost:8080`.
The backend listens at `http://localhost:8081`.

Test the connection:
```shell
curl http://localhost:8081/health
# Should respond: ok
```

If you see `Fail to fetch` in the app, ensure both services are running on correct ports.
- on Windows
  ```shell
  set GEMINI_API_KEY=your-gemini-api-key
  set PORT=8080
  .\gradlew.bat :backend:run
  ```

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html),
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform/#compose-multiplatform),
[Kotlin/Wasm](https://kotl.in/wasm/)…

We would appreciate your feedback on Compose/Web and Kotlin/Wasm in the public Slack
channel [#compose-web](https://slack-chats.kotlinlang.org/c/compose-web).
If you face any issues, please report them on [YouTrack](https://youtrack.jetbrains.com/newIssue?project=CMP).