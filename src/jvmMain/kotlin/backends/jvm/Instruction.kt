package backends.jvm

enum class Instruction(val value: Int) {
    MAGIC(-0x35014542), VERSION(0x00000034);
}