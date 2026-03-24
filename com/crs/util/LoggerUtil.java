package com.crs.util;
 
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.*;

public final class LoggerUtil {
    /* Color codes */
    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String CYAN = "\u001B[36m";
    private static final String PURPLE = "\u001B[35m";

    private static final boolean ANSI_SUPPORTED = isAnsiSupported();

    /* Log levels */
    public enum Level { DEBUG, INFO, WARNING, ERROR, ACTION }

    /* Config */
    private volatile Level minimumLevel = Level.DEBUG;
    private volatile boolean fileEnabled = false;
    private volatile String  logFilePath = "logs/crs.log";

    private static final DateTimeFormatter TIMESTAMP_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /*Async queue */
    private final BlockingQueue<String> logQueue = new ArrayBlockingQueue<>(4096);
    private final Thread writerThread;

    /* Singleton instance */
    private static final LoggerUtil INSTANCE = new LoggerUtil();
    public  static LoggerUtil getInstance() { return INSTANCE; }
 
    private LoggerUtil() {
        writerThread = new Thread(this::drainQueue, "crs-log-writer");
        writerThread.setDaemon(true);
        writerThread.start();
    }

    /* Public API */
    public void logDebug(String message) {
        emit(Level.DEBUG, null, message, null);
    }

    public void logInfo(String message) {
        emit(Level.INFO, null, message, null);
    }

    public void logWarning(String message) {
        emit(Level.WARNING, null, message, null);
    }

    public void logError(String message) {
        emit(Level.ERROR, null, message, null);
    }

    public void logError(String message, Throwable throwable) {
        emit(Level.ERROR, null, message, throwable);
    }

    public void logAction(String userId, String action, String details) {
        String msg = String.format("[ACTION] userId=%s action=%s details=%s",
            userId, action, details);
        emit(Level.ACTION, userId, msg, null);
    }

    public void logSecurityEvent(String event, String userId, String details) {
        String msg = String.format("[SECURITY] event=%s userId=%s details=%s",
            event, userId, details);
        emit(Level.ACTION, userId, msg, null);
    }

    /* Configuration setters */
    public void setMinimumLevel(Level level) {
        this.minimumLevel = level;
    }

    public void configureFileOutput(boolean enabled, String logFilePath) {
        this.fileEnabled = enabled;
        if (logFilePath != null && !logFilePath.isBlank())
            this.logFilePath = logFilePath;
        if (enabled) ensureLogDirectory();
    }

    /* Private helpers */
    private void emit(Level level, String userId, String message, Throwable throwable) {
        if (level.ordinal() < minimumLevel.ordinal()) return;
 
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FMT);
        String thread = Thread.currentThread().getName();
        String formatted = format(timestamp, level, thread, message);

        if (!logQueue.offer(formatted)) {
            writeSync(formatted, level);
        }
 
        if (throwable != null) {
            String stackTrace = stackTraceToString(throwable);
            logQueue.offer("          " + stackTrace);
        }
    }
 
    private String format(String ts, Level level, String thread, String message) {
        String levelTag = String.format("%-8s", "[" + level + "]");
        String raw = ts + "  " + levelTag + "  [" + thread + "]  " + message;
 
        if (!ANSI_SUPPORTED) return raw;
 
        String colour = switch (level) {
            case DEBUG -> CYAN;
            case INFO -> GREEN;
            case WARNING -> YELLOW;
            case ERROR -> RED;
            case ACTION -> PURPLE;
        };
        return colour + raw + RESET;
    }
 
    private void drainQueue() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                String entry = logQueue.take();
                writeSync(entry, Level.INFO);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void writeSync(String formatted, Level level) {
        System.out.println(formatted);
 
        if (fileEnabled) {
            try (var writer = new FileWriter(logFilePath, true);
                 var bw = new BufferedWriter(writer)) {
                    bw.write(stripAnsi(formatted));
                bw.newLine();
            } catch (IOException e) {
                System.err.println("LoggerUtil: could not write to log file — " + e.getMessage());
            }
        }
    }

    private static String stripAnsi(String text) {
        return text.replaceAll("\u001B\\[[;\\d]*m", "");
    }

    private void ensureLogDirectory() {
        try {
            Path dir = Paths.get(logFilePath).getParent();
            if (dir != null) Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("LoggerUtil: cannot create log directory — " + e.getMessage());
        }
    }
 
    private static String stackTraceToString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString().trim();
    }
 
    private static boolean isAnsiSupported() {
        String os = System.getProperty("os.name", "").toLowerCase();
        return !os.contains("win") || System.getenv("ANSICON") != null;
    }
}