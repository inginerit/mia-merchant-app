# MIA Merchant - VictoriaBank Payment Gateway Android App

[![Build APK](https://github.com/YOUR_USERNAME/mia-merchant-app/actions/workflows/android-build.yml/badge.svg)](https://github.com/YOUR_USERNAME/mia-merchant-app/actions)

Aplicație Android nativă pentru comercianți care utilizează sistemul MIA Plăți Instant al Băncii Naționale a Moldovei prin API-ul VictoriaBank.

## 📱 Funcționalități

### ✅ Generare QR Codes
- **QR Dinamic (DYNM)** - QR unic pentru fiecare tranzacție, cu TTL configurabil
- **QR Static (STAT)** - 3 tipuri:
  - **Fixed Amount** - Suma fixă prestabilită
  - **Controlled Amount** - Sumă în interval (min-max)
  - **Free Amount** - Client alege suma

### ✅ Rapoarte (Reconciliation)
- Selectare perioadă (dată start/end)
- Vizualizare listă tranzacții
- Detalii complete: plătitor, sumă, dată, status

### ✅ Notificări Push
- **Polling automat** - Verificare status la fiecare 20 secunde
- **Signal Webhook** - Notificare instant când plata este primită
- **Smart logic** - Stop automat când QR expiră sau este plătit
- Notificări Android native

### ✅ Securitate
- **JWT Verification** - Verificare semnătură Signal cu certificat VBCA
- **Encrypted Storage** - Credențiale stocate cu EncryptedSharedPreferences
- **Token Caching** - Refresh automat token-uri expirate
- **HTTPS Only** - Toate comunicările pe HTTPS

### ✅ Configurări
- Username/Password VictoriaBank
- IBAN comerciant
- Test Mode / Production Mode
- Webhook URL (generat automat)
- Default TTL pentru QR dinamic
- Certificat VBCA integrat

## 🏗️ Arhitectură

```
app/
├── api/           # Retrofit API client
├── data/          # Room Database + Models
├── services/      # Background services (Polling)
├── ui/            # Activities, Fragments, Adapters
└── utils/         # SecurePrefs, JWT, QR Generator
```

### Stack Tehnologic
- **Kotlin** - Limbaj principal
- **Jetpack Components** - AndroidX, Room, WorkManager
- **Retrofit** - API client
- **Jose4j** - JWT verification
- **ZXing** - QR code generation
- **Material Design 3** - UI components

## 🚀 Instalare & Build

### Cerințe
- Android Studio Hedgehog | 2023.1.1+
- JDK 17
- Android SDK 34
- Gradle 8.2+

### Build Local

```bash
# Clone repository
git clone https://github.com/YOUR_USERNAME/mia-merchant-app.git
cd mia-merchant-app

# Build Debug APK
./gradlew assembleDebug

# APK-ul va fi în:
# app/build/outputs/apk/debug/app-debug.apk

# Build Release APK
./gradlew assembleRelease
```

### Build cu GitHub Actions

1. Fork acest repository
2. Push la branch `main` sau `master`
3. GitHub Actions va compila automat APK-ul
4. Descarcă APK-ul din **Actions** → **Artifacts**

## 📝 Configurare Aplicație

### 1. Prima deschidere
La prima deschidere, aplicația va afișa ecranul **Settings**.

### 2. Configurare credențiale
Completează:
- **Username** - Username VictoriaBank API
- **Password** - Password VictoriaBank API  
- **IBAN** - IBAN-ul contului de comerciant
- **Test Mode** - ON pentru test, OFF pentru producție
- **Default TTL** - Timp implicit pentru QR dinamic (minute)

### 3. Webhook URL
Aplicația generează automat un Webhook URL. Acest URL trebuie transmis băncii pentru a primi notificări Signal.

**Format**: `https://your-server.com/api/webhook/{username}`

⚠️ **Important**: Pentru webhook funcțional, trebuie să implementezi un server care:
- Primește POST requests la URL-ul generat
- Verifică semnătura JWT cu certificatul VBCA
- Procesează Signal-urile (Payment, Expiration, Inactivation)

## 🔧 Utilizare

### Generare QR Dinamic (pentru checkout online)
1. Mergi la **Generate QR**
2. Selectează **DYNM - Dinamic**
3. Introdu suma
4. Setează TTL (ex: 30 minute)
5. Adaugă descriere
6. Apasă **Generate QR Code**

QR-ul va fi:
- Afișat pe ecran
- Salvat în **My QR Codes**
- Monitorizat automat pentru plată

### Generare QR Static (pentru magazin fizic)
1. Mergi la **Generate QR**
2. Selectează tipul dorit:
   - **STAT - Fixed** (sumă fixă)
   - **STAT - Controlled** (interval min-max)
   - **STAT - Free** (suma aleasă de client)
3. Completează câmpurile necesare
4. Apasă **Generate QR Code**

QR-ul static poate fi:
- Printant și afișat în magazin
- Refolosit de multiple ori
- Valid 90 zile de la ultima plată

### Verificare Rapoarte
1. Mergi la **Reports**
2. Selectează data **From** (start)
3. Selectează data **To** (end)
4. Apasă **Load Reports**

Vei vedea:
- Lista tuturor tranzacțiilor
- Nume plătitor
- Sumă + monedă
- Data și ora
- Status (Approved/Pending/Rejected)

## 🔐 Securitate

### JWT Signature Verification
Aplicația verifică toate Signal-urile primite de la bancă folosind:
- Certificat X.509 (VBCA.crt)
- Algoritm RS256 (RSA cu SHA-256)
- Validare payload și semnătură

### Encrypted Storage
- Toate credențialele sunt criptate cu AES256-GCM
- Token-urile JWT sunt stocate securizat
- Master Key generat cu Android Keystore

## 📊 API Integration

### Endpoints implementate:
- `POST /api/identity/token` - Autentificare
- `POST /api/v1/qr` - Generare QR
- `GET /api/v1/qr/{uuid}/status` - Status QR
- `DELETE /api/v1/qr/{uuid}` - Anulare QR
- `GET /api/v1/reconciliation/transactions` - Rapoarte
- `GET /api/v1/signal/{uuid}` - Ultimul signal
- `DELETE /api/v1/transaction/{ref}` - Refund

### Base URLs:
- **Test**: `https://test-ipspj.victoriabank.md`
- **Production**: `https://ips-api-pj.vb.md`

## 📱 Screenshots

*(Adaugă aici capturi de ecran cu aplicația)*

## 🤝 Contribuții

Contribuțiile sunt binevenite! Pentru modificări majore:

1. Fork repository-ul
2. Creează un branch (`git checkout -b feature/amazing-feature`)
3. Commit modificările (`git commit -m 'Add amazing feature'`)
4. Push la branch (`git push origin feature/amazing-feature`)
5. Deschide un Pull Request

## 📄 Licență

Acest proiect este licențiat sub GPL-2.0-or-later - vezi fișierul [LICENSE](LICENSE).

## ⚠️ Disclaimer

Această aplicație este dezvoltată independent și NU este afiliată oficial cu VictoriaBank S.A. sau Banca Națională a Moldovei.

VictoriaBank® este marcă înregistrată a VictoriaBank S.A.

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/YOUR_USERNAME/mia-merchant-app/issues)
- **Documentation**: [Wiki](https://github.com/YOUR_USERNAME/mia-merchant-app/wiki)
- **VictoriaBank API**: Contactează VictoriaBank direct pentru acces API

---

**Made with ❤️ for Moldovan merchants**
