class Player {
    static LEFT = 0;
    static RIGHT = 1;
    static UP = 2;
    static DOWN = 3;
    static SAME = 4;
    static COM = 0;
    static HUM = 1;

    constructor(name, type, circuit, game, startx, starty, color) {
        this.name = name;
        this.type = type;
        this.circuit = circuit;
        this.game = game;
        this.car = new Car(startx, starty, color);
        this.cx = 0;
        this.cy = 0;
        this.level = 0;
        this.i = 0;
        this.j = 0;
        this.distance = 0;
        this.grass = false;
    }

    ask() {
        if (this.type === Player.COM) {
            this.game.setMessage("Thinking...");
            this.game.wait = true;
            setTimeout(() => this.moveai(), 100);
        }
    }

    isai() {
        return this.type === Player.COM;
    }

    type() {
        return this.type;
    }

    clicked(x, y) {
        const cx = this.car.getCurrentPos().getX() + this.car.getLastStep().getDeltaX();
        const cy = this.car.getCurrentPos().getY() + this.car.getLastStep().getDeltaY();
        let m = -1;

        if (x === cx && y === cy) {
            m = Player.SAME;
        } else if (cx - x === -1 && cy - y === 0) {
            m = Player.RIGHT;
        } else if (cx - x === 1 && cy - y === 0) {
            m = Player.LEFT;
        } else if (cx - x === 0 && cy - y === -1) {
            m = Player.DOWN;
        } else if (cx - x === 0 && cy - y === 1) {
            m = Player.UP;
        }

        if (m >= 0) {
            this.move(m);
        }
    }

    move(m) {
        const v = this.car.getLastStep();
        if (this.game.finished()) {
            return;
        }

        switch (m) {
            case Player.LEFT:
                this.movecar(v.getDeltaX() - 1, v.getDeltaY());
                break;
            case Player.RIGHT:
                this.movecar(v.getDeltaX() + 1, v.getDeltaY());
                break;
            case Player.UP:
                this.movecar(v.getDeltaX(), v.getDeltaY() - 1);
                break;
            case Player.DOWN:
                this.movecar(v.getDeltaX(), v.getDeltaY() + 1);
                break;
            case Player.SAME:
                this.movecar(v.getDeltaX(), v.getDeltaY());
                break;
        }

        this.game.nextplayer();
        this.game.wait = false;
    }

    movecar(x, y) {
        this.car.move(new Step(x, y));
    }

    checkterrain() {
        if (this.circuit.terrain(this.car.getCurrentPos()) === 0) {
            this.car.move(new Step(0, 0));
            this.car.fault();
        }
    }

    getname() {
        return this.name;
    }

    getcar() {
        return this.car;
    }

    toString() {
        return this.getname();
    }

    moveai() {
        if (!this.isai()) {
            return;
        }

        this.cx = Math.floor(this.circuit.getsizex() / 2);
        this.cy = Math.floor(this.circuit.getsizey() / 2);

        this.level = 6 + Math.round(this.car.getspeed() * 1.4);
        this.grass = this.circuit.terrain(this.car.getCurrentPos()) <= 0;

        const v = this.car.getLastStep();
        this.distance = Math.atan2(
            -(this.cy - this.car.getCurrentPos().getY()),
            this.cx - this.car.getCurrentPos().getX()
        ) + Math.PI;

        const mv = this.ai(
            this.car.getCurrentPos().getX(),
            this.car.getCurrentPos().getY(),
            v.getDeltaX(),
            v.getDeltaY(),
            this.level
        );

        this.move(Math.floor(mv));
    }

    ai(x, y, vx, vy, l) {
        const e = [0, 0, 0, 0, 0];

        let d = Math.atan2(-(this.cy - y), this.cx - x) + Math.PI;

        // Handle wrap-around for circular distance
        if (d - this.distance > Math.PI) {
            d -= 2 * Math.PI;
        }
        if (d - this.distance < -Math.PI) {
            d += 2 * Math.PI;
        }

        if (l > 0) {
            // Check borders
            if (this.circuit.terrain(x, y) <= 0) {
                vx = 0;
                vy = 0;
                if (!this.grass) {
                    l -= 2; // Penalty for hitting grass
                }
            }

            // Search all possible routes
            if (l > 0) {
                e[0] = this.ai(x + vx - 1, y + vy, vx - 1, vy, l - 1);
                e[1] = this.ai(x + vx + 1, y + vy, vx + 1, vy, l - 1);
                e[2] = this.ai(x + vx, y + vy - 1, vx, vy - 1, l - 1);
                e[3] = this.ai(x + vx, y + vy + 1, vx, vy + 1, l - 1);
                e[4] = this.ai(x + vx, y + vy, vx, vy, l - 1);

                // Find the best direction
                for (let i = 0; i < 5; i++) {
                    if (e[i] > d) {
                        d = e[i];
                        this.j = i;
                    }
                }
            }
        }

        if (l === this.level) {
            return this.j; // Return best move
        }
        return d; // Return distance
    }
}
