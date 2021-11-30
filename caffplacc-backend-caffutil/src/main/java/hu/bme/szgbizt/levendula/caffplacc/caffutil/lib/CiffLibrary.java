package hu.bme.szgbizt.levendula.caffplacc.caffutil.lib;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

public interface CiffLibrary extends Library {

    CiffLibrary INSTANCE = Native.load("ciff", CiffLibrary.class);

    PointerByReference ciff_jpeg_compress(PointerByReference destination, NativeLongByReference size,
                                          CaffLibrary.Ciff ciff);
}
