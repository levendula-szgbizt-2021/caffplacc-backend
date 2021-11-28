package hu.bme.szgbizt.levendula.caffplacc.animation;

import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

public interface AnimationIF {

    @ApiOperation(value = "listAnimations", produces = MediaType.APPLICATION_JSON_VALUE, notes = "This endpoint can be called by a user to get a page of animations.")
    @GetMapping
    Page<AnimationResponse> listAnimations(@RequestParam(required = false, name = "title") String query, Pageable pageable);

    @ApiOperation(value = "getOneAnimation", produces = MediaType.APPLICATION_JSON_VALUE, notes = "This endpoint can be called by a user to get the details of an animation.")
    @GetMapping("/{id}")
    AnimationDetailedResponse getOneAnimation(@PathVariable String id);

    @ApiOperation(value = "createSurvey", produces = MediaType.APPLICATION_JSON_VALUE, notes = "This endpoint is used to upload an animation to the server.")
    @PostMapping
    AnimationResponse createAnimation(@RequestParam("file") MultipartFile file);

    @ApiOperation(value = "updateSurvey", produces = MediaType.APPLICATION_JSON_VALUE, notes = "This endpoint can be called by a user to change the title of one of their animations.")
    @PutMapping("/{id}")
    AnimationResponse updateAnimation(@PathVariable String id, @RequestBody AnimationUpdateRequest request);

    @ApiOperation(value = "deleteAnimation", notes = "This endpoint deletes an animation and all related comments.")
    @DeleteMapping("/{id}")
    void deleteAnimation(@PathVariable String id);

    @ApiOperation(value = "previewAnimation", notes = "This endpoint can be called to download the preview of an animation.")
    @GetMapping("/{id}/preview")
    void previewAnimation(@PathVariable String id);

    @ApiOperation(value = "downloadAnimation", notes = "This endpoint can be called to download an animation.")
    @GetMapping("/{id}/download")
    void downloadAnimation(@PathVariable String id);
}
