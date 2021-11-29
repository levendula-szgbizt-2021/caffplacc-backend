package hu.bme.szgbizt.levendula.caffplacc.caffutil.lib;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface JpegLibrary extends Library {

    JpegLibrary INSTANCE = Native.load("jpeg", JpegLibrary.class);
}
