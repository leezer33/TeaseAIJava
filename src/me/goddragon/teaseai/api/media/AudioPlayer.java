package me.goddragon.teaseai.api.media;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.media.Media;

import me.goddragon.teaseai.TeaseAI;

public class AudioPlayer {
    private Map<URI, List<MediaPlayerWrapper>> playingAudioClips = new HashMap<>();

    public void playAudioWithURI(String uri, boolean waitUntilPlaybackFinished) {
        final MediaPlayerWrapper playerWrapper = new MediaPlayerWrapper(new Media(uri), null);
        playingAudioClips
                .computeIfAbsent(new URI(uri), dummy -> new ArrayList<MediaPlayerWrapper>())
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
        playingAudioClips.forEach(
                (uri, listOfPlayerWrappers)
                        -> listOfPlayerWrappers.removeIf(
                                playerWrapper -> !playerWrapper.isPlaying()));
    }
}
