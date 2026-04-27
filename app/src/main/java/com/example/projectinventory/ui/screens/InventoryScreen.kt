package com.example.projectinventory.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.projectinventory.data.*
import com.example.projectinventory.ui.theme.*
import com.example.projectinventory.util.ImageSaver
import com.example.projectinventory.util.QRCodeGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel) {
    val items by viewModel.items.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // Optimization: Pre-calculate counts and filtered lists using derivedStateOf
    val busyItemsCount by remember { derivedStateOf { items.count { it.status == ItemStatus.BUSY } } }
    val repairItemsCount by remember { derivedStateOf { items.count { it.status == ItemStatus.REPAIR_PENDING || it.status == ItemStatus.REPAIRING } } }

    var selectedItemForCheckOut by remember { mutableStateOf<InventoryItem?>(null) }
    var selectedItemForCheckIn by remember { mutableStateOf<InventoryItem?>(null) }
    var selectedItemForRepair by remember { mutableStateOf<InventoryItem?>(null) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddJobDialog by remember { mutableStateOf(false) }
    var jobToEdit by remember { mutableStateOf<Job?>(null) }
    var jobToDelete by remember { mutableStateOf<Job?>(null) }
    var selectedItemForQRCode by remember { mutableStateOf<InventoryItem?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedJobTab by remember { mutableIntStateOf(0) } // 0: Active, 1: Upcoming, 2: Completed
    
    // New State for Drill-down
    var selectedJobForDetails by remember { mutableStateOf<Job?>(null) }
    var selectedTypeForDetails by remember { mutableStateOf<ItemType?>(null) }
    var selectedTypeInJob by remember { mutableStateOf<ItemType?>(null) }

    var selectedCategoryFilter by remember { mutableStateOf<ItemType?>(null) }

    // Optimization: Filtered items based on search and category
    val filteredItems by remember {
        derivedStateOf {
            items.filter { item ->
                val matchesSearch = searchQuery.isBlank() || 
                    item.name.contains(searchQuery, ignoreCase = true) || 
                    item.serial.contains(searchQuery, ignoreCase = true)
                val matchesCategory = selectedCategoryFilter == null || item.type == selectedCategoryFilter
                matchesSearch && matchesCategory
            }
        }
    }


    Scaffold(
        containerColor = Background,
        topBar = {
            Column(modifier = Modifier.background(Surface)) {
                CenterAlignedTopAppBar(
                    title = { 
                        val title = when {
                            selectedJobForDetails != null -> {
                                if (selectedTypeInJob != null) selectedTypeInJob!!.displayName
                                else selectedJobForDetails!!.name
                            }
                            selectedTypeForDetails != null -> selectedTypeForDetails!!.displayName
                            else -> "INVENTORY"
                        }
                        Text(
                            title, 
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        ) 
                    },
                    navigationIcon = {
                        if (selectedJobForDetails != null || selectedTypeForDetails != null) {
                            IconButton(onClick = { 
                                if (selectedJobForDetails != null && selectedTypeInJob != null) {
                                    selectedTypeInJob = null
                                } else {
                                    selectedJobForDetails = null 
                                    selectedTypeForDetails = null
                                    selectedTypeInJob = null
                                }
                            }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Surface)
                )
                
                if (selectedJobForDetails == null && selectedTypeForDetails == null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Background)
                            .padding(4.dp)
                    ) {
                        TabItem(
                            title = "All Stock", 
                            isSelected = selectedTab == 0, 
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 0 }
                        )
                        TabItem(
                            title = "On Job", 
                            isSelected = selectedTab == 1, 
                            badgeCount = busyItemsCount,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 1 }
                        )
                        TabItem(
                            title = "Repair", 
                            isSelected = selectedTab == 2, 
                            badgeCount = repairItemsCount,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 2 }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (selectedJobForDetails == null) {
                FloatingActionButton(
                    onClick = { 
                        if (selectedTab == 0 || selectedTab == 2) showAddItemDialog = true 
                        else showAddJobDialog = true 
                    },
                    containerColor = Primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(if (selectedTab == 0 || selectedTab == 2) Icons.Default.Add else Icons.Default.PostAdd, contentDescription = "Add")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = Triple(selectedTab, selectedJobForDetails, selectedTypeForDetails),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ContentTransition"
            ) { (tab, jobDetails, typeDetails) ->
                Column {
                    if (tab == 0 && jobDetails == null) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.weight(1f)) {
                                SearchBar(searchQuery) { searchQuery = it }
                            }
                            IconButton(onClick = { /* TODO: Notification */ }) {
                                Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = Secondary)
                            }
                        }

                        // Category Chips
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = selectedCategoryFilter == null,
                                    onClick = { selectedCategoryFilter = null },
                                    label = { Text("All") },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.White
                                    ),
                                    border = null
                                )
                            }
                            items(ItemType.entries.toTypedArray()) { type ->
                                FilterChip(
                                    selected = selectedCategoryFilter == type,
                                    onClick = { selectedCategoryFilter = type },
                                    label = { Text(type.displayName) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Primary,
                                        selectedLabelColor = Color.White
                                    ),
                                    border = null
                                )
                            }
                        }

                        InventoryList(
                            items = filteredItems,
                            jobs = jobs,
                            onQRCodeClick = { selectedItemForQRCode = it },
                            onCheckOut = { selectedItemForCheckOut = it },
                            onCheckIn = { selectedItemForCheckIn = it },
                            onSendToRepair = { selectedItemForRepair = it },
                            onReturnFromRepair = { viewModel.returnFromRepair(it.id) },
                            onUpdateRepairStatus = { itemId, status -> 
                                if (status == ItemStatus.REPAIRING) viewModel.startRepair(itemId)
                            }
                        )
                    } else if (tab == 1 && jobDetails == null) {
                        Column {
                            // Sub-tabs for Jobs
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Surface)
                                    .padding(bottom = 8.dp)
                            ) {
                                JobSubTab("Active", selectedJobTab == 0, Modifier.weight(1f)) { selectedJobTab = 0 }
                                JobSubTab("Upcoming", selectedJobTab == 1, Modifier.weight(1f)) { selectedJobTab = 1 }
                                JobSubTab("Completed", selectedJobTab == 2, Modifier.weight(1f)) { selectedJobTab = 2 }
                            }

                            val filteredJobs = when (selectedJobTab) {
                                0 -> {
                                    val today = java.time.LocalDate.now().toString()
                                    jobs.filter { it.date == today }
                                }
                                1 -> {
                                    val today = java.time.LocalDate.now()
                                    jobs.filter {
                                        try {
                                            java.time.LocalDate.parse(it.date).isAfter(today)
                                        } catch (e: Exception) { false }
                                    }
                                }
                                2 -> {
                                    val today = java.time.LocalDate.now()
                                    jobs.filter {
                                        try {
                                            java.time.LocalDate.parse(it.date).isBefore(today)
                                        } catch (e: Exception) { false }
                                    }
                                }
                                else -> jobs
                            }

                            JobList(
                                jobs = filteredJobs,
                                items = items,
                                statusLabel = when(selectedJobTab) {
                                    0 -> "Active"
                                    1 -> "Upcoming"
                                    2 -> "Completed"
                                    else -> ""
                                },
                                statusColor = when(selectedJobTab) {
                                    0 -> Accent
                                    1 -> Warning
                                    2 -> Secondary
                                    else -> Primary
                                },
                                onJobClick = { selectedJobForDetails = it }
                            )
                        }
                    } else if (tab == 2 && jobDetails == null) {
                        val repairItems by remember(items) {
                            derivedStateOf { items.filter { it.status == ItemStatus.REPAIR_PENDING || it.status == ItemStatus.REPAIRING } }
                        }
                        RepairList(
                            items = repairItems,
                            onStartRepair = { item -> viewModel.startRepair(item.id) },
                            onCompleteRepair = { item -> viewModel.completeRepair(item.id) },
                            onUpdateProgress = { item, progress -> viewModel.updateRepairProgress(item.id, progress) }
                        )
                    } else if (jobDetails != null) {
                        // Show Job Details and Items
                        val jobItems = items.filter { it.currentJobId == jobDetails.id }
                        
                        if (selectedTypeInJob == null) {
                            JobDetailHeader(
                                job = jobDetails,
                                items = items,
                                onEdit = { jobToEdit = jobDetails },
                                onDelete = { jobToDelete = jobDetails }
                            )
                            SearchBar(searchQuery) { searchQuery = it }
                            CategoryList(
                                items = jobItems,
                                jobs = jobs,
                                searchQuery = searchQuery,
                                onCategoryClick = { selectedTypeInJob = it },
                                showAvailableText = false
                            )
                        } else {
                            val filteredJobItems by remember(jobItems, searchQuery) {
                                derivedStateOf {
                                    if (searchQuery.isBlank()) jobItems
                                    else jobItems.filter {
                                        it.name.contains(searchQuery, ignoreCase = true) ||
                                                it.serial.contains(searchQuery, ignoreCase = true)
                                    }
                                }
                            }
                            val categorizedJobItems by remember(filteredJobItems, selectedTypeInJob) {
                                derivedStateOf {
                                    filteredJobItems.filter { it.type == selectedTypeInJob }
                                }
                            }
                            InventoryList(
                                items = categorizedJobItems,
                                jobs = jobs,
                                showJobLabel = false,
                                onQRCodeClick = { selectedItemForQRCode = it },
                                onCheckOut = { selectedItemForCheckOut = it },
                                onCheckIn = { selectedItemForCheckIn = it },
                                onSendToRepair = { selectedItemForRepair = it },
                                onReturnFromRepair = { viewModel.returnFromRepair(it.id) },
                                onUpdateRepairStatus = { itemId, status -> 
                                    if (status == ItemStatus.REPAIRING) viewModel.startRepair(itemId)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Dialogs
        if (showAddItemDialog) {
            AddItemDialog(
                onDismiss = { showAddItemDialog = false },
                onConfirm = { name, type, rate ->
                    viewModel.addItem(name, type, rate)
                    showAddItemDialog = false
                }
            )
        }
        if (showAddJobDialog) {
            AddJobDialog(
                onDismiss = { showAddJobDialog = false },
                onConfirm = { name, customer, location, date, teamTime, preset, notes, reminderEnabled ->
                    viewModel.addJob(name, customer, location, date, teamTime, preset, notes, reminderEnabled)
                    showAddJobDialog = false
                }
            )
        }
        jobToEdit?.let { job ->
            AddJobDialog(
                initialJob = job,
                onDismiss = { jobToEdit = null },
                onConfirm = { name, customer, location, date, teamTime, preset, notes, reminderEnabled ->
                    val updated = job.copy(
                        name = name,
                        customer = customer,
                        location = location,
                        date = date,
                        teamTime = teamTime,
                        preset = preset,
                        notes = notes,
                        reminderEnabled = reminderEnabled
                    )
                    viewModel.updateJob(updated)
                    selectedJobForDetails = updated
                    jobToEdit = null
                }
            )
        }
        jobToDelete?.let { job ->
            AlertDialog(
                onDismissRequest = { jobToDelete = null },
                title = { Text("Delete Job") },
                text = { Text("Are you sure you want to delete '${job.name}'? All items assigned to this job will be returned to stock.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteJob(job)
                            selectedJobForDetails = null
                            jobToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { jobToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        selectedItemForCheckOut?.let { item ->
            CheckOutDialog(item, jobs, { selectedItemForCheckOut = null }, { viewModel.checkOut(item.id, it); selectedItemForCheckOut = null })
        }
        selectedItemForCheckIn?.let { item ->
            CheckInDialog(item, { selectedItemForCheckIn = null }, { viewModel.checkIn(item.id, it); selectedItemForCheckIn = null })
        }
        selectedItemForQRCode?.let { item ->
            QRCodeDialog(item) { selectedItemForQRCode = null }
        }
        selectedItemForRepair?.let { item ->
            AlertDialog(
                onDismissRequest = { selectedItemForRepair = null },
                title = { Text("Send to Repair") },
                text = { Text("Are you sure you want to send '${item.name}' to repair?") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.sendToRepair(item.id)
                            selectedItemForRepair = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Error)
                    ) {
                        Text("Send to Repair")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedItemForRepair = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun RepairList(
    items: List<InventoryItem>,
    onStartRepair: (InventoryItem) -> Unit,
    onCompleteRepair: (InventoryItem) -> Unit,
    onUpdateProgress: (InventoryItem, Int) -> Unit
) {
    val pendingCount = items.count { it.status == ItemStatus.REPAIR_PENDING }
    val repairingCount = items.count { it.status == ItemStatus.REPAIRING }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RepairSummaryCard(
                    title = "Pending",
                    count = pendingCount,
                    color = Error,
                    modifier = Modifier.weight(1f)
                )
                RepairSummaryCard(
                    title = "In Progress",
                    count = repairingCount,
                    color = Warning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Text(
                "Maintenance List",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        if (items.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxHeight(0.6f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No items in maintenance", color = Secondary)
                }
            }
        }

        items(items, key = { it.id }) { item ->
            RepairItemCard(
                item = item,
                onStart = { onStartRepair(item) },
                onComplete = { onCompleteRepair(item) },
                onUpdateProgress = { onUpdateProgress(item, it) }
            )
        }
    }
}

@Composable
fun RepairSummaryCard(title: String, count: Int, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = Secondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                count.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun RepairItemCard(
    item: InventoryItem,
    onStart: () -> Unit,
    onComplete: () -> Unit,
    onUpdateProgress: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(item.serial, style = MaterialTheme.typography.bodySmall, color = Secondary)
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = (if (item.status == ItemStatus.REPAIRING) Warning else Error).copy(alpha = 0.1f)
                ) {
                    Text(
                        item.status.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (item.status == ItemStatus.REPAIRING) Warning else Error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (item.status == ItemStatus.REPAIRING) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Success)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ซ่อมเสร็จแล้ว / พร้อมใช้งาน")
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary)
                ) {
                    Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("เริ่มดำเนินการซ่อม")
                }
            }
            
            item.repairNote?.let {
                if (it.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Background)
                            .padding(8.dp)
                    ) {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = Secondary)
                    }
                }
            }
        }
    }
}

@Composable
fun QRCodeDialog(item: InventoryItem, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val qrBitmap = remember(item.serial) { QRCodeGenerator.generate(item.serial) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            qrBitmap?.let { 
                                ImageSaver.saveToGallery(context, it, "QR_${item.serial}")
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Save", maxLines = 1)
                    }
                    OutlinedButton(
                        onClick = {
                            qrBitmap?.let { 
                                ImageSaver.printBitmap(context, it, item.name)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Print", maxLines = 1)
                    }
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Close")
                }
            }
        },
        title = { Text("Item QR Code", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code for ${item.serial}",
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.White)
                            .padding(8.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Text(item.name, fontWeight = FontWeight.Bold)
                Text(item.serial, color = Secondary)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Surface
    )
}

@Composable
fun JobList(jobs: List<Job>, items: List<InventoryItem>, statusLabel: String, statusColor: Color, onJobClick: (Job) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (jobs.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No jobs found", color = Secondary)
                }
            }
        }
        items(jobs, key = { it.id }) { job ->
            val jobItems = remember(items, job.id) { items.filter { it.currentJobId == job.id } }
            val itemCount = jobItems.size
            val totalPrice = remember(jobItems) { jobItems.sumOf { it.dailyRate } }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onJobClick(job) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            job.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = statusColor.copy(alpha = 0.1f)
                        ) {
                            Text(
                                statusLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                                color = statusColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Secondary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            job.date,
                            style = MaterialTheme.typography.bodySmall,
                            color = Secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f), thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Inventory2,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Secondary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "$itemCount items",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Secondary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Success.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "฿ ${String.format("%,.0f", totalPrice)}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = Success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryList(
    items: List<InventoryItem>,
    jobs: List<Job>,
    searchQuery: String,
    onCategoryClick: (ItemType) -> Unit,
    showAvailableText: Boolean = true
) {
    val today = remember { java.time.LocalDate.now().toString() }
    val categories = ItemType.entries.filter { type ->
        val count = items.count { it.type == type }
        if (count == 0) return@filter false
        
        if (searchQuery.isBlank()) true
        else type.displayName.contains(searchQuery, ignoreCase = true) || 
             items.any { it.type == type && it.name.contains(searchQuery, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (categories.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No categories found", color = Secondary)
                }
            }
        }
        items(categories, key = { it.name }) { type ->
            val totalCount = remember(items, type) { items.count { it.type == type } }
            val availableCount = remember(items, type, jobs, today) {
                items.count { item ->
                    item.type == type && (
                        item.status == ItemStatus.AVAILABLE || 
                        (item.status == ItemStatus.BUSY && jobs.find { it.id == item.currentJobId }?.date?.let { it > today } == true)
                    )
                }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCategoryClick(type) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Background
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val icon = when(type) {
                                    ItemType.SPEAKER -> Icons.Default.Speaker
                                    ItemType.MIC -> Icons.Default.Mic
                                    ItemType.MIXER -> Icons.Default.SettingsInputComponent
                                    ItemType.LIGHT -> Icons.Default.Lightbulb
                                    ItemType.BACKLINE -> Icons.Default.MusicNote
                                    ItemType.VISUAL -> Icons.Default.Tv
                                    ItemType.RIGGING -> Icons.Default.Construction
                                    ItemType.POWER -> Icons.Default.Power
                                    else -> Icons.Default.Inventory
                                }
                                Icon(icon, contentDescription = null, tint = Primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(type.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            val statusText = if (showAvailableText) {
                                "พร้อมใช้ $availableCount จาก $totalCount"
                            } else {
                                "$totalCount รายการในงานนี้"
                            }
                            Text(statusText, style = MaterialTheme.typography.bodySmall, color = Secondary)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Secondary)
                }
            }
        }
    }
}

@Composable
fun InventoryList(
    items: List<InventoryItem>, 
    jobs: List<Job>,
    showJobLabel: Boolean = true,
    onQRCodeClick: (InventoryItem) -> Unit = {},
    onCheckOut: (InventoryItem) -> Unit, 
    onCheckIn: (InventoryItem) -> Unit,
    onSendToRepair: (InventoryItem) -> Unit,
    onReturnFromRepair: (InventoryItem) -> Unit,
    onUpdateRepairStatus: (String, ItemStatus) -> Unit = { _, _ -> }
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (items.isEmpty()) {
            item {
                Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No items found", color = Secondary)
                }
            }
        }
        items(items, key = { it.id }) { item ->
            ModernItemCard(
                item = item,
                jobs = jobs,
                showJobLabel = showJobLabel,
                onQRCodeClick = onQRCodeClick,
                onCheckOut = onCheckOut,
                onCheckIn = onCheckIn,
                onSendToRepair = onSendToRepair,
                onReturnFromRepair = onReturnFromRepair,
                onUpdateRepairStatus = onUpdateRepairStatus
            )
        }
    }
}

@Composable
fun TabItem(title: String, isSelected: Boolean, onClick: () -> Unit, badgeCount: Int = 0, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Surface else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) Primary else Secondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(badgeCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun JobSubTab(title: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) Primary else Secondary,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .fillMaxWidth(0.6f)
                .background(if (isSelected) Primary else Color.Transparent)
        )
    }
}

@Composable
fun SearchBar(query: String, onValueChange: (String) -> Unit) {
    TextField(
        value = query,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(12.dp)),
        placeholder = { Text("Search equipment...", color = Secondary) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Secondary) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            disabledContainerColor = Surface,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        singleLine = true
    )
}

@Composable
fun ModernItemCard(
    item: InventoryItem, 
    jobs: List<Job>,
    showJobLabel: Boolean = true,
    onQRCodeClick: (InventoryItem) -> Unit = {},
    onCheckOut: (InventoryItem) -> Unit, 
    onCheckIn: (InventoryItem) -> Unit,
    onSendToRepair: (InventoryItem) -> Unit,
    onReturnFromRepair: (InventoryItem) -> Unit,
    onUpdateRepairStatus: (String, ItemStatus) -> Unit = { _, _ -> }
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Item Icon Box
                Surface(
                    modifier = Modifier.size(64.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Background
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val icon = when(item.type) {
                            ItemType.SPEAKER -> Icons.Default.Speaker
                            ItemType.MIC -> Icons.Default.Mic
                            ItemType.MIXER -> Icons.Default.SettingsInputComponent
                            ItemType.LIGHT -> Icons.Default.Lightbulb
                            ItemType.BACKLINE -> Icons.Default.MusicNote
                            ItemType.VISUAL -> Icons.Default.Tv
                            ItemType.RIGGING -> Icons.Default.Construction
                            ItemType.POWER -> Icons.Default.Power
                            else -> Icons.Default.Inventory
                        }
                        Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(32.dp))
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(item.serial, style = MaterialTheme.typography.bodySmall, color = Secondary)
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusBadge(item.status, item, jobs)
                }
                
                IconButton(onClick = { onQRCodeClick(item) }) {
                    Icon(Icons.Default.QrCode, contentDescription = "QR", tint = Secondary, modifier = Modifier.size(20.dp))
                }
            }

            if (showJobLabel && item.currentJobId != null) {
                val jobName = jobs.find { it.id == item.currentJobId }?.name ?: item.currentJobId
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Background)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = Accent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("At: $jobName", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                val today = remember { java.time.LocalDate.now().toString() }
                val isFutureJob = item.currentJobId?.let { jobId ->
                    jobs.find { it.id == jobId }?.date?.let { it > today }
                } ?: false

                if (item.status == ItemStatus.AVAILABLE || (item.status == ItemStatus.BUSY && isFutureJob)) {
                    Button(
                        onClick = { onCheckOut(item) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text("Check Out", style = MaterialTheme.typography.labelLarge)
                    }
                    if (item.status == ItemStatus.BUSY) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { onCheckIn(item) },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Check In", color = Primary)
                        }
                    }
                } else if (item.status == ItemStatus.BUSY) {
                    OutlinedButton(
                        onClick = { onCheckIn(item) },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Check In", color = Primary)
                    }
                } else if (item.status == ItemStatus.REPAIR_PENDING) {
                    Button(
                        onClick = { onUpdateRepairStatus(item.id, ItemStatus.REPAIRING) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Warning)
                    ) {
                        Text("เริ่มซ่อม")
                    }
                } else if (item.status == ItemStatus.REPAIRING) {
                    Button(
                        onClick = { onReturnFromRepair(item) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Success)
                    ) {
                        Text("พร้อมใช้")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: ItemStatus, item: InventoryItem? = null, jobs: List<Job> = emptyList()) {
    val today = remember { java.time.LocalDate.now().toString() }
    val isFutureJob = item?.currentJobId?.let { jobId ->
        jobs.find { it.id == jobId }?.date?.let { it > today }
    } ?: false

    val (color, text) = when {
        status == ItemStatus.AVAILABLE -> Success to "Available"
        status == ItemStatus.BUSY && isFutureJob -> Primary to "Booked"
        status == ItemStatus.BUSY -> Warning to "Busy"
        status == ItemStatus.REPAIR_PENDING -> Error to "Repairing"
        status == ItemStatus.REPAIRING -> Warning to "Repairing"
        else -> Secondary to status.displayName
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun JobDetailHeader(job: Job, items: List<InventoryItem>, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Badge(containerColor = Primary.copy(alpha = 0.1f), contentColor = Primary) {
                        Text(job.preset.displayName, modifier = Modifier.padding(4.dp))
                    }
                    if (job.reminderEnabled) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Warning, modifier = Modifier.size(16.dp))
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Job", tint = Secondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Job", tint = Secondary)
                    }
                }
            }
            
            DetailRow(Icons.Default.Person, "Customer: ${job.customer.ifBlank { "-" }}")
            DetailRow(Icons.Default.Place, "Location: ${job.location.ifBlank { "-" }}")
            val jobItems = items.filter { it.currentJobId == job.id }
            val totalPrice = jobItems.sumOf { it.dailyRate }

            DetailRow(Icons.Default.Event, "Date: ${job.date.ifBlank { "-" }}")
            DetailRow(Icons.Default.Schedule, "Team Time: ${job.teamTime.ifBlank { "-" }}")
            DetailRow(
                Icons.Default.Payments, 
                "Actual Equipment Value: ฿${String.format("%,.2f", totalPrice)}",
                color = Success
            )
            
            if (job.notes.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Background)
                Text("Notes:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(job.notes, style = MaterialTheme.typography.bodySmall, color = Secondary)
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color = Color.Unspecified) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = if (color != Color.Unspecified) color else Secondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = if (color != Color.Unspecified) color else Color.Unspecified)
    }
}

@Composable
fun StatusDot(status: ItemStatus, item: InventoryItem? = null, jobs: List<Job> = emptyList()) {
    val today = remember { java.time.LocalDate.now().toString() }
    val isFutureJob = item?.currentJobId?.let { jobId ->
        jobs.find { it.id == jobId }?.date?.let { it > today }
    } ?: false

    val color = when {
        status == ItemStatus.AVAILABLE -> Success
        status == ItemStatus.BUSY && isFutureJob -> Primary
        status == ItemStatus.BUSY -> Warning
        status == ItemStatus.REPAIR_PENDING -> Error
        status == ItemStatus.REPAIRING -> Warning
        else -> Secondary
    }
    
    val displayText = if (status == ItemStatus.BUSY && isFutureJob) "จองแล้ว (ว่าง)" else status.displayName

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(displayText, style = MaterialTheme.typography.labelSmall, color = Secondary)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJobDialog(
    initialJob: Job? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, String, JobPreset, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(initialJob?.name ?: "") }
    var customer by remember { mutableStateOf(initialJob?.customer ?: "") }
    var location by remember { mutableStateOf(initialJob?.location ?: "") }
    var date by remember { mutableStateOf(initialJob?.date ?: "") }
    var teamTime by remember { mutableStateOf(initialJob?.teamTime ?: "") }
    var preset by remember { mutableStateOf(initialJob?.preset ?: JobPreset.TALK) }
    var notes by remember { mutableStateOf(initialJob?.notes ?: "") }
    var reminderEnabled by remember { mutableStateOf(initialJob?.reminderEnabled ?: false) }
    var expandedPreset by remember { mutableStateOf(false) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialJob?.date?.let {
            try {
                java.time.LocalDate.parse(it)
                    .atStartOfDay(java.time.ZoneOffset.UTC)
                    .toInstant()
                    .toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        } ?: System.currentTimeMillis()
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        date = selectedDate.toString()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { 
                    if(name.isNotBlank()) onConfirm(name, customer, location, date, teamTime, preset, notes, reminderEnabled) 
                }, 
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (initialJob == null) "Save Job" else "Update Job")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text(if (initialJob == null) "Add New Job" else "Edit Job", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Job Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = customer,
                        onValueChange = { customer = it },
                        label = { Text("Customer") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedTextField(
                                value = date,
                                onValueChange = { },
                                label = { Text("Date (YYYY-MM-DD)") },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                readOnly = true,
                                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                            )
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { showDatePicker = true }
                            )
                        }
                        
                        OutlinedTextField(
                            value = teamTime,
                            onValueChange = { teamTime = it },
                            label = { Text("Team Time") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            trailingIcon = { Icon(Icons.Default.AccessTime, contentDescription = null) }
                        )
                    }
                }
                item {
                    Box {
                        OutlinedButton(onClick = { expandedPreset = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                            Text("Preset: ${preset.displayName}")
                        }
                        DropdownMenu(expanded = expandedPreset, onDismissRequest = { expandedPreset = false }) {
                            JobPreset.entries.forEach { p ->
                                DropdownMenuItem(text = { Text(p.displayName) }, onClick = { preset = p; expandedPreset = false })
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes (FOH, Power, Cabling...)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        minLines = 3
                    )
                }
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                        Text("Enable Notification")
                    }
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Surface
    )
}

@Composable
fun AddItemDialog(onDismiss: () -> Unit, onConfirm: (String, ItemType, Double) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ItemType.SPEAKER) }
    var dailyRateStr by remember { mutableStateOf(getDefaultRate(selectedType).toString()) }
    var expanded by remember { mutableStateOf(false) }

    val isValidRate = dailyRateStr.toDoubleOrNull() != null && (dailyRateStr.toDoubleOrNull() ?: -1.0) >= 0

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { 
                    if(name.isNotBlank() && isValidRate) {
                        onConfirm(name, selectedType, dailyRateStr.toDoubleOrNull() ?: 0.0)
                    }
                }, 
                shape = RoundedCornerShape(8.dp),
                enabled = name.isNotBlank() && isValidRate
            ) {
                Text("Save Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Add New Equipment", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
                Box {
                    OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
                        Text(selectedType.displayName)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ItemType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) }, 
                                onClick = { 
                                    selectedType = type
                                    dailyRateStr = getDefaultRate(type).toString()
                                    expanded = false 
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = dailyRateStr,
                    onValueChange = { dailyRateStr = it },
                    label = { Text("Daily Rate (฿)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    isError = !isValidRate,
                    supportingText = {
                        if (!isValidRate) {
                            Text("Please enter a valid price", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Surface
    )
}

@Composable
fun CheckOutDialog(item: InventoryItem, jobs: List<Job>, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var selectedJobId by remember { mutableStateOf(jobs.firstOrNull()?.id ?: "") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Check Out: ${item.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Select destination job:", style = MaterialTheme.typography.bodyMedium, color = Secondary)
                Spacer(modifier = Modifier.height(12.dp))
                jobs.forEach { job ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { selectedJobId = job.id }.padding(vertical = 4.dp)
                    ) {
                        RadioButton(selected = (selectedJobId == job.id), onClick = { selectedJobId = job.id })
                        Text(job.name, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(selectedJobId) }, shape = RoundedCornerShape(8.dp)) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun CheckInDialog(item: InventoryItem, onDismiss: () -> Unit, onConfirm: (Boolean) -> Unit) {
    var isDamaged by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Check In: ${item.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text("Condition check:", style = MaterialTheme.typography.bodyMedium, color = Secondary)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { isDamaged = false }) {
                    RadioButton(selected = !isDamaged, onClick = { isDamaged = false })
                    Text("Perfect Condition", modifier = Modifier.padding(start = 8.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { isDamaged = true }) {
                    RadioButton(selected = isDamaged, onClick = { isDamaged = true })
                    Text("Damaged / Needs Repair", color = Error, modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        confirmButton = { Button(onClick = { onConfirm(isDamaged) }, shape = RoundedCornerShape(8.dp)) { Text("Complete") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        shape = RoundedCornerShape(24.dp)
    )
}
