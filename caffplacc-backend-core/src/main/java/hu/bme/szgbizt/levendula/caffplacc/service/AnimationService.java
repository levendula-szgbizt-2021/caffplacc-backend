package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.animation.*;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.CaffShellParser;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.CaffUtil;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.data.Caff;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.Animation;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.AnimationRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.CommentRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Service
public class AnimationService {

//    @Value("${files.upload-dir}")
//    private String uploadDirectory;
//
//    @Value("${files.preview-dir}")
//    private String previewDirectory;
    private Path fileStorageLocation;
    private Path previewStorageLocation;

    private final AnimationRepository animationRepository;
    private final CommentRepository commentRepository;
    private final AnimationResponseMapper mapper;
    private final UserRepository userRepository;
    private final CaffUtil caffUtil;

    public AnimationService(AnimationRepository animationRepository, CommentRepository commentRepository, AnimationResponseMapper mapper, UserRepository userRepository, @Value("${files.upload-dir}") String uploadDirectory, @Value("${files.preview-dir}") String previewDirectory) {
        this.animationRepository = animationRepository;
        this.commentRepository = commentRepository;
        this.mapper = mapper;
        this.userRepository = userRepository;
        this.caffUtil = new CaffShellParser(Runtime.getRuntime());
        this.fileStorageLocation = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        this.previewStorageLocation = Paths.get(previewDirectory).toAbsolutePath().normalize();
        if(!Files.exists(fileStorageLocation)) {
            try {
                Files.createDirectories(fileStorageLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(!Files.exists(previewStorageLocation)) {
            try {
                Files.createDirectories(previewStorageLocation);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Page<AnimationResponse> listAnimations(String title, Pageable pageable) {
        return animationRepository.findAllByTitleContains(title, pageable).map(mapper::map);
    }

    public AnimationDetailedResponse getOneAnimation(UUID id) {
        return mapper.detailedMap(findAnimationById(id));
    }

    public AnimationResponse createAnimation(MultipartFile file) {
        return mapper.map(animationRepository.save(createAnimationEntity(file)));
    }

    public AnimationResponse updateAnimation(UUID id, AnimationUpdateRequest request) {
        return null;
    }

    public void deleteAnimation(UUID id) {
        commentRepository.deleteAllByAnimationId(id);
        animationRepository.deleteById(id);
    }

    public Resource previewAnimation(UUID id) throws FileNotFoundException {
        String fileName = id.toString() + ".gif";
        return getResource(fileName, previewStorageLocation);
    }

    private Resource getResource(String fileName, Path resourceLocation) throws FileNotFoundException {
        try {
            Path filePath = resourceLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
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

    private Animation createAnimationEntity(MultipartFile file) {
        Animation anim = new Animation();
        try {
            //Caff caff = caffUtil.parse(file.getBytes());
            Caff caff = new Caff();
            caff.setCreator("Jancsi");
            caff.setDate(LocalDateTime.now());
            caff.setGif(file.getBytes());
            //Animation anim = new Animation();
            UUID userId = getUserToken();
            User user = userRepository.getById(userId);
            anim.setId(UUID.randomUUID());
            anim.setUserId(user.getId());
            anim.setUploaderUserName(user.getUsername());
            anim.setUploadDate(caff.getDate().toInstant(ZoneOffset.UTC));
            anim.setFileSizeInMb(file.getSize() * 0.000001); //Byte to MByte conversion
            anim.setTitle("Test"); //TODO: How do we get the title? In the request or from the parser?
            String hash = getHashOfFile(file);
            anim.setHash(hash);
            anim = animationRepository.save(anim);
            String animId = anim.getId().toString();
            String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = "";
            if(originalFilename.lastIndexOf(".") != -1) {
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
        } catch (IOException | /*InterruptedException |*/ NoSuchAlgorithmException e) {
            // TODO: Handling wrong CAFF file or other exceptions.
            e.printStackTrace();
        }
        return anim;
    }

    private String getHashOfFile(MultipartFile file) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        InputStream is = file.getInputStream();
        DigestInputStream dis = new DigestInputStream(is, md);
        while (dis.read() != -1);
        return DatatypeConverter.printHexBinary(md.digest());
    }

    private UUID getUserToken() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var userName = userDetails.getUsername();
        return userRepository.findByUsername(userName).orElseThrow(() -> new EntityNotFoundException(User.class.getName())).getId();
    }
}
