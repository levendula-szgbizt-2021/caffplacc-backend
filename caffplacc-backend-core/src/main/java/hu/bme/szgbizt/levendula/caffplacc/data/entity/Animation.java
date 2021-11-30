package hu.bme.szgbizt.levendula.caffplacc.data.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.CascadeType;
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
    String uploaderUserName;
    double fileSizeInMb;
    String hash;
    Instant uploadDate;
    String title;
    @OneToMany(cascade = CascadeType.REMOVE)
    List<Comment> comments;
}
