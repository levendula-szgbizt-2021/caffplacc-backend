package hu.bme.szgbizt.levendula.caffplacc.data.entity;

import lombok.*;

import javax.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "refreshtoken")
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    @OneToOne
    User user;
    String token;
    Instant expiryDate;
}
