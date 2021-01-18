import soundProcessing.SoundProcessing
import java.io.File

fun main(args: Array<String>) {
    var output = ""

    val soundEngine = SoundProcessing("tmp")

//    File("datasets/classical2").walk().forEach {
//        soundEngine.setFile(it)
//
//        try{
//            val data = soundEngine.toText()
//            output += data
//        }catch (e: Exception){
//            println(e.message)
//        }
//    }

    for(i in 33 until 200)
        output+=i.toChar()
    File("vocav_full.txt").writeText(output)

//    var tmp = "datasets/classical2/02 Menuet.mid"
//
//    try{
//        val soundEngine = SoundProcessing(tmp)
//        tmp = soundEngine.toText()
//        soundEngine.toMidi(tmp, "mond_3")
//    }catch(e: Exception){
//        println(e.message)
//    }
}