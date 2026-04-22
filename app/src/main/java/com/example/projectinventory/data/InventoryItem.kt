package com.example.projectinventory.data

import java.util.UUID

enum class ItemType(val displayName: String, val prefix: String) {
    SPEAKER("Speaker", "SPK"),
    MIC("Mic", "MIC"),
    CABLE("Cable", "CBL"),
    STAND("Stand", "STD"),
    MIXER("Mixer", "MIX")
}

enum class ItemStatus(val displayName: String, val emoji: String) {
    AVAILABLE("พร้อมใช้", "🟢"),
    BUSY("ติดงาน", "🟡"),
    REPAIR_PENDING("รอซ่อม", "🔴"),
    REPAIRING("กำลังซ่อม", "🟠")
}

enum class JobPreset(val displayName: String) {
    TALK("งานพูด/สัมมนา"),
    BAND("วงดนตรี"),
    DJ("DJ"),
    WEDDING("งานแต่ง"),
    OUTDOOR("งานกลางแจ้ง")
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
    val currentJobId: String? = null // ผูกกับ Job เมื่อสถานะเป็น BUSY
)

fun generateSerial(type: ItemType): String {
    val randomPart = (100000..999999).random()
    return "${type.prefix}$randomPart"
}
