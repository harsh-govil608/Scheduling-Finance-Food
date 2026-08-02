package com.lifeos.expensecapture.family.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.family.model.FamilyMember
import com.lifeos.expensecapture.family.model.FamilyRole
import com.lifeos.expensecapture.family.model.PermissionType

/**
 * The single choke point every shared module and the dashboard route sensitive content through
 * (2026-08 Family module PRD: "Permission system controlling visibility of location, documents,
 * health data, and expenses"). Two independent checks, both must pass:
 *  1. The viewer's own role - OWNER/PARENT can always see everything (family-management roles),
 *     matching FamilyRole's kdoc on why that split exists.
 *  2. The target member's own PermissionSet for that data type - a member's location/documents/
 *     health/expenses stay hidden from everyone (including Parents) if they've turned that
 *     specific visibility off for themselves.
 * A member can always see their own data regardless of either check - permissions restrict what
 * OTHERS see, never what you see about yourself.
 */
fun hasPermission(
    viewer: FamilyMember,
    target: FamilyMember,
    type: PermissionType
): Boolean {
    if (viewer.userId == target.userId) return true
    if (viewer.role == FamilyRole.OWNER || viewer.role == FamilyRole.PARENT) {
        return target.permissions.isVisible(type)
    }
    return target.permissions.isVisible(type)
}

/** Wraps [content] and only renders it when [hasPermission] passes; otherwise shows a small
 * "hidden" indicator instead of silently rendering nothing, so a Parent/Guest can tell the data
 * exists but isn't visible to them rather than assuming the member simply has none. */
@Composable
fun PermissionGate(
    viewer: FamilyMember,
    target: FamilyMember,
    type: PermissionType,
    content: @Composable () -> Unit
) {
    if (hasPermission(viewer, target, type)) {
        content()
    } else {
        HiddenByPermissionRow()
    }
}

@Composable
private fun HiddenByPermissionRow() {
    Row {
        Icon(
            Icons.Filled.Lock,
            contentDescription = null,
            modifier = Modifier.width(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "Hidden by this member's privacy settings",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
