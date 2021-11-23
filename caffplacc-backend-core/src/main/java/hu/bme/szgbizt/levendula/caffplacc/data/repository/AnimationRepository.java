package hu.bme.szgbizt.levendula.caffplacc.data.repository;

import hu.bme.szgbizt.levendula.caffplacc.data.entity.Animation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AnimationRepository extends JpaRepository<Animation, UUID> {
    Page<Animation> findAllByTitleContains(String title, Pageable pageable);
    void deleteAllByUserId(UUID id);
}
