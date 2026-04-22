package com.example.projectinventory.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val db = InventoryDatabase.getDatabase(application)
    private val dao = db.inventoryDao()

    private val _items = MutableStateFlow<List<InventoryItem>>(emptyList())
    val items: StateFlow<List<InventoryItem>> = _items.asStateFlow()

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs.asStateFlow()

    init {
        loadItems()
        loadJobs()
    }

    private fun loadItems() {
        viewModelScope.launch {
            dao.getAllItems().collect { entities ->
                if (entities.isEmpty()) {
                    seedInitialData()
                } else {
                    _items.value = entities.map { it.toDomain() }
                }
            }
        }
    }

    private fun loadJobs() {
        viewModelScope.launch {
            dao.getAllJobs().collect { entities ->
                _jobs.value = entities.map { it.toDomain() }
            }
        }
    }

    private suspend fun seedInitialData() {
        val itemsToSeed = mutableListOf<InventoryItem>()

        // --- AUDIO SYSTEM (40 Million THB) ---
        repeat(24) { itemsToSeed.add(InventoryItem(name = "L-Acoustics K1 Line Array", type = ItemType.SPEAKER)) }
        repeat(32) { itemsToSeed.add(InventoryItem(name = "L-Acoustics K2 Line Array", type = ItemType.SPEAKER)) }
        repeat(24) { itemsToSeed.add(InventoryItem(name = "L-Acoustics KS28 Subwoofer", type = ItemType.SPEAKER)) }
        repeat(16) { itemsToSeed.add(InventoryItem(name = "L-Acoustics Kara II", type = ItemType.SPEAKER)) }
        repeat(2) { itemsToSeed.add(InventoryItem(name = "DiGiCo Quantum 7", type = ItemType.MIXER)) }
        repeat(2) { itemsToSeed.add(InventoryItem(name = "DiGiCo Quantum 338", type = ItemType.MIXER)) }
        itemsToSeed.add(InventoryItem(name = "Avid S6L-32D-192", type = ItemType.MIXER))
        itemsToSeed.add(InventoryItem(name = "Yamaha RIVAGE PM10", type = ItemType.MIXER))
        repeat(24) { itemsToSeed.add(InventoryItem(name = "Shure Axient Digital ADX2FD", type = ItemType.MIC)) }
        repeat(12) { itemsToSeed.add(InventoryItem(name = "Sennheiser Digital 6000", type = ItemType.MIC)) }
        repeat(20) { itemsToSeed.add(InventoryItem(name = "Shure PSM1000 IEM System", type = ItemType.MIC)) }

        // --- VISUAL & LED WALL (30 Million THB) ---
        repeat(400) { itemsToSeed.add(InventoryItem(name = "ROE Visual CB5 LED Panel", type = ItemType.VISUAL)) }
        repeat(200) { itemsToSeed.add(InventoryItem(name = "ROE Visual Ruby R2.3", type = ItemType.VISUAL)) }
        repeat(4) { itemsToSeed.add(InventoryItem(name = "Brompton Tessera SX40 LED Processor", type = ItemType.VISUAL)) }
        repeat(2) { itemsToSeed.add(InventoryItem(name = "Disguise vx4 Media Server", type = ItemType.VISUAL)) }
        repeat(4) { itemsToSeed.add(InventoryItem(name = "Panasonic PT-RQ35K Projector", type = ItemType.VISUAL)) }

        // --- LIGHTING SYSTEM (20 Million THB) ---
        repeat(2) { itemsToSeed.add(InventoryItem(name = "MA Lighting grandMA3 full-size", type = ItemType.LIGHT)) }
        repeat(2) { itemsToSeed.add(InventoryItem(name = "MA Lighting grandMA3 light", type = ItemType.LIGHT)) }
        repeat(48) { itemsToSeed.add(InventoryItem(name = "Robe BMFL WashBeam", type = ItemType.LIGHT)) }
        repeat(60) { itemsToSeed.add(InventoryItem(name = "Martin MAC Aura PXL", type = ItemType.LIGHT)) }
        repeat(40) { itemsToSeed.add(InventoryItem(name = "Clay Paky Sharpy Plus", type = ItemType.LIGHT)) }
        repeat(24) { itemsToSeed.add(InventoryItem(name = "GLP JDC1 Hybrid Strobe", type = ItemType.LIGHT)) }

        // --- RIGGING, TRUSS & STAGING (7 Million THB) ---
        repeat(24) { itemsToSeed.add(InventoryItem(name = "CM Lodestar 1-Ton Electric Hoist", type = ItemType.RIGGING)) }
        repeat(100) { itemsToSeed.add(InventoryItem(name = "Global Truss F34 Square Truss 3m", type = ItemType.RIGGING)) }
        repeat(12) { itemsToSeed.add(InventoryItem(name = "Eurotruss HD Ground Support Tower", type = ItemType.RIGGING)) }

        // --- POWER & INFRASTRUCTURE (3 Million THB) ---
        repeat(4) { itemsToSeed.add(InventoryItem(name = "400A Power Distro Rack (3-Phase)", type = ItemType.POWER)) }
        repeat(10) { itemsToSeed.add(InventoryItem(name = "Powerlock Cable Set 50m", type = ItemType.POWER)) }
        repeat(50) { itemsToSeed.add(InventoryItem(name = "Rubber Mat / Cable Protector", type = ItemType.POWER)) }

        // --- BACKLINE ---
        itemsToSeed.add(InventoryItem(name = "Grand Piano Yamaha CFX", type = ItemType.BACKLINE))
        itemsToSeed.add(InventoryItem(name = "DW Collector's Drum Kit", type = ItemType.BACKLINE))
        itemsToSeed.add(InventoryItem(name = "Nord Stage 4 + Kronos 2", type = ItemType.BACKLINE))

        // --- CABLES (5 Million THB) ---
        repeat(100) { itemsToSeed.add(InventoryItem(name = "Sommer Stage 22 XLR Cable 10m", type = ItemType.CABLE)) }
        repeat(50) { itemsToSeed.add(InventoryItem(name = "Sommer Stage 22 XLR Cable 20m", type = ItemType.CABLE)) }
        repeat(40) { itemsToSeed.add(InventoryItem(name = "Sommer Elephant Speaker Cable 20m", type = ItemType.CABLE)) }
        repeat(30) { itemsToSeed.add(InventoryItem(name = "Cat7 High-Speed Data Cable 50m", type = ItemType.CABLE)) }
        repeat(20) { itemsToSeed.add(InventoryItem(name = "Socapex 19-pin Lighting Multi 30m", type = ItemType.CABLE)) }

        // --- STANDS & SUPPORTS (2 Million THB) ---
        repeat(50) { itemsToSeed.add(InventoryItem(name = "K&M 210/9 Professional Mic Stand", type = ItemType.STAND)) }
        repeat(20) { itemsToSeed.add(InventoryItem(name = "K&M 21436 Speaker Stand", type = ItemType.STAND)) }
        repeat(10) { itemsToSeed.add(InventoryItem(name = "Heavy Duty Crank Tower (6m)", type = ItemType.STAND)) }
        repeat(30) { itemsToSeed.add(InventoryItem(name = "Guitar Stand Multi-Rack", type = ItemType.STAND)) }

        dao.insertItems(itemsToSeed.map { it.toEntity() })

        val initialJobs = listOf(
            Job(name = "SWU MUSIC FEST", customer = "SWU", location = "SWU Ongkharak", date = "2026-05-20", preset = JobPreset.OUTDOOR),
            Job(name = "World Tour: Rock Star 2024", customer = "Global Music Ent", location = "Impact Arena", date = "2026-06-15", preset = JobPreset.BAND),
            Job(name = "EDM Festival: Neon Night", customer = "Party Planner Co.", location = "BITEC Bangna", date = "2026-07-20", preset = JobPreset.DJ),
            Job(name = "Corporate Gala: Annual 2024", customer = "Fortune 500 Co.", location = "Siam Paragon", date = "2026-08-05", preset = JobPreset.TALK),
            Job(name = "Grand Opening: Luxury Mall", customer = "Central Group", location = "Phuket", date = "2026-09-12", preset = JobPreset.OUTDOOR),
            Job(name = "Jazz in the Park", customer = "BMA", location = "Lumpini Park", date = "2026-10-05", preset = JobPreset.BAND),
            Job(name = "International Seminar 2024", customer = "UNESCO", location = "QSNCC", date = "2026-11-20", preset = JobPreset.TALK),
            Job(name = "Celebrity Wedding: K&P", customer = "Private Client", location = "Mandarin Oriental", date = "2026-12-15", preset = JobPreset.WEDDING),
            Job(name = "Rock the Town 2025", customer = "Chang", location = "Khao Yai", date = "2026-12-25", preset = JobPreset.OUTDOOR),
            Job(name = "Startup Pitch Day", customer = "Hubba", location = "True Digital Park", date = "2026-12-30", preset = JobPreset.TALK)
        )

        dao.insertJobs(initialJobs.map { it.toEntity() })

        // Auto-assign items for initial jobs
        val allItems = dao.getAllItemsList().map { it.toDomain() }.toMutableList()
        val itemsToUpdate = mutableListOf<InventoryItem>()

        initialJobs.forEach { job ->
            val requiredTypes = when {
                job.name == "SWU MUSIC FEST" -> listOf(
                    ItemType.SPEAKER to 32, 
                    ItemType.MIXER to 2, 
                    ItemType.MIC to 20, 
                    ItemType.LIGHT to 40, 
                    ItemType.VISUAL to 20, 
                    ItemType.RIGGING to 12, 
                    ItemType.CABLE to 80,
                    ItemType.STAND to 15
                )
                job.preset == JobPreset.TALK -> listOf(ItemType.MIC to 2, ItemType.SPEAKER to 2, ItemType.MIXER to 1, ItemType.STAND to 2, ItemType.CABLE to 5)
                job.preset == JobPreset.BAND -> listOf(ItemType.MIC to 8, ItemType.SPEAKER to 4, ItemType.MIXER to 1, ItemType.BACKLINE to 4, ItemType.CABLE to 20, ItemType.STAND to 6)
                job.preset == JobPreset.DJ -> listOf(ItemType.SPEAKER to 4, ItemType.MIXER to 1, ItemType.VISUAL to 4, ItemType.CABLE to 10)
                job.preset == JobPreset.WEDDING -> listOf(ItemType.MIC to 4, ItemType.SPEAKER to 4, ItemType.MIXER to 1, ItemType.LIGHT to 12, ItemType.CABLE to 15)
                job.preset == JobPreset.OUTDOOR -> listOf(ItemType.SPEAKER to 16, ItemType.MIXER to 2, ItemType.LIGHT to 24, ItemType.VISUAL to 10, ItemType.RIGGING to 12, ItemType.POWER to 4, ItemType.CABLE to 50)
                else -> emptyList()
            }
            
            requiredTypes.forEach { (type, count) ->
                var assignedCount = 0
                for (i in allItems.indices) {
                    val item = allItems[i]
                    if (item.type == type && item.status == ItemStatus.AVAILABLE && assignedCount < count) {
                        val updatedItem = item.copy(status = ItemStatus.BUSY, currentJobId = job.id)
                        allItems[i] = updatedItem
                        itemsToUpdate.add(updatedItem)
                        assignedCount++
                    }
                }
            }
        }
        
        if (itemsToUpdate.isNotEmpty()) {
            dao.updateItems(itemsToUpdate.map { it.toEntity() })
        }
    }

    fun addItem(name: String, type: ItemType) {
        val newItem = InventoryItem(name = name, type = type)
        viewModelScope.launch {
            dao.insertItem(newItem.toEntity())
        }
    }

    fun addJob(
        name: String,
        customer: String,
        location: String,
        date: String,
        teamTime: String,
        preset: JobPreset,
        notes: String,
        reminderEnabled: Boolean
    ) {
        viewModelScope.launch {
            val newJob = Job(
                name = name,
                customer = customer,
                location = location,
                date = date,
                teamTime = teamTime,
                preset = preset,
                notes = notes,
                reminderEnabled = reminderEnabled
            )
            dao.insertJob(newJob.toEntity())

            // --- AUTO-ASSIGN LOGIC ---
            val requiredTypes = when (preset) {
                JobPreset.TALK -> listOf(
                    ItemType.MIC to 2, 
                    ItemType.SPEAKER to 2, 
                    ItemType.MIXER to 1,
                    ItemType.STAND to 2,
                    ItemType.CABLE to 5
                )
                JobPreset.BAND -> listOf(
                    ItemType.MIC to 8, 
                    ItemType.SPEAKER to 4, 
                    ItemType.MIXER to 1, 
                    ItemType.BACKLINE to 4,
                    ItemType.CABLE to 20,
                    ItemType.STAND to 6
                )
                JobPreset.DJ -> listOf(
                    ItemType.SPEAKER to 4, 
                    ItemType.MIXER to 1, 
                    ItemType.VISUAL to 2,
                    ItemType.CABLE to 10
                )
                JobPreset.WEDDING -> listOf(
                    ItemType.MIC to 4, 
                    ItemType.SPEAKER to 4, 
                    ItemType.MIXER to 1, 
                    ItemType.LIGHT to 8,
                    ItemType.CABLE to 15
                )
                JobPreset.OUTDOOR -> listOf(
                    ItemType.SPEAKER to 16, 
                    ItemType.MIXER to 2, 
                    ItemType.LIGHT to 24, 
                    ItemType.VISUAL to 20,
                    ItemType.RIGGING to 12,
                    ItemType.POWER to 4,
                    ItemType.CABLE to 50
                )
            }

            val allItems = dao.getAllItemsList().map { it.toDomain() }.toMutableList()
            val itemsToUpdate = mutableListOf<InventoryItem>()
            
            requiredTypes.forEach { (type, count) ->
                var assignedCount = 0
                for (i in allItems.indices) {
                    val item = allItems[i]
                    if (item.type == type && item.status == ItemStatus.AVAILABLE && assignedCount < count) {
                        val updatedItem = item.copy(status = ItemStatus.BUSY, currentJobId = newJob.id)
                        allItems[i] = updatedItem
                        itemsToUpdate.add(updatedItem)
                        assignedCount++
                    }
                }
            }
            
            if (itemsToUpdate.isNotEmpty()) {
                dao.updateItems(itemsToUpdate.map { it.toEntity() })
            }
        }
    }

    fun updateJob(job: Job) {
        viewModelScope.launch {
            dao.insertJob(job.toEntity())
        }
    }

    fun deleteJob(job: Job) {
        viewModelScope.launch {
            val jobItems = dao.getAllItemsList().filter { it.currentJobId == job.id }
            val updatedItems = jobItems.map { 
                it.copy(status = ItemStatus.AVAILABLE.name, currentJobId = null) 
            }
            if (updatedItems.isNotEmpty()) {
                dao.updateItems(updatedItems)
            }
            dao.deleteJob(job.toEntity())
        }
    }

    fun checkOut(itemId: String, jobId: String) {
        viewModelScope.launch {
            val items = dao.getAllItemsList()
            val entity = items.find { it.id == itemId }
            entity?.let {
                dao.updateItem(it.copy(status = ItemStatus.BUSY.name, currentJobId = jobId))
            }
        }
    }

    fun checkIn(itemId: String, isDamaged: Boolean = false) {
        viewModelScope.launch {
            val items = dao.getAllItemsList()
            val entity = items.find { it.id == itemId }
            entity?.let {
                val nextStatus = if (isDamaged) ItemStatus.REPAIR_PENDING else ItemStatus.AVAILABLE
                dao.updateItem(it.copy(status = nextStatus.name, currentJobId = null))
            }
        }
    }

    fun sendToRepair(itemId: String) {
        viewModelScope.launch {
            val items = dao.getAllItemsList()
            val entity = items.find { it.id == itemId }
            entity?.let {
                dao.updateItem(it.copy(status = ItemStatus.REPAIR_PENDING.name, currentJobId = null))
            }
        }
    }

    fun updateRepairStatus(itemId: String, status: ItemStatus) {
        viewModelScope.launch {
            val items = dao.getAllItemsList()
            val entity = items.find { it.id == itemId }
            entity?.let {
                dao.updateItem(it.copy(status = status.name))
            }
        }
    }

    fun returnFromRepair(itemId: String) {
        viewModelScope.launch {
            val items = dao.getAllItemsList()
            val entity = items.find { it.id == itemId }
            entity?.let {
                dao.updateItem(it.copy(status = ItemStatus.AVAILABLE.name, currentJobId = null))
            }
        }
    }

    fun getJobById(id: String) = _jobs.value.find { it.id == id }

    // Mappers
    private fun InventoryItemEntity.toDomain() = InventoryItem(
        id = id,
        name = name,
        type = try { ItemType.valueOf(type) } catch (e: Exception) { ItemType.MIC },
        status = try { 
            when(status) {
                "REPAIR" -> ItemStatus.REPAIR_PENDING
                else -> ItemStatus.valueOf(status)
            }
        } catch (e: Exception) { 
            ItemStatus.AVAILABLE 
        },
        serial = serial,
        currentJobId = currentJobId,
        imageUrl = imageUrl,
        dailyRate = dailyRate
    )

    private fun InventoryItem.toEntity() = InventoryItemEntity(
        id = id,
        name = name,
        type = type.name,
        status = status.name,
        serial = serial,
        currentJobId = currentJobId,
        imageUrl = imageUrl,
        dailyRate = dailyRate
    )

    private fun JobEntity.toDomain() = Job(
        id = id,
        name = name,
        customer = customer,
        location = location,
        date = date,
        teamTime = teamTime,
        preset = JobPreset.valueOf(preset),
        notes = notes,
        reminderEnabled = reminderEnabled
    )

    private fun Job.toEntity() = JobEntity(
        id = id,
        name = name,
        customer = customer,
        location = location,
        date = date,
        teamTime = teamTime,
        preset = preset.name,
        notes = notes,
        reminderEnabled = reminderEnabled
    )
}
