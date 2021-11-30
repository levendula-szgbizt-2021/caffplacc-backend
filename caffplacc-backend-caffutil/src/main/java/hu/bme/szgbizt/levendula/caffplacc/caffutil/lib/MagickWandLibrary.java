package hu.bme.szgbizt.levendula.caffplacc.caffutil.lib;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface MagickWandLibrary extends Library {

    MagickWandLibrary INSTANCE = Native.load("MagickWand-6.Q16", MagickWandLibrary.class);
}
