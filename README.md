# lavaspectro

A Lavalink v4 plugin that pre-calculates spectrogram data for audio tracks, perfect for TUI visualizers or web-based bars.

## Quick Start

1. **Download**: Get the latest `.jar` from the [GitHub Releases](https://www.google.com/search?q=https://github.com/fireflyteam/lavaspectro/releases) page.
2. **Install**: Drop the `.jar` into your Lavalink `plugins/` folder.
3. **Restart**: Start Lavalink. The plugin will be active.

## API Endpoint

`GET /v4/plugins/spectrogram/{trackId}`

* **trackId**: The base64 encoded track string from Lavalink.
* **Returns**: A 2D JSON array `number[][]`.
* Inner arrays contain 64 frequency bands (0-255).
* Each frame represents ~42ms of audio.



## Lavalink Configuration

The plugin is hosted via **JitPack**, allowing for easy integration without needing GitHub personal access tokens.

### Stable Version

To use the official stable release:

```yaml
lavalink:
  plugins:
    - dependency: "team.firefly.lavalink.lavaspectro:lavaspectro-plugin:1.0.0"
      repository: "https://maven.xserv.pp.ua/releases"

```

### Development (Snapshot) Version

To use the latest development build generated on every push:

```yaml
lavalink:
  plugins:
    - dependency: "team.firefly.lavalink.lavaspectro:lavaspectro-plugin:1.0.0-SNAPSHOT"
      repository: "https://maven.xserv.pp.ua/snapshots"
      snapshot: true

```

## Usage (TypeScript + Lavacord)

```typescript
import axios from 'axios';

async function getSpectrogram(node: any, trackEncoded: string) {
    const url = `http://${node.host}:${node.port}/v4/plugins/spectrogram/${encodeURIComponent(trackEncoded)}`;
    
    try {
        const response = await axios.get(url, {
            headers: {
                Authorization: node.password
            }
        });
        
        return response.data; // number[][]
    } catch (err) {
        console.error("Failed to fetch spectrogram:", err);
    }
}

```

## Building

Requires JDK 21.

```bash
# Standard build
./gradlew build

# Snapshot build
./gradlew build -Psnapshot

```

Output jar is located in `build/libs/`.
