package soundProcessing

import java.io.File
import javax.sound.midi.*

class MidiProcessing {

    fun midiToString(source: File): Triple<String, ArrayList<MetaMessage>, Int> {
        var output = ""
        val sequenceResolution: Int
        val messageVector = ArrayList<MetaMessage>()
        if (source.isFile) {
            try {
                val midiSequence = MidiSystem.getSequence(source)
                val track = midiSequence.tracks[0]

                var lastTick = 0L

                for (i in 0 until track.size()) {
                    val midiEvent = track[i]
                    val midiMessage = midiEvent.message

                    if (midiMessage is MetaMessage)
                        messageVector.add(midiMessage)

                    else if (midiMessage is ShortMessage && midiMessage.command == NOTE_ON_SIGNAL && midiMessage.data2!=0) {
                        output += tickToString(midiEvent.tick - lastTick)
                        output += (NOTE_ON_ASCII_OFFSET + midiMessage.data1).toChar()
                        lastTick = midiEvent.tick
                    }

                }
                sequenceResolution = midiSequence.resolution
            } catch (e: Exception) {
                throw MidiDecodingException("Error decoding <${source.name}>: ${e.message}")

            }
        } else {
            throw MidiDecodingException("<${source.name}> is not a file")
        }
        return Triple(output, messageVector, sequenceResolution)
    }

    fun stringToMidi(source: Triple<String, ArrayList<MetaMessage>, Int>, file_name: String): Int {
        if(source.first.isEmpty())
            throw MidiEncodingException("MIDI data is empty")

        val midiSequence = Sequence(Sequence.PPQ, source.third) // Create a sequence with same resolution as original
        val midiTrack = midiSequence.createTrack()

        val sysexMessage = SysexMessage()
        sysexMessage.setMessage(byteArrayOf(0xF0.toByte(), 0x7E, 0x7F, 0x09, 0x01, 0xF7.toByte()), 6)
        midiTrack.add(MidiEvent(sysexMessage, 0.toLong()))      // Set the MIDi init info

        source.second.forEach{
            midiTrack.add(MidiEvent(it,0.toLong()))      // Copying all meta-messages from original midi to extended
        }

        var tick = 0         // Time-representative variable
        source.first.forEach {
            when(it.toInt()){
                in 0..tickTranlate.size+TICK_ASCII_OFFSET -> {
                    tick += tickTranlate[it.toInt() - TICK_ASCII_OFFSET]        // Offset character = moving in time
                }
                in NOTE_ON_ASCII_OFFSET..NOTE_ON_ASCII_OFFSET + NOTE_COUNT -> {
                    try {
                        midiTrack.add(noteOnEvent(tick.toLong(), it.toInt()))               // Adding note on
                        midiTrack.add(noteOffEvent(tick.toLong() + 300, it.toInt()))    // Adding note off few moments later
                    }
                    catch (e: Exception){
                        throw (e)
                    }
                }
            }
        }

        val midiMeta = MetaMessage()
        midiMeta.setMessage(0x2F, byteArrayOf(), 0)        // Set the end of MIDI sequence
        midiTrack.add(MidiEvent(midiMeta, tick+100.toLong()))


        val midiFile = File("$file_name.mid")
        MidiSystem.write(midiSequence, 0, midiFile)
        return 1
    }

    fun midiTypeConvert(input: File, output: File) {
        try {
            val sequence = MidiSystem.getSequence(input)

            val midiFormat = MidiSystem.getMidiFileFormat(input)
            if (midiFormat.type == 0) {
                throw MidiConversionException("Already type 0")
            }

            val aTracks = sequence.tracks
            if (aTracks.isEmpty()) {
                throw MidiConversionException("No tracks found")
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
            throw MidiConversionException("Exception while converting: ${e.message}")
        }
    }

    fun getType(source: File): Int{
        return  try{
                    val midiFormat = MidiSystem.getMidiFileFormat(source)
                    midiFormat.type
                }
                catch(e: Exception){
                    throw MidiConversionException("Unexpected error: ${e.message}")
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
        try{
            midiMessage.setMessage(0x90, note-NOTE_ON_ASCII_OFFSET, 0x40)
        }
        catch(e: Exception){
            throw MidiWritingException("Error writing NOTE ON event: ${e.message}")
        }
        return MidiEvent(midiMessage, tick)
    }

    private fun noteOffEvent(tick: Long, note: Int): MidiEvent {
        val midiMessage = ShortMessage()
        try {
            midiMessage.setMessage(0x80, note - NOTE_ON_ASCII_OFFSET, 0x40)
        }
        catch(e: Exception){
            throw MidiWritingException("Error writing NOTE OFF event: ${e.message}")
        }

        return MidiEvent(midiMessage, tick)
    }

}