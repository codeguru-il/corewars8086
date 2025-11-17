// ReplayManager.js

class ReplayManager {
    constructor() {
        this.eventsByCycle = new Map();
        this.warStartData = null;
        this.totalCycles = 0;
    }

    load(file) {
        return new Promise((resolve, reject) => {
            const reader = new FileReader();
            reader.onload = (event) => {
                try {
                    this._parse(event.target.result);
                    resolve(true);
                } catch (e) {
                    console.error("Error parsing replay file:", e);
                    reject(e);
                }
            };
            reader.onerror = (error) => reject(error);
            reader.readAsText(file);
        });
    }

    _parse(fileContent) {
        this.eventsByCycle.clear();
        this.totalCycles = 0;
        
        const lines = fileContent.split(/\r?\n/).filter(line => line.trim() !== '');

        if (lines.length === 0) throw new Error("Replay file is empty or invalid.");
        
        lines.forEach((line, index) => {
            try {
                const event = JSON.parse(line);
                const payload = event.payload;
                // The data key is 'round', but we will call it 'cycle' in our application logic
                const cycle = payload.round; 

                if (event.type === "WAR_START") {
                    this.warStartData = payload;
                }

                if (cycle !== undefined) {
                    if (!this.eventsByCycle.has(cycle)) {
                        this.eventsByCycle.set(cycle, []);
                    }
                    this.eventsByCycle.get(cycle).push(event);
                }

                if (event.type === 'WAR_END') {
                    this.totalCycles = cycle;
                }
            } catch (e) {
                throw new Error(`Failed to parse JSON on line ${index + 1}: ${e.message}.`);
            }
        });

        if (!this.warStartData) throw new Error("Replay file must start with a WAR_START event.");

        if (this.totalCycles === 0) {
            this.totalCycles = this.warStartData.max_rounds || 200000;
        }
    }

    getWarStartData() {
        return this.warStartData;
    }

    getEventsInCycle(cycle) {
        return this.eventsByCycle.get(cycle) || [];
    }

    getTotalCycles() {
        return this.totalCycles;
    }
}