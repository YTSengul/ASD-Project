package nl.han.asd.submarine;

import org.apache.commons.io.FileDeleteStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("ResultOfMethodCallIgnored")
@ExtendWith(MockitoExtension.class)
class FileSplitterImplTest {
    private FileSplitterImpl sut;
    private List<File> listOfTestChunks;
    private File fileToChunk;
    private File fileDestination;
    private final float MEGABYTE = 1024 * 1024; //1 megabyte
    private final String TEST_DIRECTORY = "data/chunks/";
    private final String TEST_FILE_NAME = TEST_DIRECTORY + "random_chunk_file";

    @BeforeEach
    public void setup(){
        sut = new FileSplitterImpl();
        new File(TEST_DIRECTORY).mkdirs();
    }

    @Test
    void chunkFileSuccessTest() throws IOException {
        //Make a file of 7.5 MB.
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(TEST_FILE_NAME, "rw")) {
            randomAccessFile.setLength((long) (MEGABYTE * 7.5));

            File file = new File(TEST_FILE_NAME);
            file.deleteOnExit();
            List<File> output = sut.chunkFile(new File(TEST_FILE_NAME));

            int chunks = (int) Math.ceil(file.length() / MEGABYTE);
            assertThat(output.size()).isEqualTo(chunks);
        }
    }

    @Test
    void mergeFileSuccessTest() throws IOException {
        float fileAmount = 64;
        List<File> files = new ArrayList<>();

        for (int i = 0; i < fileAmount; i++) {
            //Make a file of 1 MB and add it to the list.
            try (RandomAccessFile file = new RandomAccessFile(TEST_FILE_NAME +"_"+ i, "rw")) {
                file.setLength((long) MEGABYTE);
                files.add(new File(TEST_FILE_NAME + "_" + i));
            }
        }

        int expectedFileSize = (int) (fileAmount * MEGABYTE);
        File result = sut.mergeFile(files, new File(TEST_FILE_NAME));
        assertThat(result.length()).isEqualTo(expectedFileSize);
    }

    @Test
    void mergeFileFailTest() throws IOException {
        List<File> files = new ArrayList<>();
        File result = sut.mergeFile(files, new File(TEST_FILE_NAME));
        assertThat(result).isEqualTo(null);
    }

    @AfterEach
    void tearDown() {
        File fin = new File("data/");

        for (File file : Objects.requireNonNull(fin.listFiles())) {
            try {
                FileDeleteStrategy.FORCE.delete(file);
            } catch (IOException e) {
                fail("Failed deleting a file. Might be that a resource stream is still open");
            }
        }
    }
}
