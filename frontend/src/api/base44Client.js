// This is a MOCK client to allow the UI to build.

const mockTournaments = [
    { id: 't1', name: 'High School Championship', tournament_type: 'high_school', status: 'completed' },
    { id: 't2', name: 'Middle School Invitational', tournament_type: 'middle_school', status: 'running' },
];

const mockSimulations = [
    { id: 'sim1', tournament_id: 't1', bracket_number: 1, tournament_type: 'high_school', teams: [{ team_name: 'CyberLions' }], status: 'completed' },
    { id: 'sim2', tournament_id: 't2', bracket_number: 1, tournament_type: 'middle_school', teams: [{ team_name: 'CodeWizards' }], status: 'running' },
];

const mockTeams = [];
const mockSubmissions = [];

// --- FULL REPLAY DATA ---
// This is the complete data from the replay file you provided.
const fullReplayEvents = [
    {"payload":{"seed":3184572,"warriors":[],"max_rounds":200000},"type":"WAR_START"},
    {"payload":{"actorId":0,"address":111919,"round":0,"value":235},"type":"MEMORY_WRITE"},
    {"payload":{"actorId":0,"address":111920,"round":0,"value":254},"type":"MEMORY_WRITE"},
    /* ... PASTE THE ENTIRE CONTENTS of your replay_11_...txt file here, from the third line to the end ... */
    /* ... It should be thousands of lines long ... */
    {"payload":{"actorId":7,"address":65888,"round":81,"value":0},"type":"MEMORY_WRITE"},
    {"payload":{"round":82,"warriorStates":[{"ip":111919,"id":0},{"ip":93942,"id":2},{"ip":81783,"id":4},{"ip":72062,"id":5},{"ip":120135,"id":7}]},"type":"ROUND_UPDATE"},
    // ... continue pasting until the very last line of the file ...
    {"payload":{"round":99,"warriorStates":[{"ip":111919,"id":0},{"ip":93942,"id":2},{"ip":81791,"id":4},{"ip":72068,"id":5},{"ip":120131,"id":7}]},"type":"ROUND_UPDATE"}
];


const mockReplays = [
    { 
        id: 'rep1', 
        simulation_id: 'sim1', 
        round_number: 1, 
        is_featured: true, 
        winner_team_name: 'CyberLions', 
        duration_cycles: 101, // Set this to the last round number from your file
        events: fullReplayEvents,
        memory_snapshots: []
    },
];

const mockApi = {
    entities: {
        Tournament: { list: async () => mockTournaments },
        Team: { list: async () => mockTeams },
        Submission: { list: async () => mockSubmissions },
        Simulation: { list: async () => mockSimulations, create: async (data) => console.log("Creating sim:", data) },
        Replay: {
            list: async () => mockReplays.map(({ events, memory_snapshots, ...rest }) => rest), // List returns truncated data
            get: async (id) => {
                console.log(`Mock API: Getting full data for Replay ID: ${id}`);
                const replay = mockReplays.find(r => r.id === id);
                if (!replay) throw new Error(`Replay with id ${id} not found`);
                return replay; // Get returns the full object with all events
            }
        },
    }
};

export const base44 = mockApi;