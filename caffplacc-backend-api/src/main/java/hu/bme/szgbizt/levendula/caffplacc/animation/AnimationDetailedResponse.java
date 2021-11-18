package hu.bme.szgbizt.levendula.caffplacc.animation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AnimationDetailedResponse {

    String id;
    String uploaderUserName;
    double fileSizeInMb;
    String hash;
    LocalDateTime uploadDate;
    String title;
    List<CommentResponse> comments;
}
