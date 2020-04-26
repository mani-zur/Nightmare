import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Main {
    public static void main(String[] args) throws Exception {
        ANTLRFileStream input = new ANTLRFileStream(args[0]);

        NightmareLexer lexer = new NightmareLexer(input);

        CommonTokenStream tokens = new CommonTokenStream(lexer);
        NightmareParser parser = new NightmareParser(tokens);

        ParseTree tree = parser.prog(); 

//        System.out.println(tree.toStringTree(parser));

        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new LLVMActions(), tree);

    }
}