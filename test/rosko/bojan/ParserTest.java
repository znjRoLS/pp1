package rosko.bojan;

import java_cup.runtime.Symbol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.regex.Pattern;

/**
 * Created by rols on 4/22/17.
 */
public class ParserTest {

    static Logger logger = LogManager.getLogger(ParserTest.class);

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

    public static void main(String[] args) {

        for (File sourceCode : listFilesMatching(new File("./"), "program_parser.*")) {

            Reader reader = null;
            try {
                logger.info("Parsing source file: " + sourceCode.getAbsolutePath());
                reader = new BufferedReader(new FileReader(sourceCode));
                Lexer lexer = new Lexer(reader);
                Parser parser = new Parser(lexer);
                Symbol symbol = parser.parse();
                logger.info("Got symbol " + symbol + " " + symbol.value);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                logger.error("Parser error: " + e);
                //e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                logger.info("");
                logger.info("");
            }
        }
    }
}
