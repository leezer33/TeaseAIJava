package me.goddragon.teaseai.api.media;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;

import me.goddragon.teaseai.TeaseAI;
import me.goddragon.teaseai.utils.TeaseLogger;

public class AudioHandler {
    public void playAudio(File file, boolean waitUntilFinishedPlaying) {
        if (file == null || !file.exists()) {
            TeaseLogger.getLogger().log(Level.SEVERE,
                    "Audio " + (file == null ? "null" : file.getPath()) + " does not exist.");
            return;
        }

        try {
            playAudioWithURI(file.toURI().toURL().toExternalForm(), waitUntilFinishedPlaying);
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void playAudioWithURI(String uriText, boolean waitUntilFinishedPlaying)
            throws URISyntaxException {
        final MediaPlayer mediaPlayer = new MediaPlayer(new Media(uriText));
        final URI uri = new URI(uriText);

        mediaPlayer.setOnStopped(() -> {
            debugLog("Stopped '%s'", uri);
            mediaPlayer.dispose();
        });
        mediaPlayer.setOnEndOfMedia(() -> {
            debugLog("Finished '%s'", uri);
            mediaPlayer.dispose();
        });

        debugLog("Playing '%s'", uri);
        mediaPlayer.play();

        purgeDisposedMediaPlayers();

        if (waitUntilFinishedPlaying) {
            while (mediaPlayer.getStatus() != Status.DISPOSED) {
                TeaseAI.application.waitPossibleScripThread(10);
                TeaseAI.application.checkForNewResponses();
            }
        } else {
            playingAudioClips.computeIfAbsent(uri, dummyUri -> new ArrayList<>()).add(mediaPlayer);
        }
    }

    public void stopAudio(File file) {
        playingAudioClips.computeIfPresent(file.toURI(), (uri, listOfMediaPlayer) -> {
            debugLog("Stopping '%s'", uri);
            listOfMediaPlayer.forEach(MediaPlayer::stop);
            return null;
        });
    }

    public void stopAllAudio() {
        playingAudioClips.forEach((uri, listOfMediaPlayer) -> {
            debugLog("Stopping '%s'", uri);
            listOfMediaPlayer.forEach(MediaPlayer::stop);
        });

        playingAudioClips.clear();
    }

    private void purgeDisposedMediaPlayers() {
        playingAudioClips.forEach(
                (uri, listOfMediaPlayer)
                        -> listOfMediaPlayer.removeIf(
                                mediaPlayer -> mediaPlayer.getStatus() == Status.DISPOSED));
        playingAudioClips.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private void debugLog(String format, Object... args) {
        TeaseLogger.getLogger().log(Level.FINER, "Audio: " + String.format(format, args));
    }

    private final Map<URI, List<MediaPlayer>> playingAudioClips = new HashMap<>();
}
