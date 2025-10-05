class Game {
    constructor(canvas) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.circuit = null;
        this.players = [];
        this.playercount = 0;
        this.curplayer = 0;
        this.winner = 0;
        this.finish = false;
        this.wait = true;
        this.gamestarted = false;
        this.gridsize = 25;
        this.cursordraw = true;
    }

    newgame(types, names) {
        console.log("Starting new game...");
        this.gamestarted = true;

        // Initialize circuit
        this.circuit = new Circuit(50, 40, 30, this.gridsize);
        this.circuit.init();

        // Get start position
        const sx = this.circuit.getstartx1();
        const sy = this.circuit.getstarty();

        // Create players
        this.playercount = types.length;
        this.players = [];

        for (let i = 0; i < this.playercount; i++) {
            const c = `rgb(${75 + Math.floor(Math.random() * 180)}, ${75 + Math.floor(Math.random() * 180)}, ${75 + Math.floor(Math.random() * 180)})`;
            const startx = this.startpos(i);
            this.players[i] = new Player(names[i], types[i], this.circuit, this, startx, sy, c);
        }

        this.finish = false;
        this.winner = 0;

        // Resize canvas
        const canvasWidth = this.circuit.getsizex() * this.gridsize;
        const canvasHeight = this.circuit.getsizey() * this.gridsize;
        this.canvas.width = canvasWidth;
        this.canvas.height = canvasHeight;

        this.curplayer = this.playercount;
        this.nextplayer();
        this.render();
    }

    startpos(i) {
        const x1 = this.circuit.getstartx1();
        const x2 = this.circuit.getstartx2();
        return (i % (x2 - x1)) + x1;
    }

    nextplayer() {
        this.curplayer = ++this.curplayer >= this.playercount ? 0 : this.curplayer;
        if (this.curplayer === 0) {
            this.win();
        }
        
        if (!this.finished()) {
            this.updateUI();
        }

        // Scroll to current player
        this.scrollToCurrentPlayer();
    }

    win() {
        const w = this.checkfinished();
        if (w === -1) {
            return;
        }
        this.gamestarted = false;
        this.winner = w;
        this.finish = true;
        this.curplayer = this.winner;
        this.setMessage(this.getplayer(this.winner).getname() + " WINS!!!");
        
        // Disable computer move button
        const computerMoveBtn = document.getElementById('computer-move-btn');
        if (computerMoveBtn) {
            computerMoveBtn.disabled = true;
        }
    }

    checkfinished() {
        for (let i = 0; i < this.playercount; i++) {
            const player = this.players[i];
            const pos = player.getcar().getCurrentPos();
            
            if (pos.getY() === this.circuit.getstarty() &&
                pos.getX() >= this.circuit.getstartx1() &&
                pos.getX() < this.circuit.getstartx2() &&
                player.getcar().getturns() > 0) {
                return i;
            }
        }
        return -1;
    }

    finished() {
        return this.finish;
    }

    currentplayer() {
        return this.players[this.curplayer];
    }

    getplayer(i) {
        return this.players[i];
    }

    getplayercount() {
        return this.playercount;
    }

    getcirc() {
        return this.circuit;
    }

    getgridsize() {
        return this.gridsize;
    }

    render() {
        if (!this.circuit) return;

        // Paint circuit
        this.circuit.paint(this.ctx);

        // Draw all cars
        for (let i = 0; i < this.playercount; i++) {
            this.drawcar(this.players[i]);
        }

        // Draw cursor for current player
        if (this.cursordraw && !this.finished() && !this.wait) {
            this.drawcursor();
        }

        // Draw finish message
        if (this.finished()) {
            this.ctx.font = 'bold 30px Helvetica';
            this.ctx.fillStyle = '#E6F0F0';
            const x = this.currentplayer().getcar().getCurrentPos().getX() * this.gridsize - 300;
            const y = this.currentplayer().getcar().getCurrentPos().getY() * this.gridsize - 15;
            this.ctx.fillText(`Player ${this.currentplayer().getname()} has Finished!`, x, y);
        }
    }

    drawcar(player) {
        const car = player.getcar();
        const turns = car.getturns();
        const color = car.getColor();

        // Draw car trail
        this.ctx.strokeStyle = color;
        this.ctx.lineWidth = 2;
        this.ctx.beginPath();

        for (let i = 1; i <= turns; i++) {
            const hist = car.gethistory(i);
            const pos = car.gethistory(i - 1);
            
            let x = car.getStartPos().getX();
            let y = car.getStartPos().getY();
            
            for (let j = 0; j < i; j++) {
                const h = car.gethistory(j);
                x += h.getDeltaX();
                y += h.getDeltaY();
            }

            if (i === 1) {
                this.ctx.moveTo(car.getStartPos().getX() * this.gridsize, 
                               car.getStartPos().getY() * this.gridsize);
            }
            this.ctx.lineTo(x * this.gridsize, y * this.gridsize);
        }
        this.ctx.stroke();

        // Draw car position
        const pos = car.getCurrentPos();
        this.fillcircle(pos.getX(), pos.getY(), color);
    }

    drawcursor() {
        const player = this.currentplayer();
        const pos = player.getcar().getCurrentPos();
        const step = player.getcar().getLastStep();

        const cx = pos.getX() + step.getDeltaX();
        const cy = pos.getY() + step.getDeltaY();

        // Draw 9 possible moves
        for (let dx = -1; dx <= 1; dx++) {
            for (let dy = -1; dy <= 1; dy++) {
                const x = cx + dx;
                const y = cy + dy;
                this.drawcircle(x, y, '#FFFF00');
            }
        }

        // Draw current velocity indicator
        this.drawcircle(cx, cy, '#00FF00');
    }

    drawcircle(x, y, color) {
        const s = Math.floor(this.gridsize / 3) - 1;
        this.ctx.strokeStyle = color;
        this.ctx.lineWidth = 2;
        this.ctx.beginPath();
        this.ctx.arc(x * this.gridsize, y * this.gridsize, s, 0, 2 * Math.PI);
        this.ctx.stroke();
    }

    fillcircle(x, y, color) {
        const s = Math.floor(this.gridsize / 3) - 1;
        this.ctx.fillStyle = color;
        this.ctx.beginPath();
        this.ctx.arc(x * this.gridsize, y * this.gridsize, s, 0, 2 * Math.PI);
        this.ctx.fill();
    }

    updateUI() {
        const player = this.currentplayer();
        const playerInfo = document.getElementById('player-info');
        const turnInfo = document.getElementById('turn-info');
        const messageInfo = document.getElementById('message-info');

        if (playerInfo) {
            playerInfo.style.color = player.getcar().getColor();
            playerInfo.textContent = `Player: ${player.getname()}, Moving: ${player.getcar().getLastStep()}`;
        }

        if (turnInfo) {
            turnInfo.textContent = `Turn: ${player.getcar().getplayerturns()}`;
        }

        if (messageInfo && !this.finished()) {
            if (player.isai()) {
                messageInfo.textContent = "Click or press a key";
            } else {
                messageInfo.textContent = "Do a move";
            }
        }
    }

    setMessage(msg) {
        const messageInfo = document.getElementById('message-info');
        if (messageInfo) {
            messageInfo.textContent = msg;
        }
    }

    scrollToCurrentPlayer() {
        if (!this.gamestarted) return;

        const player = this.currentplayer();
        const pos = player.getcar().getCurrentPos();
        
        const canvasContainer = document.getElementById('canvas-container');
        if (canvasContainer) {
            const targetX = pos.getX() * this.gridsize - canvasContainer.clientWidth / 2;
            const targetY = pos.getY() * this.gridsize - canvasContainer.clientHeight / 2;

            canvasContainer.scrollTo({
                left: Math.max(0, targetX),
                top: Math.max(0, targetY),
                behavior: 'smooth'
            });
        }
    }

    handleClick(x, y) {
        if (this.wait || this.finished()) {
            return;
        }
        this.wait = true;

        const gridX = Math.round(x / this.gridsize);
        const gridY = Math.round(y / this.gridsize);

        if (this.currentplayer().getType() !== Player.HUM) {
            this.currentplayer().ask();
        } else {
            this.currentplayer().clicked(gridX, gridY);
        }

        this.wait = false;
        this.render();
    }
}
