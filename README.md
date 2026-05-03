# 📦 ProJect Inventory
### ระบบบริหารจัดการอุปกรณ์และงานสำหรับทีม Production & Event (Professional Version)

แอปพลิเคชัน Android ที่ออกแบบมาเพื่อแก้ปัญหาความวุ่นวายในการจัดการอุปกรณ์ (Inventory) และการจัดสรรคิวงาน (Job Management) โดยเน้นความง่ายในการใช้งาน (UX) และความสวยงามทันสมัย (Modern UI) พร้อมระบบติดตามสถานะแบบละเอียดและระบบซ่อมบำรุงในตัว

---

## 🎨 UI / Wireframe Design
ออกแบบ UI ด้วย Figma:

🔗 [ดู Wireframe บน Figma]([https://your-figma-link.com](https://www.figma.com/design/LJp7AsSiZqJioWs7siOYi4/Wirefream?node-id=0-1&t=37tQ5arE35GkCnl5-1))

---

## ✅ ฟีเจอร์ทั้งหมด (Features Checklist)

| ฟีเจอร์ | สถานะ | ตำแหน่ง |
| :--- | :---: | :--- |
| **จัดการสต็อกและหมวดหมู่** | ✅ | InventoryScreen |
| **ระบบค้นหา (Search System)** | ✅ | InventoryScreen |
| **QR Code Generation** | ✅ | QRCodeGenerator + InventoryScreen |
| **สั่งพิมพ์ QR Code (Print)** | ✅ | ImageSaver + PrintHelper |
| **จัดการงานและคิวจอง (Job Management)** | ✅ | InventoryScreen (Job Dialogs) |
| **ล็อคอุปกรณ์อัตโนมัติ (Auto Booking)** | ✅ | InventoryViewModel |
| **ติดตามสถานะ Real-time (Life-cycle)** | ✅ | InventoryScreen (Status Badges) |
| **ระบบแจ้งซ่อมอัตโนมัติ (Repair Flow)** | ✅ | InventoryScreen (Check-in Action) |
| **ติดตามความคืบหน้าการซ่อม** | ✅ | InventoryScreen (Repair Tab) |
| **สรุปรายได้/มูลค่า (Revenue Overview)** | ✅ | InventoryScreen (Job Detail) |

---

## 1. กลยุทธ์ UX (UX Strategy)

### เป้าหมายของแอป (App Goals)
- **สำหรับผู้ใช้**: ช่วยให้การจัดการอุปกรณ์จำนวนมากเป็นเรื่องง่าย ลดความผิดพลาดในการจัดของลงงาน
- **สำหรับทีมงาน**: ติดตามได้ทันทีว่าอุปกรณ์ตัวไหนอยู่ที่ใคร สถานะเป็นอย่างไร และต้องซ่อมบำรุงเมื่อไหร่

### กลุ่มเป้าหมาย (Target Audience)
- **Production Crew**: ผู้ที่ต้องจัดเตรียมอุปกรณ์และนำออกไปหน้างาน
- **Event Manager**: ผู้ดูแลคิวงานและการจองอุปกรณ์ในแต่ละโปรเจ็ค
- **Maintenance Team**: ช่างที่ดูแลการซ่อมบำรุงอุปกรณ์ที่ชำรุด

### Pain Points & แนวทางแก้ไข
- **อุปกรณ์สูญหาย/หาไม่เจอ**: แก้ไขด้วยระบบ **QR Code** ประจำตัวเครื่อง สแกนเพื่อเช็คสถานะได้ทันที
- **ของเสียแล้วไม่ได้รับการซ่อม**: แก้ไขด้วยระบบ **Seamless Repair Flow** เมื่อ Check-in แล้วแจ้งเสีย ของจะเด้งไปหน้าซ่อมทันที
- **จองของซ้อนกัน**: แก้ไขด้วยระบบ **Automatic Booking** ที่ล็อคสถานะอุปกรณ์ตามวันที่ใน Job

---

## 2. ผังโครงสร้างแอป (Sitemap)

- **Dashboard**: หน้าหลักแสดงรายการอุปกรณ์ทั้งหมด แยกตามหมวดหมู่ (Speaker, Mic, Mixer, ฯลฯ)
- **Inventory Detail**: รายละเอียดอุปกรณ์, เลข Serial, สถานะปัจจุบัน และ QR Code
- **Job Management**: รายการงาน (Active, Upcoming, Completed) และการเลือกอุปกรณ์ลงงาน
- **Maintenance Center**: รายการอุปกรณ์ที่รอซ่อม (Repair Pending) และกำลังซ่อม (Repairing)
- **Reports/Settings**: สรุปภาพรวมและตั้งค่าระบบ

---

## 3. การออกแบบ UI (UI Design Visuals)

- **สไตล์ (Style)**: Professional, Modern, และ Functional
- **โทนสี (Status Colors)**:
  - 🟢 **Available**: พร้อมใช้งาน (#4CAF50)
  - 🔵 **Booked**: จองแล้ว (#2196F3)
  - 🟡 **Busy**: ติดงาน (#FFEB3B)
  - 🔴 **Repair Pending**: รอซ่อม (#F44336)
  - 🟠 **Repairing**: กำลังซ่อม (#FF9800)
- **ตัวอักษร (Typography)**: ใช้ Sarabun (สำหรับภาษาไทย) เพื่อความเป็นทางการและอ่านง่าย

---

## 🍳 ProJect Inventory — Technical Documentation

### Tech Stack
| เทคโนโลยี | รายละเอียด |
| :--- | :--- |
| **Language** | Kotlin (1.9+) |
| **UI Framework** | Jetpack Compose (Declarative UI) |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Database** | Room Persistence (Schema v12) - Offline 100% |
| **Image Loading** | Coil |
| **QR Engine** | ZXing Library |
| **Build System** | Gradle (Kotlin DSL) + KSP |

### การเก็บข้อมูล (Data Storage)
- **Local Database**: ใช้ Room จัดการข้อมูล Item, Job และ Category ทั้งหมดแบบ Offline
- **Image Storage**: บันทึกรูปภาพ QR Code และรูปอุปกรณ์ลงใน Internal Storage ผ่าน ImageSaver
- **State Management**: ใช้ StateFlow และ SharedFlow ใน ViewModel เพื่อจัดการสถานะแบบ Reactive

---

## 📁 โครงสร้างไฟล์ (File Structure)

```text
app/src/main/java/com/example/projectinventory/
├── data/
│   ├── InventoryItem.kt        # Data Models (Item, Job, Enums)
│   ├── InventoryPersistence.kt # Room DB, DAOs, Type Converters
│   └── InventoryViewModel.kt    # Business Logic & Data Stream
├── ui/
│   ├── screens/
│   │   └── InventoryScreen.kt  # UI หลัก (Main Screen, Tabs, Dialogs)
│   └── theme/
│       ├── Color.kt             # ระบบสี (Brand & Status Colors)
│       ├── Type.kt              # Typography Configuration
│       └── Theme.kt             # Material 3 Theme Setup
└── util/
    ├── QRCodeGenerator.kt      # เครื่องมือสร้าง QR Code
    └── ImageSaver.kt           # จัดการการบันทึกภาพและระบบ Print
```

---

## 📋 ขั้นตอนการทำงาน (Workflows)

1. **Add Inventory**: เพิ่มอุปกรณ์ใหม่เข้าระบบ พร้อมระบุหมวดหมู่และ Serial
2. **Assign Job**: สร้างงานใหม่และเลือกอุปกรณ์ที่ต้องการใช้ ระบบจะเปลี่ยนสถานะเป็น `Booked`
3. **Check-out**: เมื่อถึงวันงาน เปลี่ยนสถานะอุปกรณ์เป็น `Busy`
4. **Check-in & Report**: เมื่อจบงาน ตรวจสอบสภาพ หากชำรุดระบบจะส่งเข้า **Repair Flow** ทันที
5. **Maintain**: ช่างดำเนินการซ่อมและกด "Restore" เพื่อคืนค่ากลับเป็น `Available`

---

## ⚙️ การติดตั้งและรันโปรเจ็ค

1. ติดตั้ง **Android Studio Ladybug (2024.2.1)** ขึ้นไป
2. Clone Repository
3. ทำการ **Gradle Sync**
4. รันแอปบน Android 10.0+ (API 29+) เพื่อรองรับฟีเจอร์การพิมพ์และจัดการไฟล์ที่สมบูรณ์

---
*Professional Inventory Solution for Production Teams*
