# RapiFy Stats Backend (counter pengguna anonim)

Gratis di Cloudflare (free tier). Pengguna app **tidak perlu login**; hanya kamu (developer) yang butuh akun Cloudflare sekali untuk deploy.

```bash
npm i -g wrangler
wrangler login
cd backend
wrangler d1 create rapify-stats          # salin database_id ke wrangler.toml
wrangler d1 execute rapify-stats --remote --file=schema.sql
wrangler deploy                          # catat URL: https://rapify-stats.<akun>.workers.dev
```

Lalu isi `STATS_BASE_URL` di
`app/src/main/java/com/padukarafi/rapify/data/stats/StatsManager.kt` dengan URL tadi dan build ulang APK.

Cek manual: `curl https://rapify-stats.<akun>.workers.dev/stats` → `{"total":0,"active30d":0}`

Catatan: endpoint `/ping` terbuka (APK open source, tidak ada secret yang bisa disembunyikan),
jadi orang iseng bisa menggelembungkan angka dengan ID palsu. Untuk hobi/komunitas ini cukup.
