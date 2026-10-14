package com.zaneschepke.wireguardautotunnel.core.ipc.dto.request

import kotlinx.serialization.Serializable

// Wi-Fi name read by the signed-in macOS app. An empty ssid clears it.
@Serializable data class WifiSsidReport(val ssid: String = "")
