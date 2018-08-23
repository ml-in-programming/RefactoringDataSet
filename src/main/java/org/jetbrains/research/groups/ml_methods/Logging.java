package org.jetbrains.research.groups.ml_methods;

import org.apache.log4j.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Logging {
    private static final Path LOG_FILE_PATH = Paths.get("./log");

    public static Logger getLogger(Class<?> aClass) {
        final Logger logger = Logger.getLogger(aClass);
        logger.setLevel(Level.DEBUG);
        try {
            logger.addAppender(new FileAppender(new PatternLayout("%p [%c.%M] - %m%n"),
                    LOG_FILE_PATH.toAbsolutePath().toString()));
        } catch (IOException e) {
            System.err.println("Cannot open " + LOG_FILE_PATH.toAbsolutePath().toString() +
                    " file for logger. Reason: " + e.getMessage());
            logger.addAppender(new ConsoleAppender(new PatternLayout("%p [%c.%M] - %m%n")));
        }
        return logger;
    }
}