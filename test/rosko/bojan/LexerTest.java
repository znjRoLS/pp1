package rosko.bojan;

/**
 * Created by rols on 4/22/17.
 */

import java_cup.runtime.Symbol;
import java.io.*;
import java.util.regex.Pattern;

public class LexerTest {

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
                logInfo("Lexical analysis, source file: " + sourceCode.getAbsolutePath());
                reader = new BufferedReader(new FileReader(sourceCode));
                Lexer lexer = new Lexer(reader);
                Symbol currToken = null;
                while( (currToken = lexer.next_token()).sym != sym.EOF) {
                    if (currToken != null) {
                        logInfo("Parsed token " + currToken +
                                ", value " + currToken.value);
                    } else {
                        logInfo("null symbol?");
                    }
                }

                logInfo("Parse successful: " + lexer.Successful);

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
                logInfo("");
                logInfo("");
            }
        }
    }

    public static void logInfo(String msg) {
        System.out.println("INFO: " + msg);
    }
}
