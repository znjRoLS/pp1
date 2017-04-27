import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * Created by rols on 4/27/17.
 */
public class ParserConfigMerger {

    public static File[] listFilesMatching(File root, String regex) {
        if(!root.isDirectory()) {
            throw new IllegalArgumentException(root+" is no directory.");
        }
        final Pattern p = Pattern.compile(regex); // careful: could also throw an exception!
        return root.listFiles(new FileFilter(){
            @Override
            public boolean accept(File file) {
                return p.matcher(file.getName()).matches();
            }
        });
    }

    public static void main(String args[]) {

        String matchingPattern = "parser_.*";

        StringBuilder parserConfig = new StringBuilder();

        File[] parserFiles = listFilesMatching(new File("spec/"), matchingPattern);

        Arrays.sort(parserFiles, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return file.getAbsolutePath().compareTo(t1.getAbsolutePath());
            }
        });

        try {
            for (int i = 0 ; i < parserFiles.length; i ++) {
                File parserConfigPart = parserFiles[i];
                    parserConfig.append("\n\n" +
                            "////////////////////////////////////////////////////////////" +
                            "////////////////////////////////////////////////////////////" +
                            "\n" +
                            "// " + parserConfigPart.getName() + "\n" +
                            "////////////////////////////////////////////////////////////" +
                            "////////////////////////////////////////////////////////////" +
                            "\n\n");
                    parserConfig.append(new String(Files.readAllBytes(Paths.get(parserConfigPart.getPath()))));
            }

            FileWriter out = new FileWriter("spec/parser.cup", false);
            out.write(parserConfig.toString());
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
