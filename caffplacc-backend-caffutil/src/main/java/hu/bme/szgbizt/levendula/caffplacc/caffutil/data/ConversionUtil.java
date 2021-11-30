package hu.bme.szgbizt.levendula.caffplacc.caffutil.data;

import com.sun.jna.platform.unix.LibCAPI;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.lib.CaffLibrary;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.lib.CiffLibrary;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Conversions of JNA mappings into better usable public Java objects.
 */
@UtilityClass
public class ConversionUtil {

    /**
     * Convert a JNA {@code struct tm} mapping to a {@code LocalDateTime} object.
     * @param ctime the {@code struct tm} mapping
     * @return the Java {@code LocalDateTime}
     */
    public LocalDateTime toLocalDateTime(CaffLibrary.Time ctime) {
        return LocalDateTime.of(1900 + ctime.year, 1 + ctime.month, ctime.day, ctime.hour, ctime.minute, ctime.second);
    }

    /**
     * Convert a JNA {@code struct caff} mapping to a public {@code Caff} object.
     * @param ccaff the {@code struct caff} mapping
     * @return the Java {@code Caff}
     */
    public Caff toCaff(CaffLibrary.Caff ccaff) {
        LocalDateTime dateTime = toLocalDateTime(ccaff.date);

        CaffLibrary.Frame[] cframes = (CaffLibrary.Frame[])ccaff.frames.toArray(Math.toIntExact(ccaff.frameCount));
        Caff.Frame[] frames = Arrays.stream(cframes).map(ConversionUtil::toFrame).toArray(Caff.Frame[]::new);

        PointerByReference gifData = new PointerByReference();
        LibCAPI.size_t.ByReference size = new LibCAPI.size_t.ByReference();
        CaffLibrary.INSTANCE.caff_gif_compress(gifData, size, ccaff);
        byte[] gif = gifData.getValue().getByteArray(0, size.getValue().intValue());

        return new Caff(ccaff.frameCount, dateTime, ccaff.creator, frames, gif);
    }

    /**
     * Convert a JNA {@code struct frame} mapping to a public {@code Caff.Frame} object.
     * @param cframe the {@code struct frame} mapping
     * @return the Java {@code Frame}
     */
    public Caff.Frame toFrame(CaffLibrary.Frame cframe) {
        Ciff ciff = toCiff(cframe.ciff);
        return new Caff.Frame(cframe.duration, ciff);
    }

    /**
     * Convert a JNA {@code struct ciff} mapping to a public {@code Ciff} object.
     * @param cciff the {@code struct ciff} mapping
     * @return the Java {@code Ciff}
     */
    public Ciff toCiff(CaffLibrary.Ciff cciff) {
        String[] tags = cciff.tags.getStringArray(0);
        CaffLibrary.Pixel[] cpixels = (CaffLibrary.Pixel[]) cciff.content.toArray(Math.toIntExact(cciff.width * cciff.height));
        Ciff.Pixel[] pixels = Arrays.stream(cpixels).map(ConversionUtil::toPixel).toArray(Ciff.Pixel[]::new);

        PointerByReference jpegData = new PointerByReference();
        NativeLongByReference size = new NativeLongByReference();
        CiffLibrary.INSTANCE.ciff_jpeg_compress(jpegData, size, cciff);
        byte[] jpeg = jpegData.getValue().getByteArray(0, size.getValue().intValue());

        return new Ciff(cciff.width, cciff.height, cciff.caption, tags, pixels, jpeg);
    }

    /**
     * Convert a JNA {@code struct pixel} mapping to a public {@code Pixel} object.
     * @param cpixel the {@code struct pixel} mapping
     * @return the Java {@code Pixel}
     */
    public Ciff.Pixel toPixel(CaffLibrary.Pixel cpixel) {
        return new Ciff.Pixel(cpixel.red, cpixel.green, cpixel.blue);
    }
}
