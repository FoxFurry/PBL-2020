package soundProcessing

import org.jtransforms.fft.DoubleFFT_1D
import kotlin.math.log
import kotlin.math.min
import kotlin.math.sqrt
import java.io.File

class FourierProcessing {
    private var window: DoubleArray? = null
    private val notes = arrayOf("G#", "A", "A#", "B", "C", "C#", "D", "D#", "E", "F", "F#", "G")
    private val noteCoef = 1.059463094359
    private val a4Pos = 49
    private val spectralResolution = 0.075
    private val windowLen = 4096

    fun FFT(file: File){
        if (!file.exists())
            print("File not found")
        else {

            val wav_data = WavRead(file)
            val samples = DoubleArray(wav_data.getSampleCount().toInt())
            wav_data.getInterleavedSamples(0, wav_data.getSampleCount(), samples)

            val FFT = FourierProcessing()

            //val stepByCount = (wav_data.getSampleCount()) / (windowLen * 0.5)
            val stepByRate = (wav_data.getSampleRate()) * (spectralResolution)

            val sampleChunks = samples.toList().chunked(stepByRate.toInt())

            var timeStamp = 0.0

            sampleChunks.take(min(200, sampleChunks.size)).forEach{
                print(String.format(">Time-%.3f-------: ", timeStamp))
                val dt = FFT.extractFrequency(it.toDoubleArray())
                val sampleSize = (it.size + 24 * it.size) * 2

                val noteMagnitude = HashMap<String, Double>()

                if(dt.isNotEmpty())
                    for(i in dt.indices){
                        if(dt[i].first<5)continue
                        val freq = wav_data.getSampleRate().toDouble() * dt[i].second.toDouble() / (sampleSize.toDouble() / 2)
                        val note = numToNote(freqToNum(freq))

                        noteMagnitude[note] = noteMagnitude.getOrDefault(note, 0).toDouble() + dt[i].first

                        println("> M: ${dt[i].first}\tI: ${dt[i].second}\tF: $freq\tNote: $note")
                    }
                val result = noteMagnitude.toList().sortedByDescending { (_,value) -> value }

                if(result.isNotEmpty())println("${result[0].first} ")
                else print('\n')

                timeStamp+=spectralResolution.toFloat()
            }
            println(timeStamp)
        }
    }

//    private fun getFreqByNum(rel_pos: Int): Double {
//        return 440 * noteCoef.pow(rel_pos - a4Pos)
//    }

    private fun freqToNum(freq: Double): Int {
        return if(freq<=0) 0
        else (log(freq / 440, noteCoef) + a4Pos).toInt()
    }

    private fun numToNote(num: Int): String {
        return notes[num % 12] + (num/12).toString()
    }

    private fun extractFrequency(sampleData: DoubleArray): List<Pair<Double,Int>> {
        val fft = DoubleFFT_1D((sampleData.size + 24 * sampleData.size).toLong())
        val a = DoubleArray((sampleData.size + 24 * sampleData.size) * 2)
        val outputArr: ArrayList<Pair<Double,Int>> = ArrayList()

        //a = sampleData.copyOf()
        System.arraycopy(applyWindow(sampleData), 0, a, 0, sampleData.size)
        fft.realForward(a)

        for (i in 50 until a.size / 2) {
            if(i>10000)break
            val re = a[2 * i]
            val im = a[2 * i + 1]
            val mag = sqrt(re * re + im * im)
            outputArr.add(Pair(mag, i))
        }

        val sortedList = outputArr.sortedWith(compareByDescending { it.first })

        return sortedList.take(20)
        //return Pair(sampleRate.toDouble() * maxInd / (a.size / 2), maxMag)
    }

    private fun buildHammWindow(size: Int) {
        if (window != null && window!!.size == size) {
            return
        }
        window = DoubleArray(size)
        for (i in 0 until size) {
            window!![i] = .54 - .46 * Math.cos(2 * Math.PI * i / (size - 1.0))
        }
    }

    private fun applyWindow(input: DoubleArray): DoubleArray {
        val res = DoubleArray(input.size)
        buildHammWindow(input.size)
        for (i in input.indices) {
            res[i] = input[i] * window!![i]
        }
        return res
    }
}