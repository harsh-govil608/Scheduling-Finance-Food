package com.lifeos.expensecapture.util

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * "Pick from Contacts" for phone-number fields (real user request, 2026-08 - Family invites and
 * Smart Split participants both want this instead of hand-typing a number). READ_CONTACTS is
 * requested only the moment this is actually tapped, matching this app's existing "ask at point
 * of use" pattern (see PermissionScreen) - never at app launch, and this whole feature works
 * fine without it if the permission is denied, just falls back to manual typing.
 *
 * Returns a single `() -> Unit` to invoke from a button's onClick - the permission-then-picker
 * two-step is handled internally so every call site stays a one-liner.
 */
@Composable
fun rememberContactPhonePicker(onPicked: (name: String, phoneNumber: String) -> Unit): () -> Unit {
    val context = LocalContext.current

    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val contactUri = result.data?.data ?: return@rememberLauncherForActivityResult
        readPhoneFromContact(context, contactUri)?.let { (name, phone) -> onPicked(name, phone) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            pickContactLauncher.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
        }
    }

    return remember {
        {
            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                pickContactLauncher.launch(Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI))
            } else {
                permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }
}

private fun readPhoneFromContact(context: android.content.Context, contactUri: Uri): Pair<String, String>? {
    return context.contentResolver.query(contactUri, null, null, null, null)?.use { cursor ->
        if (!cursor.moveToFirst()) return null
        val nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        if (numberIndex < 0) return null
        val name = if (nameIndex >= 0) cursor.getString(nameIndex) ?: "" else ""
        val number = cursor.getString(numberIndex) ?: return null
        name to number
    }
}
