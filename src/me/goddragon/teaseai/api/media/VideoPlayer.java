package me.goddragon.teaseai.api.media;

import javafx.scene.media.Media;

import me.goddragon.teaseai.TeaseAI;

public class VideoPlayer {
    private MediaPlayerWrapper playerWrapper;
    private Runnable asyncOnVideoPlaybackEnded;

    public VideoPlayer(Runnable asyncOnVideoPlaybackEnded) {
        this.asyncOnVideoPlaybackEnded = asyncOnVideoPlaybackEnded;
    }

    public void play(Media media, boolean waitUntilPlaybackFinished) {
        if (playerWrapper != null) {
            playerWrapper.stop();
        }

        playerWrapper = new MediaPlayerWrapper(media, this::asyncOnPlaybackEnded);
        ViewSwitcher.setViewToVideo(playerWrapper);
        playerWrapper.start();

        if (waitUntilPlaybackFinished) {
            while (playerWrapper.isPlaying()) {
                TeaseAI.application.waitPossibleScripThread(0);
                TeaseAI.application.checkForNewResponses();
            }
        }
    }

    public void stop() {
        playerWrapper.stop();
        ViewSwitcher.setViewToImages();
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
}
