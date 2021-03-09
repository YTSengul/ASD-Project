package nl.han.asd.submarine.chunking;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface FileSplitter {
    List<File> chunkFile(File file) throws IOException;
    File mergeFile(List<File> files, File destination) throws IOException;
}
