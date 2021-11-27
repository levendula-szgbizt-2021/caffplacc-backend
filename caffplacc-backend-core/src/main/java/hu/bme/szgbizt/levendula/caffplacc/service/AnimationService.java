package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.animation.*;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.Animation;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.AnimationRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.CommentRepository;
import hu.bme.szgbizt.levendula.caffplacc.presentation.AnimationResponseMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.UUID;

@Service
public class AnimationService {


    private final AnimationRepository animationRepository;
    private final CommentRepository commentRepository;
    private final AnimationResponseMapper mapper;

    public AnimationService(AnimationRepository animationRepository, CommentRepository commentRepository, AnimationResponseMapper mapper) {
        this.animationRepository = animationRepository;
        this.commentRepository = commentRepository;
        this.mapper = mapper;
    }

    public Page<AnimationResponse> listAnimations(String title, Pageable pageable) {
        return animationRepository.findAllByTitleContains(title, pageable).map(mapper::map);
    }

    public AnimationDetailedResponse getOneAnimation(UUID id) {
        return mapper.detailedMap(findAnimationById(id));
    }

    public AnimationResponse createAnimation(AnimationCreateRequest request) {
        return mapper.map(animationRepository.save(createAnimationEntity(request)));
    }

    public AnimationResponse updateAnimation(UUID id, AnimationUpdateRequest request) {
        return null;
    }

    public void deleteAnimation(UUID id) {
        commentRepository.deleteAllByAnimationId(id);
        animationRepository.deleteById(id);
    }

    public void previewAnimation(UUID id) {
        // todo
    }

    public void downloadAnimation(UUID id) {
        // todo
    }

    private Animation findAnimationById(UUID id) {
        return animationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Animation.class.getName()));
    }

    private Animation createAnimationEntity(AnimationCreateRequest request) {
        return new Animation(); // todo
    }
}
