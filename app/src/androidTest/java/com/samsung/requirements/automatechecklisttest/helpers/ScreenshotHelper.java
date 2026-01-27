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

    private final UiDevice device;
    private final Context context;

    public ScreenshotHelper(UiDevice device, Context context) {
        this.device = device;
        this.context = context;
    }

    /**
     * Captures a screenshot and saves it to /sdcard/Pictures/Screenshots/.
     *
     * @param filenamePrefix The prefix for the screenshot filename.
     * @return true if the screenshot was captured successfully, false otherwise.
     */
    public boolean captureScreenshot(String filenamePrefix) {
        // Create a unique filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String filename = String.format("%s_%s.png", filenamePrefix, timestamp);

        // Define the target directory as requested: /sdcard/Pictures/Screenshots/
        File screenshotDir = new File("/sdcard/Pictures/Screenshots");

        if (!screenshotDir.exists()) {
            if (screenshotDir.mkdirs()) {
                Log.i(TAG, "Created directory: " + screenshotDir.getAbsolutePath());
            } else {
                Log.w(TAG, "Failed to create directory with mkdirs(), trying public directory path...");
                File publicPictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                screenshotDir = new File(publicPictures, "Screenshots");
                if (!screenshotDir.exists() && !screenshotDir.mkdirs()) {
                    Log.e(TAG, "Failed to create screenshot directory: " + screenshotDir.getAbsolutePath());
                    return false;
                }
            }
        }

        File screenshotFile = new File(screenshotDir, filename);

        try {
            // Take screenshot with 100% scale and 100 quality
            // UiDevice.takeScreenshot handles the shell-level permission to write to /sdcard
            boolean success = device.takeScreenshot(screenshotFile, 1.0f, 100);
            if (success) {
                Log.i(TAG, "Screenshot saved to: " + screenshotFile.getAbsolutePath());
                // Force a media scan so the file appears in the Gallery/computer immediately
                device.executeShellCommand("am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file://" + screenshotFile.getAbsolutePath());
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
