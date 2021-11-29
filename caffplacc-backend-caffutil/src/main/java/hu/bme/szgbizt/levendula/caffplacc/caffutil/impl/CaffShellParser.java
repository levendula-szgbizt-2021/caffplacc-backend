package hu.bme.szgbizt.levendula.caffplacc.caffutil.impl;

import hu.bme.szgbizt.levendula.caffplacc.caffutil.CaffUtil;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.CaffUtilException;
import hu.bme.szgbizt.levendula.caffplacc.caffutil.data.Caff;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class CaffShellParser implements CaffUtil {

    private final Runtime runtime;

    public CaffShellParser(Runtime runtime) {
        this.runtime = runtime;
    }

    @Override
    public Caff parse(byte[] data) throws InterruptedException {
        var caff = new Caff();

        Process process;
        try {
            process = runtime.exec("caff -v");
        } catch (IOException ex) {
            throw new CaffUtilException("Failed to execute caff command", ex);
        }

        /*
         * This is really confusing, but input and output are from JAVA's perspective; ie input is what the process
         * outputs...
         */

        /* write to input */
        OutputStream outputStream = process.getOutputStream();
        try {
            outputStream.write(data);
            outputStream.close();
        } catch (IOException ex) {
            throw new CaffUtilException("Failed to write data to caff process", ex);
        }

        /* read error */
        new Thread(() -> {
            var scanner = new Scanner(new BufferedReader(new InputStreamReader(process.getErrorStream())));
            while (scanner.hasNextLine())
                parseLine(scanner.nextLine(), caff);
            scanner.close();
        }).start();

        /* read output */
        new Thread(() -> {
            try {
                caff.setGif(process.getInputStream().readAllBytes());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }).start();

        int result = process.waitFor();
        if (result != 0)
            throw new CaffUtilException("CAFF parse failure");

        return caff;
    }

    /* WARNING: very fragile text output parsing */
    private void parseLine(String line, Caff caff) {
        if (line.contains("count"))
            caff.setFrameCount(Long.parseUnsignedLong(getLineValuePart(line)));
        else if (line.contains("date"))
            caff.setDate(LocalDateTime.parse(getLineValuePart(line), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        else if (line.contains("name"))
            caff.setCreator(getLineValuePart(line));
    }

    private String getLineValuePart(String line) {
        String[] parts = line.split(":\\s*", 2);
        if (parts.length != 2)
            return "";
        else
            return parts[1];
    }
}
