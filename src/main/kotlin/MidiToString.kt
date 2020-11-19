import java.io.File
import javax.sound.midi.MetaMessage
import javax.sound.midi.MidiEvent
import javax.sound.midi.MidiSystem
import javax.sound.midi.ShortMessage


/*
    0 -> 6144
    1 -> 3072
    2 -> 1536
    3 -> 768
    4 -> 384
    5 -> 192
    6 -> 64
    7 -> 32
    8 -> 8
    9 -> 2

    Tick-representing chars will start from 0 to 9
    Notes ON start from 10 to 129
    Notes OFF start from 130 to 249
 */


fun tickToString(tick: Long): String{
    var output = ""
    var ticks = tick
    tickTranlate.forEachIndexed { index, element ->
        val steps = ticks / element
        if(steps!=0L) {
            output += (index).toChar().toString().repeat(steps.toInt())
            ticks -= steps * element
        }
    }
    return output
}

fun midiToString(file: File): Pair<String, ArrayList<MetaMessage>>{
    var output = ""
    val messageVector = ArrayList<MetaMessage>()
    if(file.isFile) {
        val midiSequence = MidiSystem.getSequence(file)
        var trackNumber = 0
        for (track in midiSequence.tracks) {
            trackNumber++
            var last_tick = 0L

            for (i in 0 until track.size()) {
                val midiEvent = track[i]
                val midiMessage = midiEvent.message

                if(midiMessage is MetaMessage){
                    messageVector.add(midiMessage)
                }
                else if (midiMessage is ShortMessage) {
                    if (midiMessage.command == NOTE_ON) {
                        val key = midiMessage.data1
                        if(key > 119)continue
                        //val velocity = midiMessage.data2

                        output += tickToString(midiEvent.tick - last_tick)
                        output += (NOTE_ON_OFFSET + key).toChar()
                        //println(NOTE_ON_OFFSET + key)

                        last_tick = midiEvent.tick
                    }
                    else if (midiMessage.command in NOTE_OFF_R1 until NOTE_OFF_R2){
                        val key = midiMessage.data1
                        if(key > 119)continue
                        //val velocity = midiMessage.data2

                        output += tickToString(midiEvent.tick - last_tick)
                        output += (NOTE_OFF_OFFSET + key).toChar()
                        //println(NOTE_OFF_OFFSET + key)

                        last_tick = midiEvent.tick
                    }
                }
            }
        }
        output += "\n"
        println("Success: file <${file.name}>")
    }

    return Pair(output, messageVector)
}