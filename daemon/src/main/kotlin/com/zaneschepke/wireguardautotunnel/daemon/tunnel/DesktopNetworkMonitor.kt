package com.zaneschepke.wireguardautotunnel.daemon.tunnel

import co.touchlab.kermit.Logger
import com.wgtunnel.backend.network.NetworkInfoDto
import com.wgtunnel.backend.system.DesktopNativeNetworkMonitor
import com.wgtunnel.backend.system.NetworkMonitor
import com.wgtunnel.backend.system.NetworkSnapshot
import com.zaneschepke.wireguardautotunnel.core.wifi.WifiSsidNative
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class DesktopNetworkMonitor(scope: CoroutineScope) : NetworkMonitor {
    private val log = Logger.withTag("DesktopNetworkMonitor")
    private val impl = DesktopNativeNetworkMonitor(scope)

    val info: StateFlow<NetworkInfoDto> = impl.info

    private val _wifiReadRequests =
        MutableSharedFlow<Unit>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )

    /**
     * Asks the Wi-Fi helper to read. No name is included. Late subscribers do not get a replay, and
     * an ask with nobody listening is dropped. The helper reads once when it connects.
     */
    val wifiReadRequests: SharedFlow<Unit> = _wifiReadRequests.asSharedFlow()

    override val networkState: StateFlow<NetworkSnapshot?> = impl.networkState

    init {
        scope.launch {
            var previous: NetworkInfoDto? = null
            impl.info.collect { current ->
                if (shouldAskWifiRead(previous, current)) _wifiReadRequests.tryEmit(Unit)
                previous = current
            }
        }
    }

    fun stop() = impl.stop()

    /** Records a Wi-Fi name read by the user-session helper. An empty name clears it. */
    fun setObservedSsid(ssid: String) {
        runCatching { WifiSsidNative.setObserved(ssid) }
            .onFailure { log.w(it) { "Failed to record Wi-Fi name" } }
    }
}
