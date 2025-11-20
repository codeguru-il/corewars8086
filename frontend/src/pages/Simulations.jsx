import React, { useState, useEffect, useRef, useCallback } from "react";
import { Zap, ArrowLeft, ServerCrash, PlaySquare } from "lucide-react";
import SimulationTreeSidebar from "../components/simulations/SimulationTreeSidebar.jsx";
import ReplayViewer from "./ReplayViewer.jsx";

const SESSION_STORAGE_KEY = 'simulations_selectedReplay';
const CHUNK_SIZE = 1000;

export default function Simulations() {
    const getInitialState = () => {
        try {
            const saved = sessionStorage.getItem(SESSION_STORAGE_KEY);
            return saved ? JSON.parse(saved) : null;
        } catch { return null; }
    };

    const [selectedReplayInfo, setSelectedReplayInfo] = useState(getInitialState);
    const [activeReplay, setActiveReplay] = useState(null);
    const [events, setEvents] = useState(new Map());
    const [isLoading, setIsLoading] = useState(false);
    const [error, setError] = useState(null);
    
    const [isPlaying, setIsPlaying] = useState(false);
    const [currentCycle, setCurrentCycle] = useState(0);
    const [playbackSpeed, setPlaybackSpeed] = useState(1);
    const [playbackDirection, setPlaybackDirection] = useState('forward');
    const [eventCycles, setEventCycles] = useState([]);
    
    const canvasApi = useRef(null);
    const animationFrameId = useRef(null);

    const handleReplaySelect = (replay) => {
        setSelectedReplayInfo(prev => (prev?.id === replay.id ? null : replay));
    };

    const jumpToCycle = useCallback((cycle) => {
        if (!activeReplay || !canvasApi.current) return;

        const totalCycles = activeReplay.max_rounds || 0;
        let targetCycle = Math.floor(Math.max(0, Math.min(cycle, totalCycles)));

        const getEventsFromChunk = (c) => {
            const chunkStart = Math.floor(c / CHUNK_SIZE) * CHUNK_SIZE;
            const chunk = events.get(chunkStart);
            return chunk ? chunk.filter(e => e.payload.round === c) : [];
        };
        
        // Redraw previous state to clear old pointers, then draw current state
        canvasApi.current.clear();
        const relevantEvents = [];
        for (let i = 0; i <= targetCycle; i++) {
             const chunkStart = Math.floor(i / CHUNK_SIZE) * CHUNK_SIZE;
             const chunk = events.get(chunkStart);
             if (chunk) {
                const cycleEvents = chunk.filter(e => e.payload.round === i && e.type === 'MEMORY_WRITE');
                if(cycleEvents.length > 0) relevantEvents.push(...cycleEvents);
             }
        }
        canvasApi.current.applyEvents(relevantEvents);

        const updateEvent = getEventsFromChunk(targetCycle).find(e => e.type === 'ROUND_UPDATE');
        if (updateEvent) {
            canvasApi.current.drawPointers(updateEvent.payload.warriorStates);
        }

        setCurrentCycle(targetCycle);

        if ((targetCycle >= totalCycles && playbackDirection === 'forward') || (targetCycle <= 0 && playbackDirection === 'backward')) {
            setIsPlaying(false);
        }
    }, [activeReplay, events, playbackDirection]);

    const fetchCycleChunk = useCallback(async (startCycle, filename, replayInfo) => {
        if (!filename || events.has(startCycle)) return;
        
        setIsLoading(true);
        setError(null);
        try {
            const endCycle = startCycle + CHUNK_SIZE - 1;
            const response = await fetch(`http://localhost:3001/api/replay/${filename}/cycles?start=${startCycle}&end=${endCycle}`);
            if (!response.ok) throw new Error(`Server responded with status ${response.status}`);
            const data = await response.json();
            
            setEvents(prev => new Map(prev).set(startCycle, data.events));

            if (data.warStartData && startCycle === 0) {
                setActiveReplay({
                    ...replayInfo,
                    ...data.warStartData,
                    duration_cycles: data.warStartData.max_rounds || 0,
                });
            }
        } catch (err) {
            console.error("Fetch error:", err);
            setError(err.message);
            setActiveReplay(null);
            setSelectedReplayInfo(null);
        } finally {
            setIsLoading(false);
        }
    }, [events]); // Removed unstable dependencies

    // Effect to INITIATE loading when a new replay is selected
    useEffect(() => {
        if (selectedReplayInfo) {
            sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(selectedReplayInfo));
            setEvents(new Map());
            setActiveReplay(null);
            setError(null);
            setIsPlaying(false);
            setCurrentCycle(0);
            fetchCycleChunk(0, selectedReplayInfo.filename, selectedReplayInfo);
        } else {
            sessionStorage.removeItem(SESSION_STORAGE_KEY);
            setActiveReplay(null);
        }
    }, [selectedReplayInfo]); // Intentionally not including fetchCycleChunk

    // Effect to draw the INITIAL frame once data is loaded
    useEffect(() => {
        if (activeReplay && canvasApi.current) {
             // Use a timeout to ensure canvas is ready after the state update
            setTimeout(() => jumpToCycle(0), 0);
        }
    }, [activeReplay]); // Runs only when activeReplay is set

    // Effect for the ANIMATION LOOP
    useEffect(() => {
        if (!isPlaying || !activeReplay) {
            cancelAnimationFrame(animationFrameId.current);
            return;
        }
        let lastTime = performance.now();
        const run = (currentTime) => {
            const deltaTime = currentTime - lastTime;
            const cyclesPerSecond = 60 * playbackSpeed;
            const cyclesToAdvance = (deltaTime / 1000) * cyclesPerSecond;
            const step = playbackDirection === 'forward' ? cyclesToAdvance : -cyclesToAdvance;
            const nextCycle = currentCycle + step;

            const nextChunkStart = Math.floor(nextCycle / CHUNK_SIZE) * CHUNK_SIZE;
            if (nextChunkStart >= 0 && !events.has(nextChunkStart)) {
                fetchCycleChunk(nextChunkStart, selectedReplayInfo.filename, selectedReplayInfo);
            }
            
            jumpToCycle(nextCycle);
            lastTime = currentTime;
            animationFrameId.current = requestAnimationFrame(run);
        };
        animationFrameId.current = requestAnimationFrame(run);
        return () => cancelAnimationFrame(animationFrameId.current);
    }, [isPlaying, playbackSpeed, playbackDirection, currentCycle, jumpToCycle, activeReplay, events, selectedReplayInfo, fetchCycleChunk]);
    
    return (
        <div className="grid grid-cols-[320px_1fr] h-full">
            <div className="border-r border-border overflow-y-auto">
                <SimulationTreeSidebar 
                    onReplaySelect={handleReplaySelect}
                    selectedReplayId={selectedReplayInfo?.id} 
                />
            </div>
            <div className="overflow-y-auto">
                {selectedReplayInfo ? (
                    (isLoading && !activeReplay) ? (
                        <div className="p-6 text-center h-full flex flex-col justify-center items-center">
                            <Zap className="w-16 h-16 text-primary animate-pulse" />
                            <p className="mt-4 text-muted-foreground">Loading Replay...</p>
                        </div>
                    ) : error ? (
                         <div className="p-6 h-full flex flex-col justify-center items-center">
                            <div className="p-4 text-destructive-foreground bg-destructive/20 border border-destructive rounded-lg">
                                <div className="flex items-center gap-2 font-semibold"><ServerCrash className="w-5 h-5"/> Fetch Error</div>
                                <p className="text-sm mt-2">{error}</p>
                            </div>
                        </div>
                    ) : activeReplay ? (
                        <ReplayViewer
                            key={selectedReplayInfo.id}
                            canvasApiRef={canvasApi}
                            replayData={activeReplay}
                            onClose={() => setSelectedReplayInfo(null)}
                            isPlaying={isPlaying}
                            setIsPlaying={setIsPlaying}
                            currentCycle={currentCycle}
                            jumpToCycle={jumpToCycle}
                            playbackSpeed={playbackSpeed}
                            setPlaybackSpeed={setPlaybackSpeed}
                            playbackDirection={playbackDirection}
                            setPlaybackDirection={setPlaybackDirection}
                            eventCycles={eventCycles}
                            setEventCycles={setEventCycles}
                        />
                    ) : null
                ) : (
                    <div className="p-6 md:p-8 text-center h-full flex flex-col justify-center items-center">
                        <PlaySquare className="w-24 h-24 text-muted-foreground/30 mx-auto mb-4" />
                        <h2 className="text-xl font-bold text-foreground">Select a Replay</h2>
                        <p className="text-muted-foreground">Select a replay from the tree to begin viewing.</p>
                    </div>
                )}
            </div>
        </div>
    );
}