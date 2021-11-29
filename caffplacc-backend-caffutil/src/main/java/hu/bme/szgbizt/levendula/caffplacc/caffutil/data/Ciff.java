package hu.bme.szgbizt.levendula.caffplacc.caffutil.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

/**
 * CrySyS Image File Format.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ciff {

    long width;
    long height;
    String caption;
    String[] tags;
    Pixel[] content;

    byte[] jpeg;

    /**
     * A single RGB pixel.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Pixel {

        byte red;
        byte green;
        byte blue;
    }
}
