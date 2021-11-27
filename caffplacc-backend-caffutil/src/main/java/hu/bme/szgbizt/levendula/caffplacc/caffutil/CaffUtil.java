package hu.bme.szgbizt.levendula.caffplacc.caffutil;

import hu.bme.szgbizt.levendula.caffplacc.caffutil.data.Caff;

import java.io.IOException;

public interface CaffUtil {

    Caff parse(byte[] data) throws IOException, InterruptedException;
}