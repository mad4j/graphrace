class Circuit {
    constructor(sx, sy, checkpoints, gridsize) {
        this.MG = 5; // Margin around the circuit
        this.sizex = sx + 2 * this.MG;
        this.sizey = sy + 2 * this.MG;
        this.checkpoints = checkpoints;
        this.gridsize = gridsize;
        this.circ = [];
        this.chkx = [];
        this.chky = [];
        this.starty = 0;
        this.startx1 = 0;
        this.startx2 = 0;
        this.correct = false;
    }

    init() {
        // Initialize circuit array
        this.circ = [];
        for (let i = 0; i < this.sizex; i++) {
            this.circ[i] = new Array(this.sizey).fill(0);
        }
        
        this.chkx = new Array(this.checkpoints + this.MG);
        this.chky = new Array(this.checkpoints + this.MG);

        this.trace(); // Generate random circuit
        this.setstart(); // Find start/finish line
        
        this.correct = true;
    }

    trace() {
        let i = 0;
        const rx = Math.floor((this.sizex - 2 * this.MG) / 2);
        const ry = Math.floor((this.sizey - 2 * this.MG) / 2);
        
        // Generate random checkpoints in a circular pattern
        for (let b = 0; b <= 2 * Math.PI; b += (2 * Math.PI) / this.checkpoints) {
            this.chkx[i] = Math.floor((Math.random() * (0.5 * rx) + 0.5 * rx) * Math.cos(b) + rx);
            this.chky[i] = Math.floor((Math.random() * (0.5 * ry) + 0.5 * ry) * Math.sin(b) + ry);
            i++;
        }

        // Save extra checkpoints for completing the circle
        for (i = 0; i < 5; i++) {
            this.chkx[i + this.checkpoints] = this.chkx[i];
            this.chky[i + this.checkpoints] = this.chky[i];
        }

        // Draw lines between checkpoints
        for (i = 0; i < this.checkpoints; i++) {
            let k = i + 1;
            if (i === this.checkpoints - 1) k = 0;

            let rc = (this.chky[k] - this.chky[i]) / (this.chkx[k] - this.chkx[i]);

            if (rc >= 1 || rc < -1) { // Vertical iteration
                rc = 1 / rc;
                if (this.chky[i] < this.chky[k]) {
                    for (let j = 0; j <= (this.chky[k] - this.chky[i]); j++) {
                        this.circ[this.chkx[i] + Math.floor(rc * j) + this.MG][this.chky[i] + j + this.MG] = 1;
                    }
                }
                if (this.chky[i] >= this.chky[k]) {
                    for (let j = 0; j >= (this.chky[k] - this.chky[i]); j--) {
                        this.circ[this.chkx[i] + Math.floor(rc * j) + this.MG][this.chky[i] + j + this.MG] = 1;
                    }
                }
            } else if (this.chkx[i] === this.chkx[k]) { // Vertical exception
                if (this.chky[i] < this.chky[k]) {
                    for (let j = 0; j <= (this.chky[k] - this.chky[i]); j++) {
                        this.circ[this.chkx[i] + this.MG][this.chky[i] + j + this.MG] = 1;
                    }
                }
                if (this.chky[i] >= this.chky[k]) {
                    for (let j = 0; j >= (this.chky[k] - this.chky[i]); j--) {
                        this.circ[this.chkx[i] + this.MG][this.chky[i] + j + this.MG] = 1;
                    }
                }
            } else { // Horizontal iteration
                if (this.chkx[k] > this.chkx[i]) {
                    for (let j = 0; j <= (this.chkx[k] - this.chkx[i]); j++) {
                        this.circ[this.chkx[i] + j + this.MG][this.chky[i] + Math.floor(rc * j) + this.MG] = 1;
                    }
                }
                if (this.chkx[k] <= this.chkx[i]) {
                    for (let j = 0; j >= (this.chkx[k] - this.chkx[i]); j--) {
                        this.circ[this.chkx[i] + j + this.MG][this.chky[i] + Math.floor(rc * j) + this.MG] = 1;
                    }
                }
            }
        }

        // Expand circuit to points around the route
        for (let x = 1; x < this.sizex; x++) {
            for (let y = 1; y < this.sizey; y++) {
                if (this.circ[x][y] === 1) {
                    if (this.circ[x + 1][y] !== 1) this.circ[x + 1][y] = 2;
                    if (this.circ[x - 1][y] !== 1) this.circ[x - 1][y] = 2;
                    if (this.circ[x][y + 1] !== 1) this.circ[x][y + 1] = 2;
                    if (this.circ[x][y - 1] !== 1) this.circ[x][y - 1] = 2;
                    if (this.circ[x + 1][y + 1] !== 1) this.circ[x + 1][y + 1] = 2;
                    if (this.circ[x - 1][y + 1] !== 1) this.circ[x - 1][y + 1] = 2;
                    if (this.circ[x + 1][y - 1] !== 1) this.circ[x + 1][y - 1] = 2;
                    if (this.circ[x - 1][y - 1] !== 1) this.circ[x - 1][y - 1] = 2;
                }
            }
        }

        // Store the circuit
        for (let x = 0; x < this.sizex; x++) {
            for (let y = 0; y < this.sizey; y++) {
                if (this.circ[x][y] === 2) {
                    this.circ[x][y] = 1;
                }
            }
        }
    }

    setstart() {
        this.starty = Math.floor(this.getsizey() / 2);
        let p = new Position(Math.floor(this.getsizex() / 2), this.starty);

        while (this.terrain(p) === 0) {
            p.moveTo(1, 0);
        }

        this.startx1 = p.getX();

        while (this.terrain(p) !== 0) {
            p.moveTo(1, 0);
        }

        this.startx2 = p.getX() + 1;
    }

    terrain(posOrX, y) {
        let x;
        if (posOrX instanceof Position) {
            x = posOrX.getX();
            y = posOrX.getY();
        } else {
            x = posOrX;
        }

        if (x < 0 || x >= this.sizex || y < 0 || y >= this.sizey) {
            return -1;
        }
        return this.circ[x][y];
    }

    getsizex() {
        return this.sizex;
    }

    getsizey() {
        return this.sizey;
    }

    getstarty() {
        return this.starty;
    }

    getstartx1() {
        return this.startx1;
    }

    getstartx2() {
        return this.startx2;
    }

    // Render the circuit to a canvas context
    paint(ctx) {
        const hsize = this.sizex * this.gridsize;
        const vsize = this.sizey * this.gridsize;

        // Clear canvas
        ctx.fillStyle = '#FFFFFF';
        ctx.fillRect(0, 0, hsize, vsize);

        // Draw grid
        ctx.strokeStyle = '#D3D3D3';
        ctx.setLineDash([5, 2]);
        ctx.lineWidth = 1;

        for (let x = 0; x < this.sizex; x++) {
            ctx.beginPath();
            ctx.moveTo(x * this.gridsize, 0);
            ctx.lineTo(x * this.gridsize, vsize);
            ctx.stroke();
        }

        for (let y = 0; y < this.sizey; y++) {
            ctx.beginPath();
            ctx.moveTo(0, y * this.gridsize);
            ctx.lineTo(hsize, y * this.gridsize);
            ctx.stroke();
        }

        ctx.setLineDash([]);

        // Draw track (tarmac areas)
        ctx.fillStyle = '#808080';
        for (let x = 0; x < this.sizex; x++) {
            for (let y = 0; y < this.sizey; y++) {
                if (this.circ[x][y] > 0) {
                    ctx.fillRect(
                        x * this.gridsize - 3,
                        y * this.gridsize - 3,
                        6,
                        6
                    );
                }
            }
        }

        // Draw start/finish line
        this.drawstart(ctx);
    }

    drawstart(ctx) {
        const y = this.starty * this.gridsize;
        const x1 = this.startx1 * this.gridsize;
        const x2 = this.startx2 * this.gridsize;

        ctx.strokeStyle = '#FF0000';
        ctx.lineWidth = 3;
        ctx.beginPath();
        ctx.moveTo(x1, y);
        ctx.lineTo(x2, y);
        ctx.stroke();

        // Draw checkered pattern
        ctx.fillStyle = '#000000';
        const segmentWidth = 5;
        let toggle = true;
        for (let x = x1; x < x2; x += segmentWidth) {
            if (toggle) {
                ctx.fillRect(x, y - 2, segmentWidth, 4);
            }
            toggle = !toggle;
        }
    }
}
