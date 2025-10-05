# GraphRace PWA

A Progressive Web Application (PWA) port of the classic Paper and Pencil Racing game.

## Features

- **Offline Play**: Play the game even without an internet connection
- **Responsive Design**: Works on desktop, tablet, and mobile devices
- **Touch and Mouse Support**: Click on the grid to make moves
- **Keyboard Controls**: Use arrow keys or numpad to play
- **Computer AI**: Play against the computer opponent
- **Two Player Mode**: Play with a friend locally

## How to Play

1. **Start a New Game**: Click the "New Game" button to configure players
2. **Make a Move**: 
   - Click on one of the 9 highlighted squares around your projected position
   - Use arrow keys (or numpad 2,4,5,6,8) to move
3. **Goal**: Be the first to complete a lap around the circuit

### Movement Rules

- Each turn, you can adjust your velocity by one unit in any direction
- Your car moves based on momentum - you keep your previous velocity plus any adjustment
- Hitting the grass resets your velocity to zero (crash!)
- The yellow indicator shows where you'll go if you don't change direction
- Complete a lap by crossing the red start/finish line

## Controls

### Mouse/Touch
- Click on the desired grid square to move there

### Keyboard
- **Arrow Keys**: Move in respective directions
- **Numpad 2,4,5,6,8**: Alternative movement controls
- **Spacebar or 5**: Continue with current velocity

## Installation

### As a PWA
1. Open `index.html` in a modern web browser (Chrome, Firefox, Safari, Edge)
2. Look for the "Install" option in your browser's menu
3. Install the app to your device's home screen

### Local Server
```bash
# Navigate to the pwa directory
cd pwa

# Start a local server (Python 3)
python3 -m http.server 8000

# Or use Node.js
npx http-server -p 8000

# Open http://localhost:8000 in your browser
```

## Technology Stack

- **HTML5 Canvas** for game rendering
- **Vanilla JavaScript** (ES6+) for game logic
- **CSS3** for styling and responsive design
- **Service Worker** for offline functionality
- **Web App Manifest** for PWA capabilities

## Game Architecture

The game consists of several key components:

- `Position`: Manages x,y coordinates on the grid
- `Step`: Represents velocity/movement vectors
- `Car`: Tracks car position, history, and state
- `Circuit`: Generates and renders the race track
- `Player`: Manages player state and AI logic
- `Game`: Orchestrates game flow and rendering

## AI Behavior

The computer player uses a depth-first search algorithm to find the optimal path. It:
- Looks ahead several moves based on current speed
- Evaluates positions based on angular distance around the circuit
- Penalizes moves that would hit the grass
- Tries to maintain momentum while staying on track

## Differences from Java Version

This PWA version excludes:
- Network multiplayer functionality
- Server/client networking code
- Java Applet dependencies

The core game mechanics and AI remain faithful to the original implementation.

## Credits

Based on the original GraphRace game by Ernst van Rheenen and Sieuwert van Otterloo.
PWA port created as a standalone version for modern browsers.

## License

This is a fan-made port. Please refer to the original game's license for usage terms.
