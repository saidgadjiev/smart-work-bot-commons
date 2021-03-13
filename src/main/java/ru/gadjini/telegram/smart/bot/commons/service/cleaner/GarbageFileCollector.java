package ru.gadjini.telegram.smart.bot.commons.service.cleaner;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.FileTarget;
import ru.gadjini.telegram.smart.bot.commons.service.file.temp.TempFileService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class GarbageFileCollector {

    private static final Logger LOGGER = LoggerFactory.getLogger(GarbageFileCollector.class);

    private Set<GarbageAlgorithm> algorithms;

    private TempFileService tempFileService;

    @Autowired
    public GarbageFileCollector(TempFileService tempFileService, Set<GarbageAlgorithm> algorithms) {
        this.tempFileService = tempFileService;
        this.algorithms = algorithms;
    }

    public int clean() {
        int totalDeleted = 0;
        for (FileTarget fileTarget : FileTarget.values()) {
            totalDeleted += clean(fileTarget);
        }

        return totalDeleted;
    }

    private int clean(FileTarget fileTarget) {
        try {
            AtomicInteger counter = new AtomicInteger();
            Files.list(Path.of(tempFileService.getRootDir(fileTarget)))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(file -> {
                        if (deleteIfGarbage(file)) {
                            counter.incrementAndGet();
                        }
                    });

            return counter.get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean deleteIfGarbage(File file) {
        GarbageAlgorithm algorithm = getAlgorithm(file);

        if (algorithm != null) {
            if (algorithm.isGarbage(file)) {
                boolean b = FileUtils.deleteQuietly(file);
                if (!b) {
                    LOGGER.debug("Garbage file not deleted({}, {})", file.getAbsolutePath(), algorithm.getClass().getSimpleName());
                }

                return b;
            }
        } else {
            LOGGER.debug("Algorithm not found({})", file.getAbsolutePath());
        }

        return false;
    }

    private GarbageAlgorithm getAlgorithm(File file) {
        for (GarbageAlgorithm algorithm : algorithms) {
            boolean candidate = algorithm.accept(file);

            if (candidate) {
                return algorithm;
            }
        }

        return null;
    }
}
