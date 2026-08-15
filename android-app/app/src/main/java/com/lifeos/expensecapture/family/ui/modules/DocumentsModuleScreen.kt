package com.lifeos.expensecapture.family.ui.modules

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.data.DocumentStorageRepository
import com.lifeos.expensecapture.family.data.FamilyAuthRepository
import com.lifeos.expensecapture.family.data.FamilyResult
import com.lifeos.expensecapture.family.data.SharedDocumentRepository
import com.lifeos.expensecapture.family.model.SharedDocument
import com.lifeos.expensecapture.ui.common.cardSurfaceColor
import kotlinx.coroutines.launch

/** 20 MB - generous for a photo/PDF/scanned document, small enough that an upload over a slow
 * connection still finishes in a reasonable time instead of hanging indefinitely with no
 * progress indication or way to cancel (real gap found via review, 2026-08-15). */
private const val MAX_DOCUMENT_UPLOAD_BYTES = 20L * 1024 * 1024

/** Shared Documents module (2026-08 Family module) - a real file, not a placeholder record: the
 * system document picker + a real Firebase Storage upload (see DocumentStorageRepository), then a
 * Firestore record pointing at the resulting download URL. Follows TasksModuleScreen's pattern
 * otherwise. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsModuleScreen(familyId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val authRepository = remember { FamilyAuthRepository() }
    val repository = remember(familyId) { SharedDocumentRepository(familyId = familyId) }
    val storageRepository = remember { DocumentStorageRepository() }
    val currentUserId = authRepository.currentUser?.uid ?: ""
    val currentUserName = authRepository.currentUser?.displayName ?: ""
    val coroutineScope = rememberCoroutineScope()

    // remember()'d keyed on familyId (2026-08-15 fix) - see TasksModuleScreen.kt's identical fix
    // for why an inline observeX().collectAsState() recreates the Firestore listener on every
    // recomposition instead of reusing one.
    val documents by remember(familyId) { repository.observeAll() }.collectAsState(initial = emptyList())
    var uploading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        var fileName = "document"
        var fileSize = -1L
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: fileName
                if (sizeIndex >= 0) fileSize = cursor.getLong(sizeIndex)
            }
        }

        // Real gap found via review, 2026-08-15: uploads had no size cap at all - a large
        // video/PDF would stream indefinitely with no progress indication and no way to cancel.
        // -1 (size unknown, some content providers don't report it) is let through rather than
        // blocked, since rejecting a legitimate file just because its size couldn't be read would
        // be worse than the rare case of an oversized unknown-size file slipping through.
        if (fileSize > MAX_DOCUMENT_UPLOAD_BYTES) {
            error = "That file is too large to upload (max ${MAX_DOCUMENT_UPLOAD_BYTES / (1024 * 1024)} MB)"
            return@rememberLauncherForActivityResult
        }

        uploading = true
        coroutineScope.launch {
            when (val uploadResult = storageRepository.upload(context, familyId, uri, fileName)) {
                is FamilyResult.Success -> {
                    repository.add(
                        SharedDocument(
                            familyId = familyId,
                            title = fileName,
                            storageUrl = uploadResult.value,
                            uploadedByUserId = currentUserId,
                            uploadedAt = System.currentTimeMillis()
                        ),
                        currentUserName
                    )
                    error = null
                }
                is FamilyResult.Failure -> error = uploadResult.message
            }
            uploading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documents") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { pickerLauncher.launch("*/*") }) {
                if (uploading) CircularProgressIndicator(modifier = Modifier.padding(4.dp))
                else Icon(Icons.Filled.UploadFile, contentDescription = "Upload document")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            if (documents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("No shared documents yet. Tap upload to add one.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(documents, key = { it.id }) { document ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(document.title, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
