# ⚡ IAgo Quick Reference Card

## 🚀 Start Backend (Terminal 1)
```bash
cd /home/kayque/Repos/IAgo
PORT=8081 GEMINI_API_KEY="your-api-key" ./gradlew backend:run
```

## 🌐 Build & Run Frontend (Terminal 2)
```bash
cd /home/kayque/Repos/IAgo
./gradlew wasmJsRun
# Opens: http://localhost:8080
```

## 🧪 Test API (Terminal 3)
```bash
# Health Check
curl http://localhost:8081/health

# Send Message
curl -X POST http://localhost:8081/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"Olá!"}'
```

## 📱 Build Android
```bash
cd /home/kayque/Repos/IAgo
./gradlew assembleDebug
# APK: composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

## 📋 Check Status
```bash
# Backend running?
ps aux | grep "[j]ava.*backend"

# Port 8081 in use?
lsof -i :8081

# Build errors?
./gradlew compileKotlin
```

## 🔧 Kill Backend
```bash
killall -9 java
```

## 📚 Documentation
- `RESTORATION_SUMMARY.md` - What was restored
- `DEPLOYMENT_GUIDE.md` - How to deploy
- `STATUS_REPORT.md` - Full status

## 🎯 Key Endpoints
| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/health` | GET | Health check |
| `/api/chat` | POST | Send message |
| `/api/chat` | OPTIONS | CORS preflight |

## 💾 Git Commit
```bash
git add -A
git commit -m "restore: Chatbot UI and build configuration"
git push origin main
```

## ⚙️ Environment Variables
```bash
PORT=8081                           # Backend port
GEMINI_API_KEY=xxx-yyy-zzz         # Gemini API key
IAGO_ALLOWED_ORIGINS=*             # CORS origins
```

## 📍 Important Directories
```
/home/kayque/Repos/IAgo/
├── backend/                        # Backend code
├── composeApp/                     # Frontend code
├── gradle/                         # Build configuration
├── DEPLOYMENT_GUIDE.md             # Deployment steps
├── RESTORATION_SUMMARY.md          # Technical details
└── STATUS_REPORT.md                # Complete status
```

## 🎓 Architecture
```
Frontend (Web/Android)
    ↓
GeminiRepository
    ↓
HttpClient (Ktor)
    ↓
Backend API (port 8081)
    ↓
Gemini API (Google)
```

## ✅ Pre-Deployment Checklist
- [ ] Backend runs: `curl http://localhost:8081/health`
- [ ] Chat works: Test message in UI
- [ ] Frontend builds: `./gradlew wasmJsRun`
- [ ] Android builds: `./gradlew assembleDebug`
- [ ] Documentation reviewed
- [ ] API key ready
- [ ] Backend URL configured

## 🚨 Common Issues

### Backend won't start
```bash
lsof -i :8081              # Check port
killall -9 java            # Kill stuck process
./gradlew clean            # Clean build
```

### Frontend can't connect
```bash
curl http://localhost:8081/health     # Check backend
curl -X OPTIONS http://localhost:8081/api/chat -H "Origin: http://localhost:3000" -v
```

### Build fails
```bash
./gradlew clean
./gradlew build --stacktrace
```

---

**Status**: ✅ Ready for deployment  
**Last Updated**: 2026-03-21  
**Version**: 1.0.0

