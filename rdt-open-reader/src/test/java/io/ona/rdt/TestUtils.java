package io.ona.rdt;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Paths;

/**
 * Created by Vincent Karuri on 15/11/2019
 */
public class TestUtils {

    public static String getBasePackageFilePath() {
        return Paths.get(".").toAbsolutePath().normalize().toString();
    }

    protected void createTestFile() throws IOException {
        if (!testFileExists()) {
            PrintWriter writer = new PrintWriter(getTestFilePath(), "UTF-8");
            writer.println("The first line");
            writer.close();
        }
    }

    private boolean testFileExists() {
        return new File(getTestFilePath()).exists();
    }

    public static String getTestFilePath() {
        return getBasePackageFilePath() + "/src/test/java/io/ona/rdt/";
    }
}
