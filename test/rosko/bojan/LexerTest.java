package rosko.bojan;

/**
 * Created by rols on 4/22/17.
 */

import java_cup.runtime.Symbol;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.regex.Pattern;

public class LexerTest {

    static Logger logger = LogManager.getLogger(LexerTest.class);

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

        for (File sourceCode : listFilesMatching(new File("./"), "program_lexer.*")) {
            Reader reader = null;
            try {
                logger.info("Lexical analysis, source file: " + sourceCode.getAbsolutePath());
                reader = new BufferedReader(new FileReader(sourceCode));
                Lexer lexer = new Lexer(reader);
                Symbol currToken = null;
                while( (currToken = lexer.next_token()).sym != sym.EOF) {
                    if (currToken != null) {
                        logger.info("Parsed token " + currToken +
                                ", value " + currToken.value);
                    } else {
                        logger.info("null symbol?");
                    }
                }
                if (lexer.Successful) {
                    logger.info("Parse successful!");
                } else {
                    logger.error("Parse not successful!");
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
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
