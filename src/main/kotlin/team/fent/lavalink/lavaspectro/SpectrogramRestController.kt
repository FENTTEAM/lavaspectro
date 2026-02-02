package team.fent.lavalink.lavaspectro

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import java.util.Base64

@RestController
class SpectrogramRestController(
    private val spectrogramManager: SpectrogramManager
) {

    @GetMapping("/v4/plugins/spectrogram/{trackId}")
    fun getSpectrogram(@PathVariable trackId: String): ResponseEntity<Any> {
        val data = spectrogramManager.getSpectrogram(trackId)
        return if (data != null) {
            ResponseEntity.ok(data)
        } else {
            ResponseEntity.notFound().build()
        }
    }
}
