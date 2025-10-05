class Step {
    constructor(deltaX, deltaY) {
        this.deltaX = deltaX;
        this.deltaY = deltaY;
    }

    length() {
        return Math.sqrt(this.deltaX * this.deltaX + this.deltaY * this.deltaY);
    }

    getDeltaX() {
        return this.deltaX;
    }

    getDeltaY() {
        return this.deltaY;
    }

    setDeltaX(deltaX) {
        this.deltaX = deltaX;
    }

    setDeltaY(deltaY) {
        this.deltaY = deltaY;
    }

    toString() {
        return `(${this.deltaX}, ${this.deltaY})`;
    }
}
