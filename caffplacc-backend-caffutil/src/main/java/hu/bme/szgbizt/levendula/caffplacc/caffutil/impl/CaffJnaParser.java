package hu.bme.szgbizt.levendula.caffplacc.caffutil.impl;

import com.sun.jna.platform.unix.LibCAPI;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.CaffUtil;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.CaffUtilException;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.data.Caff;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.data.ConversionUtil;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.lib.CaffLibrary;

public class CaffJnaParser implements CaffUtil {

    @Override
    public Caff parse(byte[] data) {
        CaffLibrary.Caff ccaff = new CaffLibrary.Caff();

        CaffLibrary.Caff result = CaffLibrary.INSTANCE.caff_parse(ccaff, data, new LibCAPI.size_t(data.length));
        /* TODO more fine grained error handling using cafferrno? */
        if (result == null)
            throw new CaffUtilException("CAFF parse failure");

        Caff caff = ConversionUtil.toCaff(ccaff);

        CaffLibrary.INSTANCE.caff_destroy(ccaff);
        return caff;
    }
}
