package hu.bme.szgbizt.levendula.caffplacc.caffutil;

import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;

@UtilityClass
public class TestUtil {

    public byte[] readResourceFile(String path) throws IOException {
        return Files.readAllBytes(new ClassPathResource(path).getFile().toPath());
    }
}
