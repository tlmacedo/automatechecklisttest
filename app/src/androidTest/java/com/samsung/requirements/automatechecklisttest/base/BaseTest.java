package com.samsung.requirements.automatechecklisttest.base;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.By;
import androidx.test.uiautomator.BySelector;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import androidx.test.uiautomator.UiScrollable;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.Until;

import com.samsung.requirements.automatechecklisttest.R;
import com.samsung.requirements.automatechecklisttest.helpers.ScreenshotHelper;
import com.samsung.requirements.automatechecklisttest.screens.ScreenInspector;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Classe base para testes automatizados utilizando UI Automator.
 * Fornece métodos utilitários para interação com a UI e gerenciamento do dispositivo.
 */
public class BaseTest {
    protected UiDevice device;
    protected Context appContext;
    protected ScreenInspector screenInspector;

    public static final long LAUNCH_TIMEOUT = 5000L;
    public static final long SHORT_TIMEOUT = 2000L;
    public static final long DEFAULT_TIMEOUT = 10000L;
    public static final long LONG_TIMEOUT = 20000L;

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    );

    @Rule
    public TestWatcher screenshotOnFailure = new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
            try {
                takeScreenshot("FAILED_" + description.getMethodName());
            } catch (Exception ignored) {
            }
        }
    };

    @Before
    public void setup() {
        try {
            device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
            appContext = ApplicationProvider.getApplicationContext();
            screenInspector = new ScreenInspector(device);

            device.pressBack();
            device.pressBack();
            device.pressHome();
            closeAllApps();
        } catch (RemoteException e) {
            Log.e("BaseTest", "Erro de conexão remota com o dispositivo no setup", e);
        }
    }

    /**
     * Inicia uma Activity de forma explícita via pacote e nome da classe.
     *
     * @param packageName Nome do pacote da aplicação.
     * @param className Nome completo da classe da Activity.
     */
    protected void launchActivity(String packageName, String className) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(packageName, className));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        appContext.startActivity(intent);
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), LAUNCH_TIMEOUT);
    }

    /**
     * Realiza scroll até encontrar o texto e clica.
     *
     * @param stringResId ID do recurso de string.
     */
    protected void scrollAndClick(int stringResId) {
        String text = appContext.getString(stringResId);
        scrollAndClick(text);
    }

    /**
     * Realiza scroll até encontrar o texto e clica.
     *
     * @param text Texto a ser procurado e clicado.
     */
    protected void scrollAndClick(String text) {
        try {
            UiScrollable scrollable = new UiScrollable(new UiSelector().scrollable(true));
            if (scrollable.scrollTextIntoView(text)) {
                safeClick(waitAndFindObject(By.text(text)));
            } else {
                safeClick(waitAndFindObject(By.text(text)));
            }
        } catch (Exception e) {
            Log.e("BaseTest", "Erro ao realizar scroll e clicar em: " + text);
            safeClick(waitAndFindObject(By.text(text)));
        }
    }

    /**
     * Fecha todos os aplicativos abertos recentemente.
     */
    protected void closeAllApps() throws RemoteException {
        String closeAllText;
        try {
            closeAllText = appContext.getString(R.string.close_all);
        } catch (Exception e) {
            closeAllText = "Fechar tudo";
        }
        
        device.pressRecentApps();
        device.waitForIdle(SHORT_TIMEOUT);

        UiObject2 closeBtn = device.findObject(By.textContains(closeAllText));
        if (closeBtn != null) {
            safeClick(closeBtn);
        }

        device.pressHome();
        device.waitForIdle(SHORT_TIMEOUT);
    }

    /**
     * Aguarda e retorna um objeto da UI baseado no seletor.
     */
    protected UiObject2 waitAndFindObject(BySelector selector) {
        return waitAndFindObject(selector, DEFAULT_TIMEOUT);
    }

    protected UiObject2 waitAndFindObject(BySelector selector, long timeout) {
        UiObject2 object = device.wait(Until.findObject(selector), timeout);
        if (object == null) {
            throw new NullPointerException("Objeto não encontrado: " + selector.toString());
        }
        return object;
    }

    /**
     * Verifica se um objeto está visível na tela dentro do timeout.
     */
    protected boolean waitVisible(BySelector selector, long timeout) {
        return device.wait(Until.hasObject(selector), timeout);
    }

    protected boolean waitVisible(int stringResId, long timeout) {
        String text = appContext.getString(stringResId);
        return waitVisible(By.textContains(text), timeout);
    }

    /**
     * Realiza o clique de forma segura em um objeto.
     */
    protected void safeClick(UiObject2 obj) {
        safeClick(obj, SHORT_TIMEOUT);
    }

    protected void safeClick(UiObject2 obj, long waitAfterMs) {
        Objects.requireNonNull(obj, "Objeto nulo para clique");
        obj.click();
        device.waitForIdle(waitAfterMs);
    }

    /**
     * Obtém uma propriedade do sistema via getprop.
     */
    protected String getSystemProperty(String propName) {
        try {
            Process process = Runtime.getRuntime().exec("getprop " + propName);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                return output.toString().trim();
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    /**
     * Verifica a existência de um arquivo via shell.
     */
    protected boolean checkFileExistsViaShell(String path) {
        try {
            Process process = Runtime.getRuntime().exec("ls " + path);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String output = reader.readLine();
                if (output != null) output = output.trim();
                return path.equals(output) || (output != null && !output.isEmpty() && !output.contains("No such file"));
            }
        } catch (Exception e) {
            return false;
        }
    }

    protected void openQuickSettings() {
        device.openQuickSettings();
        try { Thread.sleep(SHORT_TIMEOUT); } catch (InterruptedException ignored) {}
    }

    protected void closeQuickSettings() throws RemoteException {
        device.pressBack();
        try { Thread.sleep(SHORT_TIMEOUT); } catch (InterruptedException ignored) {}
    }

    protected boolean isElementVisible(BySelector selector, long timeout) {
        return device.wait(Until.hasObject(selector), timeout);
    }

    protected boolean waitAnyHasObject(long timeoutMs, BySelector... selectors) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            for (BySelector sel : selectors) {
                if (device.hasObject(sel)) return true;
            }
            try { Thread.sleep(150); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    protected boolean waitCheckedState(UiObject2 switchObj, boolean expected, long timeoutMs) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() - start < timeoutMs) {
            if (switchObj.isChecked() == expected) return true;
            try { Thread.sleep(150); } catch (InterruptedException ignored) {}
        }
        return switchObj.isChecked() == expected;
    }

    public void dumpObjectInfo(UiObject2 uiObject, String objectName) {
        Log.d("BaseTest", "--- Inspecionando o objeto: '" + objectName + "' ---");
        if (uiObject == null) {
            Log.d("BaseTest", "-> Objeto é NULO.");
            return;
        }
        try {
            Log.d("BaseTest", "-> Texto: '" + uiObject.getText() + "'");
            Log.d("BaseTest", "-> Desc: '" + uiObject.getContentDescription() + "'");
            Log.d("BaseTest", "-> Clicável: " + uiObject.isClickable());
            Log.d("BaseTest", "-> Checado: " + uiObject.isChecked());
        } catch (Exception ignored) { }
    }

    protected void takeScreenshot(String filename) {
        String currentPkg = device.getCurrentPackageName();
        Log.i("BaseTest", "Capturando screenshot: " + filename + " | Contexto (Package): " + currentPkg);
        ScreenshotHelper screenshotHelper = new ScreenshotHelper(device, appContext);
        screenshotHelper.captureScreenshot(filename);
    }

    public void tearDown() throws RemoteException {
        device.pressHome();
    }
}
