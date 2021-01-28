package me.goddragon.teaseai.api.scripts.nashorn;

import java.io.File;
import java.util.logging.Level;
import javafx.scene.media.MediaPlayer;

import me.goddragon.teaseai.api.media.MediaHandler;
import me.goddragon.teaseai.utils.FileUtils;
import me.goddragon.teaseai.utils.TeaseLogger;

/**
 * Created by GodDragon on 25.03.2018.
 */
public class PlayVideoFunction extends CustomFunctionExtended {
    public PlayVideoFunction() {
        super("playVideo", "showVideo", "displayVideo");
    }

    @Override
    public boolean isFunction() {
        return true;
    }

    protected MediaPlayer onCall(String pathOrUrl) {
        return onCall(pathOrUrl, false);
    }

    protected MediaPlayer onCall(String pathOrUrl, Boolean waitUntilFinishedPlaying){
        if (isHttpUrl(pathOrUrl)) {
            return MediaHandler.getHandler().playVideo(pathOrUrl, waitUntilFinishedPlaying);
        } else {
            final File file = FileUtils.getRandomMatchingFile(pathOrUrl);
            if (file != null) {
                return MediaHandler.getHandler().playVideo(file, waitUntilFinishedPlaying);
            } else {
                TeaseLogger.getLogger().log(Level.SEVERE,
                        "Matching video file for path " + pathOrUrl + " does not exist.");
            }
        }

        return null;
    }

    private boolean isHttpUrl(String path) {
        final String lowerCasePath = path.toLowerCase();
        return lowerCasePath.startsWith("http://") || lowerCasePath.startsWith("https://");
    }
}
