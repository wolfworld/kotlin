fun some(x : Any) {
when (x) {
is Int ->
0
3 ->
2
in 0..3 ->
2
else ->
1
}
when (x) {
is Int -> {
0
}
3 -> {
2
}
in 0..3 -> {
2
}
else -> {
1
}
}
when (x) {
is Int ->
{
0
}
3 ->
{
2
}
in 0..3 ->
{
2
}
else ->
{
1
}
}
when (x) {
is
Int
->
{
0
}
3
->
{
2
}
in
0..3
->
{
2
}
else
->
{
1
}
}
}

// SET_INT: BLANK_LINES_BETWEEN_BLOCK_WHEN_BRANCHES = 0
