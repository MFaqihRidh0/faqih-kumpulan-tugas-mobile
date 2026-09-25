# TaskMaster - Aplikasi CRUD SQLite & Uji Persistensi Data
**Mata Kuliah: Pemrograman Mobile (Semester 5) - Institut Teknologi Sepuluh Nopember**  
**Tugas 4: Fungsionalitas CRUD, Kebenaran Skema & Query SQLite, Uji Persistensi Data**

---

## 📱 Ringkasan Aplikasi
**TaskMaster** adalah aplikasi manajemen tugas dan estimasi anggaran proyek mahasiswa berbasis Android murni (**Java**) yang dirancang dengan antarmuka modern **Material Design 3**. Aplikasi ini mengimplementasikan siklus penuh basis data relasional lokal **SQLite** menggunakan `SQLiteOpenHelper`, pemisahan kontrak skema (`DatabaseContract`), transaksi aman (`SQLite Transaction`), query terparameterisasi untuk pencegahan *SQL Injection*, serta fitur visual **Uji Persistensi Data**.

---

## 🎯 Pemenuhan Kriteria Tugas 4

### 1. Fungsionalitas (CRUD Lengkap + Nilai Tambah)
- **Create (Tambah Data)**: Menambahkan tugas baru dengan input Judul, Deskripsi, Kategori (*Dropdown*), Prioritas (*Dropdown*), Tenggat Waktu (*DatePicker Dialog*), dan Estimasi Anggaran (Rp - tipe `REAL`). Dilengkapi validasi input *realtime*.
- **Read (Tampil Data, Filter & Search)**:
  - Menampilkan daftar tugas dalam bentuk *CardView* interaktif.
  - Pencarian tugas secara *real-time* berdasarkan Judul, Deskripsi, atau Kategori.
  - Filter cepat menggunakan *Chips*: Semua, Belum Selesai, Selesai, Prioritas Tinggi, Kuliah, Proyek.
  - Pengurutan (*Sorting*): Terbaru, Deadline Terdekat, Prioritas Tertinggi, Anggaran Terbesar.
  - Menampilkan ringkasan statistik (Total Tugas, Selesai, Pending, Total Anggaran).
- **Update (Perbarui Data)**:
  - *Full Update*: Memperbarui seluruh field tugas melalui form dialog.
  - *Quick Status Toggle*: Menyelesaikan tugas cukup dengan mencentang *checkbox* dengan animasi coret (*strikethrough*).
- **Delete (Hapus Data)**:
  - Menghapus satu tugas dengan dialog konfirmasi (*prevent accidental deletion*).
  - Menghapus seluruh data (*Reset Database*) dengan konfirmasi ganda.
- **Detail View**: Dialog informasi komprehensif menampilkan seluruh data termasuk SQLite Row ID (`_id`), waktu pembuatan, dan status keterlambatan (*overdue*).
- **Batch Sample Data**: Kemudahan pengujian melalui tombol "Muat Data Sampel" untuk asisten lab/dosen penguji.

### 2. Kebenaran Skema & Query SQLite
- **Skema Normal & Tipe Data Standar SQLite**:
  - `_id`: `INTEGER PRIMARY KEY AUTOINCREMENT`
  - `title`: `TEXT NOT NULL`
  - `description`: `TEXT`
  - `category`: `TEXT NOT NULL`
  - `priority`: `TEXT NOT NULL`
  - `due_date`: `TEXT NOT NULL`
  - `budget`: `REAL NOT NULL DEFAULT 0.0` (Mencakup tipe `REAL`)
  - `is_completed`: `INTEGER NOT NULL DEFAULT 0` (Boolean SQLite)
  - `created_at`: `TEXT NOT NULL` (Timestamp)
- **Implementasi Best Practices**:
  - `DatabaseContract` mendefinisikan konstanta nama tabel, nama kolom, query `CREATE TABLE`, dan `DROP TABLE`.
  - Operasi `INSERT` dan `UPDATE` menggunakan `ContentValues`.
  - Operasi `SELECT` dan `DELETE` menggunakan `selection` dan `selectionArgs` (parameterized query) untuk mencegah kerentanan SQL Injection.
  - Menggunakan query agregasi SQLite: `COUNT(*)`, `SUM(budget)`, dan `SUM(CASE WHEN is_completed = 1 THEN 1 ELSE 0 END)`.
  - `SQLiteDatabase.beginTransaction()` dan `setTransactionSuccessful()` pada penyisipan data sampel (*Transaction Management*).
  - Pengecekan integritas fisik database menggunakan `PRAGMA integrity_check;`.

### 3. Uji Persistensi Data
- **Penyimpanan Fisik**: Basis data disimpan permanen pada media penyimpanan internal Android (`/data/data/com.its.tugas4sqlite/databases/taskmaster.db`).
- **Verifikasi Persistensi**:
  - Pengguna dapat keluar dari aplikasi, menutup paksa (*Force Stop* / *Kill Process* di App Switcher), atau me-restart emulator/perangkat; saat aplikasi dibuka kembali, seluruh data tetap utuh.
  - Fitur bawaan **"Uji SQLite"** di bagian atas aplikasi menampilkan path penyimpanan fisik database, ukuran file (KB), jumlah baris data tersimpan, serta hasil eksekusi `PRAGMA integrity_check`.
  - Disediakan automated test (`TaskModelAndContractTest` dan `DatabasePersistenceAndroidTest`) untuk pengujian otomatis.

### 4. Desain yang Bagus (Material Design 3 & Rich Aesthetics)
- **Modern Indigo & Mint Palette**: Gradien elegan pada header dashboard.
- **Kartu Statistik Interaktif**: Menampilkan metrik tugas dan anggaran secara langsung.
- **Responsif & Bersih**: Sudut membulat (*16dp corner radius*), elevasi halus, pemisahan visual yang jelas.
- **Badging & Micro-interactions**: Badge warna berbeda untuk masing-masing prioritas (Tinggi = Merah, Sedang = Kuning, Rendah = Biru) dan kategori.
- **Empty State Informatif**: Ilustrasi dan pesan ramah jika data kosong.

---

## 📂 Struktur Proyek
```
app/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/its/tugas4sqlite/
│   │   │   ├── MainActivity.java
│   │   │   ├── adapter/
│   │   │   │   └── TaskAdapter.java
│   │   │   ├── database/
│   │   │   │   ├── DatabaseContract.java
│   │   │   │   └── DatabaseHelper.java
│   │   │   ├── model/
│   │   │   │   └── Task.java
│   │   │   ├── ui/
│   │   │   │   ├── PersistenceInfoDialog.java
│   │   │   │   ├── TaskDetailDialog.java
│   │   │   │   └── TaskFormDialog.java
│   │   │   └── util/
│   │   │       ├── CurrencyUtils.java
│   │   │       └── DateUtils.java
│   │   └── res/
│   │       ├── drawable/ (ikon vektor, gradien header, chip badges)
│   │       ├── layout/
│   │       │   ├── activity_main.xml
│   │       │   ├── item_task.xml
│   │       │   ├── dialog_task_form.xml
│   │       │   ├── dialog_task_detail.xml
│   │       │   └── dialog_persistence_info.xml
│   │       ├── menu/main_menu.xml
│   │       └── values/ (colors.xml, strings.xml, themes.xml)
│   ├── test/java/com/its/tugas4sqlite/
│   │   └── TaskModelAndContractTest.java
│   └── androidTest/java/com/its/tugas4sqlite/
│       └── DatabasePersistenceAndroidTest.java
```

---

## 🚀 Cara Menjalankan Proyek
1. Buka folder proyek ini di **Android Studio**.
2. Biarkan Gradle melakukan sinkronisasi otomatis (*Sync Project with Gradle Files*).
3. Jalankan aplikasi pada **Emulator** atau **Device Fisik Android** (klik tombol ▶️ *Run 'app'*).
4. Untuk menguji persistensi data:
   - Tambah atau modifikasi tugas.
   - Tutup aplikasi dari recent apps / Task Manager.
   - Buka kembali aplikasi, seluruh data tersimpan dengan sempurna di SQLite.
   - Tekan tombol **"Uji SQLite"** di kanan atas untuk melihat informasi teknis file database dan status integritas.
