package me.goddragon.teaseai.api.media;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaView;

import me.goddragon.teaseai.TeaseAI;
import me.goddragon.teaseai.utils.TeaseLogger;

public class VideoHandler {
    public VideoHandler(Runnable asyncOnVideoPlaybackStarted, Runnable asyncOnVideoPlaybackEnded) {
        this.asyncOnVideoPlaybackStarted = asyncOnVideoPlaybackStarted;
        this.asyncOnVideoPlaybackEnded = asyncOnVideoPlaybackEnded;
    }

    public void play(Media media, boolean waitUntilPlaybackFinished) {
        if (playerWrapper != null) {
            playerWrapper.stop();
        }

        playerWrapper = new MediaPlayerWrapper(media, this::asyncOnPlaybackStarted, this::asyncOnPlaybackEnded);
        switchToVideoView(playerWrapper);
        playerWrapper.start();

        if (waitUntilPlaybackFinished) {
            while (playerWrapper.isPlaying()) {
                TeaseAI.application.waitPossibleScripThread(0);
                TeaseAI.application.checkForNewResponses();
            }
        }
    }

    public void stop() {
        if (playerWrapper != null) {
            playerWrapper.stop();
        }
    }

    public boolean isPlaying() {
        return (playerWrapper != null) && (playerWrapper.isPlaying());
    }

    private static void switchToVideoView(MediaPlayerWrapper playerWrapper) {
        final AtomicBoolean readyFlag = new AtomicBoolean();

        TeaseAI.application.runOnUIThread(() -> {
            final MediaView mediaView = TeaseAI.application.getController().getMediaView();
            final ImageView imageView = TeaseAI.application.getController().getImageView();
            final StackPane mediaViewBox = TeaseAI.application.getController().getMediaViewBox();

            mediaView.setOpacity(1);
            imageView.setOpacity(0);

            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(mediaViewBox.widthProperty());
            mediaView.fitHeightProperty().bind(mediaViewBox.heightProperty());

            playerWrapper.bindToMediaView(mediaView);

            synchronized (readyFlag) {
                readyFlag.set(true);
                readyFlag.notifyAll();
            }
        });

        while (!readyFlag.get()) {
            try {
                synchronized (readyFlag) {
                    readyFlag.wait();
                }
            } catch (InterruptedException ex) {
                TeaseLogger.getLogger().log(Level.WARNING,
                        "Thread interrupted while initialising video user interface");
                Thread.currentThread().interrupt();
            }
        }
    }

    private void asyncOnPlaybackStarted() {
        if (asyncOnVideoPlaybackStarted != null) {
            asyncOnVideoPlaybackStarted.run();
        }
    }

    private void asyncOnPlaybackEnded() {
        if (asyncOnVideoPlaybackEnded != null) {
            asyncOnVideoPlaybackEnded.run();
        }

        final Thread scriptThread = TeaseAI.getApplication().getScriptThread();
        synchronized (scriptThread) {
            scriptThread.notifyAll();
        }
    }

    private MediaPlayerWrapper playerWrapper;
    private final Runnable asyncOnVideoPlaybackStarted;
    private final Runnable asyncOnVideoPlaybackEnded;
}
