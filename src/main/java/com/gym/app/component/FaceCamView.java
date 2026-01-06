package com.gym.app.component;

import com.gym.app.service.WebcamService;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox; // Explicit import for clarity
import javafx.scene.paint.Color;

import java.awt.image.BufferedImage;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A custom JavaFX component that wraps the WebcamService.
 * Displays a live camera feed and overlay messages (e.g., for Face Recognition
 * status).
 */
public class FaceCamView extends StackPane {

    // The JavaFX node to display the image
    private final ImageView imageView;
    // Shown while camera is initializing
    private final Label loadingLabel;

    // Container and Label for feedback overlays (e.g., "Face Recognized!")
    private final VBox overlayBox;
    private final Label overlayLabel;

    // Timer for the frame capture loop
    private Timer timer;
    private boolean isRunning = false;

    /**
     * Constructor. Initializes UI elements but does NOT start the camera.
     */
    public FaceCamView() {
        // Styling the container (black background like a camera off screen)
        this.setStyle("-fx-background-color: black; -fx-padding: 5; -fx-border-color: #444; -fx-border-width: 2;");
        this.setPrefSize(320, 240);
        this.setMaxSize(320, 240);

        // Image View to render frames
        imageView = new ImageView();
        imageView.setFitWidth(320);
        imageView.setFitHeight(240);
        imageView.setPreserveRatio(true);

        // Loading text
        loadingLabel = new Label("Initializing Camera...");
        loadingLabel.setStyle("-fx-text-fill: white;");

        // Initialize Overlay for feedback messages
        overlayLabel = new Label();
        overlayLabel.setStyle(
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-wrap-text: true; -fx-text-alignment: CENTER;");

        overlayBox = new VBox(overlayLabel);
        overlayBox.setAlignment(Pos.CENTER);
        overlayBox.setVisible(false); // Hidden by default
        overlayBox.setMaxSize(300, 220); // Slightly smaller than view to act as an overlay card

        // Add all children to the StackPane (layers them on top of each other)
        this.getChildren().addAll(loadingLabel, imageView, overlayBox);
    }

    /**
     * Starts the camera feed.
     * Open camera in a background thread to prevent UI freezing.
     */
    public void start() {
        if (isRunning)
            return;

        isRunning = true;
        loadingLabel.setVisible(true);

        Thread thread = new Thread(() -> {
            WebcamService.getInstance().openCamera();

            // Update UI on JavaFX Application Thread
            Platform.runLater(() -> {
                loadingLabel.setVisible(false);
                startFrameLoop();
            });
        });
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Scheduling a timer to poll for new frames from the webcam service ~30 times a
     * second.
     */
    private void startFrameLoop() {
        timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isRunning) {
                    cancel();
                    return;
                }

                BufferedImage bImg = WebcamService.getInstance().getImage();
                if (bImg != null) {
                    // Convert AWT BufferedImage to JavaFX Image
                    Image image = SwingFXUtils.toFXImage(bImg, null);
                    Platform.runLater(() -> imageView.setImage(image));
                }
            }
        }, 0, 33); // 33ms delay ~= 30 FPS
    }

    /**
     * Stops the camera feed and releases resources.
     */
    public void stop() {
        isRunning = false;
        if (timer != null) {
            timer.cancel();
        }
        WebcamService.getInstance().closeCamera();
        Platform.runLater(() -> imageView.setImage(null));
    }

    /**
     * Helper to get the current frame for capture/enrollment.
     * 
     * @return The current BufferedImage.
     */
    public BufferedImage getCurrentFrame() {
        return WebcamService.getInstance().getImage();
    }

    /**
     * Displays a temporary feedback message over the camera feed.
     * 
     * @param message    Text to display.
     * @param color      Background color of the overlay (e.g., GREEN for success,
     *                   RED for error).
     * @param durationMs How long to show the message in milliseconds.
     */
    public void showFeedback(String message, Color color, int durationMs) {
        Platform.runLater(() -> {
            overlayLabel.setText(message);
            // Apply semi-transparent background with rounded corners
            overlayBox.setStyle(
                    "-fx-background-color: " + toFxmlColor(color) + "; -fx-background-radius: 10; -fx-opacity: 0.8;");
            overlayBox.setVisible(true);

            // Hide after duration
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> overlayBox.setVisible(false));
                }
            }, durationMs);
        });
    }

    /**
     * Helper to convert JavaFX Color to CSS hex string.
     */
    private String toFxmlColor(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue() * 255));
    }
}
