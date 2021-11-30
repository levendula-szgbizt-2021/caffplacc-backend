package hu.bme.szgbizt.levendula.caffplacc.service;

import hu.bme.szgbizt.levendula.caffplacc.animation.*;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.CaffUtil;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.data.Caff;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.impl.CaffShellParser;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.Animation;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.Comment;
import hu.bme.szgbizt.levendula.caffplacc.data.entity.User;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.AnimationRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.CommentRepository;
import hu.bme.szgbizt.levendula.caffplacc.data.repository.UserRepository;
import hu.bme.szgbizt.levendula.caffplacc.presentation.AnimationResponseMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityNotFoundException;
import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimationServiceTest {

    @Mock
    private AnimationRepository animationRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnimationResponseMapper animationResponseMapper;

    @Mock
    private StorageService storageService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private AnimationService animationService;

    @BeforeEach
    void setUp() {
        String previewDirectory = "./src/test/resources/previews";
        String uploadDirectory = "./src/test/resources/files";
        animationService = new AnimationService(animationRepository, commentRepository, animationResponseMapper, userRepository, uploadDirectory, previewDirectory, storageService);
    }

    @AfterAll
    static void tearDown() throws IOException {
        File files = new File("src/test/resources/files");
        Path filesPath = files.toPath();
        File previews = new File("src/test/resources/previews");
        Path previewsPath = previews.toPath();
        for(File file : Objects.requireNonNull(files.listFiles())){
            file.delete();
        }
        for(File file : Objects.requireNonNull(previews.listFiles())){
            file.delete();
        }
        Files.delete(filesPath);
        Files.delete(previewsPath);
    }

    @Test
    void listAnimationsWithNotTitle() {
        Animation animation = new Animation();
        when(animationRepository.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(animation), PageRequest.of(0, 1), 1));
        when(animationResponseMapper.map(any(Animation.class))).thenReturn(new AnimationResponse());
        Page<AnimationResponse> responses = animationService.listAnimations(null, PageRequest.of(0, 5));
        verify(animationRepository, times(1)).findAll(any(Pageable.class));
        verify(animationRepository, times(0)).findAllByTitleContains(anyString(), any(Pageable.class));
    }

    @Test
    void listAnimationsWithTitle() {
        Animation animation = new Animation();
        animation.setTitle("Title");
        when(animationRepository.findAllByTitleContains(eq(animation.getTitle()), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(animation), PageRequest.of(0, 1), 1));
        when(animationResponseMapper.map(any(Animation.class))).thenReturn(new AnimationResponse());
        Page<AnimationResponse> responses = animationService.listAnimations("Title", PageRequest.of(0, 5));
        verify(animationRepository, times(0)).findAll(any(Pageable.class));
        verify(animationRepository, times(1)).findAllByTitleContains(anyString(), any(Pageable.class));
    }

    @Test
    void listMyAnimations() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        User user = new User();
        user.setId(UUID.randomUUID());
        Animation animation = new Animation();
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(animationRepository.findAllByUserId(any(UUID.class), any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(animation), PageRequest.of(0, 1), 1));
        when(animationResponseMapper.map(any(Animation.class))).thenReturn(new AnimationResponse());
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        Page<AnimationResponse> responses = animationService.listMyAnimations(PageRequest.of(0, 5));
        verify(animationRepository, times(1)).findAllByUserId(any(UUID.class), any(PageRequest.class));
    }

    @Test
    void getOneAnimation() {
        Animation animation = mock(Animation.class);
        when(animation.getId()).thenReturn(UUID.randomUUID());
        when(animationRepository.findById(any(UUID.class))).thenReturn(Optional.of(animation));
        when(commentRepository.findAllByAnimationId(any(UUID.class))).thenReturn(List.of(new Comment()));
        when(animationResponseMapper.detailedMap(any(Animation.class))).thenReturn(new AnimationDetailedResponse());
        AnimationDetailedResponse response = animationService.getOneAnimation(UUID.randomUUID());
        verify(commentRepository, times(1)).findAllByAnimationId(any(UUID.class));
        verify(animationRepository, times(1)).findById(any(UUID.class));
        verify(animation, times(1)).setComments(anyList());
    }

    @Test
    void getOneAnimationShouldThrowExceptionWhenCalledWithNonExistingUUID() {
        UUID testID = UUID.randomUUID();
        when(animationRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> animationService.getOneAnimation(testID));
    }

    @Test
    void createAnimation() throws IOException, InterruptedException, NoSuchAlgorithmException {
        String title = "Title";
        Path caffFile = Paths.get("src/test/resources/test_caff/1.caff");
        String hashOfCaff = calculateHash(caffFile.toFile());
        byte[] caffData = Files.readAllBytes(caffFile);
        Path gifFile = Paths.get("src/test/resources/test_caff/1.gif");
        byte[] gifData = Files.readAllBytes(gifFile);
        MultipartFile multipartFile = new MockMultipartFile("1.caff", caffData);
        Caff caff = new Caff();
        caff.setCreator("Jancsi");
        caff.setDate(LocalDateTime.now());
        caff.setGif(gifData);
        CaffUtil caffUtil = mock(CaffShellParser.class);
        //when(caffUtil.parse(any())).thenReturn(caff);
        ReflectionTestUtils.setField(animationService, "caffUtil", caffUtil);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("John");
        Animation animation = new Animation();
        animation.setId(UUID.randomUUID());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(userRepository.getById(any(UUID.class))).thenReturn(user);
        when(animationResponseMapper.map(any(Animation.class))).thenReturn(new AnimationResponse());
        when(animationRepository.save(any(Animation.class))).thenAnswer(anim -> anim.getArguments()[0]);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        ArgumentCaptor<Animation> argument = ArgumentCaptor.forClass(Animation.class);
        AnimationResponse response = animationService.createAnimation(title, multipartFile);
        verify(animationRepository, times(2)).save(argument.capture());
        assertEquals(user.getId().toString(), argument.getValue().getUserId().toString());
        assertEquals(user.getUsername(), argument.getValue().getUploaderUserName());
        assertEquals(title, argument.getValue().getTitle());
        //assertEquals(caff.getDate().toInstant(ZoneOffset.UTC), argument.getValue().getUploadDate());
        assertEquals(hashOfCaff, argument.getValue().getHash());
        assertTrue(Files.exists(Paths.get("src/test/resources/files/"+argument.getValue().getId()))); //No extension because we didn't specify one above
        assertTrue(Files.exists(Paths.get("src/test/resources/previews/"+argument.getValue().getId()+".gif")));
    }

    @Test
    void updateAnimation() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        AnimationUpdateRequest animationUpdateRequest = new AnimationUpdateRequest("NewTitle");
        Animation animation = mock(Animation.class);
        User user = new User();
        user.setId(UUID.randomUUID());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(animationRepository.findByIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(animation));
        when(animationRepository.save(any(Animation.class))).thenReturn(new Animation());
        when(animationResponseMapper.map(any(Animation.class))).thenReturn(new AnimationResponse());
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        AnimationResponse response = animationService.updateAnimation(UUID.randomUUID(), animationUpdateRequest);
        verify(animationRepository, times(1)).findByIdAndUserId(any(UUID.class), any(UUID.class));
        verify(animation, times(1)).setTitle(animationUpdateRequest.getTitle());
        verify(animationRepository, times(1)).save(animation);
    }

    @Test
    void updateAnimationShouldThrowExceptionWhenCalledWithNonExistingUUID() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        AnimationUpdateRequest animationUpdateRequest = new AnimationUpdateRequest("NewTitle");
        User user = new User();
        user.setId(UUID.randomUUID());
        UUID testID = UUID.randomUUID();
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(animationRepository.findByIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        assertThrows(EntityNotFoundException.class, () -> animationService.updateAnimation(testID, animationUpdateRequest));
    }

    @Test
    void deleteAnimation() throws IOException {
        UUID animationID = UUID.randomUUID();
        Path caffFile = Paths.get("src/test/resources/test_caff/1.caff");
        Path gifFile = Paths.get("src/test/resources/test_caff/1.gif");
        Path targetCaff = Paths.get("src/test/resources/files/"+animationID.toString()+".caff");
        Path targetCaffGif = Paths.get("src/test/resources/previews/"+animationID.toString()+".gif");
        Files.copy(caffFile, targetCaff, REPLACE_EXISTING);
        Files.copy(gifFile, targetCaffGif, REPLACE_EXISTING);

        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("John");
        Animation animation = new Animation();
        animation.setId(animationID);
        animation.setUserId(user.getId());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(animationRepository.findById(any(UUID.class))).thenReturn(Optional.of(animation));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        animationService.deleteAnimation(animationID);
        assertTrue(Files.notExists(Paths.get("src/test/resources/previews/"+animationID.toString()+".gif")));
        assertTrue(Files.notExists(Paths.get("src/test/resources/files/"+animationID.toString()+".caff")));
    }

    @Test
    void previewAnimation() throws IOException {
        UUID animationID = UUID.randomUUID();
        Path gifFile = Paths.get("src/test/resources/test_caff/1.gif");
        Path targetCaffGif = Paths.get("src/test/resources/previews/"+animationID.toString()+".gif");
        Files.copy(gifFile, targetCaffGif, REPLACE_EXISTING);
        Resource expectedResource = new UrlResource(targetCaffGif.toUri());

        when(storageService.getResource(anyString(), any(Path.class))).thenReturn(expectedResource);
        ResponseEntity<?> response = animationService.previewAnimation(animationID);
        assertEquals(HttpStatus.OK.toString(), response.getStatusCode().toString());
        assertEquals(MediaType.IMAGE_GIF, response.getHeaders().getContentType());
        assertEquals(expectedResource, response.getBody());
    }

    @Test
    void previewAnimationReturnsNotFoundWhenCalledWithNonExistingUUID() throws IOException {
        UUID animationID = UUID.randomUUID();

        when(storageService.getResource(anyString(), any(Path.class))).thenThrow(new FileNotFoundException());
        ResponseEntity<?> response = animationService.previewAnimation(animationID);
        assertEquals(HttpStatus.NOT_FOUND.toString(), response.getStatusCode().toString());
    }

    @Test
    void downloadAnimation() throws IOException {
        UUID animationID = UUID.randomUUID();
        Path caffFile = Paths.get("src/test/resources/test_caff/1.caff");
        Path targetCaff = Paths.get("src/test/resources/files/"+animationID.toString()+".caff");
        Resource expectedResource = new UrlResource(targetCaff.toUri());
        Files.copy(caffFile, targetCaff, REPLACE_EXISTING);

        when(storageService.getResource(anyString(), any(Path.class))).thenReturn(expectedResource);
        ResponseEntity<?> response = animationService.downloadAnimation(animationID);
        assertEquals(HttpStatus.OK.toString(), response.getStatusCode().toString());
        assertEquals(MediaType.APPLICATION_OCTET_STREAM, response.getHeaders().getContentType());
        assertEquals(expectedResource, response.getBody());
    }

    @Test
    void downloadAnimationReturnsNotFoundWhenCalledWithNonExistingUUID() throws IOException {
        UUID animationID = UUID.randomUUID();

        when(storageService.getResource(anyString(), any(Path.class))).thenThrow(new FileNotFoundException());
        ResponseEntity<?> response = animationService.downloadAnimation(animationID);
        assertEquals(HttpStatus.NOT_FOUND.toString(), response.getStatusCode().toString());
    }

    @Test
    void createComment() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("test");
        user.setPassword("pass");
        UUID testID = UUID.randomUUID();
        Animation animation = new Animation();
        animation.setId(UUID.randomUUID());
        CommentCreateUpdateRequest commentCreateUpdateRequest = new CommentCreateUpdateRequest("Comment");
        when(animationRepository.findById(any(UUID.class))).thenReturn(Optional.of(animation));
        when(userRepository.getById(any(UUID.class))).thenReturn(user);
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());
        when(animationResponseMapper.map(any(Comment.class))).thenReturn(new CommentResponse());
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        ArgumentCaptor<Comment> argument = ArgumentCaptor.forClass(Comment.class);
        CommentResponse response = animationService.createComment(testID, commentCreateUpdateRequest);
        verify(animationRepository, times(1)).findById(any(UUID.class));
        verify(userRepository, times(1)).getById(any(UUID.class));
        verify(commentRepository, times(1)).save(argument.capture());
        assertEquals(user.getId().toString(), argument.getValue().getUserId().toString());
        assertEquals(user.getUsername(), argument.getValue().getUserName());
        assertEquals(animation.getId().toString(), argument.getValue().getAnimation().getId().toString());
        assertEquals(commentCreateUpdateRequest.getContent(), argument.getValue().getContent());
    }

    @Test
    void createCommentShouldThrowExceptionWhenCalledWithNonExistingUUID() {
        UUID testID = UUID.randomUUID();
        CommentCreateUpdateRequest commentCreateUpdateRequest = new CommentCreateUpdateRequest("Comment");
        when(animationRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> animationService.createComment(testID, commentCreateUpdateRequest));
    }

    @Test
    void updateCommentRequestedByAdmin() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN")));
        UUID testID = UUID.randomUUID();
        CommentCreateUpdateRequest commentCreateUpdateRequest = new CommentCreateUpdateRequest("Comment");
        when(commentRepository.getById(any(UUID.class))).thenReturn(new Comment());
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());
        when(animationResponseMapper.map(any(Comment.class))).thenReturn(new CommentResponse());
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        ArgumentCaptor<Comment> argument = ArgumentCaptor.forClass(Comment.class);
        CommentResponse commentResponse = animationService.updateComment(testID, commentCreateUpdateRequest);
        verify(commentRepository, times(1)).getById(any(UUID.class));
        verify(commentRepository, times(0)).findByIdAndUserId(any(UUID.class), any(UUID.class));
        verify(commentRepository, times(1)).save(argument.capture());
        assertEquals(commentCreateUpdateRequest.getContent(), argument.getValue().getContent());
    }

    @Test
    void updateCommentRequestedByNonAdmin() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UUID testID = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        CommentCreateUpdateRequest commentCreateUpdateRequest = new CommentCreateUpdateRequest("Comment");
        when(commentRepository.findByIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(new Comment()));
        when(commentRepository.save(any(Comment.class))).thenReturn(new Comment());
        when(animationResponseMapper.map(any(Comment.class))).thenReturn(new CommentResponse());
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        ArgumentCaptor<Comment> argument = ArgumentCaptor.forClass(Comment.class);
        CommentResponse commentResponse = animationService.updateComment(testID, commentCreateUpdateRequest);
        verify(commentRepository, times(0)).getById(any(UUID.class));
        verify(commentRepository, times(1)).findByIdAndUserId(any(UUID.class), any(UUID.class));
        verify(commentRepository, times(1)).save(argument.capture());
        assertEquals(commentCreateUpdateRequest.getContent(), argument.getValue().getContent());
    }

    @Test
    void updateCommentShouldThrowExceptionWithNonExistingID() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UUID testID = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        CommentCreateUpdateRequest commentCreateUpdateRequest = new CommentCreateUpdateRequest("Comment");
        when(commentRepository.findByIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        assertThrows(EntityNotFoundException.class, () -> animationService.updateComment(testID, commentCreateUpdateRequest));
    }

    @Test
    void deleteCommentByAdmin() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("ROLE_ADMIN")));
        UUID testID = UUID.randomUUID();
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        animationService.deleteComment(testID);
        verify(commentRepository, times(1)).deleteById(testID);
    }

    @Test
    void deleteCommentByNoNAdmin() {
        UserDetails userDetails = new org.springframework.security.core.userdetails.User("test", "pass", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        UUID testID = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());
        when(commentRepository.findByIdAndUserId(any(UUID.class), any(UUID.class))).thenReturn(Optional.of(new Comment()));
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        animationService.deleteComment(testID);
        verify(commentRepository, times(1)).delete(any(Comment.class));
    }

    private String calculateHash(File file) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        InputStream is = new FileInputStream(file);
        DigestInputStream dis = new DigestInputStream(is, md);
        while (dis.read() != -1) ;
        return DatatypeConverter.printHexBinary(md.digest());
    }
}