import java.util.*;

public class Parser {
    private final List<Token> tokens;
    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public void parse(){
        for (Token token : this.tokens) {
            System.out.println(token.toString());
        }
    }
}