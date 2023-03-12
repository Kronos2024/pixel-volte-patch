package dev.bluehouse.enablevolte

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

@Composable
fun HeaderText(text: String) {
    Row(modifier = Modifier.padding(top = 20.dp, bottom = 12.dp)) {
        Text(text = text, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun BooleanPropertyView(
    label: String,
    toggled: Boolean?,
    enabled: Boolean = true,
    trueLabel: String = stringResource(R.string.yes),
    falseLabel: String = stringResource(R.string.no),
    onClick: ((Boolean) -> Unit)? = null,
) {
    if (toggled == null) {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)) {
            Text(text = label, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
            Text(text = stringResource(R.string.unknown), fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
        }
        return
    }
    if (onClick != null) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)) {
            Text(text = label, modifier = Modifier.weight(1F), fontSize = 18.sp)
            Switch(checked = toggled, enabled = enabled, onCheckedChange = onClick)
        }
    } else {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)) {
            Text(text = label, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
            Text(text = if (toggled) { trueLabel } else { falseLabel }, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAgentPropertyView(label: String, value: String?, onUpdate: ((String) -> Unit)? = null) {
    val labels = arrayOf(stringResource(R.string.default_), stringResource(R.string.lgu))
    val values = arrayOf(stringResource(R.string.ua_default), stringResource(R.string.ua_lgu))

    var typedText by rememberSaveable { mutableStateOf("") }
    var openTextEditDialog by rememberSaveable { mutableStateOf(false) }
    var dropdownExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableStateOf(if (values.contains(value)) values.indexOf(value) else 0) }

    if (onUpdate != null) {
        if (openTextEditDialog) {
            AlertDialog(
                onDismissRequest = {
                    // Dismiss the dialog when the user clicks outside the dialog or on the back
                    // button. If you want to disable that functionality, simply use an empty
                    // onDismissRequest.
                    openTextEditDialog = false
                },
            ) {
                Surface(
                    modifier = Modifier
                        .wrapContentWidth()
                        .wrapContentHeight(),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Column(modifier = Modifier.padding(all = 16.dp)) {
                        Text(text = stringResource(R.string.update_value), style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 24.dp))
                        ExposedDropdownMenuBox(
                            expanded = dropdownExpanded,
                            onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                            modifier = Modifier.padding(bottom = 8.dp),
                        ) {
                            TextField(
                                // The `menuAnchor` modifier must be passed to the text field for correctness.
                                modifier = Modifier.menuAnchor().wrapContentWidth(),
                                readOnly = true,
                                value = if (values[selectedIndex] == typedText) labels[selectedIndex] else "Custom",
                                onValueChange = {},
                                label = { Text("Presets") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                                colors = ExposedDropdownMenuDefaults.textFieldColors(),
                            )
                            ExposedDropdownMenu(
                                expanded = dropdownExpanded,
                                onDismissRequest = { dropdownExpanded = false },
                            ) {
                                labels.forEachIndexed { i, label ->
                                    DropdownMenuItem(
                                        text = { Text(text = label) },
                                        onClick = {
                                            typedText = values[i]
                                            selectedIndex = i
                                            dropdownExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    )
                                }
                            }
                        }
                        TextField(textStyle = TextStyle(fontSize = 14.sp), value = typedText, onValueChange = { typedText = it })
                        Row(modifier = Modifier.align(Alignment.End)) {
                            TextButton(
                                onClick = {
                                    onUpdate(typedText)
                                    openTextEditDialog = false
                                },
                            ) {
                                Text(stringResource(R.string.confirm))
                            }
                            TextButton(
                                onClick = {
                                    openTextEditDialog = false
                                },
                            ) {
                                Text(stringResource(R.string.dismiss))
                            }
                        }
                    }
                }
            }
        }
    }
    ClickablePropertyView(label = label, value = value) {
        if (value != null) {
            typedText = value
            openTextEditDialog = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StringPropertyView(label: String, value: String?, onUpdate: ((String) -> Unit)? = null) {
    var typedText by rememberSaveable { mutableStateOf("") }
    var openTextEditDialog by rememberSaveable { mutableStateOf(false) }

    if (onUpdate != null) {
        if (openTextEditDialog) {
            AlertDialog(
                onDismissRequest = {
                    // Dismiss the dialog when the user clicks outside the dialog or on the back
                    // button. If you want to disable that functionality, simply use an empty
                    // onDismissRequest.
                    openTextEditDialog = false
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onUpdate(typedText)
                            openTextEditDialog = false
                        },
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            openTextEditDialog = false
                        },
                    ) {
                        Text(stringResource(R.string.dismiss))
                    }
                },
                title = { Text(text = stringResource(R.string.update_value), style = MaterialTheme.typography.titleLarge) },
                text = {
                    TextField(value = typedText, onValueChange = { typedText = it })
                },
            )
        }
    }
    ClickablePropertyView(label = label, value = value) {
        if (value != null) {
            typedText = value
            openTextEditDialog = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickablePropertyView(label: String, value: String?, onClick: (() -> Unit)? = null) {
    if (value == null) {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)) {
            Text(text = label, modifier = Modifier.padding(bottom = 4.dp))
            Text(text = stringResource(R.string.unknown), color = MaterialTheme.colorScheme.outline, fontSize = 14f.sp)
        }
        return
    }
    if (onClick != null) {
        Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)) {
                Text(text = label, modifier = Modifier.padding(bottom = 4.dp), fontSize = 18.sp)
                Text(text = value, color = MaterialTheme.colorScheme.outline, fontSize = 14f.sp)
            }
        }
    } else {
        Column(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp)) {
            Text(text = label, modifier = Modifier.padding(bottom = 4.dp))
            Text(text = value, color = MaterialTheme.colorScheme.outline, fontSize = 14f.sp)
        }
    }
}
