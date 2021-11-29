package hu.bme.szgbizt.levendula.caffplacc.caffutil.data;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.StringJoiner;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Caff {

    long frameCount;
    LocalDateTime date;
    String creator;
    Frame[] frames;

    byte[] gif;

    /* TODO for debugging only */
    @Override
    public String toString() {
        var joiner = new StringJoiner(System.getProperty("line.separator"))
                .add("CAFF {")
                .add(String.format("  frameCount: %d", frameCount))
                .add(String.format("  date: %s", date.toString()))
                .add(String.format("  creator: %s", creator));

        for (var frame : frames)
            joiner.add("  FRAME {")
                    .add(String.format("    duration: %d", frame.getDuration()))
                    .add(String.format("    width: %d", frame.getWidth()))
                    .add(String.format("    height: %d", frame.getHeight()))
                    .add(String.format("    caption: %s", frame.getCaption()))
                    .add(String.format("    tags: [%s]", String.join(",", frame.getTags())))
                    .add("  }");

        joiner.add("}");

        return joiner.toString();
    }

    /**
     * A single animation frame.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Frame {

        long duration;
        Ciff ciff;

        public long getWidth() {
            return ciff.getWidth();
        }

        public long getHeight() {
            return ciff.getHeight();
        }

        public String getCaption() {
            return ciff.getCaption();
        }

        public String[] getTags() {
            return ciff.getTags();
        }

        public byte[] getJpeg() {
            return ciff.getJpeg();
        }
    }
}
