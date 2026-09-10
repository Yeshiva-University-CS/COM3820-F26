package edu.yu.parallel;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MainFrame extends JFrame {
    private final JPanel innerPanelContainer;
    private final SquareLayout squareLayout;
    private JButton spinButton;

    public MainFrame(String title, int width, int height) {
        super(title);
        setLayout(new BorderLayout());

        // Create a panel for the inner panels with SquareLayout
        innerPanelContainer = new JPanel();
        squareLayout = new SquareLayout(10, 20);
        innerPanelContainer.setLayout(squareLayout);

        // Add the inner panel container to the center of the main frame
        add(innerPanelContainer, BorderLayout.CENTER);

        // Create a panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        // Create the Spin button
        this.spinButton = new JButton("Spin");
        buttonPanel.add(spinButton);

        // Create the Reset button
        JButton resetButton = new JButton("Reset");
        buttonPanel.add(resetButton);

        // Create the Close button
        JButton closeButton = new JButton("Close");
        buttonPanel.add(closeButton);

        // Add the button panel to the bottom of the main frame
        add(buttonPanel, BorderLayout.SOUTH);

        // Set frame properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height);
        setLocationRelativeTo(null);
    }

    // Method to add a panel to the inner panel container
    public void addPanel(InnerPanel panel) {
        innerPanelContainer.add(panel);
        innerPanelContainer.revalidate();
        innerPanelContainer.repaint();
    }

    // Method to make the frame visible
    public void showFrame() {
        setVisible(true);
    }
}
