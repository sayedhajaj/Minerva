enum class TokenType {
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_SUB, RIGHT_SUB,
    PRINT,
    IF, ELSE,
    AND, OR,
    SEMICOLON,
    PLUS, MINUS, SLASH, STAR,
    IDENTIFIER, NUMBER, STRING,
    EQUAL, EQUAL_EQUAL, BANG_EQUAL, BANG,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    CLASS, TRUE, FALSE,
    VAR,

    EOF,
}