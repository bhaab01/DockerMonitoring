package com.ca.docker;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LoggerManager {
    private Logger logger;
    
    
    public Logger getLogger() {
        return logger;
    }


    public void setLogger(Logger logger) {
        this.logger = logger;
    }


    public LoggerManager (FileInputStream propFile) {
        LogManager manager = LogManager.getLogManager();

        try {
            if (propFile != null) {
                manager.readConfiguration(propFile);
            }
        } catch (IOException e) {
            System.err.println("Error in setting up logger: " + e);
        }

        logger = Logger.getLogger(this.getClass().getName());
        logger.setUseParentHandlers(false);
        logger.addHandler(new ConsoleHandler());

        try {
            logger.addHandler(new FileHandler());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error adding FileHandler");
        }
        
    }


}
