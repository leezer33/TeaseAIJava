package me.goddragon.teaseai.api.media;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import javafx.scene.image.ImageView;
import javafx.scene.media.MediaView;

import me.goddragon.teaseai.TeaseAI;
import me.goddragon.teaseai.utils.FileUtils;
import me.goddragon.teaseai.utils.TeaseLogger;
import me.goddragon.teaseai.utils.media.AnimatedGif;
import me.goddragon.teaseai.utils.media.Animation;
import me.goddragon.teaseai.utils.media.ImageUtils;

public class ImageHandler {
    public void showPicture(File file) {
        showPicture(file, 0);
    }

    public void showPicture(File file, int durationSeconds) {
        if (file == null) {
            debugLog("Showing nothing");
            TeaseAI.application.runOnUIThread(this::removePicture);
            return;
        }

        if (!file.exists()) {
            TeaseLogger.getLogger().log(
                    Level.SEVERE, "Picture " + file.getPath() + " does not exist.");
            return;
        }

        currentImageURL = file.getAbsolutePath();

        if (durationSeconds > 0) {
            debugLog("Showing '%s' for %d seconds", currentImageURL, durationSeconds);
        } else {
            debugLog("Showing '%s'", currentImageURL);
        }

        TeaseAI.application.runOnUIThread(() -> {
            MediaView mediaView = TeaseAI.application.getController().getMediaView();

            if (mediaView.getMediaPlayer() != null) {
                mediaView.getMediaPlayer().stop();
            }

            ImageView imageView = TeaseAI.application.getController().getImageView();

            // Handle visibilities
            mediaView.setOpacity(0);
            imageView.setOpacity(1);

            // Stop any current image animation that might be running before displaying a new
            // picture
            stopCurrentAnimation();

            if (FileUtils.getExtension(file).equalsIgnoreCase("gif")) {
                currentAnimation = new AnimatedGif(file.toURI().toString());
                currentAnimation.setCycleCount(Integer.MAX_VALUE);
                currentAnimation.play(imageView);

            } else {
                ImageUtils.setImageInView(file, imageView);
            }
        });

        if (durationSeconds > 0) {
            TeaseAI.application.sleepPossibleScripThread(durationSeconds * 1000);
        }
    }

    public File getImageFromURL(String url) throws IOException {
        currentImageURL = url;

        debugLog("Fetching '%s'", currentImageURL);

        String[] split = url.split("/");
        String path = split[split.length - 1];

        path = MediaURL.IMAGE_DOWNLOAD_PATH + File.separator + path;
        File file = new File(path);

        if (file.exists()) {
            return file;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("Referer", url);
            connection.addRequestProperty("Accept", "*/*");
            connection.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (X11; Linux x86_64; rv:12.0) Gecko/20100101 Firefox/12.0");
            connection.connect();

            final int responseCode = connection.getResponseCode();
            final String resposeMessage = connection.getResponseMessage();
            debugLog("Response code received %d '%s'", responseCode, resposeMessage);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                debugLog("Fetched %,d bytes of type '%s' encoded as '%s'",
                        connection.getContentLength(), connection.getContentType(),
                        connection.getContentEncoding());

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
                debugLog("Unsupported response code, ignoring conent");
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

    public String getCurrentImageURL() {
        return currentImageURL;
    }

    private void removePicture() {
        final ImageView imageView = TeaseAI.application.getController().getImageView();
        stopCurrentAnimation();
        imageView.setImage(null);
    }

    private void stopCurrentAnimation() {
        if (currentAnimation != null) {
            currentAnimation.stop();
            currentAnimation = null;
        }
    }

    private void debugLog(String format, Object... args) {
        TeaseLogger.getLogger().log(Level.FINER, "Image: " + String.format(format, args));
    }

    private Animation currentAnimation = null;
    private String currentImageURL;
}
