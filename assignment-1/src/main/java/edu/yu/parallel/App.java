package edu.yu.parallel;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {

    private final static Logger logger = LogManager.getLogger(App.class);
    private final static String TITLE = "Jackpot";
    private final static int WIDTH = 600;
    private final static int HEIGHT = 400;
    private final static int DFT_NUM_REELS = 3;
    private final static String INITIAL_IMAGE = "/click.png";

    public static void main(String[] args) throws MalformedURLException, URISyntaxException {
        logger.info("Hello today!");

        // Override the default by setting the SLOT_REELS environment
        // variable with a positive integer number
        var numReels = getNumberOfSlotReels();
        logger.info("Using " + String.valueOf(numReels) + " slot reels");

        var initialImage = ImageLoader.loadImageFromResources(INITIAL_IMAGE);

        // Create 3 slot reels and initialize with the above image
        var slotReels = new SlotReel[numReels];
        for (int i = 0; i < numReels; i++) {
            slotReels[i] = new SlotReel(String.valueOf(i), new InnerPanel(), initialImage);
        }

        // Create the main frame and add the slot reels to it
        MainFrame mainFrame = new MainFrame(TITLE, WIDTH, HEIGHT);
        for (SlotReel slotReel : slotReels) {
            mainFrame.addPanel(slotReel.getPanel());
        }

        mainFrame.showFrame();
        
        logger.info("Goodbye!");
    }

    private static int getNumberOfSlotReels() {
        String numReelsEnv = System.getProperty("SLOT_REELS");
        if (numReelsEnv != null) {
            try {
                int numReels = Integer.parseInt(numReelsEnv);
                if (numReels < 1)
                    throw new IllegalArgumentException(numReelsEnv);
                return numReels;
            } catch (Exception e) {
                logger.info("Invalid number for SLOT_REELS (" + numReelsEnv + "). Using default value.");
                return DFT_NUM_REELS;
            }
        } else {
            return DFT_NUM_REELS;
        }
    }
 }
