package backends.jvm

import frontend.Expr
import java.lang.Void
import frontend.Expr.Binary
import frontend.Stmt
import java.nio.ByteBuffer

class BytecodeGenerator(private val syntaxTree: List<Stmt>) {
    var bytecode: MutableList<Byte>? = null
    var className: String? = null
    private var poolsize = 0
    fun addInstruction(instruction: Int, numBytes: Int) {
        val bytes = ByteBuffer.allocate(4).putInt(instruction).array()
        for (i in bytes.size - numBytes until bytes.size) {
            bytecode!!.add(bytes[i])
        }
    }

    fun generateCode(): List<Byte> {
        val bytecode = mutableListOf<Byte>()
        poolsize = 1
        for (stmt in syntaxTree) {
//            stmt.accept(this);
        }
        addInstruction(Instruction.MAGIC.value, 4)
        addInstruction(Instruction.VERSION.value, 4)
        addInstruction(poolsize, 2)
        addInstruction(0x0700, 2)
        addInstruction(0x02, 1)
        addInstruction(0x01002, 2)
        addInstruction(className!!.length, 1)
        for (c in className!!.toCharArray()) {
            addInstruction(c.toInt(), 1)
        }
        addInstruction(0x0021, 2)
        addInstruction(0x00010000, 4)
        addInstruction(0x00000000, 4)
        addInstruction(0x00000000, 4)
        return bytecode
    }

    fun visitClassStmt(klass: Stmt.Class?): Void? {
//        className = klass.name.lexeme;
        poolsize += 2
        return null
    }

    fun visitBinaryExpr(expr: Binary?): Void? {
        return null
    }

    fun visitLiteralExpr(expr: Expr.Literal?): Void? {
        return null
    }

//    fun visitBlockStmt(stmt: frontend.Stmt.Block?): Void? {
//        return null
//    }

    fun visitExpressionStmt(stmt: Stmt.Expression?): Void? {
        return null
    }
}