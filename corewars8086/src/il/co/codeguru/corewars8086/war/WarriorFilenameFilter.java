package il.co.codeguru.corewars8086.war;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Filters warrior files according to extension (comparison is case insensitive ).
 *
 * @author DL
 */
public class WarriorFilenameFilter implements FilenameFilter {
	
    WarriorFilenameFilter(String extension) {
        m_extension = extension;
    }

    public boolean accept(File arg0, String arg1) {
        String filename = arg1.toUpperCase();
        return filename.endsWith(m_extension);
    }

    private final String m_extension;
}
