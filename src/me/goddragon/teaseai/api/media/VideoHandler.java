package me.goddragon.teaseai.api.media;

import java.io.File;
import java.net.MalformedURLException;
import java.util.function.Function;
import java.util.logging.Level;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;

import me.goddragon.teaseai.TeaseAI;
import me.goddragon.teaseai.utils.TeaseLogger;

public class VideoHandler {
    public MediaPlayer playVideo(File file, boolean waitUntilFinishedPlaying) {
        if (!file.exists()) {
            TeaseLogger.getLogger().log(
                    Level.SEVERE, "Video " + file.getPath() + " does not exist.");
        }

        try {
            return playVideo(file.toURI().toURL().toExternalForm(), waitUntilFinishedPlaying);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public MediaPlayer playVideo(String uri, boolean waitUntilFinishedPlaying) {
        if (currentVideoPlayer != null) {
            currentVideoPlayer.stop();
            currentVideoPlayer = null;
        }

        final MediaPlayer videoPlayer = new MediaPlayer(new Media(uri));

        videoPlayer.setOnStopped(() -> {
            debugLog("Stopped '%s'", uri);
            videoPlayer.dispose();
            final IObserver currentObserver = observer;
            if (currentObserver != null) {
                currentObserver.asyncOnVideoPlaybackChanged(false);
            }
        });
        videoPlayer.setOnEndOfMedia(() -> {
            debugLog("Finished '%s'", uri);
            videoPlayer.dispose();
            final IObserver currentObserver = observer;
            if (currentObserver != null) {
                currentObserver.asyncOnVideoPlaybackChanged(false);
            }
        });

        videoPlayer.setAutoPlay(true);

        debugLog("Playing '%s'", uri);
        if (observer != null) {
            observer.asyncOnVideoPlaybackChanged(true);
        }

        TeaseAI.application.runOnUIThread(() -> {
            MediaView mediaView = TeaseAI.application.getController().getMediaView();
            StackPane mediaViewBox = TeaseAI.application.getController().getMediaViewBox();

            // Handle visibilities
            mediaView.setOpacity(1);
            TeaseAI.application.getController().getImageView().setOpacity(0);

            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(mediaViewBox.widthProperty());
            mediaView.fitHeightProperty().bind(mediaViewBox.heightProperty());
            mediaView.setMediaPlayer(currentVideoPlayer);
        });

        if (waitUntilFinishedPlaying) {
            while (videoPlayer.getStatus() != Status.DISPOSED) {
                TeaseAI.application.waitPossibleScripThread(10);
                TeaseAI.application.checkForNewResponses();
            }
        } else {
            currentVideoPlayer = videoPlayer;
        }

        return currentVideoPlayer;
    }

    public void stopVideo() {
        if (currentVideoPlayer != null) {
            debugLog("Stopping");
            currentVideoPlayer.stop();
            currentVideoPlayer = null;
        }
    }

    public boolean isPlayingVideo() {
        return (currentVideoPlayer != null) && (currentVideoPlayer.getStatus() != Status.DISPOSED);
    }

    public MediaPlayer getCurrentVideoPlayer() {
        return currentVideoPlayer;
    }

    public void setVideoPlaybackObserver(IObserver observer) {
        this.observer = observer;
    }

    private void debugLog(String format, Object... args) {
        TeaseLogger.getLogger().log(Level.FINER, "Video: " + String.format(format, args));
    }

    public interface IObserver { void asyncOnVideoPlaybackChanged(boolean isPlaying); }

    private MediaPlayer currentVideoPlayer = null;
    private IObserver observer = null;
}
