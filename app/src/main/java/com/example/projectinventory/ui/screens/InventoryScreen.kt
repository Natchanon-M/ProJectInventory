package com.example.projectinventory.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    
    var selectedItemForCheckOut by remember { mutableStateOf<InventoryItem?>(null) }
    var selectedItemForCheckIn by remember { mutableStateOf<InventoryItem?>(null) }
    var selectedItemForRepair by remember { mutableStateOf<InventoryItem?>(null) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showAddJobDialog by remember { mutableStateOf(false) }
    var jobToEdit by remember { mutableStateOf<Job?>(null) }
    var jobToDelete by remember { mutableStateOf<Job?>(null) }
    var selectedItemForQRCode by remember { mutableStateOf<InventoryItem?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }
    
    // New State for Drill-down
    var selectedJobForDetails by remember { mutableStateOf<Job?>(null) }

    Scaffold(
        containerColor = Background,
        topBar = {
            Column(modifier = Modifier.background(Surface)) {
                CenterAlignedTopAppBar(
                    title = { 
                        Text(
                            if (selectedJobForDetails != null) selectedJobForDetails!!.name else "INVENTORY", 
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        ) 
                    },
                    navigationIcon = {
                        if (selectedJobForDetails != null) {
                            IconButton(onClick = { selectedJobForDetails = null }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Surface)
                )
                
                if (selectedJobForDetails == null) {
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
                            badgeCount = items.count { it.status == ItemStatus.BUSY },
                            modifier = Modifier.weight(1f),
                            onClick = { selectedTab = 1 }
                        )
                        TabItem(
                            title = "Repair", 
                            isSelected = selectedTab == 2, 
                            badgeCount = items.count { it.status == ItemStatus.REPAIR_PENDING || it.status == ItemStatus.REPAIRING },
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
                        if (selectedTab == 0) showAddItemDialog = true 
                        else showAddJobDialog = true 
                    },
                    containerColor = Primary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp)
                ) {
                    Icon(if (selectedTab == 0) Icons.Default.Add else Icons.Default.PostAdd, contentDescription = "Add")
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
                targetState = Pair(selectedTab, selectedJobForDetails),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ContentTransition"
            ) { (tab, jobDetails) ->
                Column {
                    if (tab == 0 && jobDetails == null) {
                        SearchBar(searchQuery) { searchQuery = it }
                        InventoryList(
                            items = if (searchQuery.isBlank()) items 
                                    else items.filter { it.name.contains(searchQuery, ignoreCase = true) || it.serial.contains(searchQuery, ignoreCase = true) },
                            jobs = jobs,
                            onQRCodeClick = { selectedItemForQRCode = it },
                            onCheckOut = { selectedItemForCheckOut = it },
                            onCheckIn = { selectedItemForCheckIn = it },
                            onSendToRepair = { selectedItemForRepair = it },
                            onReturnFromRepair = { viewModel.returnFromRepair(it.id) },
                            onUpdateRepairStatus = { itemId, status -> viewModel.updateRepairStatus(itemId, status) }
                        )
                    } else if (tab == 1 && jobDetails == null) {
                        JobList(
                            jobs = jobs,
                            items = items,
                            onJobClick = { selectedJobForDetails = it }
                        )
                    } else if (tab == 2 && jobDetails == null) {
                        InventoryList(
                            items = items.filter { it.status == ItemStatus.REPAIR_PENDING || it.status == ItemStatus.REPAIRING },
                            jobs = jobs,
                            onQRCodeClick = { selectedItemForQRCode = it },
                            onCheckOut = { selectedItemForCheckOut = it },
                            onCheckIn = { selectedItemForCheckIn = it },
                            onSendToRepair = { selectedItemForRepair = it },
                            onReturnFromRepair = { viewModel.returnFromRepair(it.id) },
                            onUpdateRepairStatus = { itemId, status -> viewModel.updateRepairStatus(itemId, status) }
                        )
                    } else if (jobDetails != null) {
                        // Show Job Details and Items
                        JobDetailHeader(
                            job = jobDetails,
                            onEdit = { jobToEdit = jobDetails },
                            onDelete = { jobToDelete = jobDetails }
                        )
                        val jobItems = items.filter { it.currentJobId == jobDetails.id }
                        InventoryList(
                            items = jobItems,
                            jobs = jobs,
                            showJobLabel = false,
                            onQRCodeClick = { selectedItemForQRCode = it },
                            onCheckOut = { selectedItemForCheckOut = it },
                            onCheckIn = { selectedItemForCheckIn = it },
                            onSendToRepair = { selectedItemForRepair = it },
                            onReturnFromRepair = { viewModel.returnFromRepair(it.id) },
                            onUpdateRepairStatus = { itemId, status -> viewModel.updateRepairStatus(itemId, status) }
                        )
                    }
                }
            }
        }

        // Dialogs
        if (showAddItemDialog) {
            AddItemDialog(
                onDismiss = { showAddItemDialog = false },
                onConfirm = { name, type ->
                    viewModel.addItem(name, type)
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
fun JobList(jobs: List<Job>, items: List<InventoryItem>, onJobClick: (Job) -> Unit) {
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
        items(jobs) { job ->
            val itemCount = items.count { it.currentJobId == job.id }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onJobClick(job) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(job.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(if (itemCount > 0) "$itemCount items on this job" else job.date, style = MaterialTheme.typography.bodySmall, color = Secondary)
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
    onQRCodeClick: (InventoryItem) -> Unit,
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
        items(items) { item ->
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
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onQRCodeClick(item) }
                    ) {
                        Text(item.serial, style = MaterialTheme.typography.bodySmall, color = Secondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.QrCode, contentDescription = "Show QR", modifier = Modifier.size(14.dp), tint = Secondary)
                    }
                }
                StatusDot(item.status)
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

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                when (item.status) {
                    ItemStatus.AVAILABLE -> {
                        TextButton(
                            onClick = { onSendToRepair(item) },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Repair", color = Error)
                        }
                        Button(
                            onClick = { onCheckOut(item) },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Check Out", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    ItemStatus.BUSY -> {
                        OutlinedButton(
                            onClick = { onCheckIn(item) },
                            shape = RoundedCornerShape(8.dp),
                            border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp)
                        ) {
                            Text("Check In", color = Primary)
                        }
                    }
                    ItemStatus.REPAIR_PENDING -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.status.displayName, color = Error, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onUpdateRepairStatus(item.id, ItemStatus.REPAIRING) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Warning)
                            ) {
                                Text("เริ่มซ่อม", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                    ItemStatus.REPAIRING -> {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.status.displayName, color = Warning, style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onReturnFromRepair(item) },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Success)
                            ) {
                                Text("พร้อมใช้", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun JobDetailHeader(job: Job, onEdit: () -> Unit, onDelete: () -> Unit) {
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
            DetailRow(Icons.Default.Event, "Date: ${job.date.ifBlank { "-" }}")
            DetailRow(Icons.Default.Schedule, "Team Time: ${job.teamTime.ifBlank { "-" }}")
            
            if (job.notes.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Background)
                Text("Notes:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(job.notes, style = MaterialTheme.typography.bodySmall, color = Secondary)
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Secondary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun StatusDot(status: ItemStatus) {
    val color = when(status) {
        ItemStatus.AVAILABLE -> Success
        ItemStatus.REPAIR_PENDING -> Error
        ItemStatus.REPAIRING -> Warning
        ItemStatus.BUSY -> Warning
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(6.dp))
        Text(status.displayName, style = MaterialTheme.typography.labelSmall, color = Secondary)
    }
}


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
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = teamTime,
                            onValueChange = { teamTime = it },
                            label = { Text("Team Time") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
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
fun AddItemDialog(onDismiss: () -> Unit, onConfirm: (String, ItemType) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(ItemType.SPEAKER) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { if(name.isNotBlank()) onConfirm(name, selectedType) }, shape = RoundedCornerShape(8.dp)) {
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
                            DropdownMenuItem(text = { Text(type.displayName) }, onClick = { selectedType = type; expanded = false })
                        }
                    }
                }
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
