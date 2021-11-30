package hu.bme.szgbizt.levendula.caffplacc.caffutil.lib;

import com.sun.jna.*;
import com.sun.jna.platform.unix.LibCAPI;
import com.sun.jna.ptr.PointerByReference;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.experimental.FieldDefaults;

/**
 * JNA Java access to the caff C library ({@code libcaff}).
 */
public interface CaffLibrary extends Library {

    /* ensure library dependencies are loaded */
    JpegLibrary JPEG = JpegLibrary.INSTANCE;
    CiffLibrary CIFF = CiffLibrary.INSTANCE;
    MagickCoreLibrary MAGICK_CORE = MagickCoreLibrary.INSTANCE;
    MagickWandLibrary MAGICK_WAND = MagickWandLibrary.INSTANCE;
    /* load main CAFF library */
    CaffLibrary INSTANCE = Native.load("caff", CaffLibrary.class);

    /**
     * Parse a CAFF file from binary data.
     *
     * @param caff   the CAFF object to parse into
     * @param data   the data to parse
     * @param length length of the data to parse
     * @return the CAFF object
     */
    Caff caff_parse(Caff caff, byte[] data, LibCAPI.size_t length);

    /**
     * Compress a CAFF file into a GIF.
     *
     * @param destination     pointer where GIF data should be written ({@code unsigned char **})
     * @param destinationSize size of available memory for {@code destination}
     * @param caff            CAFF to compress
     * @return the {@code destination} pointer
     */
    PointerByReference caff_gif_compress(PointerByReference destination, LibCAPI.size_t.ByReference destinationSize, Caff caff);

    /**
     * Destroy a CAFF object.
     *
     * @param caff CAFF to destroy
     */
    void caff_destroy(Caff caff);

    /**
     * JNA mapping of {@code time.h}'s {@code struct tm}.
     */
    @EqualsAndHashCode(callSuper = false)
    @FieldDefaults(level = AccessLevel.PUBLIC)
    @Structure.FieldOrder({"second", "minute", "hour", "day", "month", "year", "dayOfWeek", "dayOfYear",
            "isDaylightSavingTime", "gmtOffset", "timezone"})
    class Time extends Structure {

        /* These are the fields we use */
        int year; /* int tm_year */
        int month; /* int tm_mon */
        int day; /* int tm_mday */
        int hour; /* int tm_hour */
        int minute; /* int tm_min */
        int second; /* int tm_sec */

        /* These fields must still be here for the mapping but are ignored */
        int dayOfWeek; /* int tm_wday */
        int dayOfYear; /* int tm_yday */
        boolean isDaylightSavingTime; /* int tm_isdst */
        NativeLong gmtOffset; /* long tm_gmtoff */
        String timezone; /* char *tm_zone */
    }

    /**
     * JNA mapping of {@code libciff}'s {@code struct pixel}.
     */
    @EqualsAndHashCode(callSuper = false)
    @FieldDefaults(level = AccessLevel.PUBLIC)
    @Structure.FieldOrder({"red", "green", "blue"})
    class Pixel extends Structure {

        byte red; /* unsigned char px_r */
        byte green; /* unsigned char px_g */
        byte blue; /* unsigned char px_b */

        public static class ByReference extends Pixel implements Structure.ByReference {
        }
    }

    /**
     * JNA mapping of {@code libciff}'s {@code struct ciff}.
     */
    @EqualsAndHashCode(callSuper = false)
    @FieldDefaults(level = AccessLevel.PUBLIC)
    @Structure.FieldOrder({"width", "height", "caption", "tags", "content"})
    class Ciff extends Structure {

        long width; /* unsigned long long ciff_width */
        long height; /* unsigned long long ciff_height */
        String caption; /* char *ciff_cap */
        Pointer tags; /* char **ciff_tags */
        Pixel.ByReference content; /* struct pixel *ciff_content */

        public static class ByReference extends Ciff implements Structure.ByReference {
        }
    }

    /**
     * JNA mapping of {@code libcaff}'s {@code struct frame}.
     */
    @EqualsAndHashCode(callSuper = false)
    @FieldDefaults(level = AccessLevel.PUBLIC)
    @Structure.FieldOrder({"duration", "ciff"})
    class Frame extends Structure {

        long duration; /* unsigned long long fr_dur */
        Ciff.ByReference ciff; /* struct cuff *fr_ciff */

        public static class ByReference extends Frame implements Structure.ByReference {
        }
    }

    /**
     * JNA mapping of {@code libcaff}'s {@code struct caff}.
     */
    @EqualsAndHashCode(callSuper = false)
    @FieldDefaults(level = AccessLevel.PUBLIC)
    @Structure.FieldOrder({"headerSize", "frameCount", "date", "creator", "frames"})
    class Caff extends Structure {

        long headerSize; /* unsigned long long caff_hsize */
        long frameCount; /* unsigned long long caff_nframe */
        Time date; /* struct tm caff_date */
        String creator; /* char *caff_creator */
        Frame.ByReference frames; /* struct frame *caff_frames */
    }
}
