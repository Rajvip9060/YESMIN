# YESMIN AI — Autonomous AI Girlfriend, Jarvis Assistant, Live Voice & Coding Agent

A production-ready, futuristic Android application combining:
1. **AI Girlfriend & Companion** (Affectionate, caring, conversational in English, Hindi, and Hinglish)
2. **Personal Jarvis Assistant** (Executive task delegation, device controls, web search)
3. **Gemini Live Voice Mode** (Real-time voice conversation with live audio feedback and interruption)
4. **Autonomous AI Coding Agent** (Interactive virtual IDE, file explorer, code editor, terminal logs, and multi-step planner)
5. **Native Android App Bridge** (Launch YouTube, Gmail, Maps, WhatsApp, Telegram, Instagram, Camera, Calculator, and send notifications)
6. **Multi-Provider AI & Voice Hub** (Gemini 3.5, OpenAI, ElevenLabs Neural TTS, Android TTS)
7. **Persistent Memory & Projects System** (Backed by SQLite Room Database with KSP)

---

## 1. Custom Assistant Name & Identity
- **Default Name:** YESMIN AI
- **Custom Name:** Configurable in **Settings → Profile & Assistant → Assistant Name** (e.g., set to *Muskan*, *Jarvis*, *Aria*).
- **Global Propagation:** Once changed, the custom name dynamically updates across:
  - Home dashboard & personalized greetings ("Hi, Jaan ♥", "I'm Muskan")
  - Chat header & message avatars
  - Live Voice Mode audio greetings
  - AI System Prompt instructions & memory context
  - Settings live preview & notifications
- **Persistent Storage:** Stored in local Room database (`user_settings` table), persisting across app restarts.

---

## 2. Environment Setup & API Keys

Copy `.env.example` to `.env` or set values via the **AI Studio Secrets Panel**:

```env
# Gemini API Key (Automatically injected by AI Studio or configure manually)
GEMINI_API_KEY=YOUR_GEMINI_API_KEY

# ElevenLabs Voice Synthesis (Optional)
ELEVENLABS_API_KEY=YOUR_ELEVENLABS_KEY

# OpenAI / Custom Providers (Optional)
OPENAI_API_KEY=YOUR_OPENAI_KEY
```

---

## 3. Gemini Live Voice Setup
- Uses native Android `SpeechRecognizer` for low-latency streaming speech input.
- Connects to Google Generative Language REST endpoint (`gemini-3.5-flash`).
- Synthesizes speech with Android `TextToSpeech` (with Hindi/English accent support) or **ElevenLabs** neural voices.
- Supports instant interruption: speaking directly pauses active AI speech and resumes listening.

---

## 4. ElevenLabs Voice Setup
Navigate to **Settings → Voice → ElevenLabs**:
- Enter ElevenLabs API Key.
- Select Voice ID (e.g., `21m00Tcm4TlvDq8ikWAM` - Rachel).
- Tap **Test Voice** to verify immediate neural audio playback.

---

## 5. Native Device Apps & Actions Bridge
Supports launching native applications via Android Intents:
- **YouTube** (`com.google.android.youtube`)
- **Gmail** (`com.google.android.gm`)
- **Google Maps** (`com.google.android.apps.maps`)
- **WhatsApp** (`com.whatsapp`)
- **Telegram** (`org.telegram.messenger`)
- **Instagram** (`com.instagram.android`)
- **Chrome** (`com.android.chrome`)
- **Camera** (`MediaStore.ACTION_IMAGE_CAPTURE`)
- **Calculator** (`com.google.android.calculator`)
- **Calendar** (`com.google.android.calendar`)
- **Local Notifications** (`NotificationManagerCompat`)

---

## 6. Architecture & Technology Stack
- **UI Framework:** Jetpack Compose with Material Design 3
- **Theme:** Futuristic Dark Cyberpunk / Glassmorphism (Neon Pink `#EC4899`, Neon Purple `#A855F7`, Neon Cyan `#06B6D4`, Navy `#070A14`)
- **Database:** Android Room 2.7.0 with Kotlin Symbol Processing (KSP)
- **Networking:** Retrofit 2.12.0, OkHttp 4.10.0, Moshi 1.15.2
- **Image Analysis:** Gemini Multimodal Vision API via base64 `inlineData`

---

## 7. Build & Compilation
Run the local build task:
```bash
gradle assembleDebug
```
Or verify compilation with the automated build tool:
```bash
compile_applet
```
