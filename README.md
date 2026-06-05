# 🌿 Odak — Sakin To-Do & Odaklanma Uygulaması

Görevlerini yönet, zamanını ölç, odağını koru. Sakinleştirici adaçayı-yeşili
bir arayüzle tasarlanmış, tamamen Kotlin + Jetpack Compose ile yazılmış native
Android uygulaması.

## ✨ Özellikler

- ✅ **Görevler** — günlük görev ekle / sil, durum takibi: *Bekliyor → Devam ediyor → Yapıldı*
- 📅 **Günlük görünüm** — günler arası gezin, her günün tamamlanma yüzdesini gör
- 🖼️ **Fotoğraf ekle** — göreve galeriden veya kameradan fotoğraf iliştir
- ⏱️ **Kronometre** — tur (lap) desteğiyle
- ⏳ **Zamanlayıcı** — hazır süre seçenekleri + geri sayım, bitince titreşim
- 🍅 **Pomodoro** — 25/5/15 dakikalık odak & mola döngüleri, seans takibi
- 💾 **Yerel veritabanı** — tüm veriler Room ile cihazda saklanır
- 🎨 **Sakinleştirici tasarım** — yumuşak renkler, açık/koyu tema desteği

## 📲 İndir & Kur

> APK her güncellemede GitHub Actions tarafından otomatik derlenir.

**Tek tıkla indir:**
👉 https://github.com/KULLANICI_ADI/REPO_ADI/releases/latest/download/Odak.apk

Kurulum:
1. APK'yı telefonuna indir.
2. Açtığında "Bilinmeyen kaynaklardan yüklemeye izin ver" çıkarsa onayla.
3. Kur ve aç. 🎉

> Not: APK debug anahtarıyla imzalanır; yeni sürüme geçerken bazen eski sürümü
> kaldırman gerekebilir.

## 🛠️ Teknolojiler

- Kotlin 2.0 · Jetpack Compose (Material 3)
- Room (veritabanı) · KSP
- Coil (görsel yükleme) · ViewModel · Coroutines

## 🏗️ Kendin Derlemek İstersen

Android Studio ile aç ve çalıştır, ya da komut satırından:

```bash
./gradlew assembleRelease
# Çıktı: app/build/outputs/apk/release/app-release.apk
```

Gereksinimler: JDK 17, Android SDK (API 34).

## 📂 Proje Yapısı

```
app/src/main/java/com/odak/app/
├── data/      # Room: Task, DAO, veritabanı, repository
├── ui/
│   ├── tasks/   # Görev listesi, kart, ekleme/düzenleme sayfası
│   ├── timer/   # Kronometre, zamanlayıcı, pomodoro
│   ├── components/ # Dairesel gösterge, kontrol butonları
│   └── theme/   # Renkler, tipografi, tema
└── util/      # Tarih, fotoğraf depolama, titreşim, zaman biçimi
```

---
🤖 Generated with [Claude Code](https://claude.com/claude-code)
