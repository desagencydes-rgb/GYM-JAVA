package com.gym.app.service;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

/**
 * Singleton Service for managing the Webcam hardware.
 * Uses the 'webcam-capture' library to open, close, and fetch images.
 */
public class WebcamService {

    // Singleton instance
    private static WebcamService instance;
    private Webcam webcam;

    // Private constructor to enforce Singleton pattern
    private WebcamService() {
    }

    /**
     * Thread-safe method to get the single instance of WebcamService.
     * 
     * @return WebcamService instance.
     */
    public static synchronized WebcamService getInstance() {
        if (instance == null) {
            instance = new WebcamService();
        }
        return instance;
    }

    /**
     * Opens the default webcam if not already open.
     * Configures the resolution to VGA (640x480).
     * Opens in non-blocking mode to avoid freezing the UI.
     */
    public void openCamera() {
        if (webcam == null) {
            webcam = Webcam.getDefault();
            if (webcam != null) {
                // Set custom view size for better resolution (VGA)
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                webcam.open(true); // true = non-blocking (async) open
            }
        }
    }

    /**
     * Closes the webcam to release hardware resources.
     * Should be called when the view is destroyed or app closes.
     */
    public void closeCamera() {
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
            webcam = null;
        }
    }

    /**
     * Captures a single frame from the webcam.
     * 
     * @return BufferedImage of the current frame, or null if camera is closed.
     */
    public BufferedImage getImage() {
        if (webcam != null && webcam.isOpen()) {
            return webcam.getImage();
        }
        return null;
    }

    /**
     * Checks if the webcam is currently active.
     * 
     * @return true if open, false otherwise.
     */
    public boolean isOpen() {
        return webcam != null && webcam.isOpen();
    }
}
