// PlaybackControl.js

class PlaybackControl {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        this.running = false;
        this.direction = 'forward';
        this.speed = 1;
        this.speedLevels = [1, 2, 4, 8, 16];
        this.currentSpeedIndex = 0;
        this.currentCycle = 0;
        this.maxCycle = 0;

        this.icons = {
            playForward: 'icons/play-forward.png',
            playBackward: 'icons/play-backward.png',
            pause: 'icons/pause.png',
            rewind: 'icons/rewind.png',
            fastForward: 'icons/fast-forward.png',
            jumpStart: 'icons/jump-start.png',
            jumpEnd: 'icons/jump-end.png'
        };
        
        this.render();
        this.bindEvents();
    }

    render() {
        this.container.innerHTML = `
            <div class="playback-control">
                <div class="event-input-section">
                    <label class="event-label">Event Cycles (comma-separated)</label>
                    <input type="text" class="event-input" placeholder="e.g., 100, 500, 1000, 5000" />
                </div>
                <div class="round-counter">
                    <span class="round-label">CYCLE</span>
                    <span class="cycle-value">0</span>
                    <span class="cycle-max">/ 0</span>
                </div>
                <div class="controls">
                    <button class="control-btn" data-action="prev" title="Previous Event / Jump to Start"><img src="${this.icons.jumpStart}" alt="Previous Event"></button>
                    <button class="control-btn" data-action="slowDown" title="Slow Down / Reverse"><img src="${this.icons.rewind}" alt="Rewind"></button>
                    <button class="control-btn center" data-action="playPause" title="Play"><img src="${this.icons.playForward}" alt="Play"></button>
                    <button class="control-btn" data-action="speedUp" title="Speed Up / Forward"><img src="${this.icons.fastForward}" alt="Fast Forward"></button>
                    <button class="control-btn" data-action="next" title="Next Event / Jump to End"><img src="${this.icons.jumpEnd}" alt="Next Event"></button>
                </div>
            </div>
        `;

        this.centerBtn = this.container.querySelector('[data-action="playPause"]');
        this.centerBtnIcon = this.centerBtn.querySelector('img');
        this.cycleValue = this.container.querySelector('.cycle-value');
        this.cycleMax = this.container.querySelector('.cycle-max');
        this.eventInput = this.container.querySelector('.event-input');
    }

    bindEvents() {
        this.container.querySelector('[data-action="prev"]').addEventListener('click', () => this.dispatchEvent('prev'));
        this.container.querySelector('[data-action="next"]').addEventListener('click', () => this.dispatchEvent('next'));
        this.container.querySelector('[data-action="slowDown"]').addEventListener('click', () => this.slowDown());
        this.container.querySelector('[data-action="playPause"]').addEventListener('click', () => this.playPause());
        this.container.querySelector('[data-action="speedUp"]').addEventListener('click', () => this.speedUp());

        this.eventInput.addEventListener('input', (e) => {
            const cycles = e.target.value.split(',').map(s => parseInt(s.trim())).filter(n => !isNaN(n));
            this.dispatchEvent('setEvents', { cycles: [...new Set(cycles)].sort((a, b) => a - b) });
        });
    }
    
    updateDisplay(cycle, maxCycle) {
        this.currentCycle = cycle;
        this.maxCycle = maxCycle;
        this.cycleValue.textContent = Math.floor(this.currentCycle).toLocaleString();
        this.cycleMax.textContent = `/ ${this.maxCycle.toLocaleString()}`;
    }

    updateCenterButtonIcon() {
        if (this.running) {
            this.centerBtnIcon.src = this.icons.pause;
            this.centerBtnIcon.alt = 'Pause';
            this.centerBtn.classList.add('playing');
        } else {
            this.centerBtnIcon.src = this.direction === 'forward' ? this.icons.playForward : this.icons.playBackward;
            this.centerBtnIcon.alt = this.direction === 'forward' ? 'Play Forward' : 'Play Backward';
            this.centerBtn.classList.remove('playing');
        }
    }

    slowDown() {
        if (!this.running) {
            if (this.direction !== 'backward') {
                this.direction = 'backward';
                this.updateCenterButtonIcon();
                this.dispatchEvent('setDirection', { direction: 'backward' });
            }
        } else {
            if (this.currentSpeedIndex > 0) {
                this.currentSpeedIndex--;
                this.speed = this.speedLevels[this.currentSpeedIndex];
                this.dispatchEvent('setSpeed', { speed: this.speed });
            }
        }
    }

    speedUp() {
        if (!this.running) {
            if (this.direction !== 'forward') {
                this.direction = 'forward';
                this.updateCenterButtonIcon();
                this.dispatchEvent('setDirection', { direction: 'forward' });
            }
        } else {
            if (this.currentSpeedIndex < this.speedLevels.length - 1) {
                this.currentSpeedIndex++;
                this.speed = this.speedLevels[this.currentSpeedIndex];
                this.dispatchEvent('setSpeed', { speed: this.speed });
            }
        }
    }

    playPause() {
        this.running = !this.running;
        if (this.running) {
            this.dispatchEvent('play', { speed: this.speed, direction: this.direction });
        } else {
            this.dispatchEvent('pause', {});
        }
        this.updateCenterButtonIcon();
    }

    dispatchEvent(eventName, detail = {}) {
        const event = new CustomEvent(eventName, { detail, bubbles: true });
        this.container.dispatchEvent(event);
    }
    
    onPlaybackEnd() {
        this.running = false;
        this.updateCenterButtonIcon();
    }
}