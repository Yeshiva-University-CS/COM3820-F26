package edu.yu.parallel;

import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class ImageLoader {

    public static ImageIcon loadImageFromResources(String resourceFilePath) {
        URL imageURL = ImageLoader.class.getResource(resourceFilePath);
        if (imageURL == null) {
            throw new IllegalArgumentException("Image not found: " + resourceFilePath);
        }

        try {
            return new ImageIcon(ImageIO.read(imageURL));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
