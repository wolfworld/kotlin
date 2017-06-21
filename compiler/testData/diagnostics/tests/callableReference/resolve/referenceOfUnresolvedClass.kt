// !DIAGNOSTICS: -UNUSED_PARAMETER, -UNUSED_EXPRESSION

fun test() {
    <!UNRESOLVED_REFERENCE!>Unresolved<!>::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>
    foo(<!UNRESOLVED_REFERENCE!>Unresolved<!>::<!DEBUG_INFO_MISSING_UNRESOLVED!>foo<!>)
    foo(<!UNRESOLVED_REFERENCE!>Unresolved<!>::<!DEBUG_INFO_MISSING_UNRESOLVED!>unresolved<!>)
    ::<!UNRESOLVED_REFERENCE!>unresolved<!>
}

fun foo(x: Any) {}
fun foo() {}