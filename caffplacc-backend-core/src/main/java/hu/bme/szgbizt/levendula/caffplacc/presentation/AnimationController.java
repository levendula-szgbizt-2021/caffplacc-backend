package hu.bme.szgbizt.levendula.caffplacc.presentation;

import hu.bme.szgbizt.levendula.caffplacc.animation.*;
import hu.bme.szgbizt.levendula.caffplacc.service.AnimationService;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
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
    @GetMapping()
    public Page<AnimationResponse> listMyAnimations(Pageable pageable) {
        return service.listMyAnimations(pageable);
    }

    @Override
    @GetMapping("/search")
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
    public AnimationResponse createAnimation(@RequestParam String title, @RequestParam("file") MultipartFile file) {
        return service.createAnimation(title, file);
    }

    @Override
    @PutMapping("/{id}")
    public AnimationResponse updateAnimation(@PathVariable String id, @RequestBody AnimationUpdateRequest request) {
        return service.updateAnimation(UUID.fromString(id), request);
    }

    @Override
    @DeleteMapping("/{id}")
    public void deleteAnimation(@PathVariable String id) {
        try {
            service.deleteAnimation(UUID.fromString(id));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @GetMapping("/{id}/preview")
    public ResponseEntity<?> previewAnimation(@PathVariable String id) {
        try {
            Resource resource = service.previewAnimation(UUID.fromString(id));
            String contentType = "image/gif";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().location(URI.create(id)).build();
        }
    }

    @Override
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadAnimation(@PathVariable String id) {
        try {
            Resource resource = service.downloadAnimation(UUID.fromString(id));
            String contentType = "application/octet-stream";
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (FileNotFoundException e) {
            return ResponseEntity.notFound().location(URI.create(id)).build();
        }
    }

    @Override
    @PostMapping("/{id}/comment")
    public CommentResponse createComment(@PathVariable String id, CommentCreateUpdateRequest request) {
        return service.createComment(UUID.fromString(id), request);
    }

    @Override
    @PutMapping("/{id}/comment/{commentId}")
    public CommentResponse updateComment(@PathVariable String id, @PathVariable String commentId, CommentCreateUpdateRequest request) {
        return service.updateComment(UUID.fromString(id), UUID.fromString(commentId), request);
    }

    @Override
    @DeleteMapping("/{id}/comment/{commentId}")
    public void deleteComment(@PathVariable String id, @PathVariable String commentId) {
        service.deleteComment(UUID.fromString(id));
    }
}
