package hu.bme.szgbizt.levendula.caffplacc.data.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Animation {

    @Id
    UUID id;
    UUID userId;
    double fileSizeInMb;
    String sha512Hash;
    Instant uploadDate;
    String title;
    @OneToMany
    List<Comment> comments;
}
