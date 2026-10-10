package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUpdate
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950

@Composable
fun UpdatesManagementScreen(
    updates: List<AppUpdate>,
    isLoading: Boolean,
    isDialogOpen: Boolean,
    editingId: String?,
    versionName: String,
    versionCode: String,
    minVersionCode: String,
    isForce: Boolean,
    title: String,
    changelog: String,
    downloadUrl: String,
    buttonText: String,
    isActive: Boolean,
    onOpenCreateDialog: () -> Unit,
    onOpenEditDialog: (AppUpdate) -> Unit,
    onCloseDialog: () -> Unit,
    onVersionNameChange: (String) -> Unit,
    onVersionCodeChange: (String) -> Unit,
    onMinVersionCodeChange: (String) -> Unit,
    onIsForceChange: (Boolean) -> Unit,
    onTitleChange: (String) -> Unit,
    onChangelogChange: (String) -> Unit,
    onDownloadUrlChange: (String) -> Unit,
    onButtonTextChange: (String) -> Unit,
    onIsActiveChange: (Boolean) -> Unit,
    onSaveUpdate: () -> Unit,
    onToggleStatus: (AppUpdate) -> Unit,
    onDeleteUpdate: (AppUpdate) -> Unit,
    onRefresh: () -> Unit,
    onShowToast: (String) -> Unit
) {
    Scaffold(
        containerColor = Slate900,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenCreateDialog,
                containerColor = ElectricBlue,
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Update")
                    Text(text = "নতুন আপডেট প্রকাশ", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Slate900)
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "অ্যাপ আপডেট ম্যানেজমেন্ট",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Supabase app_updates টেবিল সিঙ্ক ও ফোর্স আপডেট কন্ট্রোল",
                        style = MaterialTheme.typography.bodySmall.copy(color = Slate400)
                    )
                }

                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800)
                ) {
                    Icon(imageVector = Icons.Default.SystemUpdate, contentDescription = null, tint = NeonCyan)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "রিলোড", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && updates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonCyan)
                }
            } else if (updates.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.SystemUpdate,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Slate400
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "কোনো আপডেট রিলিজ পাওয়া যায়নি।",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Slate400)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onOpenCreateDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                        ) {
                            Text(text = "প্রথম আপডেট প্রকাশ করুন")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(updates) { update ->
                        AppUpdateCard(
                            update = update,
                            onEdit = { onOpenEditDialog(update) },
                            onToggle = { onToggleStatus(update) },
                            onDelete = { onDeleteUpdate(update) }
                        )
                    }
                }
            }
        }
    }

    // Publish / Edit Update Dialog Modal
    if (isDialogOpen) {
        AlertDialog(
            onDismissRequest = onCloseDialog,
            containerColor = Slate950,
            title = {
                Text(
                    text = if (editingId == null) "নতুন আপডেট প্রকাশ করুন" else "আপডেট রিলিজ সম্পাদনা",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = versionName,
                        onValueChange = onVersionNameChange,
                        label = { Text("ভার্সন নাম (e.g. v6.1.0)") },
                        singleLine = true,
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = versionCode,
                            onValueChange = onVersionCodeChange,
                            label = { Text("ভার্সন কোড (Int)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = minVersionCode,
                            onValueChange = onMinVersionCodeChange,
                            label = { Text("মিনিমাম কোড") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = { Text("নোটিফিকেশন টাইটেল") },
                        singleLine = true,
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = changelog,
                        onValueChange = onChangelogChange,
                        label = { Text("চেঞ্জলগ (বাংলা বুলেট পয়েন্ট)") },
                        minLines = 3,
                        maxLines = 6,
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = downloadUrl,
                        onValueChange = onDownloadUrlChange,
                        label = { Text("ডাউনলোড URL (Telegram / APK link)") },
                        singleLine = true,
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = buttonText,
                        onValueChange = onButtonTextChange,
                        label = { Text("বাটন টেক্সট (e.g. এখনই আপডেট করুন)") },
                        singleLine = true,
                        colors = textFieldColors(),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "ফোর্স আপডেট (Force Update)", color = Color.White)
                        Checkbox(
                            checked = isForce,
                            onCheckedChange = onIsForceChange
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "স্ট্যাটাস সক্রিয় (Is Active)", color = Color.White)
                        Switch(
                            checked = isActive,
                            onCheckedChange = onIsActiveChange,
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonCyan)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onSaveUpdate,
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text(text = "সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onCloseDialog) {
                    Text(text = "বাতিল", color = Slate400)
                }
            }
        )
    }
}

@Composable
fun AppUpdateCard(
    update: AppUpdate,
    onEdit: () -> Unit,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = if (update.isActive) NeonCyan.copy(alpha = 0.5f) else Slate800,
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = Slate800),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (update.isActive) NeonCyan.copy(alpha = 0.2f) else Slate900)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = update.latestVersionName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (update.isActive) NeonCyan else Slate400
                            )
                        )
                    }

                    if (update.isForceUpdate) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color.Red.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "FORCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                            )
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = NeonCyan)
                    }
                    IconButton(onClick = onToggle) {
                        Icon(
                            imageVector = if (update.isActive) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = "Toggle Active",
                            tint = if (update.isActive) Color.Green else Slate400
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = update.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = update.changelog,
                style = MaterialTheme.typography.bodyMedium.copy(color = Slate400)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "কোড: ${update.latestVersionCode} | মিনিমাম: ${update.minSupportedVersionCode}",
                    style = MaterialTheme.typography.labelSmall.copy(color = Slate400)
                )
                Text(
                    text = update.buttonText,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                )
            }
        }
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NeonCyan,
    unfocusedBorderColor = Slate400,
    focusedLabelColor = NeonCyan,
    unfocusedLabelColor = Slate400,
    cursorColor = NeonCyan,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
