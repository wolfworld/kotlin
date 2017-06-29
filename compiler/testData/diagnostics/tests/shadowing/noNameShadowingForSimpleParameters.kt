// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_ANONYMOUS_PARAMETER

open class Base {
    open fun foo(name: String) {}
}

fun test1(name: String) {
    class Local : Base() {
        override fun foo(name: String) {
        }
    }
}

fun test2(param: String) {
    fun local(param: String) {}
}

val test3: Int
    get() {
        1.let { field -> true }
        return 1
    }

val test4: Int = 0
    get() {
        if (field > 0) {
            1.let { <!NAME_SHADOWING!>field<!> -> true }
        }
        return 1
    }