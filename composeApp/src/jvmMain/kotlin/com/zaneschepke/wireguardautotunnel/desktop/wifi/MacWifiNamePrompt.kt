package com.zaneschepke.wireguardautotunnel.desktop.wifi

import com.zaneschepke.wireguardautotunnel.core.wifi.WifiAuthorization

enum class MacWifiNamePrompt {
    Allow,

    Denied,

    ServicesOff,
}

fun macWifiNamePrompt(status: WifiAuthorization?): MacWifiNamePrompt? =
    when (status) {
        WifiAuthorization.NotDetermined -> MacWifiNamePrompt.Allow
        WifiAuthorization.Denied,
        WifiAuthorization.Restricted -> MacWifiNamePrompt.Denied
        WifiAuthorization.ServicesOff -> MacWifiNamePrompt.ServicesOff
        WifiAuthorization.AuthorizedAlways,
        WifiAuthorization.AuthorizedWhenInUse,
        WifiAuthorization.Unsupported,
        null -> null
    }
