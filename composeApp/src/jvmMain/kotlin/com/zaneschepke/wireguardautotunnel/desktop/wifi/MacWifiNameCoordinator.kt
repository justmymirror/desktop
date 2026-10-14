package com.zaneschepke.wireguardautotunnel.desktop.wifi

import co.touchlab.kermit.Logger
import com.wgtunnel.backend.loadBackendNativeLibrary
import com.zaneschepke.wireguardautotunnel.core.wifi.WifiAuthorization
import com.zaneschepke.wireguardautotunnel.core.wifi.WifiSsidNative
import dev.nucleusframework.core.runtime.Platform
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Tracks the macOS Location grant used to read the Wi-Fi name.
 *
 */
class MacWifiNameCoordinator(private val scope: CoroutineScope) {
    private val log = Logger.withTag("MacWifiName")
    private val _authorization = MutableStateFlow<WifiAuthorization?>(null)
    val authorization: StateFlow<WifiAuthorization?> = _authorization.asStateFlow()

    init {
        if (Platform.Current == Platform.MacOS) {
            scope.launch {
                if (!loadBackendNativeLibrary()) {
                    log.e { "Wi-Fi name reader failed to load" }
                    _authorization.value = WifiAuthorization.Unsupported
                    return@launch
                }
                while (isActive) {
                    refreshAuthorization()
                    delay(POLL_INTERVAL)
                }
            }
        }
    }

    fun requestAuthorization() {
        if (Platform.Current != Platform.MacOS) return
        scope.launch {
            runCatching { WifiSsidNative.requestAuthorization() }
                .onFailure { log.w(it) { "Wi-Fi name prompt failed" } }
            repeat(PROMPT_POLLS) {
                delay(PROMPT_POLL_INTERVAL)
                refreshAuthorization()
                val status = _authorization.value
                if (status != null && status != WifiAuthorization.NotDetermined) return@launch
            }
        }
    }

    fun openLocationSettings() {
        if (Platform.Current != Platform.MacOS) return
        scope.launch {
            for (url in LOCATION_SETTINGS_URLS) {
                val opened = runCatching {
                    ProcessBuilder("open", url).start().waitFor() == 0
                }
                    .getOrDefault(false)
                if (opened) return@launch
            }
            log.w { "Could not open Location Services settings" }
        }
    }

    private fun refreshAuthorization() {
        _authorization.value = readAuthorization()
    }

    private fun readAuthorization(): WifiAuthorization = runCatching {
        WifiSsidNative.authorizationStatus()
    }
        .getOrElse {
            log.w(it) { "Wi-Fi name authorization check failed" }
            WifiAuthorization.Unsupported
        }

    private companion object {
        val POLL_INTERVAL = 4.seconds
        const val PROMPT_POLLS = 40
        val PROMPT_POLL_INTERVAL = 500.milliseconds
        val LOCATION_SETTINGS_URLS =
            listOf(
                "x-apple.systempreferences:com.apple.settings.PrivacySecurity.extension?Privacy_LocationServices",
                "x-apple.systempreferences:com.apple.preference.security?Privacy_LocationServices",
            )
    }
}
