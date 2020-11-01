import java.io.File
import kotlin.math.log
import kotlin.math.pow


val notes = arrayOf("G#","A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G")
val note_coef = 1.059463094359
val a4_pos = 49

fun getFreqByNum(rel_pos: Int): Double {
    return 440 * note_coef.pow(rel_pos - a4_pos)
}

fun freqToNum(freq: Double): Int {
    return (log(freq/440, note_coef) + a4_pos).toInt()
}

fun numToNote(num: Int): String {
    var output = ""
    output += notes[num%12]
    output += (num/12).toString()
    return output
}

fun main(args: Array<String>) {
    val path = "samples/250Hz_44100Hz_16bit_05sec.wav"
    val wav_file = File(path)
    if (!wav_file.exists())
        print("File not found")
    else {
        val wav_data = WavRead(wav_file)
        val samples = DoubleArray(wav_data.getSampleCount().toInt())
        wav_data.getInterleavedSamples(0, wav_data.getSampleCount(), samples)

        val FFT = Fourier()

        val dt = FFT.extractFrequency(samples, wav_data.getSampleRate().toInt())
        val num = freqToNum(dt)
        val note = numToNote(num)
        println("Freq $dt\tNote $note")
    }
}