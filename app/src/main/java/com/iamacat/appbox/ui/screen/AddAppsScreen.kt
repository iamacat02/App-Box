package com.iamacat.appbox.ui.screen

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iamacat.appbox.data.model.AppEntry
import com.iamacat.appbox.ui.component.AppIcon
import com.iamacat.appbox.ui.component.EmptySectionState
import com.iamacat.appbox.ui.component.LauncherToolbar
import com.iamacat.appbox.ui.event.AddAppsEffect
import com.iamacat.appbox.ui.event.AddAppsUiEvent
import com.iamacat.appbox.ui.state.AddAppsUiState
import com.iamacat.appbox.ui.theme.GreenifyColors
import com.iamacat.appbox.ui.viewmodel.AddAppsViewModel

@Composable
fun AddAppsScreen(
    viewModel: AddAppsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                AddAppsEffect.NavigateBack -> onBack()
                is AddAppsEffect.ShowMessage ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    AddAppsContent(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onBack = onBack
    )
}

@Composable
private fun AddAppsContent(
    uiState: AddAppsUiState,
    onEvent: (AddAppsUiEvent) -> Unit,
    onBack: () -> Unit
) {
    var searchActive by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Column {
                LauncherToolbar(
                    title = if (uiState.sectionName.isBlank()) "Add Apps" else "Add Apps to ${uiState.sectionName}",
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                if (searchActive) {
                                    onEvent(AddAppsUiEvent.SearchQueryChanged(""))
                                }
                                searchActive = !searchActive
                            }
                        ) {
                            Icon(
                                imageVector = if (searchActive) Icons.Filled.Close else Icons.Filled.Search,
                                contentDescription = if (searchActive) "Close search" else "Search apps"
                            )
                        }
                    }
                )
                if (searchActive) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { onEvent(AddAppsUiEvent.SearchQueryChanged(it)) },
                        placeholder = { Text("Search apps") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GreenifyColors.Primary,
                            cursorColor = GreenifyColors.Primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { onEvent(AddAppsUiEvent.Submit) },
                    enabled = uiState.selectedCount > 0 && !uiState.isSaving &&
                        uiState.errorMessage == null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenifyColors.Primary,
                        contentColor = GreenifyColors.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(48.dp)
                ) {
                    Text(
                        text = if (uiState.selectedCount > 0) {
                            "ADD (${uiState.selectedCount})"
                        } else {
                            "ADD"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )

                uiState.visibleApps.isEmpty() -> EmptySectionState(
                    title = "No apps found",
                    message = if (uiState.searchQuery.isBlank()) {
                        "No launchable apps were found on this device"
                    } else {
                        "No apps match \"${uiState.searchQuery}\""
                    }
                )

                else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items = uiState.visibleApps, key = { it.packageName }) { app ->
                        AddAppsRow(
                            app = app,
                            alreadyAdded = app.packageName in uiState.existingPackageNames,
                            selected = app.packageName in uiState.selectedPackageNames,
                            onToggle = { onEvent(AddAppsUiEvent.ToggleApp(app.packageName)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddAppsRow(
    app: AppEntry,
    alreadyAdded: Boolean,
    selected: Boolean,
    onToggle: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = selected || alreadyAdded,
                    enabled = !alreadyAdded,
                    role = Role.Checkbox,
                    onValueChange = { onToggle() }
                )
                .padding(start = 20.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
        ) {
            AppIcon(
                packageName = app.packageName,
                contentDescription = null,
                size = 44.dp
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.label,
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (alreadyAdded) "Already in this section" else app.packageName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Checkbox(
                checked = selected || alreadyAdded,
                onCheckedChange = null,
                enabled = !alreadyAdded,
                colors = CheckboxDefaults.colors(checkedColor = GreenifyColors.Primary)
            )
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = 20.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}
