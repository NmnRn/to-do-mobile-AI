# 🌿 Akış — Sakin To-Do, Plan & Odaklanma Uygulaması

Görevlerini yönet, gününü planla, zamanını ölç, odağını koru. Sakinleştirici
adaçayı-yeşili bir arayüzle tasarlanmış; tamamen Kotlin + Jetpack Compose ile
yazılmış native Android uygulaması. **Türkçe ve İngilizce** dil desteği var.

## ✨ Özellikler

### 📋 Görevler
- Günlük görev ekle / sil, durum takibi: *Bekliyor → Devam ediyor → Yapıldı*
- ⏰ **Saatli alarm** — göreve saat ver, tam vaktinde kilit ekranını uyandıran alarm çalsın
- 🔁 **Tekrarlayan görevler** — her gün / hafta içi / haftalık; tamamlanınca bir sonraki otomatik oluşur
- ☑️ **Alt görevler** — görev içinde kontrol listesi ve ilerleme
- 🚩 **Öncelik & kategori** — Düşük/Orta/Yüksek renk çubuğu + serbest etiket, akıllı sıralama
- 🖼️ Göreve galeriden veya kameradan fotoğraf iliştir
- 📅 Günler arası gezin, her günün tamamlanma yüzdesini gör

### 🗓️ Günlük Plan (zaman bloklama)
- Gün içine **saat aralıkları** gir: *09:00 → 10:30, ne yapılacak*
- Devam eden blok **"ŞİMDİ"** olarak vurgulanır, tamamlandı işareti, toplam süre özeti

### ⏱️ Zaman araçları
- **Kronometre** — tur (lap) desteğiyle
- **Zamanlayıcı** — hazır süreler + geri sayım, bitince ses & titreşim
- **Pomodoro** — odak/mola döngüleri, seans takibi
- Çalışırken kalıcı bildirim; uygulama arka planda olsa da süre işler

### 🔔 Bildirimler
- Yapılandırılabilir **görev hatırlatmaları** (sıklık + sessiz saatler)
- Kilit ekranında görünür, tam ekran alarm, pil optimizasyonu muafiyeti

### 📊 Görünürlük & veri
- 🏠 **Ana ekran widget'ı** — bugünün görevleri ve tamamlanma sayısı
- 📈 **Haftalık odak raporu** — son 7 günün tamamlanan görev + odak dakikası grafiği
- 💾 **Yedekleme** — görevleri JSON olarak dışa aktar / geri yükle
- 🌐 **Dil** — Sistem / Türkçe / English (Ayarlar'dan anında değişir)
- 🎨 Sistem/Aydınlık/Karanlık tema
- 🔄 Uygulama içi güncelleme: Ayarlar'dan son commit'e göre kontrol et, indir & kur

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

> Not: Uygulama **sabit bir imza anahtarıyla** imzalanır. Çok eski bir sürümün
> kuruluysa, yeni sürümü kurmadan önce eskisini bir kez kaldırman gerekebilir.

## 🛠️ Teknolojiler

- Kotlin 2.0 · Jetpack Compose (Material 3)
- Room (veritabanı) · KSP · WorkManager
- Coil (görsel yükleme) · ViewModel · Coroutines
- AlarmManager (saatli alarmlar) · AppWidget · Foreground Service

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
├── data/      # Room: Task, PlanBlock, DAO, veritabanı, repository
├── task/      # Saatli alarmlar, tekrar, alarm/aksiyon/boot receiver'ları
├── reminder/  # Periyodik hatırlatma (WorkManager)
├── timer/     # Canlı süre için foreground service
├── widget/    # Ana ekran widget'ı
├── ui/
│   ├── tasks/   # Görev listesi, kart, ekleme/düzenleme sayfası
│   ├── plan/    # Günlük plan ekranı ve düzenleyici
│   ├── timer/   # Kronometre, zamanlayıcı, pomodoro
│   ├── settings/# Tema, dil, bildirim, rapor, yedek, güncelleme
│   └── theme/   # Renkler, tipografi, tema
└── util/      # Tarih, dil yöneticisi, fotoğraf, yedek, rapor, bildirim
```

Diller `res/values/` (Türkçe) ve `res/values-en/` (İngilizce) altında.

---
🤖 Generated with [Claude Code](https://claude.com/claude-code)
