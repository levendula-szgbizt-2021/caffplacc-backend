package hu.bme.szgbizt.levendula.caffplacc.caffutil;

import hu.bme.szgbizt.levendula.caffplacc.caffutil.data.Caff;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

class CaffShellParserTest {

    @Test
    void testParse() throws IOException, InterruptedException {
        var parser = new CaffShellParser(Runtime.getRuntime());
        byte[] caffData = readResourceFile("1.caff");
        byte[] gifData = readResourceFile("1.gif");

        Caff caff = parser.parse(caffData);
        Assertions.assertEquals(2, caff.getNFrame());
        Assertions.assertEquals(LocalDateTime.of(2020, 7, 2, 14, 50), caff.getDate());
        Assertions.assertEquals("Test Creator", caff.getCreator());
        Assertions.assertArrayEquals(gifData, caff.getGif());
    }

    private byte[] readResourceFile(String path) throws IOException {
        return Files.readAllBytes(new ClassPathResource(path).getFile().toPath());
    }
}
