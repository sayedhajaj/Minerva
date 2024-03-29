actual fun getStandardLibrary(): String {
    return js("require('./standard_library/boolean.minerva')") as String +
            js("require('./standard_library/integer.minerva')") as String +
            js("require('./standard_library/decimal.minerva')") as String +
            js("require('./standard_library/char.minerva')") as String +
            js("require('./standard_library/iterable.minerva')") as String +
            js("require('./standard_library/string.minerva')") as String +
            js("require('./standard_library/array.minerva')") as String +
            js("require('./standard_library/math.minerva')") as String +
            js("require('./standard_library/random.minerva')") as String +
            js("require('./standard_library/Canvas.minerva')") as String
}

actual fun loadSource(path: String): String {
    val fullPath = "./$path"
    return js("require(fullPath)") as String
}