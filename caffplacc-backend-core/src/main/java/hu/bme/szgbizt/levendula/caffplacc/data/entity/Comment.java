package hu.bme.szgbizt.levendula.caffplacc.data.entity;

import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Comment {

    @Id
    UUID id;
    UUID userId;
    String userName;
    String content;
    Instant date;
    @ManyToOne
    @JoinColumn(name = "animation_id")
    Animation animation;
}
