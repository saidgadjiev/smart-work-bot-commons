package ru.gadjini.telegram.smart.bot.commons.utils;

public class MemoryUtils {

    private MemoryUtils() {
    }

    public static long toKbit(long bytes) {
        return (long) (bytes * 0.008f);
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1024;
        long absBytes = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absBytes < unit) return bytes + " B";
        int exp = (int) (Math.log(absBytes) / Math.log(unit));
        long th = (long) Math.ceil(Math.pow(unit, exp) * (unit - 0.05));
        if (exp < 6 && absBytes >= th - ((th & 0xFFF) == 0xD00 ? 51 : 0)) exp++;
        String pre = ("KMGTPE").charAt(exp - 1) + "";
        if (exp > 4) {
            bytes /= unit;
            exp -= 1;
        }
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
