package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.HamsterCore;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 调试日志工具类
 * 用于将调试信息输出到独立的日志文件中
 */
public class DebugLogger {
    private static final Logger LOGGER = LogManager.getLogger(DebugLogger.class);
    private static final String DEBUG_LOG_FILE_NAME = "hamstercore_debug.log";
    
    private static PrintStream debugPrintStream;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    static {
        initializeDebugLog();
    }
    
    /**
     * 初始化调试日志文件
     */
    private static void initializeDebugLog() {
        try {
            // 获取Minecraft日志目录
            Path logDir = Paths.get(".", "logs");
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }
            
            Path debugLogFile = logDir.resolve(DEBUG_LOG_FILE_NAME);
            OutputStream fileOutputStream = Files.newOutputStream(debugLogFile);
            debugPrintStream = new PrintStream(fileOutputStream, true);
            
            LOGGER.info("调试日志文件已初始化: {}", debugLogFile.toAbsolutePath());
        } catch (IOException e) {
            LOGGER.error("初始化调试日志文件失败", e);
        }
    }
    
    /**
     * 记录调试信息
     * @param message 调试信息
     */
    public static void log(String message) {
        if (debugPrintStream != null) {
            String timestamp = dateFormat.format(new Date());
            debugPrintStream.println("[" + timestamp + "] [DEBUG] " + message);
            debugPrintStream.flush();
        }
    }
    
    /**
     * 记录调试信息（带格式化）
     * @param format 格式化字符串
     * @param args 参数
     */
    public static void log(String format, Object... args) {
        log(String.format(format, args));
    }
    
    /**
     * 记录对象信息
     * @param obj 对象
     */
    public static void log(Object obj) {
        log(String.valueOf(obj));
    }
    
    /**
     * 记录警告信息
     * @param message 警告信息
     */
    public static void warn(String message) {
        if (debugPrintStream != null) {
            String timestamp = dateFormat.format(new Date());
            debugPrintStream.println("[" + timestamp + "] [WARN] " + message);
            debugPrintStream.flush();
        }
    }
    
    /**
     * 记录警告信息（带格式化）
     * @param format 格式化字符串
     * @param args 参数
     */
    public static void warn(String format, Object... args) {
        warn(String.format(format, args));
    }
    
    /**
     * 记录错误信息
     * @param message 错误信息
     */
    public static void error(String message) {
        if (debugPrintStream != null) {
            String timestamp = dateFormat.format(new Date());
            debugPrintStream.println("[" + timestamp + "] [ERROR] " + message);
            debugPrintStream.flush();
        }
    }
    
    /**
     * 记录错误信息（带格式化）
     * @param format 格式化字符串
     * @param args 参数
     */
    public static void error(String format, Object... args) {
        error(String.format(format, args));
    }
    
    /**
     * 记录错误信息和异常堆栈
     * @param message 错误信息
     * @param throwable 异常对象
     */
    public static void error(String message, Throwable throwable) {
        error(message);
        if (debugPrintStream != null && throwable != null) {
            throwable.printStackTrace(debugPrintStream);
            debugPrintStream.flush();
        }
    }
    
    /**
     * 关闭调试日志流
     */
    public static void close() {
        if (debugPrintStream != null) {
            debugPrintStream.close();
            debugPrintStream = null;
        }
    }
}