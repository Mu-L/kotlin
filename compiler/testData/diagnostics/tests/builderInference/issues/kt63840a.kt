// ISSUE: KT-63840
// CHECK_TYPE_WITH_EXACT
// WITH_STDLIB

fun test() {
    val buildee = build {
        select(
            <!TYPE_MISMATCH("DifferentType; TargetType"), TYPE_MISMATCH("DifferentType; TargetType")!>replaceTypeVariable(TargetType())<!>,
            DifferentType()
        )
    }
    // exact type equality check — turns unexpected compile-time behavior into red code
    // considered to be non-user-reproducible code for the purposes of these tests
    checkExactType<Buildee<TargetType>>(buildee)
}




fun <T> select(vararg values: T): T = values.first()

class TargetType
class DifferentType

class Buildee<TV> {
    fun replaceTypeVariable(value: TV): TV { val temp = storage; storage = value; return temp }
    private var storage: TV = null!!
}

fun <PTV> build(instructions: Buildee<PTV>.() -> Unit): Buildee<PTV> {
    return Buildee<PTV>().apply(instructions)
}
