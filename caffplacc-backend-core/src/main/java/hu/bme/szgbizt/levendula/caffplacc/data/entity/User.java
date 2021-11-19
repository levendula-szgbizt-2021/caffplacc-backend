package hu.bme.szgbizt.levendula.caffplacc.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.FieldDefaults;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity(name = "CaffUser")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Inheritance(strategy = InheritanceType.JOINED)
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    UUID id;
    String username;
    @JsonIgnore
    String password;
    @ElementCollection
    List<hu.bme.szgbizt.levendula.caffplacc.data.entity.UserRole> roles;
}
