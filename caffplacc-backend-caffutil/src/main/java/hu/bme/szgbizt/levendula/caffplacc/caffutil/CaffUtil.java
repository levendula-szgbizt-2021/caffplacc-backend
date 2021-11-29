package hu.bme.szgbizt.levendula.caffplacc.caffutil;

import hu.bme.szgbizt.levendula.caffplacc.caffutil.data.Caff;

public interface CaffUtil {

    Caff parse(byte[] data) throws CaffUtilException, InterruptedException;
}