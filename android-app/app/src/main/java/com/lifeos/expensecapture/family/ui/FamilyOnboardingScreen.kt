package com.lifeos.expensecapture.family.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lifeos.expensecapture.ui.common.cardSurfaceColor

/**
 * Reached only once a member is signed in with no family yet (see FamilyAppViewModel's kdoc for
 * the three-state flow this sits in). Two independent, equally-first-class paths - "create a
 * family" (becomes Owner) or "join one" (via a code someone shared, typed here or arrived at
 * through a deep link that pre-fills it - deep-link handling itself is a nav-graph concern, not
 * this screen's).
 */
@Composable
fun FamilyOnboardingScreen(
    viewModel: FamilyAppViewModel,
    prefilledJoinCode: String? = null,
    onFamilyReady: (String) -> Unit
) {
    var familyName by remember { mutableStateOf("") }
    var joinCode by remember { mutableStateOf(prefilledJoinCode ?: "") }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Set up your family", style = MaterialTheme.typography.headlineSmall)
        Text(
            "A family is a shared space for tasks, calendar, expenses, documents, health records, " +
                "and emergency contacts - visible to the members you invite, with permissions you control.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Create a new family", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text("Family name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.createFamily(familyName.trim(), onFamilyReady) },
                    enabled = familyName.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Create family") }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text("  or  ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(modifier = Modifier.weight(1f))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = cardSurfaceColor())
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Join with an invite code", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = joinCode,
                    onValueChange = { joinCode = it.uppercase() },
                    label = { Text("Invite code") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.joinFamily(joinCode.trim(), onFamilyReady) },
                    enabled = joinCode.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Join family") }
            }
        }
    }
}
