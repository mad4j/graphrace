GraphRace
=========

Implementation of the classic paper and pencil racetrack game.

## About

GraphRace is a digital version of the pen-and-paper racing game where players race around a randomly generated circuit. The game uses vector-based movement where players build momentum and must carefully navigate the track to avoid crashing.

## Available Versions

### 1. Java Desktop Application
Located in: `dolmisani.games.graphrace/`

The original Java implementation with full GUI and applet support.

**Features:**
- Desktop application using Java Swing
- Network multiplayer support
- Multiple AI difficulty levels
- Original game logic and physics

**Running:**
```bash
cd dolmisani.games.graphrace
# Compile and run using your Java IDE or build tools
```

### 2. Progressive Web Application (PWA) ⭐ NEW
Located in: `pwa/`

A modern browser-based version that works offline and can be installed like a native app.

**Features:**
- 🌐 Works in any modern web browser
- 📱 Responsive design (mobile & desktop)
- 💾 Offline play after first load
- 🎮 Mouse/touch and keyboard controls
- 🤖 Computer AI opponent
- 📥 Installable as standalone app
- ❌ **No networking** (single device only)

**Quick Start:**
```bash
cd pwa
python3 -m http.server 8080
# Open http://localhost:8080 in your browser
```

See [pwa/README.md](pwa/README.md) for detailed documentation.

## Game Rules

1. Each turn, you can adjust your velocity by ±1 in both X and Y directions
2. Your car moves based on its current velocity
3. Velocity carries over between turns (momentum)
4. Hitting the track boundary (grass) resets your velocity to zero (crash!)
5. First player to complete a lap (cross the finish line) wins

## Controls (PWA Version)

- **Mouse/Touch**: Click on desired grid position
- **Keyboard**: Arrow keys or numpad (2,4,5,6,8)
- **Spacebar/5**: Continue with current velocity

## Credits

Original game design and Java implementation:
- Ernst van Rheenen (ernst@bluering.nl)
- Sieuwert van Otterloo
- Homepage: www.bluering.nl

PWA port created for modern web browsers without networking functionality.

## License

Please refer to the original source code for licensing information.
