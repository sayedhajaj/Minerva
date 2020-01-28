

abstract class Expr {
    interface Visitor <R> {
        R visitLiteralExpr(Literal expr);
    }

    class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        final Object value;
    }
}