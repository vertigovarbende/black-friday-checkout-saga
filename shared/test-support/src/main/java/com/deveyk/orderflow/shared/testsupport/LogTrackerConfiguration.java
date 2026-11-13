package com.deveyk.orderflow.shared.testsupport;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public abstract class LogTrackerConfiguration {

    private static final String ROOT_PACKAGE = "com.deveyk.orderflow";

    protected final LogTracker logTracker = new LogTracker();

    private Level previousLevel;

    @BeforeEach
    protected void setUpLogTracker() {
        this.logTracker.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        this.logTracker.start();
        final Logger logger = rootPackageLogger();
        this.previousLevel = logger.getLevel();
        logger.setLevel(Level.TRACE);
        logger.addAppender(this.logTracker);
    }

    @AfterEach
    protected void tearDownLogTracker() {
        final Logger logger = rootPackageLogger();
        logger.detachAppender(this.logTracker);
        logger.setLevel(this.previousLevel);
        this.logTracker.stop();
    }

    private static Logger rootPackageLogger() {
        return (Logger) LoggerFactory.getLogger(ROOT_PACKAGE);
    }

    protected static class LogTracker extends ListAppender<ILoggingEvent> {

        /** Verilen seviyede, içinde {@code messageFragment} geçen ilk log mesajı. */
        public Optional<String> findMessage(final Level level, final String messageFragment) {
            return this.list.stream()
                    .filter(event -> event.getLevel().equals(level))
                    .map(ILoggingEvent::getFormattedMessage)
                    .filter(formattedMessage -> formattedMessage.contains(messageFragment))
                    .findFirst();
        }
    }
}
