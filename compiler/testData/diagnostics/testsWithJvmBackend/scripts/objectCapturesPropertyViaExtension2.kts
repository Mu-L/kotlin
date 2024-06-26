// RENDER_ALL_DIAGNOSTICS_FULL_TEXT


// see KT-49443
// two similar examples check dependency on declarations ordering

interface I {
    fun rename()
}

class DefaultEachEntryConfiguration(val entry: Int) : I {
    override fun rename() {
        entry.copy()
    }
}

<!SCRIPT_CAPTURING_OBJECT!>object ZipHelper<!> {
    fun buildZip() {
        DefaultEachEntryConfiguration(0).rename()
    }
}

fun Int.copy() = Unit
