package me.goddragon.teaseai.api.media;

import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaView;

import me.goddragon.teaseai.TeaseAI;

public final class ViewSwitcher {
    public static void setViewToImages() {
    }

    public static void setViewToVideo(MediaPlayerWrapper mediaPlayerWrapper) {
        TeaseAI.application.runOnUIThread(() -> {
            final MediaView mediaView = TeaseAI.application.getController().getMediaView();
            final StackPane mediaViewBox = TeaseAI.application.getController().getMediaViewBox();

            // Handle visibilities
            mediaView.setOpacity(1);
            TeaseAI.application.getController().getImageView().setOpacity(0);

            mediaView.setPreserveRatio(true);
            mediaView.fitWidthProperty().bind(mediaViewBox.widthProperty());
            mediaView.fitHeightProperty().bind(mediaViewBox.heightProperty());
            mediaPlayerWrapper.bindToMediaView(mediaView);
        });
    }

    private ViewSwitcher() {
    }
}
