class Position {
    constructor(x, y) {
        this.x = x;
        this.y = y;
    }

    getX() {
        return this.x;
    }

    setX(x) {
        this.x = x;
    }

    getY() {
        return this.y;
    }

    setY(y) {
        this.y = y;
    }

    moveTo(stepOrDeltaX, deltaY) {
        if (stepOrDeltaX instanceof Step) {
            this.x += stepOrDeltaX.getDeltaX();
            this.y += stepOrDeltaX.getDeltaY();
        } else {
            this.x += stepOrDeltaX;
            this.y += deltaY;
        }
    }

    toString() {
        return `(${this.x}, ${this.y})`;
    }
}
