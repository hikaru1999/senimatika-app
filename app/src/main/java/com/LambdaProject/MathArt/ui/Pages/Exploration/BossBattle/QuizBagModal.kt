package com.LambdaProject.MathArt.ui.Pages.Exploration.BossBattle

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.LambdaProject.MathArt.R
import com.LambdaProject.MathArt.data.Inventory
import com.LambdaProject.MathArt.data.PowerUpType

@Composable
fun QuizBagModal(
    inventory: Inventory,
    onClose: () -> Unit,
    onUsePowerUp: (PowerUpType) -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        title = { Text("Gunakan Bantuan") },
        text = {
            Column {
                if (inventory.powerUps.isEmpty()) {
                    Text("Kamu tidak punya power-up.")
                } else {
                    inventory.powerUps.distinct().forEach { pu ->
                        val count = inventory.powerUps.count { it == pu }
                        val (title, icon, desc) = when(pu) {
                            PowerUpType.FREEZE_TIMER -> Triple("Chrono Freeze", R.drawable.ic_pu_freeze, "Freeze timer 5 detik")
                            PowerUpType.REMOVE_TWO_OPTIONS -> Triple("Truth Filter", R.drawable.ic_pu_magic, "Hapus 2 opsi salah")
                            PowerUpType.STREAK_PROTECTION -> Triple("Battle Shield", R.drawable.ic_pu_shield, "Proteksi streak")
                        }
                        ListItem(
                            headlineContent = { Text(title) },
                            supportingContent = { Text(desc) },
                            trailingContent = { Text("x$count", fontWeight = FontWeight.Bold) },
                            leadingContent = { Image(painterResource(icon), null, modifier = Modifier.size(32.dp)) },
                            modifier = Modifier.clickable { onUsePowerUp(pu) }
                        )
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onClose) { Text("Batal") } }
    )
}