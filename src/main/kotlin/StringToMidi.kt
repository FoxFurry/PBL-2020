import javax.sound.midi.*;
import javax.sound.midi.MidiSystem
import javax.sound.midi.MidiEvent

import javax.sound.midi.MetaMessage
import javax.sound.midi.ShortMessage
import javax.sound.midi.SysexMessage

import java.io.File


fun noteOnEvent(tick: Long, note: Int): MidiEvent{
    val midiMessage = ShortMessage()
    midiMessage.setMessage(0x90, note-NOTE_ON_OFFSET, 0x60)
    return MidiEvent(midiMessage, tick)
}

fun noteOffEvent(tick: Long, note: Int): MidiEvent{
    val midiMessage = ShortMessage()
    midiMessage.setMessage(0x80, note-NOTE_OFF_OFFSET, 0x60)
    return MidiEvent(midiMessage, tick)
}

fun stringToMidi(source: Pair<String, ArrayList<MetaMessage>>, file_name: String){

    val trackName = "ML Output"

    val midiSequence = Sequence(Sequence.PPQ, 24)
    val midiTrack = midiSequence.createTrack()

    val b = byteArrayOf(0xF0.toByte(), 0x7E, 0x7F, 0x09, 0x01, 0xF7.toByte())
    val sm = SysexMessage()
    sm.setMessage(b, 6)
    var midiEvent = MidiEvent(sm, 0.toLong())
    midiTrack.add(midiEvent)

    val mt = MetaMessage()
    val bt = byteArrayOf(0x07, 0xA1.toByte(), 0x20)
    mt.setMessage(0x51, bt, 3)
    midiTrack.add(MidiEvent(mt, 0.toLong()))

    source.second.forEach{
        midiTrack.add(MidiEvent(it, 0.toLong()))
    }


    var midiMessage = ShortMessage()
    midiMessage.setMessage(0xB0, 0x7D, 0x00)
    midiEvent = MidiEvent(midiMessage, 0.toLong())
    midiTrack.add(midiEvent)

    midiMessage = ShortMessage()
    midiMessage.setMessage(0xB0, 0x7F, 0x00)
    midiEvent = MidiEvent(midiMessage, 0.toLong())
    midiTrack.add(midiEvent)

//****  set instrument to Piano  ****

//****  set instrument to Piano  ****
//    mm = ShortMessage()
//    mm.setMessage(0xC0, 0x00, 0x00)
//    me = MidiEvent(mm, 0.toLong())
//    t.add(me)
    var tick = 1
    source.first.forEach {
        if(it.toInt() in tickTranlate.indices){
            tick+=(tickTranlate[it.toInt()]/2.5).toInt()
        }
        else if (it.toInt() in NOTE_ON_OFFSET until NOTE_OFF_OFFSET){
            midiTrack.add(noteOnEvent(tick.toLong(), it.toInt()))
        }
        else
            midiTrack.add(noteOffEvent(tick.toLong(), it.toInt()))
    }

    val midiMeta = MetaMessage()
    midiMeta.setMessage(0x2F, byteArrayOf(), 0)
    midiEvent = MidiEvent(midiMeta, tick+1.toLong())
    midiTrack.add(midiEvent)

    val midiFile = File("$file_name.mid")
    MidiSystem.write(midiSequence, 1, midiFile)
}