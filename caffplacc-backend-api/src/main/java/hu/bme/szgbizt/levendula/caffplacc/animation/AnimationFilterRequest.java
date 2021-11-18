package hu.bme.szgbizt.levendula.caffplacc.animation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnimationFilterRequest {

    String hash;
    String title;
    LocalDateTime from;
    LocalDateTime to;
}
