// WITH_STDLIB

// DISABLE_IR_VISIBILITY_CHECKS: ANY
// ^ Some functions, calls to which are emitted by the serialization plugin into user code, have internal visibility.
//   See https://github.com/Kotlin/kotlinx.serialization/issues/2703

import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.internal.*

enum class Plain {
    A, B
}

@Serializable enum class WithNames {
    @SerialName("A") ENTRY1,
    @SerialName("B") ENTRY2
}

@Serializable
class Holder(val p: Plain, val w: WithNames)

@OptIn(InternalSerializationApi::class)
fun testSerializers(): String {
    val cs = (Holder.serializer() as GeneratedSerializer<*>).childSerializers()
    val str1 = cs[0].toString()
    if (!str1.contains("kotlinx.serialization.internal.EnumSerializer")) return str1

    /**
     * Serialization 1.5.0+ have runtime factories to create EnumSerializer instead of synthetic $serializer, saving bytecode
     * and bringing consistency.
     */
//    val str2 = cs[1].toString()
//    if (!str2.contains("kotlinx.serialization.internal.EnumSerializer")) return str2
    return "OK"
}

fun testSerialization(previous: String): String {
    if (previous != "OK") return previous
    val h = Holder(Plain.B, WithNames.ENTRY1)
    val s = Json.encodeToString(Holder.serializer(), h)
    if (s != """{"p":"B","w":"A"}""") return s
    return "OK"
}

fun box(): String {
    return testSerialization(testSerializers())
}
