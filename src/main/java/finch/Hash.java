package finch;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;

/**
 * Thanks https://github.com/AIRahimi/hashing-directories
 */
public class Hash {
    public static final String NEW_LINE = System.getProperty("line.separator");
    private File file;
    private File saveTo;

    private StringBuilder output = new StringBuilder();

    public Hash(File file, File saveTo) {
        this.file = file;
        this.saveTo = saveTo;

        clearFile();
    }

    public void scanDirectory() {
        scanDirectory(file);
    }

    private void scanDirectory(File directory) {
        File entry;

        //System.out.println("Starting search of directory " + directory.getAbsolutePath());

        if (directory.isDirectory()) {
            String[] directoryContents = directory.list();

            if (directoryContents == null) {
                return;
            }

            for (String fileInDirectory : directoryContents) {
                entry = new File(directory, fileInDirectory);
                if (fileInDirectory.charAt(0) == '.') {
                    continue;
                }
                if (entry.isDirectory()) {
                    scanDirectory(entry);
                } else {
                    writeToHashFile(entry);
                }
            }
        } else {
            writeToHashFile(directory);
        }
    }


    private void writeToHashFile(File fileToHash) {
        String hash_sha256 = createHash(fileToHash);
        try {
            FileOutputStream fos = new FileOutputStream(saveTo, true);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            DataOutputStream dos = new DataOutputStream(bos);

            dos.writeBytes(fileToHash.getAbsolutePath() + "," + hash_sha256 + NEW_LINE);
            //System.out.println(fileToHash + " has been hashed");
            dos.close();

            output.append(fileToHash + " has been hashed" + NEW_LINE);
        } catch (FileNotFoundException e) {
            System.err.println("Writing to sum.check - FileNotFound Exception: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Writing to sum.check - IO Exception: " + e.getMessage());
        }
    }


    private void clearFile() {
        try {
            FileOutputStream fos = new FileOutputStream(saveTo);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            DataOutputStream dos = new DataOutputStream(bos);

            dos.writeBytes("########## HASH FILE ##########" + NEW_LINE);
            dos.close();
        } catch (FileNotFoundException e) {
            System.err.println("Clearing sum.check - FileNotFound Exception: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Clearing sum.check - IO Exception: " + e.getMessage());
        }
    }

    private String createHash(File file) {
        String hash = "";
        try {
            FileInputStream fis = new FileInputStream(file);
            hash = DigestUtils.sha256Hex(fis);
        } catch (FileNotFoundException e) {
            System.err.println("Creating hash - FileNotFound Exception: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Creating hash - IO Exception: " + e.getMessage());
        }

        return hash;
    }

    public String getOutputString() {
        return output.toString();
    }
}
