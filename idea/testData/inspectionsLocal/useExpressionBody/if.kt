// PROBLEM: none

fun abs(x: Int): Int {
    // Yes we do not report anything here
    <caret>return if (x > 0) {
        x
    }
    else {
        -x
    }
}