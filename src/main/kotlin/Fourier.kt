import org.jtransforms.fft.DoubleFFT_1D

class Fourier {
    private var window: DoubleArray? = null

    fun extractFrequency(sampleData: DoubleArray, sampleRate: Int): Double {
        val fft = DoubleFFT_1D((sampleData.size + 24 * sampleData.size).toLong())
        val a = DoubleArray((sampleData.size + 24 * sampleData.size) * 2)
        System.arraycopy(applyWindow(sampleData), 0, a, 0, sampleData.size)
        fft.realForward(a)

        var maxMag = Double.NEGATIVE_INFINITY
        var maxInd = -1
        for (i in 0 until a.size / 2) {
            val re = a[2 * i]
            val im = a[2 * i + 1]
            val mag = Math.sqrt(re * re + im * im)
            if (mag > maxMag) {
                maxMag = mag
                maxInd = i
            }
        }
        return sampleRate.toDouble() * maxInd / (a.size / 2)
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