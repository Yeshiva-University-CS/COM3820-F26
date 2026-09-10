package edu.yu.parallel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class InnerPanel extends JPanel {
    private JLabel imageLabel;
    private ImageIcon originalImageIcon;

    public InnerPanel() {
        setLayout(new BorderLayout());
        setBackground(Color.LIGHT_GRAY);

        imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(100, 100));

        Border imageBorder = BorderFactory.createLineBorder(Color.BLACK);
        imageLabel.setBorder(imageBorder);

        add(imageLabel, BorderLayout.CENTER);

        // Add a component listener to resize the image after the panel is shown
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (originalImageIcon != null) {
                    resizeAndSetImage(originalImageIcon);
                }
            }
        });
    }

    public void setImage(ImageIcon imageIcon) {
        this.originalImageIcon = imageIcon;
        resizeAndSetImage(imageIcon);
    }

    private void resizeAndSetImage(ImageIcon imageIcon) {
        if (getWidth() == 0 || getHeight() == 0) {
            return; // Avoid resizing if the panel size is not yet determined
        }

        // Get the original image
        Image originalImage = imageIcon.getImage();

        // Calculate new dimensions (80% of the panel's size)
        int newWidth = (int) (getWidth() * 0.8);
        int newHeight = (int) (getHeight() * 0.8);

        // Preserve the aspect ratio
        double aspectRatio = (double) originalImage.getWidth(null) / originalImage.getHeight(null);
        if (newWidth / aspectRatio <= newHeight) {
            newHeight = (int) (newWidth / aspectRatio);
        } else {
            newWidth = (int) (newHeight * aspectRatio);
        }

        // Resize the image
        Image resizedImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);

        // Set the resized image
        imageLabel.setIcon(resizedIcon);
        revalidate();
        repaint();
    }
}
