package com.lifeos.expensecapture.family.data

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

/**
 * Real file upload for the shared Documents module (2026-08 Family module) - Firebase Storage,
 * not a fake/local-only "document" record with no actual file behind it, per this module's
 * "avoid mock data" requirement. Files land under families/{familyId}/documents/{uuid}-{name} so
 * two members uploading a file with the same name never collide.
 */
class DocumentStorageRepository(
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    suspend fun upload(context: Context, familyId: String, fileUri: Uri, fileName: String): FamilyResult<String> {
        return try {
            val path = "families/$familyId/documents/${java.util.UUID.randomUUID()}-$fileName"
            val ref = storage.reference.child(path)
            context.contentResolver.openInputStream(fileUri)?.use { input ->
                ref.putStream(input).await()
            } ?: return FamilyResult.Failure("Couldn't read the selected file")
            val downloadUrl = ref.downloadUrl.await()
            FamilyResult.Success(downloadUrl.toString())
        } catch (e: Exception) {
            FamilyResult.Failure(e.message ?: "Upload failed")
        }
    }
}
