package team.fent.lavalink.lavaspectro

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class SpectrogramPlugin {

    private val log = LoggerFactory.getLogger(SpectrogramPlugin::class.java)

    init {
        log.info("SpectrogramPlugin initialized!")
    }
}
