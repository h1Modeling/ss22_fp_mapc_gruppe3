package de.feu.massim22.group3.utils.logging;

import java.util.logging.*;
import java.util.Set;
import java.util.Date;
import java.util.HashSet;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileAlreadyExistsException;
import java.io.IOException;


/**
 * Class that extends the functionality of the java.util.logging module to enable that specific
 * log-files can be generated depending on a given string (e. g. one for each agent
 * and/or desire).
 * 
 * @author Phil Heger
 *
 */

// Loglevels: SEVERE, WARNING, INFO, CONFIG, FINE
// not implemented: FINER, FINEST

public class AgentLogger {

    // Logger configuration
    private static boolean WRITE_SEPARATE_FILES = true;
    // Level for console output and main log file
    // To print all messages: Level.ALL
    // Also sets max. Level for the separate files!
    private static Level MAIN_LOG_LEVEL = Level.ALL;
    // Level for separate (filtered) log files
    private static Level FILES_LOG_LEVEL = Level.ALL;

    private static Logger logger = null;
    // tags given with logging statements to specify file name of log file
    private static Set<String> tags = new HashSet<>();

    // Output Format definition
    private static final Formatter simpleFormat = new SimpleFormatter() {
        private static final String format = "[%1$tT:%1$tL] [%2$-7s] %3$s\n";

        @Override
        public synchronized String format(LogRecord lr) {
            return String.format(format, new Date(lr.getMillis()), lr.getLevel().getName(), lr.getMessage());
        }
    };

    static {
        // get logger
        logger = Logger.getLogger("de.feu.massim22.group3");
        logger.setLevel(MAIN_LOG_LEVEL);

        logger.setUseParentHandlers(false);

        // Define Console output
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(MAIN_LOG_LEVEL);
        ch.setFormatter(simpleFormat);
        logger.addHandler(ch);

        // Write output of the main logger to file
        try {
            try {
                Path path = Paths.get("logs");
                Files.createDirectory(path);
            } catch (FileAlreadyExistsException ignored) {
            }

            FileHandler fh = new FileHandler("logs/AgentLogger.log");
            logger.addHandler(fh);
            fh.setFormatter(simpleFormat);
            fh.setLevel(MAIN_LOG_LEVEL);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    static private void addFileHandler(String tag) {
        try {
            FileHandler fh = new FileHandler("logs/" + tag + ".log");
            fh.setFilter(record -> record.getMessage().contains(tag));
            fh.setFormatter(simpleFormat);
            logger.addHandler(fh);
            fh.setLevel(FILES_LOG_LEVEL);
            tags.add(tag);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Generate logging output of log level <code>fine</code> and write it to a designated log-file
     * 
     * @param tag name of the log-file into which the output is to be written
     * @param logMessage Message to be written to log-output
     */
    static public void fine(String tag, String logMessage) {
        logger.fine("[" + tag + "] " + logMessage);
        if (WRITE_SEPARATE_FILES && !tags.contains(tag)) {
            addFileHandler(tag);
        }
    }

    /**
     * Generate logging output of log level <code>fine</code>
     * 
     * @param logMessage Message to be written to log-output
     */
    static public void fine(String logMessage) {
        logger.fine(logMessage);
    }

    /**
     * Generate logging output of log level <code>config</code> and write it to a designated log-file
     * 
     * @param tag name of the log-file into which the output is to be written
     * @param logMessage Message to be written to log-output
     */
    static public void config(String tag, String logMessage) {
        logger.config("[" + tag + "] " + logMessage);
        if (WRITE_SEPARATE_FILES && !tags.contains(tag)) {
            addFileHandler(tag);
        }
    }

    /**
     * Generate logging output of log <code>config</code> fine
     * 
     * @param logMessage Message to be written to log-output
     */
    static public void config(String logMessage) {
        logger.config(logMessage);
    }

    /**
     * Generate logging output of log level <code>info</code> and write it to a designated log-file
     * 
     * @param tag name of the log-file into which the output is to be written
     * @param logMessage Message to be written to log-output
     */
    static public void info(String tag, String logMessage) {
        logger.info("[" + tag + "] " + logMessage);
        if (WRITE_SEPARATE_FILES && !tags.contains(tag)) {
            addFileHandler(tag);
        }
    }

    /**
     * Generate logging output of log level <code>info</code>
     * 
     * @param logMessage Message to be written to log-output
     */
    static public void info(String logMessage) {
        logger.info(logMessage);
    }

    /**
     * Generate logging output of log level <code>warning</code> and write it to a designated log-file
     * 
     * @param tag name of the log-file into which the output is to be written
     * @param logMessage Message to be written to log-output
     */
    static public void warning(String tag, String logMessage) {
        logger.warning("[" + tag + "] " + logMessage);
        if (WRITE_SEPARATE_FILES && !tags.contains(tag)) {
            addFileHandler(tag);
        }
    }

    /**
     * Generate logging output of log level <code>warning</code>
     * 
     * @param logMessage Message to be written to log-output
     */
    static public void warning(String logMessage) {
        logger.warning(logMessage);
    }

    /**
     * Generate logging output of log level <code>severe</code> and write it to a designated log-file
     * 
     * @param tag name of the log-file into which the output is to be written
     * @param logMessage Message to be written to log-output
     */
    static public void severe(String tag, String logMessage) {
        logger.severe("[" + tag + "] " + logMessage);
        if (WRITE_SEPARATE_FILES && !tags.contains(tag)) {
            addFileHandler(tag);
        }
    }

    /**
     * Generate logging output of log level <code>severe</code>
     * 
     * @param logMessage Message to be written to log-output
     */
    static public void severe(String logMessage) {
        logger.severe(logMessage);
    }
}