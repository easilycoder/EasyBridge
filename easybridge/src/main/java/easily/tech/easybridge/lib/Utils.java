package easily.tech.easybridge.lib;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by lemon on 29/03/2018.
 */
public class Utils {

    public static synchronized String readAssetFile(Context context, String path) {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open(path);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            StringBuilder fileContent = new StringBuilder();
            do {
                line = bufferedReader.readLine();
                // ignore the comments string in file
                if (line != null && !line.matches("^\\s*\\/\\/.*")) {
                    fileContent.append(line);
                }
            } while (line != null);
            bufferedReader.close();
            inputStream.close();
            return fileContent.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
        }
        return null;
    }
}
