# Kunci rilis (supaya update di dalam aplikasi bisa terpasang)

Android hanya mau memasang update kalau APK baru ditandatangani dengan kunci yang SAMA dengan versi yang sudah terpasang.
Kunci debug otomatis dibuat ulang di setiap build GitHub Actions, jadi tiap rilis punya tanda tangan berbeda dan update gagal.

## Sekali saja: buat keystore
```
keytool -genkeypair -v -keystore rapify-release.keystore -alias rapify \
  -keyalg RSA -keysize 2048 -validity 10000
```
Pakai password yang sama untuk keystore dan key. Di Termux: `pkg install openjdk-17` dulu.

**Simpan file `rapify-release.keystore` dan passwordnya di tempat aman (di luar repo).**
Kalau hilang, update untuk user yang sudah pasang tidak akan pernah bisa dikirim lagi.

## Isi GitHub Secrets
Repo > Settings > Secrets and variables > Actions > New repository secret:

| Nama | Isi |
|------|-----|
| `RAPIFY_KEYSTORE_BASE64` | hasil `base64 -w 0 rapify-release.keystore` |
| `RAPIFY_KEYSTORE_PASSWORD` | password keystore |
| `RAPIFY_KEY_ALIAS` | `rapify` |
| `RAPIFY_KEY_PASSWORD` | password key (sama dengan keystore) |

## Catatan
- Versi yang ditandatangani kunci baru ini tidak bisa menimpa versi lama yang memakai kunci debug. User perlu uninstall sekali, setelah itu semua update berikutnya mulus.
- Jangan pernah commit file keystore ke repo. `.gitignore` sudah memblokirnya.
