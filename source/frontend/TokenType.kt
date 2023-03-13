package frontend

enum class TokenType {
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, LEFT_SUB, RIGHT_SUB,
    PRINT,
    IF, ELSE,
    AND, OR,
    SEMICOLON, COLON, COMMA, DOT,
    PLUS, MINUS, SLASH, STAR,
    IDENTIFIER, INTEGER, DECIMAL, STRING, CHAR,
    EQUAL, EQUAL_EQUAL, BANG_EQUAL, BANG,
    GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,
    MODULO,
    CLASS, TRUE, FALSE, CONSTRUCTOR,
    VAR, WHILE, FOR, ARROW, FUNCTION, UNTIL,
    THIS, EXTENDS, SUPER,
    NULL, BOOLEAN, ANY,
    UNION, TYPEMATCH, MATCH,
    INTERFACE, IMPLEMENTS, EXTERNAL,
    PRINT_TYPE,
    ENUM,
    AS,
    TYPE,
    OPERATOR_MODIFIER,
    EOF,
}