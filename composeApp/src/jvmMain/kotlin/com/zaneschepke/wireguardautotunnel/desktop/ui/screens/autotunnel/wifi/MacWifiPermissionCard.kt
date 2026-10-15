package com.zaneschepke.wireguardautotunnel.desktop.ui.screens.autotunnel.wifi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zaneschepke.wireguardautotunnel.composeapp.generated.resources.Res
import com.zaneschepke.wireguardautotunnel.composeapp.generated.resources.wifi_name_allow
import com.zaneschepke.wireguardautotunnel.composeapp.generated.resources.wifi_name_denied_body
import com.zaneschepke.wireguardautotunnel.composeapp.generated.resources.wifi_name_open_settings
import com.zaneschepke.wireguardautotunnel.composeapp.generated.resources.wifi_name_permission_body
import com.zaneschepke.wireguardautotunnel.composeapp.generated.resources.wifi_name_permission_title
import com.zaneschepke.wireguardautotunnel.composeapp.generated.resources.wifi_name_services_off_body
import com.zaneschepke.wireguardautotunnel.composeapp.generated.resources.wifi_name_unavailable_title
import com.zaneschepke.wireguardautotunnel.desktop.ui.common.text.DescriptionText
import com.zaneschepke.wireguardautotunnel.desktop.wifi.MacWifiNameCoordinator
import com.zaneschepke.wireguardautotunnel.desktop.wifi.MacWifiNamePrompt
import com.zaneschepke.wireguardautotunnel.desktop.wifi.macWifiNamePrompt
import dev.nucleusframework.core.runtime.Platform
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
fun MacWifiPermissionNotice(coordinator: MacWifiNameCoordinator = koinInject()) {
    if (Platform.Current != Platform.MacOS) return
    val status by coordinator.authorization.collectAsState()
    val prompt = macWifiNamePrompt(status) ?: return
    MacWifiPermissionCard(
        prompt = prompt,
        onAllow = coordinator::requestAuthorization,
        onOpenSettings = coordinator::openLocationSettings,
    )
}

@Composable
fun MacWifiPermissionCard(
    prompt: MacWifiNamePrompt,
    onAllow: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val title =
        stringResource(
            when (prompt) {
                MacWifiNamePrompt.Allow -> Res.string.wifi_name_permission_title
                MacWifiNamePrompt.Denied,
                MacWifiNamePrompt.ServicesOff -> Res.string.wifi_name_unavailable_title
            }
        )
    val body =
        stringResource(
            when (prompt) {
                MacWifiNamePrompt.Allow -> Res.string.wifi_name_permission_body
                MacWifiNamePrompt.Denied -> Res.string.wifi_name_denied_body
                MacWifiNamePrompt.ServicesOff -> Res.string.wifi_name_services_off_body
            }
        )
    val button =
        stringResource(
            when (prompt) {
                MacWifiNamePrompt.Allow -> Res.string.wifi_name_allow
                MacWifiNamePrompt.Denied,
                MacWifiNamePrompt.ServicesOff -> Res.string.wifi_name_open_settings
            }
        )
    val onClick =
        when (prompt) {
            MacWifiNamePrompt.Allow -> onAllow
            MacWifiNamePrompt.Denied,
            MacWifiNamePrompt.ServicesOff -> onOpenSettings
        }

    Column(
        modifier =
            Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        DescriptionText(body)
        Button(onClick = onClick) { Text(button) }
    }
}