import java.util.*;

public class Scanner {
    
    private String source;
    private final List<Token> tokens = new ArrayList<Token>();
    private int start = 0, current = 0, line = 1;

    private final static Map<String, TokenType> keywords = new HashMap<String, TokenType>();
    static {
        keywords.put("class", TokenType.CLASS);
    }

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
            case '+': addToken(TokenType.ADD); break;
        
            default:
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                    break;
                }
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

    private boolean isAlpha(char c) {
        return 
        (c >= 'a' && c <= 'z') || 
        (c >= 'A' && c <= 'Z') ||
        (c == '_');
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    public void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String literal = source.substring(start, current);
        boolean isKeyword = keywords.containsKey(literal);
        if (isKeyword) {
            addToken(keywords.get(literal));
        } else {
            addToken(TokenType.IDENTIFIER, literal);
        }
    }
    
    public void number() {
        while (isDigit(peek())) advance();
        if (match('.')) {
            while (isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private char advance() {
        current++;
        return source.charAt(current-1);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current >= source.length() - 1) return '\0';
        return source.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }
}