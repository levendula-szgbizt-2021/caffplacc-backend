package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.animation.*;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.CaffShellParser;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.CaffUtil;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.data.Caff;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.Animation;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.Comment;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.AnimationRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.CommentRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.exception.CaffplaccException;
import hu.bme.szgbizt.levendula.caffplacc.presentation.AnimationResponseMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Slf4j
@Service
public class AnimationService {

    private final AnimationRepository animationRepository;
    private final CommentRepository commentRepository;
    private final AnimationResponseMapper mapper;
    private final UserRepository userRepository;
    private final CaffUtil caffUtil;
    private final Path fileStorageLocation;
    private final Path previewStorageLocation;

    public AnimationService(AnimationRepository animationRepository, CommentRepository commentRepository, AnimationResponseMapper mapper, UserRepository userRepository,
                            @Value("${files.upload-dir}") String uploadDirectory, @Value("${files.preview-dir}") String previewDirectory) {
        this.animationRepository = animationRepository;
        this.commentRepository = commentRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.caffUtil = new CaffShellParser(Runtime.getRuntime());
        this.fileStorageLocation = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        this.previewStorageLocation = Paths.get(previewDirectory).toAbsolutePath().normalize();
        if (!Files.exists(fileStorageLocation)) {
            try {
                Files.createDirectories(fileStorageLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (!Files.exists(previewStorageLocation)) {
            try {
                Files.createDirectories(previewStorageLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Page<AnimationResponse> listAnimations(String title, Pageable pageable) {
        if (title == null) {
            return animationRepository.findAll(pageable).map(mapper::map);
        } else {
            return animationRepository.findAllByTitleContains(title, pageable).map(mapper::map);
        }
    }

    public Page<AnimationResponse> listMyAnimations(Pageable pageable) {
        return animationRepository.findAllByUserId(getUserToken(), pageable).map(mapper::map);
    }

    public AnimationDetailedResponse getOneAnimation(UUID id) {
        var animation = findAnimationById(id);
        var comments = commentRepository.findAllByAnimationId(animation.getId());
        animation.setComments(comments);
        return mapper.detailedMap(animation);
    }

    public AnimationResponse createAnimation(String title, MultipartFile file) {
        return mapper.map(animationRepository.save(createAnimationEntity(title, file)));
    }

    public AnimationResponse updateAnimation(UUID id, AnimationUpdateRequest request) {
        var animation = findAnimationByIdAndUserId(id, getUserToken());
        animation.setTitle(request.getTitle());
        return mapper.map(animationRepository.save(animation));
    }

    public void deleteAnimation(UUID id) throws IOException {
        UUID userId = getUserToken();
        Animation anim = findAnimationById(id);
        if (anim.getUserId().equals(userId)) {
            commentRepository.deleteAllByAnimationId(id);
            animationRepository.deleteById(id);
            String fileName = id.toString() + ".caff";
            String previewName = id.toString() + ".gif";
            Path filePath = fileStorageLocation.resolve(fileName).normalize();
            Path previewPath = previewStorageLocation.resolve(previewName).normalize();
            Files.delete(filePath);
            Files.delete(previewPath);
        }
    }

    public Resource previewAnimation(UUID id) throws FileNotFoundException {
        String fileName = id.toString() + ".gif";
        return getResource(fileName, previewStorageLocation);
    }

    private Resource getResource(String fileName, Path resourceLocation) throws FileNotFoundException {
        try {
            Path filePath = resourceLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException | FileNotFoundException ex) {
            throw new FileNotFoundException("File not found " + fileName);
        }
    }

    public Resource downloadAnimation(UUID id) throws FileNotFoundException {
        String fileName = id.toString() + ".caff";
        return getResource(fileName, fileStorageLocation);
    }

    private Animation findAnimationById(UUID id) {
        return animationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException(Animation.class.getName()));
    }

    private Animation findAnimationByIdAndUserId(UUID id, UUID userId) {
        return animationRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new EntityNotFoundException(Animation.class.getName()));
    }

    private Animation createAnimationEntity(String title, MultipartFile file) {
        Animation anim = new Animation();
        try {
            //Caff caff = caffUtil.parse(file.getBytes());
            Caff caff = new Caff();
            caff.setCreator("Jancsi");
            caff.setDate(LocalDateTime.now());
            caff.setGif(file.getBytes());

            UUID userId = getUserToken();
            User user = userRepository.getById(userId);

            anim.setId(UUID.randomUUID());
            anim.setUserId(user.getId());
            anim.setUploaderUserName(user.getUsername());
            anim.setUploadDate(caff.getDate().toInstant(ZoneOffset.UTC));
            anim.setFileSizeInMb(file.getSize() * 0.000001); //Byte to MByte conversion
            anim.setTitle(title);

            String hash = getHashOfFile(file);
            anim.setHash(hash);

            anim = animationRepository.save(anim);
            String animId = anim.getId().toString();
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

            String fileExtension = "";
            if (originalFilename.lastIndexOf(".") != -1) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = animId + fileExtension;
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String previewFileName = animId + ".gif";
            Path previewTargetLocation = previewStorageLocation.resolve(previewFileName);
            InputStream bis = new ByteArrayInputStream(caff.getGif());
            Files.copy(bis, previewTargetLocation, StandardCopyOption.REPLACE_EXISTING);

            return anim;
        } catch (IOException e) {
            throw new CaffplaccException("FILE_UPLOAD_FAILED");
        } /*catch (InterruptedException e) {
            throw new CaffplaccException("FILE_UPLOAD_FAILED");

        } */ catch (NoSuchAlgorithmException e) {
            throw new CaffplaccException("FILE_UPLOAD_FAILED");
        }
    }

    private String getHashOfFile(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        InputStream is = file.getInputStream();
        DigestInputStream dis = new DigestInputStream(is, md);
        while (dis.read() != -1) ;
        return DatatypeConverter.printHexBinary(md.digest());
    }

    public CommentResponse createComment(UUID id, CommentCreateUpdateRequest request) {
        var animation = findAnimationById(id);
        var user = userRepository.getById(getUserToken());
        var comment = new Comment(UUID.randomUUID(), user.getId(), user.getUsername(), request.getContent(), Instant.now(), animation);
        return mapper.map(commentRepository.save(comment));
    }

    public CommentResponse updateComment(UUID id, UUID commentId, CommentCreateUpdateRequest request) {
        Comment comment = null;
        if (isAdministrator()){
            comment = commentRepository.getById(commentId);
        }
        else {
            comment = findCommentByIdAndUserId(commentId, getUserToken());
        }
        comment.setContent(request.getContent());
        return mapper.map(commentRepository.save(comment));
    }

    public void deleteComment(UUID id) {
        if (isAdministrator()){
            commentRepository.deleteById(id);
        }
        else {
            var comment = findCommentByIdAndUserId(id, getUserToken());
            commentRepository.delete(comment);
        }
    }

    private Comment findCommentByIdAndUserId(UUID id, UUID userId) {
        return commentRepository.findByIdAndUserId(id, userId).orElseThrow(() -> new EntityNotFoundException(Comment.class.getName()));
    }

    private UUID getUserToken() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var userName = userDetails.getUsername();
        return userRepository.findByUsername(userName).orElseThrow(() -> new EntityNotFoundException(User.class.getName())).getId();
    }

    private boolean isAdministrator() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getAuthorities().stream().anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
    }
}
