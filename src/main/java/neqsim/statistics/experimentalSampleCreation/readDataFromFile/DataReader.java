/*
 * DataReader.java
 *
 * Created on 1. februar 2001, 11:38
 */
package neqsim.statistics.experimentalSampleCreation.readDataFromFile;

import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * <p>
 * DataReader class.
 * </p>
 *
 * @author even solbraa
 * @version $Id: $Id
 */
public class DataReader implements DataReaderInterface {
    protected String fileName;
    protected ArrayList sampleObjectList = new ArrayList();

    /**
     * <p>
     * Constructor for DataReader.
     * </p>
     */
    public DataReader() {}

    /**
     * <p>
     * Constructor for DataReader.
     * </p>
     *
     * @param fileName a {@link java.lang.String} object
     */
    public DataReader(String fileName) {
        this.fileName = fileName;
        readData();
    }

    /** {@inheritDoc} */
    @Override
    public void readData() {
        StringTokenizer tokenizer;
        String token;

        String path = "c:/logdata/" + this.fileName + ".log";
        System.out.println(path);

        try (RandomAccessFile file = new RandomAccessFile(path, "r")) {
            long filepointer = 0;
            long length = file.length();
            for (int i = 0; i < 6; i++) {
                file.readLine();
            }
            do {
                System.out.println("test");
                String s = file.readLine();
                tokenizer = new StringTokenizer(s);
                token = tokenizer.nextToken();

                filepointer = file.getFilePointer();
                tokenizer.nextToken();
            }

            while (filepointer < length);
        } catch (Exception e) {
            String err = e.toString();
            System.out.println(err);
        }
    }

    /**
     * <p>
     * Getter for the field <code>sampleObjectList</code>.
     * </p>
     *
     * @return a {@link java.util.ArrayList} object
     */
    public ArrayList getSampleObjectList() {
        return sampleObjectList;
    }

    /**
     * <p>
     * main.
     * </p>
     *
     * @param args an array of {@link java.lang.String} objects
     */
    @SuppressWarnings("unused")
    public static void main(String[] args) {
        DataReader reader = new DataReader("31011222");
    }
}
