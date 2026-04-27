# ProJectInventory - ระบบบริหารจัดการอุปกรณ์และงาน (Inventory & Job Management)

แอปพลิเคชันสำหรับบริหารจัดการคลังอุปกรณ์ (Inventory) สำหรับธุรกิจเช่าอุปกรณ์ หรือทีมงาน Event/Production ช่วยให้การติดตามสถานะอุปกรณ์และการจัดสรรลงในงานต่างๆ เป็นเรื่องง่ายและเป็นระบบ

## 🌟 ฟีเจอร์หลัก (Features)

- **Inventory Management**: เพิ่ม แก้ไข และลบอุปกรณ์ โดยแบ่งตามหมวดหมู่ (Speaker, Mic, Mixer, Light, ฯลฯ)
- **Job Management**: สร้างรายการงาน (Jobs) เพื่อจองและจัดสรรอุปกรณ์ลงในแต่ละงาน
- **Status Tracking**: ติดตามสถานะอุปกรณ์แบบ Real-time:
  - `พร้อมใช้ (Available)`
  - `ติดงาน (Busy)` - พร้อมระบบบอกสถานะ "จองแล้วแต่ยังว่าง" หากยังไม่ถึงวันงาน
  - `รอซ่อม (Repair Pending)` และ `กำลังซ่อม (Repairing)`
- **QR Code System**: สร้าง QR Code อัตโนมัติสำหรับอุปกรณ์ทุกชิ้น พร้อมฟังก์ชันบันทึกลงเครื่องหรือสั่งพิมพ์ (Print)
- **Revenue Calculation**: คำนวณรายได้/มูลค่าอุปกรณ์ที่ใช้ในแต่ละงานโดยอัตโนมัติ
- **Smart Filtering & Search**: ค้นหาอุปกรณ์ตามชื่อหรือ Serial Number และกรองตามหมวดหมู่
- **Check-in/Check-out System**: ระบบยืม-คืนอุปกรณ์ พร้อมตรวจสอบสภาพอุปกรณ์หลังจบงาน

## 🛠️ Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Modern Android UI)
- **Architecture**: MVVM (Model-View-ViewModel)
- **Local Database**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
- **Design System**: Material Design 3
- **Dependency Injection**: Android ViewModel & State Management
- **Other Libraries**:
  - `ZXing`: สำหรับสร้างและจัดการ QR Code
  - `KSP`: สำหรับประมวลผล Room Database
  - `Glance`: สำหรับรองรับ App Widgets (ในอนาคต)

## 📂 โครงสร้างโปรเจ็ค (Project Structure)

- `data/`: เก็บ Model (InventoryItem, Job), Enums และ Room Database Configuration
- `ui/screens/`: โค้ดส่วนหน้าจอหลัก (`InventoryScreen.kt`) และ UI Logic
- `ui/theme/`: การกำหนดสี, Font และสไตล์ของแอป (Material 3)
- `util/`: คลาสช่วยเหลือต่างๆ เช่น `QRCodeGenerator` และ `ImageSaver`

## 🚀 เริ่มต้นใช้งาน

1. Clone โปรเจ็คนี้ลงในเครื่อง
2. เปิดด้วย **Android Studio (Ladybug หรือใหม่กว่า)**
3. Sync Gradle และกด Run แอปบน Emulator หรือเครื่องจริง (รองรับ Android 8.0+ / API 26+)

---
*โปรเจ็คนี้พัฒนาขึ้นเพื่อเพิ่มประสิทธิภาพในการทำงานของทีม Production และลดความผิดพลาดในการจัดเตรียมอุปกรณ์*
