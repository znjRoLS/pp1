package rosko.bojan;

import java_cup.runtime.Symbol;

import java.io.*;
import java.util.logging.Logger;

/**
 * Created by rols on 4/22/17.
 */
public class ParserTest {

    public static void main(String[] args) {
        Logger log = Logger.getLogger(ParserTest.class.toString());

        Reader reader = null;
        try {
            File sourceCode = new File("program.mj");
            log.info("Parsing source file: " + sourceCode.getAbsolutePath());
            reader = new BufferedReader(new FileReader(sourceCode));
            Lexer lexer = new Lexer(reader);
            Parser parser = new Parser(lexer);
            Symbol symbol = parser.parse();
            log.info("Got symbol " + symbol + " " + symbol.value);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
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
