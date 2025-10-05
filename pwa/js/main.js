// Main application logic
let game = null;

document.addEventListener('DOMContentLoaded', () => {
    const canvas = document.getElementById('game-canvas');
    const newGameBtn = document.getElementById('new-game-btn');
    const computerMoveBtn = document.getElementById('computer-move-btn');
    const startGameBtn = document.getElementById('start-game-btn');
    const cancelGameBtn = document.getElementById('cancel-game-btn');
    const newGameDialog = document.getElementById('new-game-dialog');

    // Initialize game
    game = new Game(canvas);

    // Start default game
    const defaultTypes = [Player.COM, Player.HUM];
    const defaultNames = ['Computer', 'Human'];
    game.newgame(defaultTypes, defaultNames);

    // New game button
    newGameBtn.addEventListener('click', () => {
        showNewGameDialog();
    });

    // Computer move button
    computerMoveBtn.addEventListener('click', () => {
        if (!game.wait && game.gamestarted) {
            game.currentplayer().ask();
        }
    });

    // Start game from dialog
    startGameBtn.addEventListener('click', () => {
        const player1Name = document.getElementById('player1-name').value || 'Player 1';
        const player1Type = document.getElementById('player1-type').value === 'computer' ? Player.COM : Player.HUM;
        const player2Name = document.getElementById('player2-name').value || 'Player 2';
        const player2Type = document.getElementById('player2-type').value === 'computer' ? Player.COM : Player.HUM;

        const types = [player1Type, player2Type];
        const names = [player1Name, player2Name];

        game.newgame(types, names);
        hideNewGameDialog();
        computerMoveBtn.disabled = false;
    });

    // Cancel game dialog
    cancelGameBtn.addEventListener('click', () => {
        hideNewGameDialog();
    });

    // Canvas click handler
    canvas.addEventListener('click', (event) => {
        const rect = canvas.getBoundingClientRect();
        const x = event.clientX - rect.left;
        const y = event.clientY - rect.top;
        game.handleClick(x, y);
    });

    // Keyboard controls
    document.addEventListener('keydown', (event) => {
        if (game.finished() || game.wait) return;

        game.wait = true;

        const player = game.currentplayer();

        if (player.getType() !== Player.HUM) {
            player.ask();
            game.wait = false;
            return;
        }

        let moved = false;

        switch (event.key) {
            case '4':
            case 'ArrowLeft':
                player.move(Player.LEFT);
                moved = true;
                break;
            case '6':
            case 'ArrowRight':
                player.move(Player.RIGHT);
                moved = true;
                break;
            case '8':
            case 'ArrowUp':
                player.move(Player.UP);
                moved = true;
                break;
            case '2':
            case 'ArrowDown':
                player.move(Player.DOWN);
                moved = true;
                break;
            case '5':
            case ' ':
                player.move(Player.SAME);
                moved = true;
                break;
        }

        if (moved) {
            game.render();
        }

        game.wait = false;
    });

    // Handle window resize
    window.addEventListener('resize', () => {
        if (game && game.gamestarted) {
            game.scrollToCurrentPlayer();
        }
    });
});

function showNewGameDialog() {
    const dialog = document.getElementById('new-game-dialog');
    if (dialog) {
        dialog.classList.add('show');
    }
}

function hideNewGameDialog() {
    const dialog = document.getElementById('new-game-dialog');
    if (dialog) {
        dialog.classList.remove('show');
    }
}

// Helper function to show notification
function showNotification(message) {
    const messageInfo = document.getElementById('message-info');
    if (messageInfo) {
        messageInfo.textContent = message;
    }
}
