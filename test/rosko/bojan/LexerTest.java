package rosko.bojan;

/**
 * Created by rols on 4/22/17.
 */

import java_cup.runtime.Symbol;
import java.io.*;
import java.util.logging.Logger;

public class LexerTest {
    //Logger log = Logger.getLogger(LexerTest.class.toString());
    static String [] testPrograms = {
            "program_test_lexer1.mj",
            "program_test_lexer2.mj",
            "program_test_lexer3.mj",
            "program_test_lexer4.mj",
    };

    public static void main(String[] args) {

        for (String testProgram : testPrograms) {
            Reader reader = null;
            try {
                File sourceCode = new File(testProgram);
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
