### COM-3820 Parallel Programming
# Assignment #1: Jackpot!



## Background and Motivation

The motivation for this assignment is twofold:

First, for you to internalize the fact that "without threading, we are functionally blocked from implementing many applications." Briefly: any Java application that must "do more than one thing" must use threads. (This is independent of the topic we'll be focusing on this semester: doing multiple things simultaneously, in parallel, on multiple cores.) Here, even on a single-core machine, we need threads to enable the application to be concurrent: i.e., to be structured such that the OS can time-slice independent parts of the application so that they appear to be executing simultaneously. In this assignment, in order to spin our slot machine, we must update the individual reels so that they appear to spin concurrently.

Second, in order to stop, reset, or close your multi-threaded application and have it exit properly, you will need to be able to properly control your threads. As we will discuss in lecture, you will need to interrupt the running threads and have those threads properly respond to and handle the interruption request.

The assignment is designed for you to engage with the Java Thread material. Although you certainly don't need to be an "expert" for this assignment, I suggest that you take a look at the Java Concurrency Tutorial, specifically the section on Thread Objects and interruption.



## Requirements

You will complete a simple slot machine application. Begin by cloning the course [Git repository](https://github.com/Yeshiva-University-CS/COM3820-F26), open the Maven project in your favorite IDE, and ensure that you can build and run the application. From the command line, you can build and run the Maven project like so:

```
assignment-1> mvn clean package
assignment-1> java -jar ./target/jackpot-1.jar
```

1. Load the list of MLB team logo images using the provided helper class and log the number of images you loaded:

   ```
   12:21:37.558 [main] INFO - Hello today!
   12:21:37.559 [main] INFO - Using 3 slot reels
   12:21:37.855 [main] INFO - Loaded 30 images
   ```

2. When the **Spin** button is pressed, each slot reel will cycle through images in a different order, displaying each image for 50 milliseconds before moving on to the next image. The text of the button will also change to read "Stop". You must also log that the button was pressed:

   ```
   12:47:09.119 [AWT-EventQueue-0] INFO - Spin button pressed
   ```

   - **a.** If no button is pressed, each slot reel will continue to cycle through the images for a random amount of time between 2 and 4 seconds. When an individual slot reel's time elapses, the reel will remain displaying the logo it had reached, and the program should log a completion message:

     ```
     12:21:43.488 [Thread-1] INFO - Slot reel #0 completed
     12:21:44.197 [Thread-2] INFO - Slot reel #1 completed
     12:21:44.209 [Thread-3] INFO - Slot reel #2 completed
     ```

     (Note: the slot numbers should correspond to slot reels going from left to right.)

   - **b.** If the **Stop** button is pressed while one or more slot reels is spinning, those slot reels which are spinning must stop cycling through the images and log that they were interrupted, as opposed to completing. (See the next items — the same applies to the **Reset** and **Close** buttons.)

     ```
     12:47:09.119 [AWT-EventQueue-0] INFO - Spin button pressed
     12:47:11.234 [Thread-10] INFO - Slot reel #1 completed
     12:47:11.600 [AWT-EventQueue-0] INFO - Stop button pressed
     12:47:11.600 [Thread-11] INFO - Slot reel #2 was interrupted
     12:47:11.605 [Thread-9] INFO - Slot reel #0 was interrupted
     ```
   - **c.** Once the reels have stopped -- either because the **Stop** or **Reset** button was pressed, or the reels have all completed -- the text of the button must revert back to "Spin".
     
3. When the **Reset** button is pressed, the slot reels are reset to display the initial game logo. As noted above, if any of the reels are in the midst of spinning, they must interrupt themselves and not wait until their random cycling time completes. You must also log that the **Reset** button was pressed:

   ```
   12:56:29.521 [AWT-EventQueue-0] INFO - Spin button pressed
   12:56:32.093 [Thread-14] INFO - Slot reel #1 completed
   12:56:32.312 [Thread-15] INFO - Slot reel #2 completed
   12:56:32.441 [AWT-EventQueue-0] INFO - Reset button pressed
   12:56:32.442 [Thread-13] INFO - Slot reel #0 was interrupted
   ```

4. When the **Close** button is pressed, close the window and the application should shut down. As noted above, if any of the reels are in the midst of spinning, they must interrupt themselves and not wait until their random cycling time completes. You must also log that the **Close** button was pressed.

   ```
   13:02:11.883 [AWT-EventQueue-0] INFO - Spin button pressed
   13:02:11.884 [Thread-14] INFO - Slot reel #1 completed
   13:02:11.885 [Thread-15] INFO - Slot reel #2 completed
   13:02:11.886 [AWT-EventQueue-0] INFO - Close button pressed
   13:02:11.887 [Thread-13] INFO - Slot reel #0 was interrupted
   13:02:11.888 [main] INFO - Goodbye!
   ```

5. You will notice in the original application stub provided to you that the *Goodbye* message is logged immediately when the program starts. As you can see, the *main* function is ending, but the application does not close. This is because the AWT UI thread is a user thread and is still alive. Only after disposing of the window on a close event does the application complete.

   You are to arrange your code so that the *main* function does not complete until the window is closed. That is, you should not log the *Goodbye* message until after the **Close** button has been pressed.

   ```
   13:02:11.887 [AWT-EventQueue-0] INFO - Close button pressed
   13:02:11.888 [main] INFO - Goodbye!
   ```

6. The program is written in a flexible way such that you can specify the number of reels on your slot machine using a Java property. The following command line will start a Jackpot application with 4 reels:

   ```
   assignment-1> java -DSLOT_REELS=4 -jar ./target/jackpot-1.jar
   ```

   When writing your code, you will do so in a generic way so that it will work with any number of slot reels.



## Additional Instructions

**For this assignment, you may <u>not</u> use any code from the `java.util.concurrent` package.**

**For this assignment, you should <u>not</u> use any AI code generation. Learn the basic threading on your own.**



## Submission

**Git**

- All submitted assignments must be checked into the [YU GitHub system](https://github.com/Yeshiva-University-CS), into the **PP-COM3820** branch that you created for this course.
- This assignment will be checked into the `/assignment-1` folder. That is, your `pom.xml` file will be in this directory.

