package soundProcessing

import java.io.File
import javax.sound.midi.MetaMessage
import kotlin.math.max


const val NOTE_ON_OFFSET = 50

const val NOTE_ON_LH = 0x90

const val TICK_FILTER = 30
const val TICK_OFFSET = 35


val tickTranlate = arrayOf(6144, 3072, 1536, 768, 384, 192, 64, 32, 8, 2)

class SoundProcessing {
    private var sourceFile: File
    private var lastMessages = ArrayList<MetaMessage>()
    private var lastResolution = 120


    constructor(source: File){
        sourceFile = source
    }
    constructor(source: String){
        sourceFile = File(source)
    }

    fun toText(): String?{
        if(sourceFile.extension == "wav" || sourceFile.extension == "mp3"){
            throw(Exception("Wav and mp3 are not supported"))
        }
        else if(sourceFile.extension != "midi" && sourceFile.extension != "mid"){
            println("Unrecognized extension")
            return null
        }
        val tmpFile = sourceFile
        val bufferFile = File("tmp_of.tmp")

        val buffer = MidiProcessing().getType(sourceFile)
        when{
            buffer > 0 -> {
                if(MidiProcessing().midiTypeConvert(sourceFile, bufferFile)!=1){
                    println("Error converting midi type to zero")
                    return null
                }
                sourceFile = bufferFile
            }
            buffer < 0 -> {
                println("Error getting midi type data")
                return null
            }
        }

        val midiToStringData = MidiProcessing().midiToString(sourceFile) ?: return null
        lastMessages = midiToStringData.second
        lastResolution = max(24, midiToStringData.third)

        bufferFile.delete()
        sourceFile = tmpFile
        println("Success <${sourceFile.name}>")
        return midiToStringData.first
    }

    fun toMidi(source: String, output: String): Int?{
        if(source.isEmpty()){
            println("Source string is empty")
            return null
        }

        return MidiProcessing().stringToMidi(Triple(source, lastMessages, lastResolution), output)
    }

    fun setFile(fl: File){
        sourceFile = fl
    }

}

