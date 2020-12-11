package soundProcessing

import java.io.File
import javax.sound.midi.*

class MidiProcessing {
    fun midiToString(source: File): Triple<String, ArrayList<MetaMessage>, Int>? {
        var output = ""
        val sequenceResolution: Int
        val messageVector = ArrayList<MetaMessage>()
        if (source.isFile) {
            try {
                val midiSequence = MidiSystem.getSequence(source)
                sequenceResolution = midiSequence.resolution

                var lastTick = 0L
                val track = midiSequence.tracks[0]
                for (i in 0 until track.size()) {

                    val midiEvent = track[i]
                    val midiMessage = midiEvent.message

                    if (midiMessage is MetaMessage)
                        messageVector.add(midiMessage)
                    else if (midiMessage is ShortMessage) {
                        val key = midiMessage.data1
                        if (midiMessage.command == NOTE_ON_SIGNAL && midiMessage.data2!=0) {
                            output += tickToString(midiEvent.tick - lastTick)
                            output += (NOTE_ON_ASCII_OFFSET + key).toChar()
                            lastTick = midiEvent.tick
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error getting sequence for <${source.name}>")
                return null
            }
        } else {
            println("Error getting sequence for <${source.name}>")
            return null
        }
        return Triple(output, messageVector, sequenceResolution)
    }

    fun stringToMidi(source: Triple<String, ArrayList<MetaMessage>, Int>, file_name: String): Int? {
        if(source.first.isEmpty())
            return null

        val midiSequence = Sequence(Sequence.PPQ, source.third)
        val midiTrack = midiSequence.createTrack()

        val sysexMessage = SysexMessage()
        sysexMessage.setMessage(byteArrayOf(0xF0.toByte(), 0x7E, 0x7F, 0x09, 0x01, 0xF7.toByte()), 6)
        midiTrack.add(MidiEvent(sysexMessage, 0.toLong()))

        source.second.forEach{
            midiTrack.add(MidiEvent(it,0.toLong()))
        }


        var tick = 0
        var last_tick = -1

        val notesPressed = ArrayList<Int>()
        source.first.forEach {
            when(it.toInt()){
                in 0..tickTranlate.size+TICK_ASCII_OFFSET -> {
                    tick += tickTranlate[it.toInt() - TICK_ASCII_OFFSET]
                }
                in NOTE_ON_ASCII_OFFSET..181 -> {
                    midiTrack.add(noteOnEvent(tick.toLong(), it.toInt()))
                    midiTrack.add(noteOffEvent(tick.toLong()+300, it.toInt()))
                }
            }
        }

        val midiMeta = MetaMessage()
        midiMeta.setMessage(0x2F, byteArrayOf(), 0)
        midiTrack.add(MidiEvent(midiMeta, tick+1.toLong()))


        val midiFile = File("$file_name.mid")
        MidiSystem.write(midiSequence, 0, midiFile)
        return 1
    }

    fun midiTypeConvert(input: File, output: File): Int? {
        try {
            val sequence = MidiSystem.getSequence(input)

            val midiFormat = MidiSystem.getMidiFileFormat(input)
            if (midiFormat.type == 0) {
                return 1
            }

            val aTracks = sequence.tracks
            if (aTracks.isEmpty()) {
                return 1
            }

            val firstTrack = aTracks[0]
            var nTrack = 1
            while (nTrack < aTracks.size) {
                val track = aTracks[nTrack]
                for (i in 0 until track.size()) {
                    firstTrack.add(track[i])
                }
                sequence.deleteTrack(track)
                nTrack++
            }

            MidiSystem.write(sequence, 0, output)
        }
        catch (e: Exception){
            return null
        }
        return 1
    }

    fun getType(source: File): Int{
        return  try{
                    val midiFormat = MidiSystem.getMidiFileFormat(source)
                    midiFormat.type
                }
                catch(e: Exception){
                    -1
                }
    }

    private fun tickToString(tick: Long): String {
        var output = ""
        var ticks = tick
        tickTranlate.forEachIndexed { index, element ->
            val steps = ticks / element
            if (steps != 0L) {
                output += (index + TICK_ASCII_OFFSET).toChar().toString().repeat(steps.toInt())
                ticks -= steps * element
            }
        }
        return output
    }

    private fun noteOnEvent(tick: Long, note: Int): MidiEvent {
        val midiMessage = ShortMessage()
        midiMessage.setMessage(0x90, note-NOTE_ON_ASCII_OFFSET, 0x40)
        return MidiEvent(midiMessage, tick)
    }

    private fun noteOffEvent(tick: Long, note: Int): MidiEvent {
        val midiMessage = ShortMessage()
        midiMessage.setMessage(0x80, note-NOTE_ON_ASCII_OFFSET, 0x40)
        return MidiEvent(midiMessage, tick)
    }

}