package com.samsung.requirements.automatechecklisttest.helpers

import android.content.Context
import android.os.Environment
import androidx.test.uiautomator.UiDevice
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScreenshotHelper(private val device: UiDevice, private val context: Context) {

    companion object {
        private const val SCREENSHOT_DIRECTORY_NAME = "test_screenshots"
    }

    fun captureScreenshot(filenamePrefix: String): Boolean {
        // Cria um nome de arquivo único com timestamp para evitar sobrescrever
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val filename = "${filenamePrefix}_${timestamp}.png"

        // Determina o diretório para salvar os screenshots
        // Usando o diretório de arquivos externos específico do aplicativo para que não exija permissões de armazenamento complexas
        // em APIs mais recentes, e é limpo quando o app é desinstalado.
        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val screenshotDir = File(picturesDir, SCREENSHOT_DIRECTORY_NAME)

        if (!screenshotDir.exists()) {
            if (!screenshotDir.mkdirs()) {
                println("ScreenshotHelper: Falha ao criar o diretório de screenshots: ${screenshotDir.absolutePath}")
                return false
            }
        }

        val screenshotFile = File(screenshotDir, filename)

        try {
            // Usa o UiDevice para capturar o screenshot
            // O UiDevice.takeScreenshot requer um File e uma escala (1.0f para 100%) e qualidade (100)
            val success = device.takeScreenshot(screenshotFile, 1.0f, 100)
            if (success) {
                println("ScreenshotHelper: Screenshot salvo em: ${screenshotFile.absolutePath}")
            } else {
                println("ScreenshotHelper: UiDevice.takeScreenshot() retornou false.")
            }
            return success
        } catch (e: Exception) {
            println("ScreenshotHelper: Exceção ao capturar screenshot: ${e.message}")
            e.printStackTrace()
            return false
        }
    }
}