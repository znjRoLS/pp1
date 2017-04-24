package rosko.bojan;

/**
 * Created by rols on 4/22/17.
 */

import java_cup.runtime.Symbol;
import java.io.*;
import java.util.logging.Logger;

public class LexerTest {
    public static void main(String[] args) {
        Logger log = Logger.getLogger(LexerTest.class.toString());
        Reader reader = null;
        try {
            File sourceCode = new File("program.mj");
            log.info("Lexical analysis, source file: " + sourceCode.getAbsolutePath());
            reader = new BufferedReader(new FileReader(sourceCode));
            Lexer lexer = new Lexer(reader);
            Symbol currToken = null;
            while( (currToken = lexer.next_token()).sym != sym.EOF) {
                if (currToken != null) {
                    System.out.println("LEXER_INFO: Parsed token " + currToken +
                            ", value " + currToken.value);
                }
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
        }

    }
}
