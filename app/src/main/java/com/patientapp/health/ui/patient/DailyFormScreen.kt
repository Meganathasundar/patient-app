package com.patientapp.health.ui.patient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val vasColors = listOf(
    Color(0xFF4CAF50), // 0 - green
    Color(0xFF66BB6A), // 1 - light green
    Color(0xFF8BC34A), // 2 - lime
    Color(0xFFCDDC39), // 3 - yellow-green
    Color(0xFFFFEB3B), // 4 - yellow
    Color(0xFFFFC107), // 5 - amber
    Color(0xFFFF9800), // 6 - orange
    Color(0xFFFF7043), // 7 - deep orange
    Color(0xFFEF5350), // 8 - red
    Color(0xFFE53935), // 9 - dark red
    Color(0xFFC62828), // 10 - darkest red
)

private val vasLabels = listOf(
    "No pain",
    "",
    "Mild",
    "",
    "Moderate\ndiscomfort",
    "",
    "Severe\npain",
    "",
    "Very severe\npain",
    "",
    "Most pain\npossible"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyFormScreen(
    uiState: PatientUiState,
    onSubmit: (
        temperature: Double,
        symptoms: String,
        painLevel: Int,
        hasOtherSymptoms: Boolean,
        otherSymptomsDescription: String,
        tookMedicine: Boolean,
        medicineDescription: String
    ) -> Unit,
    onClearSubmitState: () -> Unit,
    onSignOut: () -> Unit
) {
    var showSurvey by remember { mutableStateOf(false) }
    var painLevel by remember { mutableIntStateOf(-1) }
    var hasOtherSymptoms by remember { mutableStateOf<Boolean?>(null) }
    var otherSymptomsText by remember { mutableStateOf("") }
    var tookMedicine by remember { mutableStateOf<Boolean?>(null) }
    var medicineText by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Patient Dashboard") },
                actions = {
                    TextButton(onClick = onSignOut) {
                        Text("Sign out")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = listState
        ) {
            if (uiState.submitSuccess) {
                item(key = "success") {
                    Card(
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Survey submitted successfully.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                item(key = "dismiss") {
                    TextButton(onClick = {
                        onClearSubmitState()
                        showSurvey = false
                        painLevel = -1
                        hasOtherSymptoms = null
                        otherSymptomsText = ""
                        tookMedicine = null
                        medicineText = ""
                        validationError = null
                    }) {
                        Text("Dismiss")
                    }
                }
            }
            if (uiState.submitError != null) {
                item(key = "error") {
                    Card(
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = uiState.submitError,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            if (!showSurvey && !uiState.submitSuccess) {
                item(key = "landing") {
                    Spacer(modifier = Modifier.height(80.dp))
                }
                item(key = "landing_content") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Daily Health Survey",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Submit your daily health information\nto keep your doctor informed.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { showSurvey = true },
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Submit Survey",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
            }

            if (showSurvey && !uiState.submitSuccess) {
                item(key = "survey_title") {
                    Text(
                        "Daily Health Survey",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                item(key = "vas_header") {
                    Column {
                        Text(
                            text = "Visual Analog Scale",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select the face that describes how you are feeling",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                for (level in 10 downTo 0) {
                    item(key = "vas_$level") {
                        VasRow(
                            level = level,
                            isSelected = level == painLevel,
                            enabled = !uiState.isSubmitting,
                            onSelect = {
                                painLevel = level
                                validationError = null
                            }
                        )
                    }
                }

                item(key = "other_symptoms") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        YesNoRadioSection(
                            title = "Other symptoms",
                            selected = hasOtherSymptoms,
                            onSelectionChange = { hasOtherSymptoms = it },
                            enabled = !uiState.isSubmitting
                        )
                        AnimatedVisibility(
                            visible = hasOtherSymptoms == true,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            OutlinedTextField(
                                value = otherSymptomsText,
                                onValueChange = { otherSymptomsText = it },
                                label = { Text("Describe other symptoms") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isSubmitting
                            )
                        }
                    }
                }

                item(key = "medicine") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        YesNoRadioSection(
                            title = "Took medicine for pain",
                            selected = tookMedicine,
                            onSelectionChange = { tookMedicine = it },
                            enabled = !uiState.isSubmitting
                        )
                        AnimatedVisibility(
                            visible = tookMedicine == true,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            OutlinedTextField(
                                value = medicineText,
                                onValueChange = { medicineText = it },
                                label = { Text("Medicine name / details") },
                                minLines = 2,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !uiState.isSubmitting
                            )
                        }
                    }
                }

                if (validationError != null) {
                    item(key = "validation_error") {
                        Card(
                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = validationError.orEmpty(),
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                item(key = "buttons") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = {
                                showSurvey = false
                                validationError = null
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isSubmitting
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (painLevel < 0) {
                                    validationError = "Please select a pain level"
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(
                                            listState.layoutInfo.totalItemsCount - 1
                                        )
                                    }
                                } else {
                                    validationError = null
                                    onSubmit(
                                        0.0,
                                        "",
                                        painLevel,
                                        hasOtherSymptoms == true,
                                        if (hasOtherSymptoms == true) otherSymptomsText else "",
                                        tookMedicine == true,
                                        if (tookMedicine == true) medicineText else ""
                                    )
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !uiState.isSubmitting
                        ) {
                            Text(if (uiState.isSubmitting) "Submitting\u2026" else "Submit")
                        }
                    }
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun VasRow(
    level: Int,
    isSelected: Boolean,
    enabled: Boolean,
    onSelect: () -> Unit
) {
    val bgColor = if (isSelected) vasColors[level].copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (isSelected) vasColors[level] else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(
                width = if (isSelected) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled, onClick = onSelect)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = level.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = vasColors[level],
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            FaceIcon(level = level, color = vasColors[level])
        }

        Spacer(modifier = Modifier.width(12.dp))

        val label = vasLabels[level]
        if (label.isNotEmpty()) {
            Text(
                text = label.replace("\n", " "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(vasColors[level], CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "\u2713",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun FaceIcon(level: Int, color: Color) {
    Canvas(modifier = Modifier.size(36.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 - 2

        drawCircle(color = color, radius = radius, center = center)
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = radius,
            center = center
        )
        drawCircle(
            color = color,
            radius = radius,
            center = center,
            style = Stroke(width = 2f)
        )

        val eyeColor = Color(0xFF333333)
        val eyeY = center.y - radius * 0.2f
        val eyeLeftX = center.x - radius * 0.3f
        val eyeRightX = center.x + radius * 0.3f
        val eyeRadius = radius * 0.08f

        drawCircle(color = eyeColor, radius = eyeRadius, center = Offset(eyeLeftX, eyeY))
        drawCircle(color = eyeColor, radius = eyeRadius, center = Offset(eyeRightX, eyeY))

        drawMouth(level, center, radius, eyeColor)
    }
}

private fun DrawScope.drawMouth(level: Int, center: Offset, radius: Float, color: Color) {
    val mouthY = center.y + radius * 0.35f
    val mouthWidth = radius * 0.5f
    val strokeWidth = 2.5f

    when {
        level <= 1 -> {
            // Big smile arc
            drawArc(
                color = color,
                startAngle = 0f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - mouthWidth, mouthY - mouthWidth * 0.5f),
                size = Size(mouthWidth * 2, mouthWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        level <= 3 -> {
            // Slight smile
            drawArc(
                color = color,
                startAngle = 10f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(center.x - mouthWidth, mouthY - mouthWidth * 0.2f),
                size = Size(mouthWidth * 2, mouthWidth * 0.6f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        level <= 5 -> {
            // Flat line
            drawLine(
                color = color,
                start = Offset(center.x - mouthWidth, mouthY),
                end = Offset(center.x + mouthWidth, mouthY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
        level <= 7 -> {
            // Slight frown
            drawArc(
                color = color,
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(center.x - mouthWidth, mouthY - mouthWidth * 0.1f),
                size = Size(mouthWidth * 2, mouthWidth * 0.6f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
        else -> {
            // Big frown
            drawArc(
                color = color,
                startAngle = 190f,
                sweepAngle = 160f,
                useCenter = false,
                topLeft = Offset(center.x - mouthWidth, mouthY),
                size = Size(mouthWidth * 2, mouthWidth),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Tear drop for level 10
            if (level >= 10) {
                val tearX = center.x + radius * 0.45f
                val tearY = center.y - radius * 0.05f
                drawTearDrop(tearX, tearY, radius * 0.12f, Color(0xFF42A5F5))
            }
        }
    }
}

private fun DrawScope.drawTearDrop(x: Float, y: Float, dropRadius: Float, color: Color) {
    drawCircle(color = color, radius = dropRadius, center = Offset(x, y + dropRadius))
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(x, y - dropRadius * 0.5f)
        lineTo(x - dropRadius, y + dropRadius)
        lineTo(x + dropRadius, y + dropRadius)
        close()
    }
    drawPath(path, color)
}

// -- Yes/No Radio Section --

@Composable
private fun YesNoRadioSection(
    title: String,
    selected: Boolean?,
    onSelectionChange: (Boolean) -> Unit,
    enabled: Boolean
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = enabled) { onSelectionChange(true) }
                    .padding(end = 4.dp)
            ) {
                RadioButton(
                    selected = selected == true,
                    onClick = { onSelectionChange(true) },
                    enabled = enabled,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text("Yes", style = MaterialTheme.typography.bodyLarge)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(enabled = enabled) { onSelectionChange(false) }
                    .padding(end = 4.dp)
            ) {
                RadioButton(
                    selected = selected == false,
                    onClick = { onSelectionChange(false) },
                    enabled = enabled,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary
                    )
                )
                Text("No", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
