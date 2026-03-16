package com.fitlife.app.features.profile.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fitlife.app.core.ui.theme.FitGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onNavigateBack: () -> Unit) {
    var expandedFaq by remember { mutableStateOf<Int?>(null) }

    val faqs = listOf(
        "Cum adaug un antrenament?" to "Mergi la secțiunea Antrenamente din bara de navigare de jos și apasă butonul + pentru a începe un antrenament nou.",
        "Cum îmi urmăresc caloriile?" to "Accesează secțiunea Nutriție și adaugă alimentele consumate la fiecare masă. Aplicația calculează automat totalul caloric.",
        "Cum actualizez greutatea?" to "Mergi la Progres și apasă butonul pentru a adăuga o nouă măsurătoare. Poți urmări evoluția în grafic.",
        "Cum schimb obiectivele zilnice?" to "Accesează Profil > Editează profil și modifică valorile pentru calorii, pași, apă și antrenamente.",
        "Aplicația funcționează offline?" to "Da, datele sunt salvate local. Sincronizarea se face automat când te conectezi la internet."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajutor & Suport", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Înapoi")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Contact support
            Text("Contactează-ne", style = MaterialTheme.typography.labelLarge, color = FitGreen, fontWeight = FontWeight.Bold)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column {
                    HelpContactItem(Icons.Default.Email, "Email suport", "support@fitlife.app")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    HelpContactItem(Icons.Default.Chat, "Chat live", "Disponibil 9:00 - 18:00")
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    HelpContactItem(Icons.Default.Forum, "Comunitate", "Forum utilizatori FitLife")
                }
            }

            // FAQ
            Text("Întrebări frecvente", style = MaterialTheme.typography.labelLarge, color = FitGreen, fontWeight = FontWeight.Bold)
            faqs.forEachIndexed { index, (question, answer) ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedFaq = if (expandedFaq == index) null else index }
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Help, null, tint = FitGreen, modifier = Modifier.size(20.dp))
                            Text(question, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                            Icon(
                                if (expandedFaq == index) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        if (expandedFaq == index) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                answer,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HelpContactItem(icon: ImageVector, title: String, subtitle: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = FitGreen, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}
