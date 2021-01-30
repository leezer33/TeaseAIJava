package me.goddragon.teaseai.api.media;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javafx.scene.media.Media;
import javafx.scene.media.MediaException;

import me.goddragon.teaseai.TeaseAI;
import me.goddragon.teaseai.utils.TeaseLogger;

public class AudioHandler {
    public void playAudioWithURI(String uriText, boolean waitUntilPlaybackFinished) {
        URI uri;
        try {
            uri = new URI(uriText);
        } catch (URISyntaxException ex) {
            TeaseLogger.getLogger().log(
                    Level.SEVERE, "Cannot parse '" + uriText + "' as a URI: " + ex.getMessage());
            return;
        }

        Media media;
        try {
            media = new Media(uriText);
        } catch (MediaException ex) {
            TeaseLogger.getLogger().log(
                    Level.SEVERE, "Audio format of '" + uri + "' unknown: " + ex.getMessage());
            return;
        }

        final MediaPlayerWrapper playerWrapper =
                new MediaPlayerWrapper(media, null, this::asyncOnPlaybackEnded);
        playingAudioClips.computeIfAbsent(uri, dummy -> new ArrayList<MediaPlayerWrapper>())
                .add(playerWrapper);
        playerWrapper.start();

        if (waitUntilPlaybackFinished) {
            while (playerWrapper.isPlaying()) {
                TeaseAI.application.waitPossibleScripThread(0);
                TeaseAI.application.checkForNewResponses();
            }
        }

        purgeFinishedPlayers();
    }

    public void stopAudio(File file) {
        playingAudioClips.computeIfPresent(file.toURI(), (uri, listOfPlayerWrappers) -> {
            listOfPlayerWrappers.forEach(MediaPlayerWrapper::stop);
            return null;
        });
    }

    public void stopAllAudio() {
        playingAudioClips.forEach(
                (uri, listOfMediaPlayer) -> listOfMediaPlayer.forEach(MediaPlayerWrapper::stop));

        playingAudioClips.clear();
    }

    private void purgeFinishedPlayers() {
        playingAudioClips.forEach((uri, listOfPlayerWrappers)
                                          -> listOfPlayerWrappers.removeIf(
                                                  playerWrapper -> !playerWrapper.isPlaying()));

        playingAudioClips.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private void asyncOnPlaybackEnded() {
        final Thread scriptThread = TeaseAI.getApplication().getScriptThread();
        synchronized (scriptThread) {
            scriptThread.notifyAll();
        }
    }

    private Map<URI, List<MediaPlayerWrapper>> playingAudioClips = new HashMap<>();
}
