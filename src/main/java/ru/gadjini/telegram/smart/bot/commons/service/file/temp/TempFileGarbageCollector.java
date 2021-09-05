package ru.gadjini.telegram.smart.bot.commons.service.file.temp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.gadjini.telegram.smart.bot.commons.io.SmartTempFile;

import java.util.ArrayList;
import java.util.List;

@Component
public class TempFileGarbageCollector {

    private TempFileService tempFileService;

    @Autowired
    public TempFileGarbageCollector(TempFileService tempFileService) {
        this.tempFileService = tempFileService;
    }

    public GarbageFileCollection getNewCollection() {
        return new GarbageFileCollection();
    }

    public class GarbageFileCollection {

        private List<SmartTempFile> garbageFiles = new ArrayList<>();

        public void addFile(SmartTempFile file) {
            garbageFiles.add(file);
        }

        public void delete() {
            garbageFiles.forEach(f -> tempFileService.delete(f));
        }
    }
}
