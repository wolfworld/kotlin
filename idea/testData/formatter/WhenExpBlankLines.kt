fun f(x: Int) = when(x) {
    1 -> "Foo"
    2 -> "Bar"
    3 -> {
        "Foo"
    }
    4 -> "Bar"
    else -> "Xyzzy"
}

// SET_INT: BLANK_LINES_BETWEEN_EXPRESSION_WHEN_BRANCHES = 1
