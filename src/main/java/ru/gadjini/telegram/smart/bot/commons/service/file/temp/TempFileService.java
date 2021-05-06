package ru.gadjini.telegram.smart.bot.commons.service.file.temp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;
import ru.gadjini.telegram.smart.bot.commons.property.BotProperties;
import ru.gadjini.telegram.smart.bot.commons.property.ServerProperties;
import ru.gadjini.telegram.smart.bot.commons.service.ContentsApi;
import ru.gadjini.telegram.smart.bot.commons.utils.SmartFileUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class TempFileService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TempFileService.class);

    private Map<FileTarget, TempDirectoryService> fileTypeServices = new HashMap<>();

    @Value("${temp.dir:#{systemProperties['java.io.tmpdir']}}")
    private String tempDir;

    @Value("${downloads.temp.dir:#{systemProperties['java.io.tmpdir']}}")
    private String downloadsTempDir;

    @Value("${uploads.temp.dir:#{systemProperties['java.io.tmpdir']}}")
    private String uploadsTempDir;

    private ServerProperties serverProperties;

    private BotProperties botProperties;

    private ContentsApi contentApi;

    @Autowired
    public TempFileService(ServerProperties serverProperties,
                           BotProperties botProperties, ContentsApi contentApi) {
        this.serverProperties = serverProperties;
        this.botProperties = botProperties;
        this.contentApi = contentApi;
    }

    @PostConstruct
    public void init() {
        tempDir = mkdirsAndGet(tempDir, botProperties.getName());
        downloadsTempDir = mkdirsAndGet(downloadsTempDir, botProperties.getName());
        uploadsTempDir = mkdirsAndGet(uploadsTempDir, botProperties.getName());

        LOGGER.debug("Temp dir({},{},{})", tempDir, downloadsTempDir, uploadsTempDir);

        fileTypeServices.put(FileTarget.TEMP, new TempDirectoryService(tempDir));
        fileTypeServices.put(FileTarget.DOWNLOAD, new TempDirectoryService(downloadsTempDir));
        fileTypeServices.put(FileTarget.UPLOAD, new TempDirectoryService(uploadsTempDir));
    }

    public String getRootDir(FileTarget tempFileType) {
        return fileTypeServices.get(tempFileType).getRootDir();
    }

    public FileTarget getFileTarget(String filePath) {
        if (filePath.startsWith(uploadsTempDir)) {
            return FileTarget.UPLOAD;
        } else if (filePath.startsWith(downloadsTempDir)) {
            return FileTarget.DOWNLOAD;
        } else {
            return FileTarget.TEMP;
        }
    }

    public SmartTempFile createTempDir(FileTarget tempFileType, long chatId, String tag) {
        return fileTypeServices.get(tempFileType).createTempDir(chatId, tag);
    }

    public String getTempDir(FileTarget tempFileType, long chatId, String tag) {
        return fileTypeServices.get(tempFileType).getTempDir(chatId, tag);
    }

    public SmartTempFile getTempFile(FileTarget tempFileType, long chatId, String fileId, String tag, String ext) {
        return fileTypeServices.get(tempFileType).getTempFile(chatId, fileId, tag, ext);
    }

    public SmartTempFile getTempFile(FileTarget tempFileType, long chatId, String tag, String ext) {
        return getTempFile(tempFileType, chatId, null, tag, ext);
    }

    public String getTempFile(FileTarget tempFileType, String parent, long chatId, String fileId, String tag, String ext) {
        return fileTypeServices.get(tempFileType).getTempFile(parent, chatId, fileId, tag, ext);
    }

    public SmartTempFile createTempFile(FileTarget tempFileType, SmartTempFile parent, long chatId, String fileName) {
        return fileTypeServices.get(tempFileType).createTempFile(parent, chatId, fileName);
    }

    public SmartTempFile createTempFile(FileTarget tempFileType, SmartTempFile parent, long chatId, String fileId, String tag, String ext) {
        return fileTypeServices.get(tempFileType).createTempFile(parent, chatId, fileId, tag, ext);
    }

    public SmartTempFile createTempFile(FileTarget tempFileType, long chatId, String fileId, String tag, String ext) {
        return fileTypeServices.get(tempFileType).createTempFile(chatId, fileId, tag, ext);
    }

    public SmartTempFile createTempFile(FileTarget tempFileType, long chatId, String tag, String ext) {
        return createTempFile(tempFileType, chatId, null, tag, ext);
    }

    public SmartTempFile createTempFile(FileTarget tempFileType, String tag, String ext) {
        return createTempFile(tempFileType, 0, null, tag, ext);
    }

    public void delete(SmartTempFile file) {
        if (file == null) {
            return;
        }
        try {
            if (isRemoteFile(file.getAbsolutePath())) {
                contentApi.delete(file);
            }

            file.smartDelete();
            LOGGER.debug("Delete({})", file.getAbsolutePath());
        } catch (Throwable e) {
            LOGGER.error("Error delete({}, {})", file.getAbsolutePath(), e.getMessage());
        }
    }

    private boolean isRemoteFile(String filePath) {
        return !serverProperties.isPrimaryServer() && filePath.startsWith(downloadsTempDir);
    }

    private String mkdirsAndGet(String parent, String child) {
        File file = new File(parent, child);
        SmartFileUtils.mkdirs(file);

        return file.getAbsolutePath();
    }
}
