package ru.gadjini.telegram.smart.bot.commons.service.format;

import java.util.ArrayList;
import java.util.List;

public enum Format {

    PPTX(FormatCategory.DOCUMENTS),
    PPT(FormatCategory.DOCUMENTS),
    PPTM(FormatCategory.DOCUMENTS),
    POTX(FormatCategory.DOCUMENTS),
    POT(FormatCategory.DOCUMENTS),
    POTM(FormatCategory.DOCUMENTS),
    PPS(FormatCategory.DOCUMENTS),
    PPSX(FormatCategory.DOCUMENTS),
    PPSM(FormatCategory.DOCUMENTS),
    XLSX(FormatCategory.DOCUMENTS),
    XLS(FormatCategory.DOCUMENTS),
    DOC(FormatCategory.DOCUMENTS),
    DOCX(FormatCategory.DOCUMENTS),
    RTF(FormatCategory.DOCUMENTS),
    PDF(FormatCategory.DOCUMENTS),
    PDF_LOSSYY(FormatCategory.DOCUMENTS) {
        @Override
        public String getExt() {
            return PDF.getExt();
        }

        @Override
        public boolean isDummy() {
            return true;
        }
    },
    PNG(FormatCategory.IMAGES),
    HEIC(FormatCategory.IMAGES),
    HEIF(FormatCategory.IMAGES),
    ICO(FormatCategory.IMAGES),
    SVG(FormatCategory.IMAGES),
    JPG(FormatCategory.IMAGES),
    JP2(FormatCategory.IMAGES),
    BMP(FormatCategory.IMAGES),
    TXT(FormatCategory.DOCUMENTS),
    TIFF(FormatCategory.IMAGES),
    EPUB(FormatCategory.DOCUMENTS),
    WEBP(FormatCategory.IMAGES),
    PHOTO(FormatCategory.IMAGES),
    TGS(FormatCategory.IMAGES),
    GIF(FormatCategory.IMAGES),
    STICKER(FormatCategory.IMAGES) {
        @Override
        public String getExt() {
            return getAssociatedFormat().getExt();
        }

        @Override
        public boolean isDummy() {
            return true;
        }

        @Override
        public Format getAssociatedFormat() {
            return WEBP;
        }
    },
    HTML(FormatCategory.WEB),
    HTMLZ(FormatCategory.WEB),
    URL(FormatCategory.WEB) {
        @Override
        public boolean isDownloadable() {
            return false;
        }

        @Override
        public boolean isDummy() {
            return true;
        }

        @Override
        public boolean isUserSelectable() {
            return false;
        }
    },
    TEXT(FormatCategory.DOCUMENTS) {
        @Override
        public boolean isDownloadable() {
            return false;
        }

        @Override
        public boolean isDummy() {
            return true;
        }

        @Override
        public boolean isUserSelectable() {
            return false;
        }
    },
    ZIP(FormatCategory.ARCHIVE),
    RAR(FormatCategory.ARCHIVE),
    AZW(FormatCategory.DOCUMENTS),
    AZW3(FormatCategory.DOCUMENTS),
    AZW4(FormatCategory.DOCUMENTS),
    CBZ(FormatCategory.DOCUMENTS),
    CBR(FormatCategory.DOCUMENTS),
    CBC(FormatCategory.DOCUMENTS),
    CHM(FormatCategory.DOCUMENTS),
    DJVU(FormatCategory.DOCUMENTS),
    FB2(FormatCategory.DOCUMENTS),
    FBZ(FormatCategory.DOCUMENTS),
    LIT(FormatCategory.DOCUMENTS),
    LRF(FormatCategory.DOCUMENTS),
    MOBI(FormatCategory.DOCUMENTS),
    ODT(FormatCategory.DOCUMENTS),
    PRC(FormatCategory.DOCUMENTS),
    PDB(FormatCategory.DOCUMENTS),
    PML(FormatCategory.DOCUMENTS),
    RB(FormatCategory.DOCUMENTS),
    SNB(FormatCategory.DOCUMENTS),
    TCR(FormatCategory.DOCUMENTS),
    TXTZ(FormatCategory.DOCUMENTS),
    OEB(FormatCategory.DOCUMENTS),
    PMLZ(FormatCategory.DOCUMENTS),
    CSV(FormatCategory.DOCUMENTS),
    ODP(FormatCategory.DOCUMENTS),
    XPS(FormatCategory.DOCUMENTS),
    XML(FormatCategory.DOCUMENTS),
    SWF(FormatCategory.DOCUMENTS),
    OTP(FormatCategory.DOCUMENTS),
    DIF(FormatCategory.DOCUMENTS),
    FODS(FormatCategory.DOCUMENTS),
    NUMBERS(FormatCategory.DOCUMENTS),
    ODS(FormatCategory.DOCUMENTS),
    SXC(FormatCategory.DOCUMENTS),
    TSV(FormatCategory.DOCUMENTS),
    XLSM(FormatCategory.DOCUMENTS),
    XLTX(FormatCategory.DOCUMENTS),
    XLTM(FormatCategory.DOCUMENTS),
    XLAM(FormatCategory.DOCUMENTS),
    XLSB(FormatCategory.DOCUMENTS),
    MHTML(FormatCategory.DOCUMENTS),
    MHT(FormatCategory.DOCUMENTS),
    PCL(FormatCategory.DOCUMENTS),
    PS(FormatCategory.DOCUMENTS),
    CGM(FormatCategory.DOCUMENTS),
    DOT(FormatCategory.DOCUMENTS),
    DOCM(FormatCategory.DOCUMENTS),
    DOTX(FormatCategory.DOCUMENTS),
    DOTM(FormatCategory.DOCUMENTS),
    OTT(FormatCategory.DOCUMENTS),
    MERGE_PDFS(FormatCategory.DOCUMENTS) {
        @Override
        public String getName() {
            return "MERGE";
        }

        @Override
        public boolean isDummy() {
            return true;
        }
    },
    PDF_IMPORT(FormatCategory.DOCUMENTS) {
        @Override
        public String getExt() {
            return PDF.getExt();
        }

        @Override
        public boolean isDummy() {
            return true;
        }
    },
    MP4(FormatCategory.VIDEO) {
        @Override
        public boolean supportsStreaming() {
            return true;
        }

        @Override
        public boolean canBeSentAsVideo() {
            return true;
        }
    },
    _3GP(FormatCategory.VIDEO) {
        @Override
        public String getExt() {
            return "3gp";
        }

        @Override
        public String getName() {
            return "3GP";
        }
    },
    STREAM(FormatCategory.VIDEO) {
        @Override
        public Format getAssociatedFormat() {
            return MP4;
        }

        @Override
        public String getExt() {
            return getAssociatedFormat().getExt();
        }

        @Override
        public String getName() {
            return name();
        }

        @Override
        public boolean isDummy() {
            return true;
        }
    },
    AVI(FormatCategory.VIDEO),
    FLV(FormatCategory.VIDEO),
    M4V(FormatCategory.VIDEO) {
        @Override
        public boolean supportsStreaming() {
            return true;
        }

        @Override
        public boolean canBeSentAsVideo() {
            return true;
        }
    },
    MKV(FormatCategory.VIDEO),
    MOV(FormatCategory.VIDEO) {
        @Override
        public boolean canBeSentAsVideo() {
            return true;
        }

        @Override
        public boolean supportsStreaming() {
            return true;
        }
    },
    MPEG(FormatCategory.VIDEO),
    MPG(FormatCategory.VIDEO),
    MTS(FormatCategory.VIDEO),
    VOB(FormatCategory.VIDEO),
    WEBM(FormatCategory.VIDEO),
    WMV(FormatCategory.VIDEO),
    TS(FormatCategory.VIDEO),
    IMAGES(FormatCategory.IMAGES),
    PDFS(FormatCategory.DOCUMENTS),
    COMPRESS(FormatCategory.COMMON) {
        @Override
        public boolean isDownloadable() {
            return false;
        }

        @Override
        public boolean isDummy() {
            return true;
        }
    },
    AAC(FormatCategory.AUDIO) {
        @Override
        public boolean canBeSentAsAudio() {
            return true;
        }
    },
    AMR(FormatCategory.AUDIO),
    AIFF(FormatCategory.AUDIO),
    FLAC(FormatCategory.AUDIO) {
        @Override
        public boolean canBeSentAsAudio() {
            return true;
        }
    },
    MP3(FormatCategory.AUDIO) {
        @Override
        public boolean canBeSentAsAudio() {
            return true;
        }
    },
    OGG(FormatCategory.AUDIO) {
        @Override
        public boolean canBeSentAsAudio() {
            return true;
        }
    },
    WAV(FormatCategory.AUDIO) {
        @Override
        public boolean canBeSentAsAudio() {
            return true;
        }
    },
    WMA(FormatCategory.AUDIO),
    M4A(FormatCategory.AUDIO),
    M4B(FormatCategory.AUDIO),
    OPUS(FormatCategory.AUDIO) {
        @Override
        public boolean canBeSentAsAudio() {
            return true;
        }
    },
    SPX(FormatCategory.AUDIO),
    MID(FormatCategory.AUDIO),
    VOICE(FormatCategory.AUDIO) {
        @Override
        public String getExt() {
            return OGG.getExt();
        }
    },
    RA(FormatCategory.AUDIO),
    RM(FormatCategory.AUDIO),
    EDIT(FormatCategory.COMMON) {
        @Override
        public boolean isUserSelectable() {
            return false;
        }

        @Override
        public boolean isDummy() {
            return true;
        }

        @Override
        public boolean isDownloadable() {
            return false;
        }
    },
    UNKNOWN(FormatCategory.COMMON);

    private FormatCategory category;

    Format(FormatCategory category) {
        this.category = category;
    }

    public String getExt() {
        return name().toLowerCase();
    }

    public String getName() {
        return name();
    }

    public FormatCategory getCategory() {
        return category;
    }

    public boolean canBeSentAsAudio() {
        return false;
    }

    public boolean isDownloadable() {
        return true;
    }

    public boolean isDummy() {
        return false;
    }

    public boolean supportsStreaming() {
        return false;
    }

    public boolean canBeSentAsVideo() {
        return false;
    }

    public Format getAssociatedFormat() {
        return this;
    }

    public boolean isUserSelectable() {
        return true;
    }

    public static List<Format> filter(FormatCategory category) {
        List<Format> result = new ArrayList<>();
        for (Format value : Format.values()) {
            if (value.getCategory() == category) {
                result.add(value);
            }
        }

        return result;
    }
}
