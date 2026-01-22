package com.samsung.requirements.automatechecklisttest.ut

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.telephony.TelephonyManager
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import com.samsung.requirements.automatechecklisttest.base.BaseTest
import org.junit.Test

/**
 * Teste de baixo nível para verificar o estado e o tipo de conexão de dados ativa (4G, 5G, Wi-Fi, etc).
 */
class DataConnectionUT : BaseTest() {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun checkActiveDataIconType() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        Log.i("DataConnectionUT", "--- Verificando Conexão de Dados ---")

        if (capabilities == null) {
            Log.w("DataConnectionUT", "Nenhuma conexão ativa detectada.")
            return
        }

        when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                Log.i("DataConnectionUT", "Ícone ativo: WI-FI")
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val networkType = getCellularNetworkType()
                Log.i("DataConnectionUT", "Ícone ativo: DADOS MÓVEIS ($networkType)")
            }
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                Log.i("DataConnectionUT", "Ícone ativo: ETHERNET")
            }
            else -> {
                Log.i("DataConnectionUT", "Ícone ativo: OUTRO")
            }
        }
        
        Log.i("DataConnectionUT", "Velocidade de Link (Downstream): ${capabilities.linkDownstreamBandwidthKbps} Kbps")
        Log.i("DataConnectionUT", "------------------------------------")
    }

    private fun getCellularNetworkType(): String {
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        
        return try {
            val networkType = telephonyManager.dataNetworkType
            when (networkType) {
                TelephonyManager.NETWORK_TYPE_NR -> "5G"
                TelephonyManager.NETWORK_TYPE_LTE -> "4G/LTE"
                TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_HSPA -> "3G+"
                TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_GPRS -> "2G"
                else -> "Desconhecido ($networkType)"
            }
        } catch (e: SecurityException) {
            "Sem permissão: ${e.message}"
        }
    }
}
