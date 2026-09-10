package edu.yu.parallel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    private final static Logger logger = LogManager.getLogger(AppTest.class);

    @BeforeEach
    void setUp() {
        logger.info("Before");
    }

    @AfterEach
    void tearDown() {
        logger.info("Before");        
    }

    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }
}
