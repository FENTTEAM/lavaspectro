# lavaspectro

A Lavalink v4 plugin that pre-calculates spectrogram data for audio tracks, perfect for TUI visualizers or web-based bars.

## Quick Start

1.  **Download/Build**: Get the `.jar` from the GitHub Releases page.
2.  **Install**: Drop the `.jar` into your Lavalink `plugins/` folder.
3.  **Restart**: Start Lavalink. The plugin will be active.

## API Endpoint

`GET /v4/plugins/spectrogram/{trackId}`

-   **trackId**: The base64 encoded track string from Lavalink.
-   **Returns**: A 2D JSON array `number[][]`. 
    -   Inner arrays contain 64 frequency bands (0-255).
    -   Each frame represents ~42ms of audio.

## Lavalink Configuration

The plugin is hosted on GitHub Packages. You can configure Lavalink to download it automatically.

### GitHub Packages Authentication
To use GitHub Packages, you may need to provide a Personal Access Token (PAT) with `read:packages` scope if the repository is private. For public repositories, it might still be required by Lavalink's downloader.

```yaml
lavalink:
  plugins:
    - dependency: "team.fent.lavalink.lavaspectro:lavaspectro:1.0.0"
      repository: "https://maven.pkg.github.com/FENTTEAM/lavaspectro"
```

### Snapshot/Commit Version
For development versions:
```yaml
lavalink:
  plugins:
    - dependency: "team.fent.lavalink.lavaspectro:lavaspectro:1.0.0-abcdef1"
      repository: "https://maven.pkg.github.com/FENTTEAM/lavaspectro"
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

```bash
./gradlew build
```
For a snapshot build (includes commit hash):
```bash
./gradlew build -Psnapshot
```
Requires JDK 21. Output jar is in `build/libs/`.