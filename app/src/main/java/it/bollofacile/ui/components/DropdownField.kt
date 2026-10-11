package it.bollofacile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DropdownField(
    modifier: Modifier = Modifier,
    label: String,
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    placeholder: String = "Seleziona",
    error: String? = null,
    enabled: Boolean = true
) {
    var expanded by mutableStateOf(false)
    
    ExposedDropdownMenuBox(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = if (error != null) 8.dp else 0.dp),
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        androidx.compose.material3.TextField(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) { }
                .then(
                    if (enabled) Modifier else Modifier
                ),
            value = selectedOption,
            onValueChange = { },
            label = { Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            placeholder = { Text(text = placeholder) },
            readOnly = true,
            singleLine = true,
            isError = error != null,
            supportingText = { if (error != null) Text(text = error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error) },
            trailingIcon = {
                androidx.compose.material.icons.Icons.Default.ExpandMore
            },
            colors = androidx.compose.material3.TextFieldDefaults.textFieldColors(
                containerColor = if (enabled) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainer,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = KeyboardType.Text),
            keyboardActions = KeyboardActions(onDone = { expanded = true })
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                MenuItem(
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    text = { Text(text = option) },
                    selected = selectedOption == option
                )
            }
        }
    }
}