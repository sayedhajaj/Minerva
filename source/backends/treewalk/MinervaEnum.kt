package backends.treewalk

import frontend.Token

class MinervaEnum(val members: List<Token>) {

    open fun get(name: Token): Any? {
        if (members.any { it.lexeme == name.lexeme }) return members.indexOfFirst { it.lexeme == name.lexeme }

        return null
    }

}