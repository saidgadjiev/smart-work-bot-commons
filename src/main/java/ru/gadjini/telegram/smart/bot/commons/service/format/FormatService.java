package ru.gadjini.telegram.smart.bot.commons.service.format;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import ru.gadjini.telegram.smart.bot.commons.utils.MimeTypeUtils;
import ru.gadjini.telegram.smart.bot.commons.utils.UrlUtils;

import static ru.gadjini.telegram.smart.bot.commons.service.format.Format.TEXT;
import static ru.gadjini.telegram.smart.bot.commons.service.format.Format.URL;

@Service
public class FormatService {

    public Format getAssociatedFormat(String format) {
        if ("jpeg".equals(format)) {
            return Format.JPG;
        }
        format = format.toUpperCase();
        for (Format f : Format.values()) {
            if (f.getName().equals(format)) {
                return f;
            }
        }

        return null;
    }

    public String getExt(String fileName, String mimeType) {
        String ext = getExtension(fileName);

        if (StringUtils.isBlank(ext)) {
            return MimeTypeUtils.getExtension(null, mimeType);
        }

        return ext;
    }

    public Format getFormat(String fileName, String mimeType) {
        Format f = tryGetFormatByFileName(fileName);

        if (f == null) {
            return tryGetFormatByMimeType(fileName, mimeType);
        }

        return f;
    }

    private Format tryGetFormatByFileName(String fileName) {
        String extension = getExtension(fileName);

        return findFormat(extension);
    }

    private Format findFormat(String extension) {
        for (Format format : Format.values()) {
            if (!format.isDummy() && format.getExt().equals(extension)) {
                return format;
            }
        }

        return null;
    }

    private Format tryGetFormatByMimeType(String fileName, String mimeType) {
        String extension = MimeTypeUtils.getExtension(getExtension(fileName), mimeType);

        return findFormat(extension);
    }

    private String getExtension(String fileName) {
        String extension = StringUtils.defaultIfBlank(FilenameUtils.getExtension(fileName), "").toLowerCase();

        if ("jpeg".equals(extension)) {
            return "jpg";
        }

        return extension;
    }

    public Format getFormat(String text) {
        if (UrlUtils.isUrl(text)) {
            return URL;
        }

        return TEXT;
    }
}
