package edu.yu.parallel;

import javax.swing.ImageIcon;

public class SlotReel {
    private final String id;
    private final InnerPanel panel;
    private final ImageIcon initialImage;
    private ImageIcon currentImage;

    public SlotReel(String id, InnerPanel panel, ImageIcon initialImage) {
        this.id = id;
        this.panel = panel;
        this.initialImage = initialImage;
        setImage(initialImage); // Set the initial image
    }

    public final void setImage(ImageIcon image) {
        this.currentImage = image;
        panel.setImage(image);
    }

    public void reset() {
        setImage(initialImage);
    }

    public String getId() {
        return id;
    }

    public InnerPanel getPanel() {
        return panel;
    }

    public ImageIcon getCurrentImage() {
        return currentImage;
    }
}
