import React, { useState, useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { ChevronRight, ChevronDown, PlaySquare, Trophy, Circle, ServerCrash } from "lucide-react";

const formatTournamentName = (name) => {
    if (!name) return "General";
    return name.replace(/_/g, ' ').replace(/\b\w/g, char => char.toUpperCase());
};

const SESSION_STORAGE_KEY = 'simulationTreeState';

export default function SimulationTreeSidebar({ onReplaySelect, selectedReplayId }) {
    const [expandedTournaments, setExpandedTournaments] = useState(() => {
        try {
            const savedState = sessionStorage.getItem(SESSION_STORAGE_KEY);
            return savedState ? new Set(JSON.parse(savedState)) : new Set();
        } catch { return new Set(); }
    });

    useEffect(() => {
        sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(Array.from(expandedTournaments)));
    }, [expandedTournaments]);

    const { data: tournaments = [], isError, isLoading } = useQuery({
        queryKey: ['tournaments'],
        queryFn: () => fetch('http://localhost:3001/api/tournaments').then(res => res.json())
    });
    const { data: simulations = [] } = useQuery({ queryKey: ['simulations'], queryFn: () => fetch('http://localhost:3001/api/simulations').then(res => res.json()) });
    const { data: allReplays = [] } = useQuery({ queryKey: ['replays'], queryFn: () => fetch('http://localhost:3001/api/replays').then(res => res.json()) });

    const toggleTournament = (tourneyId) => {
        const newExpanded = new Set(expandedTournaments);
        if (newExpanded.has(tourneyId)) newExpanded.delete(tourneyId);
        else newExpanded.add(tourneyId);
        setExpandedTournaments(newExpanded);
    };

    const getCircleColor = (index) => {
      if (index === 0) return 'text-yellow-400'; // Gold for the 1st item
      if (index === 1) return 'text-gray-400';   // Silver for the 2nd item
      if (index === 2) return 'text-orange-400'; // Bronze for the 3rd item
      return 'text-green-700';                   // A default color for the rest
    };

    return (
        <div className="flex flex-col h-full bg-background">
            <div className="p-4 border-b border-border">
                <h2 className="text-lg font-semibold text-primary flex items-center gap-2">
                    <Trophy className="w-5 h-5" /> Tournaments
                </h2>
            </div>
            <div className="flex-1 overflow-y-auto p-2">
                {isLoading ? <p className="p-4 text-sm text-muted-foreground">Loading...</p> :
                 isError ? (
                    <div className="p-3 m-2 text-destructive-foreground bg-destructive/20 border border-destructive rounded-lg">
                        <div className="flex items-center gap-2 font-semibold"><ServerCrash className="w-5 h-5"/> Connection Error</div>
                        <p className="text-sm mt-2">Failed to connect to backend server.</p>
                    </div>
                 ) : tournaments.length > 0 ? (
                    tournaments.map((tournament) => {
                        const isTournamentExpanded = expandedTournaments.has(tournament.id);
                        const tournamentSimIds = new Set(simulations.filter(sim => sim.tournament_id === tournament.id).map(sim => sim.id));
                        const tournamentReplays = allReplays.filter(replay => tournamentSimIds.has(replay.simulation_id));

                        return (
                            <div key={tournament.id} className="mb-1">
                                <button onClick={() => toggleTournament(tournament.id)} className="w-full flex items-center gap-2 px-3 py-2 text-left hover:bg-muted rounded-lg transition-colors group text-sm font-semibold">
                                    {isTournamentExpanded ? <ChevronDown className="w-4 h-4" /> : <ChevronRight className="w-4 h-4" />}
                                    <span className="text-foreground">{formatTournamentName(tournament.name)}</span>
                                </button>
                                {isTournamentExpanded && (
                                    <div className="ml-4 mt-1 space-y-1 border-l-2 border-border pl-4">
                                        {tournamentReplays.length > 0 ? (
                                            tournamentReplays.map((replay, index) => {
                                                const isActive = replay.id === selectedReplayId;
                                                return (
                                                    <button key={replay.id} onClick={() => onReplaySelect(replay)} className={`w-full text-left flex items-center gap-3 p-2 rounded-lg transition-colors ${isActive ? 'bg-primary/20' : 'hover:bg-muted'}`}>
                                                        <PlaySquare className={`w-4 h-4 shrink-0 ${isActive ? 'text-primary' : 'text-muted-foreground'}`} />
                                                        <div className="flex-1 min-w-0">
                                                            <div className={`text-sm truncate ${isActive ? 'text-primary font-semibold' : 'text-foreground'}`}>
                                                                {replay.filename.replace('.jsonl', '').replace(/_/g, ' ')}
                                                            </div>
                                                        </div>
                                                    </button>
                                                )
                                            })
                                        ) : (
                                            <div className="px-3 py-2 text-xs text-muted-foreground">No replays for this tournament.</div>
                                        )}
                                    </div>
                                )}
                            </div>
                        );
                    })
                ) : (
                    <div className="text-center py-12 text-muted-foreground">
                        <Trophy className="w-12 h-12 mx-auto mb-3" />
                        <p className="text-sm">No tournaments found</p>
                    </div>
                )}
            </div>
        </div>
    );
}