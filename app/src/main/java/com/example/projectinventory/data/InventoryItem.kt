package com.example.projectinventory.data

import java.util.UUID

enum class ItemType(val displayName: String, val prefix: String) {
    SPEAKER("Speaker", "SPK"),
    MIC("Mic", "MIC"),
    CABLE("Cable", "CBL"),
    STAND("Stand", "STD"),
    MIXER("Mixer", "MIX"),
    LIGHT("Lighting", "LGT"),
    BACKLINE("Backline", "BKL"),
    VISUAL("Visual/LED", "VSL"),
    RIGGING("Rigging/Truss", "RIG"),
    POWER("Power Distro", "PWR")
}

enum class ItemStatus(val displayName: String, val emoji: String) {
    AVAILABLE("พร้อมใช้", "🟢"),
    BUSY("ติดงาน", "🟡"),
    REPAIR_PENDING("รอซ่อม", "🔴"),
    REPAIRING("กำลังซ่อม", "🟠")
}

enum class JobPreset(val displayName: String, val basePrice: Double) {
    TALK("งานพูด/สัมมนา", 15000.0),
    BAND("วงดนตรี", 45000.0),
    DJ("DJ", 25000.0),
    WEDDING("งานแต่ง", 35000.0),
    OUTDOOR("งานกลางแจ้ง", 85000.0)
}

data class Job(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val customer: String = "",
    val location: String = "",
    val date: String, // Date of the event
    val teamTime: String = "", // เวลานัดทีม
    val preset: JobPreset = JobPreset.TALK,
    val notes: String = "", // จุดปลั๊กไฟ, จุด FOH, ระยะสาย, ข้อห้ามสถานที่
    val reminderEnabled: Boolean = false
)

data class InventoryItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: ItemType,
    val status: ItemStatus = ItemStatus.AVAILABLE,
    val serial: String = generateSerial(type),
    val imageUrl: String? = null,
    val currentJobId: String? = null,
    val dailyRate: Double = getDefaultRate(type),
    val repairNote: String? = null,
    val repairStartDate: String? = null,
    val repairProgress: Int = 0 // 0-100
)

fun getDefaultRate(type: ItemType): Double {
    return when (type) {
        ItemType.SPEAKER -> 3500.0
        ItemType.MIC -> 1500.0
        ItemType.CABLE -> 100.0
        ItemType.STAND -> 200.0
        ItemType.MIXER -> 15000.0
        ItemType.LIGHT -> 2500.0
        ItemType.BACKLINE -> 5000.0
        ItemType.VISUAL -> 1200.0
        ItemType.RIGGING -> 1500.0
        ItemType.POWER -> 3000.0
    }
}

fun generateSerial(type: ItemType): String {
    val randomPart = (100000..999999).random()
    return "${type.prefix}$randomPart"
}
