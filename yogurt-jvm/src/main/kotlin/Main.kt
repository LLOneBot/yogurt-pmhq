@file:JvmName("JvmMain")

import java.io.PrintStream

fun main() {
    val utf8out = PrintStream(System.out, true, "UTF-8")
    System.setOut(utf8out)
    org.ntqqrev.yogurt.main()
}