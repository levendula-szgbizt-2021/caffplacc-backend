package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.animation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AnimationService {
    public Page<AnimationResponse> listAnimations(AnimationFilterRequest request, Pageable pageable) {
        return null;
    }

    public AnimationDetailedResponse getOneAnimation(UUID fromString) {
        return null;
    }

    public AnimationResponse createAnimation(AnimationCreateRequest request) {
        return null;
    }

    public AnimationResponse updateAnimation(UUID fromString, AnimationUpdateRequest request) {
        return null;
    }

    public void deleteAnimation(UUID fromString) {
    }

    public void previewAnimation(UUID fromString) {
    }

    public void downloadAnimation(UUID fromString) {
    }
}
