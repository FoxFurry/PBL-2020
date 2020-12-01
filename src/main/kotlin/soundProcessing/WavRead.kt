package soundProcessing

import java.io.File
import java.io.IOException
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioInputStream
import javax.sound.sampled.AudioSystem

class WavRead constructor(path: File){
    private var data_stream: AudioInputStream? = null
    private var data_format: AudioFormat? = null

    init {
        data_stream = AudioSystem.getAudioInputStream(path)
        data_stream?.let { tmp_format -> data_format = tmp_format.format}
    }

    fun getSampleCount(): Long {
        val total = data_stream!!.frameLength *
                data_format!!.frameSize * 8 / data_format!!.sampleSizeInBits
        return total / data_format!!.channels
    }

    fun getSampleRate(): Float{
        return data_format!!.sampleRate
    }

    @Throws(IOException::class, IllegalArgumentException::class)
    fun getInterleavedSamples(
        begin: Long, end: Long,
        samples: DoubleArray
    ) {
        val nbSamples = end - begin
        val nbBytes = nbSamples * (data_format!!.sampleSizeInBits / 8) *
                data_format!!.channels
        require(nbBytes <= Int.MAX_VALUE) { "too many samples" }
        val inBuffer = ByteArray(nbBytes.toInt())
        data_stream!!.read(inBuffer, 0, inBuffer.size)

        decodeBytes(inBuffer, samples)
    }

    private fun decodeBytes(audioBytes: ByteArray, audioSamples: DoubleArray) {
        val sampleSizeInBytes = data_format!!.sampleSizeInBits / 8
        val sampleBytes = IntArray(sampleSizeInBytes)
        var k = 0
        for (i in audioSamples.indices) {
            if (data_format!!.isBigEndian) {
                for (j in 0 until sampleSizeInBytes) {
                    sampleBytes[j] = audioBytes[k++].toInt()
                }
            } else {
                var j = sampleSizeInBytes - 1
                while (j >= 0) {
                    sampleBytes[j] = audioBytes[k++].toInt()
                    if (sampleBytes[j] != 0) j = j + 0
                    j--
                }
            }
            var ival = 0
            for (j in 0 until sampleSizeInBytes) {
                ival += sampleBytes[j]
                if (j < sampleSizeInBytes - 1) ival = ival shl 8
            }
            val ratio = Math.pow(2.0, data_format!!.sampleSizeInBits - 1.toDouble())
            val sampleVal = ival.toDouble() / ratio
            audioSamples[i] = sampleVal
        }
    }
}