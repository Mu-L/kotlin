// TARGET_BACKEND: WASM

// RUN_THIRD_PARTY_OPTIMIZER
// WASM_DCE_EXPECTED_OUTPUT_SIZE: wasm  38_283
// WASM_DCE_EXPECTED_OUTPUT_SIZE: mjs    5_324
// WASM_OPT_EXPECTED_OUTPUT_SIZE:        9_139

fun box(): String {
    println("Hello, World!")
    return "OK"
}
