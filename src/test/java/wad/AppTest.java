package wad;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author airhacks.com
 */
public class AppTest {

    @Test
    public void currentFolderFolderName() {
        String expected = "wad";
        Path currentPath = Paths.get("").toAbsolutePath().getFileName();
        assertThat(currentPath.toString(), is(expected));
    }

    @Test
    public void addTrailingSlash() {
        String expected = File.separator;

        String actual = App.addTrailingSlash("").toString();
        assertThat(actual, is(expected));

        actual = App.addTrailingSlash(File.separator).toString();
        assertThat(actual, is(expected));
    }


}
