package com.skynet.common

import android.Manifest
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.util.Log
import androidx.annotation.RequiresPermission

object ConnectWifi {
    // 암호 필요 없을경우
    fun connectOpenCapabilities(ssid: String): WifiConfiguration {
        val wfc = WifiConfiguration()
        wfc.SSID = "\"$ssid\""
        wfc.priority = 40

        wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
        wfc.allowedAuthAlgorithms.clear()
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)

        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)

        // connect(wfc);
        return wfc
    }

    /**
     * WEP 방식 일 때 설정
     */
    fun connectWEP(ssid: String): WifiConfiguration {
        val wfc = WifiConfiguration()
        wfc.SSID = "\"$ssid\""
        wfc.priority = 40

        val password = "123456789"

        wfc.status = WifiConfiguration.Status.DISABLED
        wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
        wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)

        val length = password.length
        if ((length == 10 || length == 26 || length == 58)
            && password.matches(Regex("[0-9A-Fa-f]*"))) {
            wfc.wepKeys[0] = password
        } else {
            wfc.wepKeys[0] = "\"$password\""
        }
        // connect(wfc);
        return wfc
    }

    /**
     * WPA, WPA2 방식 일 때 설정
     */
    fun connectWPA(ssid: String): WifiConfiguration {
        // 공통 부분
        val wfc = WifiConfiguration()
        wfc.SSID = "\"$ssid\""
        wfc.priority = 40

        wfc.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
        wfc.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
        wfc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)

        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
        wfc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)

        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
        wfc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)

        val password = "123456789"
        wfc.preSharedKey = "\"$password\""

        // connect(wfc);
        return wfc
    }

    /**
     * 원하는 네트워크 아이디에 AP 에 연결
     */
    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE])
    fun connect(wfc: WifiConfiguration, wifi: WifiManager, ssid: String) {
        var isId = false
        var networkID = 0
        var tempID = 0
        //String tempSSID;

        val wifiConfigurationList = wifi.configuredNetworks
        for (w in wifiConfigurationList) {
            if (w.SSID == "\"$ssid\"") {
                isId = true
                tempID = w.networkId
                //tempSSID = w.SSID;
                break
            } else {
                // Log.e("check", "else : id = "+w.SSID);
            }
        }

        networkID = if (isId) {
            tempID
        } else {
            wifi.addNetwork(wfc)
        }

        val bEnableNetwork = wifi.enableNetwork(networkID, true)
        if (bEnableNetwork) {
            Log.d("", "Connected!")
        } else {
            Log.d("", "Disconnected!")
        }
    }
}