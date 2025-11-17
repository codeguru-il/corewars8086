class ReplayCanvas {
    constructor(canvasId) {
        this.canvas = document.getElementById(canvasId);
        this.ctx = this.canvas.getContext('2d');

        this.BOARD_WIDTH = 256;
        this.DOT_SIZE = 3;
        this.canvas.width = this.BOARD_WIDTH * this.DOT_SIZE;
        this.canvas.height = (65536 / this.BOARD_WIDTH) * this.DOT_SIZE;

        this.warriorColors = [
            '#4285F4', '#DB4437', '#F4B400', '#0F9D58',
            '#e91e63', '#9c27b0', '#673ab7', '#3f51b5',
            '#00bcd4', '#ff9800', '#795548', '#607d8b'
        ];
        this.pointerColors = this.warriorColors.map(c => this._brightenColor(c));
    }

    _brightenColor(hex) {
        const r = parseInt(hex.slice(1, 3), 16),
              g = parseInt(hex.slice(3, 5), 16),
              b = parseInt(hex.slice(5, 7), 16);
        return `rgb(${Math.min(255, r + 90)}, ${Math.min(255, g + 90)}, ${Math.min(255, b + 90)})`;
    }

    clear() {
        this.ctx.fillStyle = '#000000';
        this.ctx.fillRect(0, 0, this.canvas.width, this.canvas.height);
    }

    drawInitialState(warriors) {
        // This is now handled by processing cycle 1 events in the main dashboard
        this.clear();
    }

    applyEvents(events) {
        events.forEach(event => {
            if (event.type === 'MEMORY_WRITE') {
                const payload = event.payload;
                const arenaOffset = payload.address - 0x10000;
                const x = (arenaOffset % this.BOARD_WIDTH) * this.DOT_SIZE;
                const y = Math.floor(arenaOffset / this.BOARD_WIDTH) * this.DOT_SIZE;
                this.ctx.fillStyle = this.warriorColors[payload.actorId % this.warriorColors.length];
                this.ctx.fillRect(x, y, this.DOT_SIZE, this.DOT_SIZE);
            }
        });
    }

    drawPointers(warriorStates) {
        warriorStates.forEach(state => {
            const arenaOffset = state.ip - 0x10000;
            const x = (arenaOffset % this.BOARD_WIDTH) * this.DOT_SIZE;
            const y = Math.floor(arenaOffset / this.BOARD_WIDTH) * this.DOT_SIZE;
            this.ctx.fillStyle = this.pointerColors[state.id % this.pointerColors.length];
            this.ctx.fillRect(x, y, this.DOT_SIZE, this.DOT_SIZE);
        });
    }
}