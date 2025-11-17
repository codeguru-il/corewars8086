// dashboard.js

document.addEventListener('DOMContentLoaded', () => {
    // --- DOM Element References ---
    const fileInput = document.getElementById('file-input');
    const warriorList = document.querySelector('#warrior-list ul');
    const eventLog = document.querySelector('#event-log textarea');
    const playbackContainer = document.getElementById('playback-container');

    // --- Component Instances ---
    const replayManager = new ReplayManager();
    const replayCanvas = new ReplayCanvas('replay-canvas');
    const playbackControl = new PlaybackControl('playback-container');

    // --- State Variables ---
    let isPlaying = false;
    let currentCycle = 0;
    let playbackInterval = null;
    let playbackDirection = 'forward';
    let playbackSpeed = 1;
    let eventCycles = []; // The list of user-defined event cycles

    // --- Event Listeners ---
    fileInput.addEventListener('change', async (event) => {
        const file = event.target.files[0];
        if (!file) return;
        stopPlayback();
        try {
            await replayManager.load(file);
            initializeDashboard();
        } catch (e) {
            alert('Failed to load or parse replay file.');
            console.error(e);
        }
    });
    
    playbackContainer.addEventListener('play', (e) => {
        playbackDirection = e.detail.direction;
        playbackSpeed = e.detail.speed;
        startPlayback();
    });

    playbackContainer.addEventListener('pause', () => stopPlayback());

    playbackContainer.addEventListener('setSpeed', (e) => {
        playbackSpeed = e.detail.speed;
        if (isPlaying) {
            clearInterval(playbackInterval);
            startPlayback();
        }
    });
    
    playbackContainer.addEventListener('setDirection', (e) => {
        playbackDirection = e.detail.direction;
    });

    // --- FIX: Handle new 'prev' and 'next' events ---
    playbackContainer.addEventListener('prev', () => {
        let targetCycle = 0; // Default to start
        if (eventCycles.length > 0) {
            // Find the last event cycle that is less than the current one
            for (let i = eventCycles.length - 1; i >= 0; i--) {
                if (eventCycles[i] < currentCycle) {
                    targetCycle = eventCycles[i];
                    break;
                }
            }
        }
        jumpToCycle(targetCycle);
    });
    
    playbackContainer.addEventListener('next', () => {
        let targetCycle = replayManager.getTotalCycles(); // Default to end
        if (eventCycles.length > 0) {
            // Find the first event cycle that is greater than the current one
            for (let i = 0; i < eventCycles.length; i++) {
                if (eventCycles[i] > currentCycle) {
                    targetCycle = eventCycles[i];
                    break;
                }
            }
        }
        jumpToCycle(targetCycle);
    });
    
    playbackContainer.addEventListener('setEvents', (e) => {
        eventCycles = e.detail.cycles;
        logEvent(`Event cycles set to: ${eventCycles.join(', ')}`);
    });

    // --- Core Functions ---
    function initializeDashboard() {
        stopPlayback();
        currentCycle = 0;
        eventCycles = []; // Clear events on new file load
        const startData = replayManager.getWarStartData();
        
        playbackControl.onPlaybackEnd(); 
        
        replayCanvas.clear();
        warriorList.innerHTML = '';
        eventLog.value = '';
        startData.warriors.forEach((warrior, id) => {
            const li = document.createElement('li');
            li.textContent = warrior.name;
            li.style.backgroundColor = replayCanvas.warriorColors[id % replayCanvas.warriorColors.length];
            li.id = `warrior-${id}`;
            warriorList.appendChild(li);
        });

        jumpToCycle(0);
        logEvent('Replay loaded. Press Play to start.');
    }

    function startPlayback() {
        clearInterval(playbackInterval);
        isPlaying = true;
        
        if (playbackDirection === 'forward' && currentCycle >= replayManager.getTotalCycles()) {
            currentCycle = 0;
        } else if (playbackDirection === 'backward' && currentCycle <= 0) {
            currentCycle = replayManager.getTotalCycles();
        }

        const cyclesPerSecond = 60 * playbackSpeed;
        const delay = Math.max(1, 1000 / cyclesPerSecond);

        playbackInterval = setInterval(() => {
            const stepAmount = playbackDirection === 'forward' ? 1 : -1;
            jumpToCycle(currentCycle + stepAmount);
        }, delay);
    }

    function stopPlayback() {
        isPlaying = false;
        clearInterval(playbackInterval);
        playbackInterval = null;
        if (playbackControl.running) {
            playbackControl.onPlaybackEnd();
        }
    }
    
    function jumpToCycle(cycle) {
        let reachedBoundary = false;
        const maxCycle = replayManager.getTotalCycles();
        if (cycle > maxCycle) {
            cycle = maxCycle;
            if (isPlaying) logEvent('Reached end of replay.');
            reachedBoundary = true;
        }
        if (cycle < 0) {
            cycle = 0;
            if (isPlaying) logEvent('Reached start of replay.');
            reachedBoundary = true;
        }

        currentCycle = cycle;
        drawCycle(currentCycle);

        if (reachedBoundary && isPlaying) {
            stopPlayback();
        }
    }

    function drawCycle(cycle) {
        const prevCycleToClear = cycle > 0 ? cycle - 1 : 0;
        const nextCycleToClear = cycle + 1;
        
        replayCanvas.applyEvents(replayManager.getEventsInCycle(prevCycleToClear));
        replayCanvas.applyEvents(replayManager.getEventsInCycle(nextCycleToClear));

        const currentEvents = replayManager.getEventsInCycle(cycle);
        replayCanvas.applyEvents(currentEvents); 
        
        const cycleUpdateEvent = currentEvents.find(e => e.type === 'ROUND_UPDATE');
        if (cycleUpdateEvent) {
            replayCanvas.drawPointers(cycleUpdateEvent.payload.warriorStates);
        }

        currentEvents.forEach(e => {
            if (e.type === 'WARRIOR_DEATH') {
                const payload = e.payload;
                const warriorElem = document.getElementById(`warrior-${payload.warrior_id}`);
                if (warriorElem && !warriorElem.style.textDecoration) {
                    warriorElem.style.textDecoration = 'line-through';
                    warriorElem.style.opacity = '0.6';
                    logEvent(`Cycle ${cycle}: ${payload.warriorName} died.`);
                }
            }
        });
        
        playbackControl.updateDisplay(cycle + 1, replayManager.getTotalCycles());
    }

    function logEvent(message) {
        eventLog.value += message + '\n';
        eventLog.scrollTop = eventLog.scrollHeight;
    }
});