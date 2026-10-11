package it.bollofacile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format.DateTimeFormatter

@Composable
fun DatePickerField(
    modifier: Modifier = Modifier,
    label: String,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    placeholder: String = "GG/MM/AAAA",
    error: String? = null,
    enabled: Boolean = true
) {
    val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    
    TextField(
        modifier = modifier.fillMaxWidth(),
        value = formattedDate,
        onValueChange = { },
        label = { Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        placeholder = { Text(text = placeholder) },
        readOnly = true,
        singleLine = true,
        isError = error != null,
        supportingText = { if (error != null) Text(text = error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) },
        trailingIcon = {
            androidx.compose.material.icons.Icons.Default.CalendarMonth
        },
        colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Number),
        onClick = {
            // Date picker dialog would be shown here
            // For now, we'll just show a placeholder
        }
    )
}