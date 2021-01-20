package me.goddragon.teaseai.api.media;

import java.io.*;
import java.net.URISyntaxException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import javafx.scene.media.MediaPlayer;
import me.goddragon.teaseai.utils.TeaseLogger;

/**
 * Created by GodDragon on 22.03.2018.
 */
public class MediaHandler {
    private static MediaHandler handler = new MediaHandler();

    public static MediaHandler getHandler() {
        return handler;
    }

    public static void setHandler(MediaHandler handler) {
        MediaHandler.handler = handler;
    }

    private final VideoHandler videoHandler = new VideoHandler();
    private final AudioHandler audioHandler = new AudioHandler();
    private final ImageHandler imageHandler = new ImageHandler();
    private final AtomicBoolean imagesLocked = new AtomicBoolean(false);

    public MediaPlayer playVideo(File file) {
        return playVideo(file, false);
    }

    public MediaPlayer playVideo(File file, boolean waitUntilFinishedPlaying) {
        return videoHandler.playVideo(file, waitUntilFinishedPlaying);
    }

    public MediaPlayer playVideo(String uri, boolean waitUntilFinishedPlaying) {
        return videoHandler.playVideo(uri, waitUntilFinishedPlaying);
    }

    public void stopVideo() {
        videoHandler.stopVideo();
    }

    public boolean isPlayingVideo() {
        return videoHandler.isPlayingVideo();
    }

    public MediaPlayer getCurrentVideoPlayer()
    {
        return videoHandler.getCurrentVideoPlayer();
    }

    public void showPicture(File file) {
        imageHandler.showPicture(file);
    }

    public void showPicture(File file, int durationSeconds) {
        imageHandler.showPicture(file, durationSeconds);
    }

    public File getImageFromURL(String url) throws IOException {
        return imageHandler.getImageFromURL(url);
    }

    public String getCurrentImageURL() {
        return imageHandler.getCurrentImageURL();
    }

    public void playAudio(String path) {
        playAudio(path, false);
    }

    public void playAudio(String path, boolean waitUntilFinishedPlaying) {
        playAudio(new File(path), waitUntilFinishedPlaying);
    }

    public void playAudio(File file) {
        playAudio(file, false);
    }

    public void playAudio(File file, boolean waitUntilFinishedPlaying) {
        audioHandler.playAudio(file, waitUntilFinishedPlaying);
    }

    public void playAudioWithURI(String uri, boolean waitUntilFinishedPlaying)
            throws URISyntaxException {
        audioHandler.playAudioWithURI(uri, waitUntilFinishedPlaying);
    }

    public void stopAudio(String path) {
        stopAudio(new File(path));
    }

    public void stopAudio(File file) {
        audioHandler.stopAudio(file);
    }

    public void stopAllAudio() {
        audioHandler.stopAllAudio();
    }

    public boolean isImagesLocked() {
        return imagesLocked.get();
    }

    public void setImagesLocked(boolean isLocked) {
        debugLog("Images %s by script", isLocked ? "locked" : "unlocked");
        imagesLocked.set(isLocked);
    }

    private MediaHandler() {
        videoHandler.setVideoPlaybackObserver(this::asyncOnVideoPlaybackChanged);
    }

    private void asyncOnVideoPlaybackChanged(boolean isPlayingVideo) {
        debugLog("Images %s because video playback has %s", isPlayingVideo ? "locked" : "unlocked",
                isPlayingVideo ? "started" : "stopped");
        imagesLocked.set(isPlayingVideo);
    }

    private void debugLog(String format, Object... args) {
        TeaseLogger.getLogger().log(Level.FINER, "Media: " + String.format(format, args));
    }
}
