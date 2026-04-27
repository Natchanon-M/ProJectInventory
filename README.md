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

### 📁 `app/src/main/java/com/example/projectinventory/`
- **`MainActivity.kt`**: จุดเริ่มต้นของแอปพลิเคชัน จัดการ Compose UI, Permissions และเป็นโฮสต์สำหรับหน้าจอหลัก

#### 📁 `data/` (การจัดการข้อมูล)
- **`InventoryItem.kt`**: นิยาม Model ข้อมูล (InventoryItem, Job) และสถานะต่างๆ (ItemStatus, ItemType, JobPreset)
- **`InventoryPersistence.kt`**: จัดการฐานข้อมูล Room ประกอบด้วย Database Configuration, DAO และ Entities
- **`InventoryViewModel.kt`**: หัวใจหลักของ Business Logic เชื่อมต่อข้อมูลจาก DB มายัง UI และคำนวณรายได้/สถานะอุปกรณ์

#### 📁 `ui/` (ส่วนแสดงผล)
- **`screens/InventoryScreen.kt`**: ไฟล์ UI หลักที่รวมหน้าจอสต็อก, รายการงาน, และระบบซ่อมบำรุงเข้าด้วยกัน
- **`theme/`**: กำหนดค่าสี (`Color.kt`), รูปแบบตัวอักษร (`Type.kt`) และธีมหลักของแอป (`Theme.kt`)

#### 📁 `util/` (เครื่องมือช่วยเหลือ)
- **`QRCodeGenerator.kt`**: เครื่องมือสร้าง QR Code จาก Serial Number ของอุปกรณ์
- **`ImageSaver.kt`**: จัดการการบันทึกรูปภาพลง Gallery และระบบการพิมพ์ QR Code (Print)
- **`ReminderReceiver.kt`**: ตัวจัดการการแจ้งเตือน (Notifications) ตามเวลาที่นัดหมายในงาน

#### 📁 `widget/` (ส่วนเสริมหน้าจอ Home)
- **`InventoryWidget.kt`**: จัดการระบบ App Widget เพื่อดูสรุปสถานะอุปกรณ์และงานจากหน้าจอหลัก

## 🚀 เริ่มต้นใช้งาน

1. Clone โปรเจ็คนี้ลงในเครื่อง
2. เปิดด้วย **Android Studio (Ladybug หรือใหม่กว่า)**
3. Sync Gradle และกด Run แอปบน Emulator หรือเครื่องจริง (รองรับ Android 8.0+ / API 26+)

---
*โปรเจ็คนี้พัฒนาขึ้นเพื่อเพิ่มประสิทธิภาพในการทำงานของทีม Production และลดความผิดพลาดในการจัดเตรียมอุปกรณ์*
