package ru.gadjini.telegram.smart.bot.commons.service.file.temp;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.SecureRandom;

public class TempDirectoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileService.class);

    private static final SecureRandom RANDOM = new SecureRandom();

    private String tempDir;

    public TempDirectoryService(String tempDir) {
        this.tempDir = tempDir;
    }

    public String getRootDir() {
        return tempDir;
    }

    public SmartTempFile createTempDir(long chatId, String tag) {
        try {
            File dir = new File(tempDir, generateDirName(chatId, tag));
            Files.createDirectory(dir.toPath());

            return new SmartTempFile(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getTempDir(long chatId, String tag) {
        File dir = new File(tempDir, generateDirName(chatId, tag));

        return dir.getAbsolutePath();
    }

    public SmartTempFile getTempFile(long chatId, String fileId, String tag, String ext) {
        String filePath = getTempFile(tempDir, chatId, fileId, tag, ext);

        return new SmartTempFile(new File(filePath));
    }

    public SmartTempFile getTempFile(long chatId, String tag, String ext) {
        return getTempFile(chatId, null, tag, ext);
    }

    public String getTempFile(String parent, long chatId, String fileId, String tag, String ext) {
        File file = new File(parent, generateFileName(chatId, fileId, tag, ext));

        LOGGER.debug("Get({})", file.getAbsolutePath());
        return file.getAbsolutePath();
    }

    public SmartTempFile createTempFile(SmartTempFile parent, long chatId, String fileName) {
        try {
            File file = new File(parent.getFile(), fileName);
            Files.createFile(file.toPath());
            file.setReadable(true, false);
            file.setWritable(true, false);

            LOGGER.debug("Create({}, {})", chatId, file.getAbsolutePath());
            return new SmartTempFile(file, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SmartTempFile createTempFile(SmartTempFile parent, long chatId, String fileId, String tag, String ext) {
        try {
            File file = new File(parent.getFile(), generateFileName(chatId, fileId, tag, ext));
            Files.createFile(file.toPath());
            file.setReadable(true, false);
            file.setWritable(true, false);

            LOGGER.debug("Create({}, {}, {})", chatId, fileId, file.getAbsolutePath());
            return new SmartTempFile(file, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SmartTempFile createTempFile(long chatId, String fileId, String tag, String ext) {
        try {
            File file = new File(tempDir, generateFileName(chatId, fileId, tag, ext));
            Files.createFile(file.toPath());
            file.setReadable(true, false);
            file.setWritable(true, false);

            LOGGER.debug("Create({}, {}, {})", chatId, fileId, file.getAbsolutePath());
            return new SmartTempFile(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SmartTempFile createTempFile(long chatId, String tag, String ext) {
        return createTempFile(chatId, null, tag, ext);
    }

    public SmartTempFile createTempFile(String tag, String ext) {
        return createTempFile(0, null, tag, ext);
    }

    public String generateFileName(long chatId, String fileId, String tag, String ext) {
        tag = StringUtils.defaultIfBlank(tag, "-");
        ext = StringUtils.defaultIfBlank(ext, "tmp");
        fileId = StringUtils.defaultIfBlank(fileId, "-");
        long n = RANDOM.nextLong();

        return "tag_" + tag + "_chatId_" + chatId + "_fileId_" + fileId + "_time_" + System.nanoTime() + "_salt_" + Long.toUnsignedString(n) + "." + ext;
    }

    public String generateDirName(long chatId, String tag) {
        tag = StringUtils.defaultIfBlank(tag, "-");
        long n = RANDOM.nextLong();

        return "tag_" + tag + "_chatId_" + chatId + "_time_" + System.currentTimeMillis() + "_salt_" + Long.toUnsignedString(n);
    }
}
