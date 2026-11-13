# Panduan Pemula — Menjalankan Project

## A. Persiapan
1. Instal JDK 17 (atau lebih baru).
2. Instal Apache Maven.
3. Pastikan perintah berikut berhasil di terminal:
   - `java -version`
   - `mvn -version`

## B. Membuka project
1. Ekstrak `VisualRealismSuite.zip`.
2. Buka folder `VisualRealismSuite` (folder yang langsung berisi `pom.xml`).
3. Di IntelliJ IDEA pilih **Open** lalu pilih folder tersebut. Jika memakai NetBeans, pilih **File > Open Project** dan arahkan ke folder tersebut.
4. Tunggu Maven mengunduh LWJGL, JOML, dan native library.

## C. Menjalankan
Buka terminal pada folder project, lalu jalankan satu per satu:
```bash
mvn clean compile
mvn exec:java
```
Jangan menjalankan file Java satu per satu karena resource shader dan native LWJGL harus ditemukan dari konfigurasi Maven.

## D. Uji lima indikator
1. **Phong / Blinn-Phong**: tekan B beberapa kali. Perhatikan kilauan (specular highlight).
2. **Smooth / Flat**: tekan F. Perhatikan perbedaan permukaan bola.
3. **Z-Buffer**: tekan Z untuk mematikan/mengaktifkan depth test.
4. **Back-Face Culling**: tekan C untuk mematikan/mengaktifkan pembuangan sisi belakang.
5. **Texture**: tekan T untuk menampilkan/menghilangkan tekstur.
6. **Filtering**: tekan M untuk membandingkan Nearest dengan Linear + Mipmap.
7. **Shadow Mapping**: tekan H untuk menampilkan/menghilangkan bayangan.

Untuk screenshot, ambil minimal satu gambar untuk setiap perbandingan. Tampilkan juga terminal yang mencatat perubahan status bila diperlukan.

## E. Jika ada error
- Salin seluruh pesan error dari terminal, mulai dari baris pertama yang bertuliskan `[ERROR]` atau `Exception`.
- Kirim screenshot tampilan error dan sebutkan apakah kamu memakai IntelliJ IDEA, NetBeans, atau VS Code.
- Jangan menghapus file shader atau folder `resources`.
