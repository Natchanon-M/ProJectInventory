# 📦 ProJect Inventory
### ระบบบริหารจัดการอุปกรณ์และงานสำหรับทีม Production & Event (Professional Version)

แอปพลิเคชัน Android ที่ออกแบบมาเพื่อแก้ปัญหาความวุ่นวายในการจัดการอุปกรณ์ (Inventory) และการจัดสรรคิวงาน (Job Management) โดยเน้นความง่ายในการใช้งาน (UX) และความสวยงามทันสมัย (Modern UI) พร้อมระบบติดตามสถานะแบบละเอียดและระบบซ่อมบำรุงในตัว

---

## 🚀 ฟีเจอร์ที่สำคัญ (Key Features)

### 1. การจัดการสต็อกและหมวดหมู่ (Smart Inventory)
- **Dashboard ดีไซน์ใหม่**: แสดงรายการอุปกรณ์พร้อมไอคอนแบ่งตามประเภท (Speaker, Mic, Mixer, Light, ฯลฯ)
- **Category Filter**: กรองอุปกรณ์ตามประเภทได้ทันทีผ่านแถบ Chip ด้านบน
- **Search System**: ค้นหาอุปกรณ์ด้วยชื่อหรือ Serial Number ได้อย่างรวดเร็ว
- **QR Code Engine**: ทุกอุปกรณ์จะมี QR Code ของตัวเอง สามารถกดดู เซฟลงเครื่อง หรือสั่งพิมพ์ (Print) เพื่อแปะติดกับตัวอุปกรณ์ได้

### 2. ระบบจัดการงานและคิวจอง (Advanced Job Management)
- **Job Segmentation**: แบ่งสถานะงานออกเป็น 3 ระยะ:
  - `Active`: งานที่กำลังดำเนินการอยู่ในปัจจุบัน
  - `Upcoming`: งานในอนาคตที่จองอุปกรณ์ไว้แล้ว
  - `Completed`: ประวัติงานที่จบไปแล้ว
- **Automatic Booking**: เมื่อใส่อุปกรณ์ลงในงาน ระบบจะล็อคสถานะอุปกรณ์นั้นๆ ให้โดยอัตโนมัติ
- **Revenue Overview**: สรุปยอดรวมมูลค่าอุปกรณ์หรือรายได้ในแต่ละงาน

### 3. ระบบสถานะอุปกรณ์แบบ Real-time (Item Life-cycle)
ติดตามวงจรชีวิตของอุปกรณ์ได้อย่างแม่นยำ:
- 🟢 **Available (พร้อมใช้งาน)**: อุปกรณ์ว่างพร้อมลงงาน
- 🔵 **Booked (จองแล้ว)**: อุปกรณ์ถูกจองสำหรับงานในอนาคต แต่ตัวยังอยู่ที่คลัง
- 🟡 **Busy (ติดงาน)**: อุปกรณ์กำลังใช้งานอยู่ในหน้างานปัจจุบัน
- 🔴 **Repair Pending (รอซ่อม)**: อุปกรณ์แจ้งเสียจากการ Check-in และรอช่างดำเนินการ
- 🟠 **Repairing (กำลังซ่อม)**: อุปกรณ์อยู่ในขั้นตอนการซ่อมบำรุง

### 4. ระบบซ่อมบำรุง (Maintenance & Repair Flow)
- **Seamless Integration**: เมื่อจบงาน (Check-in) หากเลือกสถานะเป็น "ชำรุด" อุปกรณ์จะถูกส่งเข้าหน้าซ่อมบำรุงทันที
- **Repair Tracking**: บันทึกวันที่เริ่มซ่อม, รายละเอียดอาการเสีย (Repair Note) และความคืบหน้า
- **One-tap Restore**: เมื่อซ่อมเสร็จ สามารถกด "พร้อมใช้งาน" เพื่อคืนสถานะกลับสู่สต็อกได้ทันที

---

## 🛠 ข้อมูลทางเทคนิค (Tech Stack)

### Core Technologies
- **Language**: Kotlin (1.9+)
- **UI Framework**: **Jetpack Compose** (Declarative UI)
- **Local Database**: **Room Persistence** (Schema v12) - จัดการข้อมูลแบบ Offline 100%
- **Architecture**: **MVVM** (Model-View-ViewModel) พร้อม StateFlow สำหรับการจัดการ State แบบ Reactive

### Libraries & Tools
- **Material Design 3**: ใช้คอมโพเนนต์ล่าสุดเพื่อ UI ที่สวยงามและรองรับ Dynamic Color
- **ZXing Library**: สำหรับการ Generate และสแกน QR Code
- **KSP (Kotlin Symbol Processing)**: เพื่อเพิ่มประสิทธิภาพในการ Compile Room Database
- **Coil**: สำหรับการโหลดและจัดการรูปภาพอุปกรณ์
- **ImageSaver/PrintHelper**: ระบบจัดการไฟล์ภาพและการพิมพ์เอกสาร

---

## 📁 โครงสร้างโปรเจ็ค (Project Structure)

```text
app/src/main/java/com/example/projectinventory/
├── data/
│   ├── InventoryItem.kt        # Data Models (Item, Job, Enums)
│   ├── InventoryPersistence.kt # Room DB, DAOs, Type Converters
│   └── InventoryViewModel.kt    # Business Logic & Data Stream
├── ui/
│   ├── screens/
│   │   └── InventoryScreen.kt  # UI หลัก (Main Screen & Dialogs)
│   └── theme/
│       ├── Color.kt             # ระบบสี (Brand Colors)
│       ├── Type.kt              # Typography (Sarabun/Sans-serif)
│       └── Theme.kt             # Material Theme Configuration
└── util/
    ├── QRCodeGenerator.kt      # เครื่องมือสร้าง QR Code
    └── ImageSaver.kt           # จัดการการบันทึกภาพและระบบ Print
```

---

## 📋 ขั้นตอนการทำงานของแอป (Workflows)

1. **เพิ่มอุปกรณ์**: ใส่รายละเอียด ชื่อ, Serial, และหมวดหมู่
2. **สร้างงาน**: กำหนดชื่อสถานที่ วันที่ และเลือกอุปกรณ์ที่ต้องการใช้
3. **หน้างาน (Check-out)**: เมื่อถึงวันงาน กด Check-out เพื่อเปลี่ยนสถานะเป็น `Busy`
4. **คืนของ (Check-in)**: เมื่อจบงาน ตรวจสอบสภาพของ หากปกติจะกลับเป็น `Available` หากเสียจะกลายเป็น `Repair Pending`
5. **การซ่อม**: ช่างกด "เริ่มดำเนินการซ่อม" ในหน้า Repair และกด "ซ่อมเสร็จ" เมื่ออุปกรณ์พร้อมกลับไปใช้งาน

---

## ⚙️ การติดตั้งและรันโปรเจ็ค

1. ติดตั้ง **Android Studio Ladybug (2024.2.1)** หรือเวอร์ชันที่ใหม่กว่า
2. Clone Repository นี้
3. ทำการ **Gradle Sync** เพื่อดาวน์โหลด Dependencies
4. รันแอปพลิเคชันบน Emulator หรือ Physical Device (แนะนำ Android 10.0+ / API 29+)

---
*พัฒนาโดยผู้เชี่ยวชาญ เพื่อชาว Production และ Event โดยเฉพาะ*
