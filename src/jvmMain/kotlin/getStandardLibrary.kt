actual fun getStandardLibrary() =
    MinervaCompiler::class.java.getResource("standard_library/boolean.minerva").readText() +
            MinervaCompiler::class.java.getResource("standard_library/integer.minerva").readText() +
            MinervaCompiler::class.java.getResource("standard_library/decimal.minerva").readText() +
            MinervaCompiler::class.java.getResource("standard_library/char.minerva").readText() +
            MinervaCompiler::class.java.getResource("standard_library/iterable.minerva").readText() +
            MinervaCompiler::class.java.getResource("standard_library/string.minerva").readText() +
            MinervaCompiler::class.java.getResource("standard_library/array.minerva").readText()