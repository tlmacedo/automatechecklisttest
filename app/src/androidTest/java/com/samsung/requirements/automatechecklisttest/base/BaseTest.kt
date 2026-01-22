package com.samsung.requirements.automatechecklisttest.base

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.UiScrollable
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.samsung.requirements.automatechecklisttest.R
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description

open class BaseTest {
    protected lateinit var device: UiDevice
    protected lateinit var appContext: Context
    protected lateinit var screenInspector: com.samsung.requirements.automatechecklisttest.screens.ScreenInspector

    @get:Rule
    var permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    companion object {
        const val LAUNCH_TIMEOUT = 5_000L
        const val SHORT_TIMEOUT = 2_000L
        const val DEFAULT_TIMEOUT = 10_000L
        const val LONG_TIMEOUT = 20_000L
    }

    @get:Rule
    val screenshotOnFailure = object : TestWatcher() {
        override fun failed(e: Throwable?, description: Description) {
            try {
                takeScreenshot("FAILED_${description.methodName}.png")
            } catch (_: Exception) { }
        }
    }

    @Before
    open fun setup() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        appContext = ApplicationProvider.getApplicationContext()
        screenInspector = com.samsung.requirements.automatechecklisttest.screens.ScreenInspector(device)

        device.pressBack()
        device.pressBack()
        device.pressHome()
        closeAllApps()
    }

    /**
     * Inicia uma Activity de forma explícita via pacote e nome da classe.
     */
    protected fun launchActivity(packageName: String, className: String) {
        val intent = Intent().apply {
            component = ComponentName(packageName, className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        appContext.startActivity(intent)
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), LAUNCH_TIMEOUT)
    }

    /**
     * Realiza scroll e clica usando um ID de string do recurso.
     */
    protected fun scrollAndClick(stringResId: Int) {
        val text = appContext.getString(stringResId)
        scrollAndClick(text)
    }

    protected fun scrollAndClick(text: String) {
        try {
            val scrollable = UiScrollable(UiSelector().scrollable(true))
            if (scrollable.scrollTextIntoView(text)) {
                val obj = waitAndFindObject(By.text(text))
                safeClick(obj)
            } else {
                safeClick(waitAndFindObject(By.text(text)))
            }
        } catch (e: Exception) {
            Log.e("BaseTest", "Erro ao scrollar e clicar em: $text")
            safeClick(waitAndFindObject(By.text(text)))
        }
    }

    protected fun closeAllApps() {
        val closeAllText = try { appContext.getString(R.string.close_all) } catch (e: Exception) { "Fechar tudo" }
        device.pressRecentApps()
        device.waitForIdle(SHORT_TIMEOUT)

        val closeBtn = device.findObject(By.textContains(closeAllText))
        closeBtn?.let { safeClick(it) }
        
        device.pressHome()
        device.waitForIdle(SHORT_TIMEOUT)
    }

    protected fun waitAndFindObject(selector: BySelector, timeout: Long = DEFAULT_TIMEOUT): UiObject2 {
        return device.wait(Until.findObject(selector), timeout)
            ?: throw NullPointerException("Objeto não encontrado: $selector")
    }

    protected fun waitVisible(selector: BySelector, timeout: Long = DEFAULT_TIMEOUT): Boolean {
        return device.wait(Until.hasObject(selector), timeout)
    }

    protected fun waitVisible(stringResId: Int, timeout: Long = DEFAULT_TIMEOUT): Boolean {
        val text = appContext.getString(stringResId)
        return waitVisible(By.textContains(text), timeout)
    }

    protected fun safeClick(obj: UiObject2?, waitAfterMs: Long = SHORT_TIMEOUT) {
        requireNotNull(obj) { "Objeto nulo para clique" }
        obj.click()
        device.waitForIdle(waitAfterMs)
    }

    // ----------------- MÉTODOS DE BAIXO NÍVEL (UT) -----------------

    /**
     * Obtém uma propriedade do sistema via getprop.
     */
    protected fun getSystemProperty(propName: String): String {
        return try {
            val process = Runtime.getRuntime().exec("getprop $propName")
            process.inputStream.bufferedReader().use { it.readText() }.trim()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    /**
     * Verifica a existência de um arquivo via shell.
     */
    protected fun checkFileExistsViaShell(path: String): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("ls $path")
            val output = process.inputStream.bufferedReader().use { it.readText() }.trim()
            // Se o ls retornar o próprio caminho, o arquivo existe.
            output == path || (output.isNotEmpty() && !output.contains("No such file"))
        } catch (e: Exception) {
            false
        }
    }

    protected fun openQuickSettings() {
        device.openQuickSettings()
        Thread.sleep(SHORT_TIMEOUT)
    }

    protected fun closeQuickSettings() {
        device.pressBack()
        Thread.sleep(SHORT_TIMEOUT)
    }

    protected fun isElementVisible(selector: BySelector, timeout: Long = SHORT_TIMEOUT): Boolean {
        return device.wait(Until.hasObject(selector), timeout)
    }

    protected fun waitAnyHasObject(timeoutMs: Long, vararg selectors: BySelector): Boolean {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            for (sel in selectors) {
                if (device.findObject(sel) != null) return true
            }
            Thread.sleep(150)
        }
        return false
    }

    protected fun waitCheckedState(
        switchObj: UiObject2,
        expected: Boolean,
        timeoutMs: Long = DEFAULT_TIMEOUT
    ): Boolean {
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (switchObj.isChecked == expected) return true
            Thread.sleep(150)
        }
        return (switchObj.isChecked == expected)
    }

    fun dumpObjectInfo(uiObject: UiObject2?, objectName: String) {
        Log.d("BaseTest", "--- Inspecionando o objeto: '$objectName' ---")
        if (uiObject == null) {
            Log.d("BaseTest", "-> Objeto é NULO.")
            return
        }
        try {
            Log.d("BaseTest", "-> Texto: '${uiObject.text}'")
            Log.d("BaseTest", "-> Desc: '${uiObject.contentDescription}'")
            Log.d("BaseTest", "-> Clicável: ${uiObject.isClickable}")
            Log.d("BaseTest", "-> Checado: ${uiObject.isChecked}")
        } catch (e: Exception) { }
    }

    protected fun takeScreenshot(filename: String) {
        val screenshotHelper = com.samsung.requirements.automatechecklisttest.helpers.ScreenshotHelper(device, appContext)
        screenshotHelper.captureScreenshot(filename)
    }

    open fun tearDown() {
        device.pressHome()
    }
}
