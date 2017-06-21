class Foo {
    <!NON_FINAL_MEMBER_IN_FINAL_CLASS!>open<!> fun openFoo() {}
    fun finalFoo() {}
}

class Bar : <!FINAL_SUPERTYPE!>Foo<!>() {
    override fun openFoo() {}
    <!OVERRIDING_FINAL_MEMBER!>override<!> fun finalFoo() {}
}


open class A {
    open fun foo() {}
}

class B : A()
class C : <!FINAL_SUPERTYPE!>B<!>() {
    override fun foo() {}
}