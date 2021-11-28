package hu.bme.szgbizt.levendula.caffplacc.presentation;

import hu.bme.szgbizt.levendula.caffplacc.animation.*;
import hu.bme.szgbizt.levendula.caffplacc.service.AnimationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@PreAuthorize("hasRole('ROLE_USER')")
@RequestMapping("/api/anim")
public class AnimationController implements AnimationIF {

    private final AnimationService service;

    public AnimationController(AnimationService service) {
        this.service = service;
    }

    @Override
    @GetMapping
    public Page<AnimationResponse> listAnimations(@RequestParam(required = false, name = "title") String title, Pageable pageable) {
        return service.listAnimations(title, pageable);
    }

    @Override
    @GetMapping("/{id}")
    public AnimationDetailedResponse getOneAnimation(@PathVariable String id) {
        return service.getOneAnimation(UUID.fromString(id));
    }

    @Override
    @PostMapping
    public AnimationResponse createAnimation(@RequestParam("file") MultipartFile file) {
        return service.createAnimation(file);
    }

    @Override
    @PutMapping("/{id}")
    public AnimationResponse updateAnimation(@PathVariable String id, @RequestBody AnimationUpdateRequest request) {
        return service.updateAnimation(UUID.fromString(id), request);
    }

    @Override
    @DeleteMapping("/{id}")
    public void deleteAnimation(@PathVariable String id) {
        service.deleteAnimation(UUID.fromString(id));
    }

    @Override
    @GetMapping("/{id}/preview")
    public void previewAnimation(@PathVariable String id) {
        service.previewAnimation(UUID.fromString(id));
    }

    @Override
    @GetMapping("/{id}/download")
    public void downloadAnimation(@PathVariable String id) {
        service.downloadAnimation(UUID.fromString(id));
    }
}
