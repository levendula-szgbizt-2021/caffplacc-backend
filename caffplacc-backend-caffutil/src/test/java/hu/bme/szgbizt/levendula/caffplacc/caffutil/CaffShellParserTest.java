package hu.bme.szgbizt.levendula.caffplacc.caffutil;

import hu.bme.szgbizt.levendula.caffplacc.caffutil.data.Caff;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.impl.CaffShellParser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

class CaffShellParserTest {

    private CaffUtil parser;

    @BeforeEach
    void initParser() {
        parser = new CaffShellParser(Runtime.getRuntime());
    }

    @Test
    void testParse() throws IOException, InterruptedException {
        byte[] caffData = TestUtil.readResourceFile("1.caff");

        Caff caff = parser.parse(caffData);
        Assertions.assertEquals(2, caff.getFrameCount());
        Assertions.assertEquals(LocalDateTime.of(2020, 7, 2, 14, 50), caff.getDate());
        Assertions.assertEquals("Test Creator", caff.getCreator());
    }

    @Test
    void testParseError() throws IOException {
        byte[] caffData = TestUtil.readResourceFile("bad.caff");

        Assertions.assertThrows(CaffUtilException.class, () -> parser.parse(caffData));
    }
}
