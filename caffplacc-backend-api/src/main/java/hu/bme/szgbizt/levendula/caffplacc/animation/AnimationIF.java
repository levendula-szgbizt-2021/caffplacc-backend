package hu.bme.szgbizt.levendula.caffplacc.animation;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

public interface AnimationIF {
    @ApiOperation(value = "listAnimations", produces = MediaType.APPLICATION_JSON_VALUE, notes = "")
    @GetMapping
    Page<AnimationResponse> listAnimations(@RequestParam("query") String query, Pageable pageable);

    @ApiOperation(value = "getOneAnimation", produces = MediaType.APPLICATION_JSON_VALUE, notes = "")
    @GetMapping("/{id}")
    AnimationDetailedResponse getOneAnimation(@PathVariable String id);

    @ApiOperation(value = "createSurvey", produces = MediaType.APPLICATION_JSON_VALUE, notes = "")
    @PostMapping
    AnimationResponse createAnimation(@RequestBody AnimationCreateRequest request, @RequestParam("file") MultipartFile file);

    @ApiOperation(value = "updateSurvey", produces = MediaType.APPLICATION_JSON_VALUE, notes = "")
    @PutMapping("/{id}")
    AnimationResponse updateAnimation(@PathVariable String id, @RequestBody AnimationUpdateRequest request);

    @ApiOperation(value = "deleteAnimation", notes = "")
    @DeleteMapping("/{id}")
    void deleteAnimation(@PathVariable String id);

    @ApiOperation(value = "previewAnimation", notes = "")
    @GetMapping("/{id}/preview")
    void previewAnimation(@PathVariable String id);

    @ApiOperation(value = "downloadAnimation", notes = "")
    @GetMapping("/{id}/download")
    void downloadAnimation(@PathVariable String id);
}
