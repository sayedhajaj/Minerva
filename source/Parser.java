import java.util.*;

public class Parser {

    private static class ParseError	extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;
    
    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse(){
        List<Stmt> statements = new ArrayList<>();
        for (Token token : this.tokens) {
            System.out.println(token.toString());
        }

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(TokenType.CLASS)) return classDeclaration();

            else return statement();
        } catch (ParseError error) {
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect class name.");
        consume(TokenType.LEFT_BRACE, "Expect '{' before class body.");
        while (!isAtEnd() && !check(TokenType.RIGHT_BRACE)) advance();
        consume(TokenType.RIGHT_BRACE, "Expect '}' after class body");
        advance();
        return new Stmt.Class(name);
    }

    private Stmt statement() {

        return expressionStatement();
    }

    private Stmt expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression");
        return new Stmt.Expression(expr);
    }

    private Expr expression() {
        return term();
    }

    private Expr term() {
        Expr expr = primary();
        if (match(TokenType.PLUS)) {
            Token operator = previous();
            Expr right = primary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr primary() {
        if (match(TokenType.NUMBER)) return new Expr.Literal(previous().literal);
        return null;
    }

    private Stmt function() {
        return functionBody();
    }

    private Stmt functionBody() {
        return new Stmt.Block(block());
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<Stmt>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block");
        return statements;
    }

    private Token advance() {
        current++;
        return previous();
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current-1);
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType tokenType) {
        if (isAtEnd()) return false;
        return peek().type == tokenType;
    }

    private ParseError error(Token token, String message) {
        // log here
        return new ParseError();
    }
    
}