package com.example.kidsenglishlab.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.kidsenglishlab.data.UserProfile

@Composable
fun FirstLaunchWelcomeDialog(
    isOpen: Boolean,
    onSaveProfile: (name: String, avatar: String) -> Unit
) {
    if (!isOpen) return

    var playerName by remember { mutableStateOf("") }
    var selectedAvatar by remember { mutableStateOf(UserProfile.AVATAR_OPTIONS.first()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = { /* Force child to enter name on first launch */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false, usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth(0.92f)
                .padding(16.dp)
                .testTag("first_launch_welcome_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header badge
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7))
                        .border(3.dp, Color(0xFFFDE68A), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = selectedAvatar, fontSize = 42.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "👋 Hoş Geldin!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF1E293B)
                )

                Text(
                    text = "İngilizce öğrenme macerasına başlamadan önce adını yaz ve karakterini seç:",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Name Input with explicit high-contrast text color
                OutlinedTextField(
                    value = playerName,
                    onValueChange = {
                        if (it.length <= 15) {
                            playerName = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Oyuncu Adı (Örn: Elif, Can)", color = Color(0xFF64748B)) },
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color(0xFF0F172A),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF0F172A),
                        unfocusedTextColor = Color(0xFF0F172A),
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC),
                        cursorColor = Color(0xFF2563EB),
                        focusedBorderColor = Color(0xFF3B82F6),
                        unfocusedBorderColor = Color(0xFFCBD5E1),
                        focusedLabelColor = Color(0xFF2563EB),
                        unfocusedLabelColor = Color(0xFF64748B)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_name_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Avatar Selection
                Text(
                    text = "Favori Karakterini Seç:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155),
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(UserProfile.AVATAR_OPTIONS) { av ->
                        val isSelected = av == selectedAvatar
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color(0xFFDBEAFE) else Color(0xFFF8FAFC))
                                .border(
                                    if (isSelected) 2.5.dp else 1.5.dp,
                                    if (isSelected) Color(0xFF3B82F6) else Color(0xFFE2E8F0),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { selectedAvatar = av },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = av, fontSize = 26.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 3D Start Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(4.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF1D4ED8))
                        .clickable {
                            val finalName = playerName.trim()
                            if (finalName.isEmpty()) {
                                errorMessage = "Lütfen bir oyuncu adı yazın!"
                            } else {
                                onSaveProfile(finalName, selectedAvatar)
                            }
                        }
                        .testTag("start_adventure_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF3B82F6), Color(0xFF2563EB))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Maceraya Başla! 🚀",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsDialog(
    isOpen: Boolean,
    profiles: List<UserProfile>,
    activeProfileId: String,
    onSelectProfile: (String) -> Unit,
    onAddProfile: (name: String, avatar: String) -> Unit,
    onUpdateProfile: (id: String, name: String, avatar: String) -> Unit,
    onDeleteProfile: (id: String) -> Unit,
    onResetProgress: (profileId: String) -> Unit,
    onOpenThemes: () -> Unit,
    onOpenVoices: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val activeProfile = profiles.find { it.id == activeProfileId } ?: profiles.firstOrNull() ?: UserProfile.defaultProfile()
    var isEditingActive by remember { mutableStateOf(false) }
    var isAddingNew by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    // Edit form states
    var editName by remember(activeProfile) { mutableStateOf(activeProfile.name) }
    var editAvatar by remember(activeProfile) { mutableStateOf(activeProfile.avatar) }

    // New profile form states
    var newName by remember { mutableStateOf("") }
    var newAvatar by remember { mutableStateOf("🚀") }
    var addError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 500.dp)
                .fillMaxWidth(0.94f)
                .padding(12.dp)
                .testTag("settings_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEFF6FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ayarlar & Profiller",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1E293B)
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Kapat", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 1. ACTIVE PROFILE SECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFDBEAFE))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "👤 AKTİF OYUNCU",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF2563EB)
                            )
                            IconButton(
                                onClick = {
                                    isEditingActive = !isEditingActive
                                    editName = activeProfile.name
                                    editAvatar = activeProfile.avatar
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Düzenle", tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
                            }
                        }

                        if (!isEditingActive) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEF3C7))
                                        .border(2.dp, Color(0xFFFDE68A), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = activeProfile.avatar, fontSize = 28.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = activeProfile.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${activeProfile.completedWords.size} / 100 Kelime Tamamlandı",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }
                            }
                        } else {
                            // Edit Form Inline
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = { if (it.length <= 15) editName = it },
                                    label = { Text("Oyuncu Adı", color = Color(0xFF64748B)) },
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = Color(0xFF0F172A),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedTextColor = Color(0xFF0F172A),
                                        unfocusedTextColor = Color(0xFF0F172A),
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White,
                                        cursorColor = Color(0xFF2563EB),
                                        focusedBorderColor = Color(0xFF3B82F6),
                                        unfocusedBorderColor = Color(0xFFCBD5E1),
                                        focusedLabelColor = Color(0xFF2563EB),
                                        unfocusedLabelColor = Color(0xFF64748B)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(UserProfile.AVATAR_OPTIONS) { av ->
                                        val isSel = av == editAvatar
                                        Box(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSel) Color(0xFFDBEAFE) else Color.White)
                                                .border(
                                                    if (isSel) 2.dp else 1.dp,
                                                    if (isSel) Color(0xFF3B82F6) else Color(0xFFCBD5E1),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable { editAvatar = av },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = av, fontSize = 22.sp)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { isEditingActive = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        Text("İptal", color = Color(0xFF475569), fontSize = 12.sp)
                                    }
                                    Button(
                                        onClick = {
                                            if (editName.isNotBlank()) {
                                                onUpdateProfile(activeProfile.id, editName.trim(), editAvatar)
                                                isEditingActive = false
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Kaydet", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. MULTI-PROFILE SELECTION & MANAGEMENT (UP TO 3)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "👥 PROFİLLER (${profiles.size}/3)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF475569)
                    )

                    if (profiles.size < 3 && !isAddingNew) {
                        Surface(
                            onClick = {
                                isAddingNew = true
                                newName = ""
                                newAvatar = "🚀"
                                addError = null
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF86EFAC))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Profil Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Profiles List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    profiles.forEach { profile ->
                        val isCurrent = profile.id == activeProfileId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable {
                                    if (!isCurrent) {
                                        onSelectProfile(profile.id)
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isCurrent) 2.dp else 1.dp,
                                if (isCurrent) Color(0xFF3B82F6) else Color(0xFFE2E8F0)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(if (isCurrent) Color(0xFFDBEAFE) else Color(0xFFE2E8F0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = profile.avatar, fontSize = 22.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = profile.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = Color(0xFF1E293B)
                                            )
                                            if (isCurrent) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(Color(0xFF2563EB))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text("Aktif", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                        Text(
                                            text = "⭐ ${profile.completedWords.size} Kelime",
                                            fontSize = 12.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }
                                }

                                if (profiles.size > 1 && !isCurrent) {
                                    IconButton(
                                        onClick = { onDeleteProfile(profile.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Profili Sil", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Add Profile Inline Card
                AnimatedVisibility(visible = isAddingNew) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF86EFAC))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "➕ Yeni Profil Ekle",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = newName,
                                onValueChange = {
                                    if (it.length <= 15) {
                                        newName = it
                                        addError = null
                                    }
                                },
                                label = { Text("Yeni Oyuncu Adı", color = Color(0xFF64748B)) },
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color(0xFF0F172A),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF0F172A),
                                    unfocusedTextColor = Color(0xFF0F172A),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    cursorColor = Color(0xFF16A34A),
                                    focusedBorderColor = Color(0xFF16A34A),
                                    unfocusedBorderColor = Color(0xFF86EFAC),
                                    focusedLabelColor = Color(0xFF16A34A),
                                    unfocusedLabelColor = Color(0xFF64748B)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (addError != null) {
                                Text(
                                    text = addError!!,
                                    color = Color(0xFFDC2626),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(UserProfile.AVATAR_OPTIONS) { av ->
                                    val isSel = av == newAvatar
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (isSel) Color(0xFFDCFCE7) else Color.White)
                                            .border(
                                                if (isSel) 2.dp else 1.dp,
                                                if (isSel) Color(0xFF16A34A) else Color(0xFFCBD5E1),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable { newAvatar = av },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(text = av, fontSize = 20.sp)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { isAddingNew = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Vazgeç", color = Color(0xFF475569), fontSize = 12.sp)
                                }
                                Button(
                                    onClick = {
                                        if (newName.trim().isEmpty()) {
                                            addError = "Lütfen bir isim yazın"
                                        } else {
                                            onAddProfile(newName.trim(), newAvatar)
                                            isAddingNew = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Ekle", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 3. RESET PROGRESS SECTION
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFFECDD3))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "🔄 İlerlemeyi Sıfırla",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE11D48)
                                )
                                Text(
                                    text = "Yıldızları ve başarıları temizle",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9F1239)
                                )
                            }

                            Button(
                                onClick = { showResetConfirm = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Sıfırla", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (showResetConfirm) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDA4AF))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "${activeProfile.name} için tamamlanan tüm kelimeler sıfırlansın mı?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Button(
                                            onClick = { showResetConfirm = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0)),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.padding(end = 6.dp)
                                        ) {
                                            Text("Vazgeç", color = Color(0xFF475569), fontSize = 11.sp)
                                        }
                                        Button(
                                            onClick = {
                                                onResetProgress(activeProfile.id)
                                                showResetConfirm = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Text("Evet, Sıfırla", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Short-cuts for Voice & Themes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        onClick = {
                            onDismiss()
                            onOpenThemes()
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFAF5FF),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE9D5FF)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = Color(0xFFA855F7), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Tema Değiştir", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7E22CE))
                        }
                    }

                    Surface(
                        onClick = {
                            onDismiss()
                            onOpenVoices()
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF0FDF4),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ses Seçimi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                        }
                    }
                }
            }
        }
    }
}
