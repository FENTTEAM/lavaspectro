package team.firefly.lavalink.lavaspectro

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.format.StandardAudioDataFormats
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.tools.io.MessageInput
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.log10

@Service
class SpectrogramManager(
    private val serverAudioPlayerManager: AudioPlayerManager
) {
    private val log = LoggerFactory.getLogger(SpectrogramManager::class.java)
    private val cache = ConcurrentHashMap<String, List<ByteArray>>()
    
    private val localManager = DefaultAudioPlayerManager().apply {
        configuration.outputFormat = StandardAudioDataFormats.COMMON_PCM_S16_BE
    }
    
    private val BAND_COUNT = 64
    private val WINDOW_SIZE = 2048

    fun getSpectrogram(trackId: String): List<ByteArray>? {
        if (cache.containsKey(trackId)) {
            return cache[trackId]
        }
        
        try {
            val track = decodeTrack(trackId)
            if (track != null) {
                if (track.info.isStream) {
                    return null 
                }
                
                val data = calculateSpectrogram(track)
                cache[trackId] = data
                return data
            }
        } catch (e: Exception) {
            log.error("Failed to calculate spectrogram for $trackId", e)
        }
        
        return null
    }

    private fun decodeTrack(trackId: String): AudioTrack? {
        val bytes = Base64.getDecoder().decode(trackId)
        val stream = ByteArrayInputStream(bytes)
        val input = MessageInput(stream)
        return serverAudioPlayerManager.decodeTrack(input)?.decodedTrack
    }

    private fun calculateSpectrogram(track: AudioTrack): List<ByteArray> {
        val player = localManager.createPlayer()
        
        try {
            player.playTrack(track)
            
            val spectrogram = mutableListOf<ByteArray>()
            val buffer = ShortArray(WINDOW_SIZE)
            var bufferIdx = 0
            
            val transformer = FastFourierTransformer(DftNormalization.STANDARD)
            
            val startTime = System.currentTimeMillis()
            val timeout = 120000L
            
            while (true) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    log.warn("Spectrogram calculation timed out for ${track.info.title}")
                    break
                }

                val frame = player.provide()
                if (frame == null) {
                    if (player.playingTrack == null) {
                        break
                    }
                    try { Thread.sleep(5) } catch (e: InterruptedException) { Thread.currentThread().interrupt(); break }
                    continue
                }
                
                val data = frame.data
                if (data == null) continue
                
                val shorts = ShortArray(data.size / 2)
                ByteBuffer.wrap(data).order(ByteOrder.BIG_ENDIAN).asShortBuffer().get(shorts)
                
                for (sample in shorts) {
                    buffer[bufferIdx++] = sample
                    if (bufferIdx >= WINDOW_SIZE) {
                        val fftData = DoubleArray(WINDOW_SIZE) { i -> buffer[i].toDouble() }
                        applyHann(fftData)
                        val complex = transformer.transform(fftData, TransformType.FORWARD)
                        val bands = processBands(complex)
                        spectrogram.add(bands)
                        bufferIdx = 0
                    }
                }
            }
            
            log.info("Calculated spectrogram: ${spectrogram.size} frames for track ${track.info.title}")
            return spectrogram
            
        } finally {
            player.destroy()
        }
    }

    private fun applyHann(window: DoubleArray) {
        for (i in window.indices) {
            window[i] *= 0.5 * (1 - kotlin.math.cos(2 * kotlin.math.PI * i / (WINDOW_SIZE - 1)))
        }
    }

    private fun processBands(complex: Array<Complex>): ByteArray {
        val magnitudes = DoubleArray(complex.size / 2) { i -> complex[i].abs() }
        val bands = ByteArray(BAND_COUNT)
        
        val nyquist = localManager.configuration.outputFormat.sampleRate / 2.0
        val logMin = kotlin.math.ln(20.0)
        val logMax = kotlin.math.ln(nyquist)
        
        for (i in 0 until BAND_COUNT) {
            val targetFreq = kotlin.math.exp(logMin + (logMax - logMin) * i / (BAND_COUNT - 1))
            val startBin = (targetFreq * magnitudes.size / nyquist).toInt().coerceIn(magnitudes.indices)
            val endBin = (targetFreq * 1.2 * magnitudes.size / nyquist).toInt().coerceIn(startBin + 1, magnitudes.size)
            
            var sumPower = 0.0
            for (j in startBin until endBin) {
                val mag = magnitudes[j]
                sumPower += mag * mag
            }
            val power = sumPower / (endBin - startBin)
            val db = 10 * log10(power / WINDOW_SIZE + 1e-10)
            bands[i] = (db * 1.2).toInt().coerceIn(0, 255).toByte()
        }
        return bands
    }
}