package hu.bme.szgbizt.levendula.caffplacc.caffutil.lib;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface MagickCoreLibrary extends Library {

    MagickCoreLibrary INSTANCE = Native.load("MagickCore-6.Q16", MagickCoreLibrary.class);
}
