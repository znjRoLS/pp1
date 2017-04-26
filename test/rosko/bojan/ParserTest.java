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

        String matchingPattern = "program.mj";
        //String matchingPattern = "program_parser.*";

        for (File sourceCode : listFilesMatching(new File("./"), matchingPattern)) {

            Reader reader = null;
            try {
                logger.info("Parsing source file: " + sourceCode.getAbsolutePath());
                reader = new BufferedReader(new FileReader(sourceCode));
                Lexer lexer = new Lexer(reader);
                Parser parser = new Parser(lexer);
                Symbol symbol = parser.parse();
                logger.info("Got symbol " + symbol + " " + symbol.value);

                parser.context.symCnt.printAllCounts();

//                logger.info("Found " + parser.symCnt.get("const") + " global constants");
//                logger.info("Found " + parser.symCnt.get("var") + " global variables");
//                logger.info("Found " + parser.symCnt.get("class") + " global classes");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                logger.error("Parser error: " + e);

                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);

                logger.error(sw.toString());
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
