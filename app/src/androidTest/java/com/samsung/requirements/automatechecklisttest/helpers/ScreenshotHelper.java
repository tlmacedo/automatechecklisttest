package com.samsung.requirements.automatechecklisttest.helpers;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.test.uiautomator.UiDevice;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Helper class to capture screenshots using UiDevice.
 */
public class ScreenshotHelper {

    private static final String TAG = "ScreenshotHelper";
    private static final String SCREENSHOT_DIRECTORY_NAME = "test_screenshots";

    private final UiDevice device;
    private final Context context;

    public ScreenshotHelper(UiDevice device, Context context) {
        this.device = device;
        this.context = context;
    }

    /**
     * Captures a screenshot and saves it to the application's external pictures directory.
     *
     * @param filenamePrefix The prefix for the screenshot filename.
     * @return true if the screenshot was captured successfully, false otherwise.
     */
    public boolean captureScreenshot(String filenamePrefix) {
        // Create a unique filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String filename = String.format("%s_%s.png", filenamePrefix, timestamp);

        // Determine the directory to save screenshots
        // Using context.getExternalFilesDir avoids permission issues on newer Android versions
        File picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (picturesDir == null) {
            Log.e(TAG, "External pictures directory is not available.");
            return false;
        }

        File screenshotDir = new File(picturesDir, SCREENSHOT_DIRECTORY_NAME);

        if (!screenshotDir.exists() && !screenshotDir.mkdirs()) {
            Log.e(TAG, "Failed to create screenshot directory: " + screenshotDir.getAbsolutePath());
            return false;
        }

        File screenshotFile = new File(screenshotDir, filename);

        try {
            // Take screenshot with 100% scale and 100 quality
            boolean success = device.takeScreenshot(screenshotFile, 1.0f, 100);
            if (success) {
                Log.i(TAG, "Screenshot saved to: " + screenshotFile.getAbsolutePath());
            } else {
                Log.e(TAG, "UiDevice.takeScreenshot() returned false.");
            }
            return success;
        } catch (Exception e) {
            Log.e(TAG, "Exception while capturing screenshot: " + e.getMessage(), e);
            return false;
        }
    }
}
