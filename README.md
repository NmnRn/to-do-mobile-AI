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
- 🎨 **Sakinleştirici tasarım** — yumuşak renkler, Sistem/Aydınlık/Karanlık tema
- 🔄 **Uygulama içi güncelleme** — Ayarlar'dan son commit'e göre kontrol et, indir & kur

## 📲 İndir & Kur

> APK her güncellemede GitHub Actions tarafından otomatik derlenir.

**Tek tıkla indir:**
👉 https://github.com/NmnRn/to-do-mobile-AI/releases/latest/download/Odak.apk

Kurulum:
1. APK'yı telefonuna indir.
2. Açtığında "Bilinmeyen kaynaklardan yüklemeye izin ver" çıkarsa onayla.
3. Kur ve aç. 🎉

Sonraki güncellemeler **uygulama içinden** yapılır: **Ayarlar → Güncellemeleri
kontrol et → İndir ve kur**. Uygulama, kurulu sürümün commit'i ile depodaki son
commit'i karşılaştırır; fark varsa en güncel APK'yı indirip kurar.

> Tek seferlik not: Bu sürüm artık **sabit bir imza anahtarıyla** imzalanıyor.
> Daha önce eski bir Odak kurduysan, bu yeni sürümü kurmadan önce eskisini bir
> kez kaldırman gerekebilir (imza değişikliği). Sonrasında güncellemeler sorunsuz
> üstüne kurulur.

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
