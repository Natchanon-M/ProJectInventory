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
                    seedInitialItems()
                } else {
                    _items.value = entities.map { it.toDomain() }
                }
            }
        }
    }

    private fun loadJobs() {
        viewModelScope.launch {
            dao.getAllJobs().collect { entities ->
                if (entities.isEmpty()) {
                    seedInitialJobs()
                } else {
                    _jobs.value = entities.map { it.toDomain() }
                }
            }
        }
    }

    private suspend fun seedInitialItems() {
        val initialItems = listOf(
            InventoryItem(name = "Shure SM58", type = ItemType.MIC),
            InventoryItem(name = "JBL EON715", type = ItemType.SPEAKER),
            InventoryItem(name = "XLR Cable 10m", type = ItemType.CABLE),
            InventoryItem(name = "Behringer X32", type = ItemType.MIXER),
            InventoryItem(name = "Mic Stand", type = ItemType.STAND)
        )
        initialItems.forEach { dao.insertItem(it.toEntity()) }
    }

    private suspend fun seedInitialJobs() {
        val initialJobs = listOf(
            Job(name = "Concert A", date = "2024-05-01"),
            Job(name = "Wedding B", date = "2024-05-05"),
            Job(name = "Conference C", date = "2024-05-10")
        )
        initialJobs.forEach { dao.insertJob(it.toEntity()) }
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
        viewModelScope.launch {
            dao.insertJob(newJob.toEntity())
        }
    }

    fun updateJob(job: Job) {
        viewModelScope.launch {
            dao.insertJob(job.toEntity())
        }
    }

    fun deleteJob(job: Job) {
        viewModelScope.launch {
            // Unassign items from this job before deleting
            _items.value.filter { it.currentJobId == job.id }.forEach { item ->
                val updated = item.copy(status = ItemStatus.AVAILABLE, currentJobId = null)
                dao.updateItem(updated.toEntity())
            }
            dao.deleteJob(job.toEntity())
        }
    }

    fun checkOut(itemId: String, jobId: String) {
        viewModelScope.launch {
            val item = _items.value.find { it.id == itemId }
            item?.let {
                val updated = it.copy(status = ItemStatus.BUSY, currentJobId = jobId)
                dao.updateItem(updated.toEntity())
            }
        }
    }

    fun checkIn(itemId: String, isDamaged: Boolean = false) {
        viewModelScope.launch {
            val item = _items.value.find { it.id == itemId }
            item?.let {
                val nextStatus = if (isDamaged) ItemStatus.REPAIR_PENDING else ItemStatus.AVAILABLE
                val updated = it.copy(status = nextStatus, currentJobId = null)
                dao.updateItem(updated.toEntity())
            }
        }
    }

    fun sendToRepair(itemId: String) {
        viewModelScope.launch {
            val item = _items.value.find { it.id == itemId }
            item?.let {
                val updated = it.copy(status = ItemStatus.REPAIR_PENDING, currentJobId = null)
                dao.updateItem(updated.toEntity())
            }
        }
    }

    fun updateRepairStatus(itemId: String, status: ItemStatus) {
        viewModelScope.launch {
            val item = _items.value.find { it.id == itemId }
            item?.let {
                val updated = it.copy(status = status)
                dao.updateItem(updated.toEntity())
            }
        }
    }

    fun returnFromRepair(itemId: String) {
        viewModelScope.launch {
            val item = _items.value.find { it.id == itemId }
            item?.let {
                val updated = it.copy(status = ItemStatus.AVAILABLE, currentJobId = null)
                dao.updateItem(updated.toEntity())
            }
        }
    }

    fun getJobById(id: String) = _jobs.value.find { it.id == id }

    // Mappers
    private fun InventoryItemEntity.toDomain() = InventoryItem(
        id = id,
        name = name,
        type = ItemType.valueOf(type),
        status = ItemStatus.valueOf(status),
        serial = serial,
        currentJobId = currentJobId,
        imageUrl = imageUrl
    )

    private fun InventoryItem.toEntity() = InventoryItemEntity(
        id = id,
        name = name,
        type = type.name,
        status = status.name,
        serial = serial,
        currentJobId = currentJobId,
        imageUrl = imageUrl
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
