# IAgo - Quick Deployment Guide

## 🎯 Current Status
- ✅ **Backend**: Running on port 8081
- ✅ **Frontend**: Restored and ready to build
- ✅ **Android**: Ready to build
- ✅ **Dependencies**: All resolved

## 📋 Prerequisites

### For Local Development
- Java 21+ (Gradle will handle this)
- Kotlin 2.3.0
- Android SDK (for Android builds)

### For Production
- Backend: Docker or direct JVM execution
- Frontend: Static hosting (GitHub Pages, Netlify, etc.)
- API Key: Valid Gemini API key

## 🚀 Quick Start (Local)

### 1. Start Backend
```bash
cd /home/kayque/Repos/IAgo

# Terminal 1: Start Backend
export PORT=8081
export GEMINI_API_KEY="your-api-key-here"
./gradlew backend:run
```

### 2. Build & Run Frontend (Web)
```bash
cd /home/kayque/Repos/IAgo

# Terminal 2: Build WASM
./gradlew wasmJsRun

# This will start a dev server on http://localhost:8080
# Open in browser and test the chat
```

### 3. Test Chat
```bash
# Terminal 3: Test API directly
curl -X POST http://localhost:8081/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Olá, como você está?"}'
```

## 📱 Android Build

### Debug APK
```bash
cd /home/kayque/Repos/IAgo
./gradlew assembleDebug

# APK location:
# composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

### With Custom Backend URL
```bash
cd /home/kayque/Repos/IAgo
./gradlew assembleDebug \
  -PIAGO_BACKEND_URL="https://your-backend-url:8081"
```

### Install on Device
```bash
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

## 🌐 GitHub Pages Deployment

### 1. Build Web Frontend
```bash
cd /home/kayque/Repos/IAgo
./gradlew build

# WASM output will be in: composeApp/build/dist/wasmJs/
```

### 2. Deploy to GitHub Pages
```bash
# Copy WASM build to docs folder
mkdir -p docs
cp -r composeApp/build/dist/wasmJs/* docs/

# Update backend URL in docs/index.html
# Change: window.IAGO_BACKEND_URL = "..."

# Commit and push
git add docs/
git commit -m "Deploy IAgo to GitHub Pages"
git push origin main
```

### 3. Configure Repository Settings
- Go to GitHub repository Settings
- Under "Pages"
- Set source to `main` branch, `/docs` folder
- Wait 5 minutes for deployment

### 4. Access at
```
https://aniversarioyago.github.io/IAgo/
```

## 🔧 Configuration Options

### Backend Environment Variables
```bash
PORT=8081                                    # Server port
GEMINI_API_KEY=your-key-here                # Gemini API key (REQUIRED)
IAGO_ALLOWED_ORIGINS=http://localhost:3000  # CORS origins (optional)
```

### Frontend Configuration (composeApp/src/webMain/resources/index.html)
```javascript
// Localhost
window.IAGO_BACKEND_URL = "http://localhost:8081"

// Production (GitHub Pages)
window.IAGO_BACKEND_URL = "https://your-backend-server:8081"
```

### Android Configuration (buildConfig injection)
Configured at compile time via build.gradle.kts

## 📦 Docker Deployment (Optional)

### Backend Dockerfile
```dockerfile
FROM eclipse-temurin:21-jdk

WORKDIR /app
COPY . .

RUN chmod +x gradlew
ENV PORT=8081
ENV GEMINI_API_KEY=${GEMINI_API_KEY}

EXPOSE 8081

CMD ["./gradlew", "backend:run"]
```

### Build & Run
```bash
docker build -t iago-backend .
docker run -p 8081:8081 \
  -e GEMINI_API_KEY="your-key" \
  iago-backend
```

## ✅ Verification Checklist

- [ ] Backend health: `curl http://localhost:8081/health`
- [ ] Chat API: `curl -X POST http://localhost:8081/api/chat ...`
- [ ] Web builds: `./gradlew wasmJsRun`
- [ ] Android builds: `./gradlew assembleDebug`
- [ ] CORS working: `curl -X OPTIONS http://localhost:8081/api/chat -H "Origin: ..."`
- [ ] GitHub Pages deployed: Check at https://aniversarioyago.github.io/IAgo/

## 🐛 Troubleshooting

### Backend won't start
```bash
# Check if port is in use
lsof -i :8081

# Kill existing process
killall -9 java

# Check logs
tail -100 /tmp/backend.log
```

### Frontend can't connect to backend
```bash
# Check backend is running
curl http://localhost:8081/health

# Verify CORS headers
curl -X OPTIONS http://localhost:8081/api/chat -v

# Check network tab in browser DevTools
```

### API key issues
```bash
# Verify key is set
echo $GEMINI_API_KEY

# Check backend logs for API errors
grep -i "gemini\|error" /tmp/backend.log
```

### Android build fails
```bash
# Clean build
./gradlew clean

# Build with verbose output
./gradlew assembleDebug --stacktrace

# Check if Java home is set
echo $JAVA_HOME
```

## 📚 References

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-mobile/compose-multiplatform.html)
- [Ktor Client](https://ktor.io/docs/client-overview.html)
- [Google Gemini API](https://ai.google.dev/)

---

**Next Steps**:
1. Generate a valid Gemini API key
2. Start the backend with your key
3. Build and test locally
4. Deploy to production


