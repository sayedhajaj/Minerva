import java.util.*;

public class Scanner {
    
    private String source;
    private final List<Token> tokens = new ArrayList<Token>();
    private int start = 0, current = 0, line = 1;

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;

        
            default:
                break;
        }
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private char advance() {
        current++;
        return source.charAt(current-1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}