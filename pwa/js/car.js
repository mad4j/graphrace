class Car {
    constructor(sx, sy, color) {
        this.currentPos = new Position(sx, sy);
        this.startPos = new Position(sx, sy);
        this.color = color;
        this.turn = 0;
        this.fault = new Array(500).fill(false);
        this.faultcount = 0;
        this.hist = new Array(500);
        this.hist[0] = new Step(0, 0);
    }

    move(step) {
        this.currentPos.moveTo(step);
        this.hist[++this.turn] = step;
    }

    markFault() {
        this.fault[this.turn] = true;
        this.faultcount++;
    }

    getCurrentPos() {
        return this.currentPos;
    }

    getStartPos() {
        return this.startPos;
    }

    getturns() {
        return this.turn;
    }

    getplayerturns() {
        return this.turn - this.faultcount;
    }

    gethistory(i) {
        return this.hist[i];
    }

    getLastStep() {
        return this.hist[this.turn];
    }

    getspeed() {
        return this.hist[this.turn].length();
    }

    getColor() {
        return this.color;
    }

    toString() {
        return `Car[${this.currentPos} - ${this.getLastStep()}]`;
    }
}
