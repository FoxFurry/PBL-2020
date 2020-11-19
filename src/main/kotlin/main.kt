import java.io.File

const val NOTE_ON_OFFSET = 10
const val NOTE_OFF_OFFSET = 130
const val NOTE_ON = 0x90
const val NOTE_OFF_R1 = 0x80
const val NOTE_OFF_R2 = 0x8F


val tickTranlate = arrayOf(6144,3072,1536,768,384,192,64,32,8,2)


fun main(args: Array<String>) {
//    var output = ""
//
//    File("datasets/classical").walk().take(1000).forEach {
//        output += midiToString(it)
//    }
//
//    File("output2.txt").writeText(output)

    val convert = midiToString(File("samples/midi.mid"))
    //println(convert)
    stringToMidi(convert, "alb_esp1_red")
//    val ms = File("output_last.txt").readText()
//
//    stringToMidi(ms, "big_fuck")

}