package hu.bme.szgbizt.levendula.caffplacc.caffutil.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Caff {

    long nFrame;
    LocalDateTime date;
    String creator;
    byte[] gif;
}
