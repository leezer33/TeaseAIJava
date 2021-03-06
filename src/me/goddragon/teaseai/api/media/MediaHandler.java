package me.goddragon.teaseai.api.media;

import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import me.goddragon.teaseai.TeaseAI;
import me.goddragon.teaseai.utils.FileUtils;
import me.goddragon.teaseai.utils.TeaseLogger;
import me.goddragon.teaseai.utils.media.AnimatedGif;
import me.goddragon.teaseai.utils.media.Animation;
import me.goddragon.teaseai.utils.media.ImageUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Created by GodDragon on 22.03.2018.
 */
public class MediaHandler {

    private static MediaHandler handler = new MediaHandler();

    private HashMap<URI, MediaPlayer> playingAudioClips = new HashMap<>();

    private MediaPlayer currentVideoPlayer = null;
    private Animation currentAnimation = null;
    private boolean imagesLocked = false;

    private String currentImageURL;

    public MediaPlayer playVideo(File file) {
        return playVideo(file, false);
    }

    public MediaPlayer playVideo(File file, boolean wait) {
        if (!file.exists()) {
            TeaseLogger.getLogger().log(Level.SEVERE, "Video " + file.getPath() + " does not exist.");
            return null;
        }

        try {
            return playVideo(file.toURI().toURL().toExternalForm(), wait);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public MediaPlayer playVideo(String uri, boolean wait) {
        currentVideoPlayer = new MediaPlayer(new Media(uri));
        currentVideoPlayer.setAutoPlay(true);
        this.imagesLocked = true;

        TeaseAI.application.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                MediaView mediaView = TeaseAI.application.getController().getMediaView();
                StackPane mediaViewBox = TeaseAI.application.getController().getMediaViewBox();

                //Handle visibilities
                mediaView.setOpacity(1);
                TeaseAI.application.getController().getImageView().setOpacity(0);

                mediaView.setPreserveRatio(true);
                mediaView.fitWidthProperty().bind(mediaViewBox.widthProperty());
                mediaView.fitHeightProperty().bind(mediaViewBox.heightProperty());
                mediaView.setMediaPlayer(currentVideoPlayer);
            }
        });

        //Check if we want to wait for the media to finish
        if (wait) {
            waitForPlayer(currentVideoPlayer);
            currentVideoPlayer = null;
        } else {
            //Unlock the images again (of course they can be unlocked by the user during the video)
            currentVideoPlayer.setOnEndOfMedia(new Runnable() {
                @Override
                public void run() {
                    imagesLocked = false;
                    currentVideoPlayer = null;
                }
            });
        }

        return currentVideoPlayer;
    }

    public void stopVideo() {
        currentVideoPlayer.stop();
        currentVideoPlayer = null;
    }

    public void showPicture(File file) {
        showPicture(file, 0);
    }

    public void showPicture(File file, int durationSeconds) {
                if (file == null) {
                    TeaseAI.application.runOnUIThread(new Runnable() {
                        @Override
                        public void run() {
                            removePicture();
                        }
                    });

            return;
        }

        if (!file.exists()) {
            TeaseLogger.getLogger().log(Level.SEVERE, "Picture " + file.getPath() + " does not exist.");
            return;
        }
        currentImageURL = file.getAbsolutePath();
        TeaseAI.application.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                MediaView mediaView = TeaseAI.application.getController().getMediaView();

                if (mediaView.getMediaPlayer() != null) {
                    mediaView.getMediaPlayer().stop();
                }

                ImageView imageView = TeaseAI.application.getController().getImageView();

                //Handle visibilities
                mediaView.setOpacity(0);
                imageView.setOpacity(1);

                //Stop any current image animation that might be running before displaying a new picture
                stopCurrentAnimation();

                if (FileUtils.getExtension(file).equalsIgnoreCase("gif")) {
                    currentAnimation = new AnimatedGif(file.toURI().toString());
                    currentAnimation.setCycleCount(Integer.MAX_VALUE);
                    currentAnimation.play(imageView);

                } else {
                    ImageUtils.setImageInView(file, imageView);
                }
            }
        });

        if (durationSeconds > 0) {
            TeaseAI.application.sleepPossibleScripThread(durationSeconds * 1000);
        }
    }

    private void removePicture() {
        ImageView imageView = TeaseAI.application.getController().getImageView();
        stopCurrentAnimation();
        imageView.setImage(null);
    }

    private void stopCurrentAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
    }

    private MediaPlayer getAudioPlayer(String uri) {
        Media hit = new Media(uri);
        MediaPlayer mediaPlayer = new MediaPlayer(hit);
        return mediaPlayer;
    }

    /*public MediaPlayer playSoundFromFolder(String path) {
        return playSoundFromFolder(path, false);
    }

    public MediaPlayer playSoundFromFolder(String path, boolean wait) {
        return playAudio(new File("Sounds\\" + path), wait);
    }*/

    public MediaPlayer playAudio(String path) {
        return playAudio(path, false);
    }

    public MediaPlayer playAudio(String path, boolean wait) {
        return playAudio(new File(path), wait);
    }

    public MediaPlayer playAudio(File file) {
        return playAudio(file, false);
    }

    public MediaPlayer playAudio(File file, boolean wait) {
        if (file == null || !file.exists()) {
            TeaseLogger.getLogger().log(Level.SEVERE, "Audio " + (file == null ? "null" : file.getPath()) + " does not exist.");
            return null;
        }

        try {
            return playAudioWithURI(file.toURI().toURL().toExternalForm(), wait);
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    public MediaPlayer playAudioWithURI(String uri, boolean wait) throws URISyntaxException {
        MediaPlayer mediaPlayer = getAudioPlayer(uri);
        playingAudioClips.put(new URI(uri), mediaPlayer);
        mediaPlayer.play();

        if (wait) {
            waitForPlayer(mediaPlayer);
        }

        return mediaPlayer;
    }

    public void stopAudio(String path) {
        stopAudio(new File(path));
    }

    public void stopAudio(File file) {
        if (!playingAudioClips.containsKey(file.toURI())) {
            return;
        }

        playingAudioClips.get(file.toURI()).stop();
        playingAudioClips.remove(file.toURI());
    }

    public void stopAllAudio() {
        for (Map.Entry<URI, MediaPlayer> clips : playingAudioClips.entrySet()) {
            clips.getValue().stop();
            playingAudioClips.get(clips.getKey()).stop();
        }
    }

    public File getImageFromURL(String url) throws IOException {
        currentImageURL = url;

        String[] split = url.split("/");
        String path = split[split.length - 1];

        path = MediaURL.IMAGE_DOWNLOAD_PATH + File.separator + path;
        File file = new File(path);

        if (file.exists()) {
            return file;
        }

        TeaseLogger.getLogger().log(
                Level.FINER, String.format("Fetching url '%s'", url));

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("Referer", url);
            connection.addRequestProperty("Accept", "*/*");
            connection.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (X11; Linux x86_64; rv:12.0) Gecko/20100101 Firefox/12.0");
            connection.connect();

            final int responseCode = connection.getResponseCode();
            final String responseMessage = connection.getResponseMessage();
            TeaseLogger.getLogger().log(
                    Level.FINER, String.format("Response code received %d '%s'", responseCode, responseMessage));

            if (responseCode == HttpURLConnection.HTTP_OK) {
                TeaseLogger.getLogger().log(
                    Level.FINER, String.format("Fetched %,d bytes of type '%s' encoded as '%s'",
                        connection.getContentLength(), connection.getContentType(),
                        connection.getContentEncoding()));

                InputStream in = new BufferedInputStream(connection.getInputStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];

                int n;
                while (-1 != (n = in.read(buf))) {
                    out.write(buf, 0, n);
                }
                out.close();
                in.close();

                byte[] response = out.toByteArray();

                FileOutputStream fos = new FileOutputStream(path);
                fos.write(response);
                fos.close();
            } else {
                TeaseLogger.getLogger().log(
                    Level.WARNING, "Unsupported response code, ignoring conent");
            }
        } catch (IOException ex) {
            TeaseLogger.getLogger().log(
                    Level.WARNING, "Unable to find image on url " + url + ": " + ex.getMessage());
        } catch (ClassCastException ex) {
            TeaseLogger.getLogger().log(
                    Level.SEVERE, "Url " + url + " does not appear to be an http connection");
        }

        if (file.exists()) {
            return file;
        }

        return null;
    }

    public void waitForPlayer(MediaPlayer mediaPlayer) {
        final boolean[] hasFinishedPlaying = {false};
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                imagesLocked = false;

                synchronized (TeaseAI.application.getScriptThread()) {
                    TeaseAI.application.getScriptThread().notify();
                }

                hasFinishedPlaying[0] = true;
            }
        });

        while (!hasFinishedPlaying[0]) {
            TeaseAI.application.waitPossibleScripThread(0);

            //Check whether there are new responses to handle
            TeaseAI.application.checkForNewResponses();
        }
    }

    public boolean isPlayingVideo() {
        return currentVideoPlayer != null;
    }

    public MediaPlayer getCurrentVideoPlayer() {
        return currentVideoPlayer;
    }

    public boolean isImagesLocked() {
        return imagesLocked;
    }

    public void setImagesLocked(boolean imagesLocked) {
        this.imagesLocked = imagesLocked;
    }

    public String getCurrentImageURL() {
        return currentImageURL;
    }

    public static MediaHandler getHandler() {
        return handler;
    }

    public static void setHandler(MediaHandler handler) {
        MediaHandler.handler = handler;
    }
}
