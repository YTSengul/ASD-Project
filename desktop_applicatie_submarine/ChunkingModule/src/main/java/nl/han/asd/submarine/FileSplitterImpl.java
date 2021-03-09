package nl.han.asd.submarine;

import nl.han.asd.submarine.chunking.FileSplitter;
import nl.han.asd.submarine.exception.FileMergingException;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FileSplitterImpl implements FileSplitter {
    private static final Logger LOG = Logger.getLogger(FileSplitterImpl.class.getName());

    @Override
    public List<File> chunkFile(File file) throws IOException {
        byte[] buffer = new byte[1024 * 1024]; //1 megabyte
        ArrayList<File> pieces = new ArrayList<>();

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {

            int bytesAmount;

            while ((bytesAmount = bis.read(buffer)) > 0) {
                String chunkName = UUID.randomUUID().toString();
                File chunk = new File("data/chunks/", chunkName);

                try (FileOutputStream out = new FileOutputStream(chunk)) {
                    out.write(buffer, 0, bytesAmount);
                }

                pieces.add(chunk);
            }
        } catch (IOException ex) {
            //TODO ODZKJZ-487: Integrate the error into the user interface instead of logging it.
            String message = "An exception was thrown while chuncking the file into multiple files. The file could not be chuncked.";
            LOG.log(Level.SEVERE, message , ex);
            throw new FileMergingException(message, ex);
        }
        return pieces;
    }

    @Override
    public File mergeFile(List<File> files, File destination) throws IOException {
        if (files.isEmpty()) return null;

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destination))) {

            for (File file : files) {
                Files.copy(file.toPath(), bos);
            }
            return destination;

        } catch (IOException ex) {
            //TODO ODZKJZ-487: Integrate the error into the user interface instead of logging it.
            String message = "An exception was thrown while merging the files into a single file. The files could not be merged.";
            LOG.log(Level.SEVERE, message , ex);
            throw new FileMergingException(message, ex);
        }
    }

}
