const express = require('express');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const readline = require('readline');

const app = express();
const PORT = 3001;
app.use(cors());

// --- CONSTANTS ---
const REPLAYS_DIR = path.join(__dirname, '..', 'replays');

// --- MOCK DATABASE (Tournaments/Simulations remain mock for now) ---
const mockTournaments = [
    { id: 't1', name: 'High School Championship 2025', tournament_type: 'high_school' },
    { id: 't2', name: 'Middle School Invitational 2025', tournament_type: 'middle_school' },
];
const mockSimulations = [
    { id: 'sim1', tournament_id: 't1', bracket_number: 1 },
    { id: 'sim2', tournament_id: 't1', bracket_number: 2 },
    { id: 'sim3', tournament_id: 't2', bracket_number: 1 },
];


// --- API ENDPOINTS ---

// Mock endpoints for tournaments and simulations remain unchanged
app.get('/api/tournaments', (req, res) => res.json(mockTournaments));
app.get('/api/simulations', (req, res) => res.json(mockSimulations));

// [MODIFIED] Endpoint to list available replays from the filesystem
app.get('/api/replays', (req, res) => {
    fs.readdir(REPLAYS_DIR, (err, files) => {
        if (err) {
            // If the directory doesn't exist or there's an error, return an empty list
            if (err.code === 'ENOENT') {
                console.warn(`Replays directory not found at: ${REPLAYS_DIR}`);
                return res.json([]);
            }
            console.error("Error reading replays directory:", err);
            return res.status(500).json({ error: 'Failed to read replays directory' });
        }
        
        const replayFiles = files
            .filter(file => file.startsWith('replay_') && file.endsWith('.jsonl'))
            .map((file, index) => {
                // Filename format from Java: replay_{war_id}_{timestamp}.jsonl
                const parts = file.replace('.jsonl', '').split('_');
                const warId = parseInt(parts[1], 10) || 0;

                // Correlate replay to a simulation based on index for this mock
                const simId = mockSimulations[index % mockSimulations.length].id;
                
                return {
                    id: file, // Use filename as the unique ID
                    filename: file,
                    simulation_id: simId,
                    round_number: warId, // In the UI this is displayed as "Round #", which is the War ID
                    is_featured: index < 3, // Feature the first 3 for demo purposes
                };
            });
        res.json(replayFiles);
    });
});

// [NEW] Endpoint to get a chunk of events from a specific replay file
app.get('/api/replay/:filename/cycles', async (req, res) => {
    const { filename } = req.params;
    const startCycle = parseInt(req.query.start, 10) || 0;
    const endCycle = parseInt(req.query.end, 10) || (startCycle + 1000);

    // --- Security Check ---
    // Prevent directory traversal attacks. Ensure the filename is simple.
    if (!/^[a-zA-Z0-9_.-]+$/.test(filename) || filename.includes('..')) {
        return res.status(400).json({ error: 'Invalid filename' });
    }

    const filePath = path.join(REPLAYS_DIR, filename);

    // Verify the resolved path is still within the intended directory
    if (!filePath.startsWith(REPLAYS_DIR)) {
        return res.status(400).json({ error: 'Invalid path' });
    }

    try {
        await fs.promises.access(filePath); // Check if file exists

        const fileStream = fs.createReadStream(filePath);
        const rl = readline.createInterface({
            input: fileStream,
            crlfDelay: Infinity
        });

        let warStartData = null;
        const events = [];
        let isFirstLine = true;

        rl.on('line', (line) => {
            try {
                const event = JSON.parse(line);

                // The first line is always WAR_START and should be included in the first chunk
                if (isFirstLine) {
                    if (event.type === 'WAR_START') {
                        warStartData = event.payload;
                    }
                    isFirstLine = false;
                }

                const cycle = event.payload?.round;
                if (cycle !== undefined && cycle >= startCycle && cycle <= endCycle) {
                    events.push(event);
                }
            } catch (parseErr) {
                // Log and ignore corrupted lines
                console.warn(`Skipping corrupted line in ${filename}: ${parseErr.message}`);
            }
        });

        rl.on('close', () => {
            res.json({ warStartData, events });
        });

        rl.on('error', (streamErr) => {
             console.error(`Error streaming file ${filename}:`, streamErr);
             if (!res.headersSent) {
                res.status(500).json({ error: 'Failed to read replay file' });
             }
        });

    } catch (err) {
        if (err.code === 'ENOENT') {
            return res.status(404).json({ error: 'Replay file not found' });
        }
        console.error(`Error accessing file ${filename}:`, err);
        return res.status(500).json({ error: 'Internal server error' });
    }
});


app.listen(PORT, () => {
    console.log(`Backend server listening on http://localhost:${PORT}`);
    console.log(`Serving replays from: ${REPLAYS_DIR}`);
});