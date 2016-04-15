package org.droidplanner.android.utils;

import android.support.annotation.Nullable;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Scanner;

public class IOUtils {

    public static void writeToStream(OutputStream outputStream, @Nullable String json) {
        if (json == null) {
            silentClose(outputStream);
            return;
        }
        DataOutputStream dataOutputStream = new DataOutputStream(
                new BufferedOutputStream(outputStream));
        try {
            dataOutputStream.writeBytes(json);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            silentClose(dataOutputStream);
        }
    }

    public static String convertStreamToString(InputStream is) {
        Scanner s = new Scanner(is, "UTF-8").useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void silentClose(@Nullable OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
