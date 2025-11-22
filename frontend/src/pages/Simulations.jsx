import React, { useState, useEffect, useRef, useCallback } from "react";
import { Zap, ArrowLeft, ServerCrash, PlaySquare } from "lucide-react";
import SimulationTreeSidebar from "../components/simulations/SimulationTreeSidebar.jsx";
import ReplayViewer from "./ReplayViewer.jsx";

const SESSION_STORAGE_KEY = 'simulations_selectedReplay';
// --- THIS IS THE MODIFIED LINE ---
const CHUNK_SIZE = 5000; // Changed from 1000 to 5000
const DRAW_INTERVAL_MS = 1000 / 30;
const PREFETCH_THRESHOLD = CHUNK_SIZE * 0.75; 

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
    const eventsRef = useRef(events);
    useEffect(() => { eventsRef.current = events; });

    const directionRef = useRef(playbackDirection);
    useEffect(() => { directionRef.current = playbackDirection; });

    const speedRef = useRef(playbackSpeed);
    useEffect(() => { speedRef.current = playbackSpeed; });

    const playbackTimeRef = useRef(currentCycle);
    const cycleRef = useRef(currentCycle);
    useEffect(() => { cycleRef.current = currentCycle; });
    const lastDrawnCycleRef = useRef(0);

    const handlePlay = useCallback(() => {
        playbackTimeRef.current = cycleRef.current;
        setIsPlaying(true);
    }, []);

    const handlePause = useCallback(() => setIsPlaying(false), []);
    const handleSetDirection = useCallback((dir) => setPlaybackDirection(dir), []);
    const handleSetSpeed = useCallback((speed) => setPlaybackSpeed(speed), []);
    const handleSetEventCycles = useCallback((cycles) => setEventCycles(cycles), []);

    const handleReplaySelect = (replay) => {
        setSelectedReplayInfo(prev => (prev?.id === replay.id ? null : replay));
    };
    
    const jumpToCycle = useCallback(async (cycle) => {
        if (!activeReplay || !canvasApi.current) return;
        
        const totalCycles = activeReplay.max_rounds || 0;
        const targetCycle = Math.floor(Math.max(0, Math.min(cycle, totalCycles)));

        const targetChunkStart = Math.floor(targetCycle / CHUNK_SIZE) * CHUNK_SIZE;
        await Promise.all([
            fetchCycleChunk(targetChunkStart, selectedReplayInfo.filename),
            fetchCycleChunk(targetChunkStart - CHUNK_SIZE, selectedReplayInfo.filename),
            fetchCycleChunk(targetChunkStart + CHUNK_SIZE, selectedReplayInfo.filename),
        ]);
        
        playbackTimeRef.current = targetCycle;
        
        canvasApi.current.clear();
        const memoryWriteEvents = [];
        const currentEvents = eventsRef.current;
        for (let i = 0; i <= targetCycle; i++) {
            const chunkStart = Math.floor(i / CHUNK_SIZE) * CHUNK_SIZE;
            const chunk = currentEvents.get(chunkStart);
            if (chunk) {
                for (const event of chunk) {
                    if (event.payload.round === i && event.type === 'MEMORY_WRITE') {
                        memoryWriteEvents.push(event);
                    }
                }
            }
        }
        canvasApi.current.applyEvents(memoryWriteEvents);
        
        const targetChunk = currentEvents.get(targetChunkStart);
        if (targetChunk) {
            const updateEvent = targetChunk.find(e => e.payload.round === targetCycle && e.type === 'ROUND_UPDATE');
            if (updateEvent) {
                canvasApi.current.drawPointers(updateEvent.payload.warriorStates);
            }
        }
        setCurrentCycle(targetCycle);
        lastDrawnCycleRef.current = targetCycle;

        if ((targetCycle >= totalCycles && directionRef.current === 'forward') || 
            (targetCycle <= 0 && directionRef.current === 'backward')) {
            setIsPlaying(false);
        }
    }, [activeReplay, selectedReplayInfo]);

    const fetchCycleChunk = useCallback(async (startCycle, filename) => {
        if (!filename || startCycle < 0 || eventsRef.current.has(startCycle)) return;
        
        if (eventsRef.current.size === 0) setIsLoading(true);
        setError(null);

        try {
            const response = await fetch(`http://localhost:3001/api/replay/${filename}/cycles?start=${startCycle}&end=${startCycle + CHUNK_SIZE - 1}`);
            if (!response.ok) throw new Error(`Server responded with status ${response.status}`);
            const data = await response.json();
            
            setEvents(prevEvents => {
                const newEvents = new Map(prevEvents);
                newEvents.set(startCycle, data.events);
                const currentChunkStart = Math.floor(cycleRef.current / CHUNK_SIZE) * CHUNK_SIZE;
                const validChunks = new Set([currentChunkStart, currentChunkStart - CHUNK_SIZE, currentChunkStart + CHUNK_SIZE]);
                for (const key of newEvents.keys()) {
                    if (!validChunks.has(key)) newEvents.delete(key);
                }
                return newEvents;
            });

            if (data.warStartData && startCycle === 0 && selectedReplayInfo) {
                setActiveReplay({ ...selectedReplayInfo, ...data.warStartData, duration_cycles: data.warStartData.max_rounds || 0 });
            }
        } catch (err) {
            console.error("Fetch error:", err);
            setError(err.message);
            setActiveReplay(null);
            setSelectedReplayInfo(null);
        } finally {
            setIsLoading(false);
        }
    }, [selectedReplayInfo]);

    useEffect(() => {
        if (selectedReplayInfo) {
            sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(selectedReplayInfo));
            setEvents(new Map());
            setActiveReplay(null);
            setError(null);
            setIsPlaying(false);
            setCurrentCycle(0);
            playbackTimeRef.current = 0;
            lastDrawnCycleRef.current = 0;
            setPlaybackDirection('forward');
            setPlaybackSpeed(1);
            fetchCycleChunk(0, selectedReplayInfo.filename);
        } else {
            sessionStorage.removeItem(SESSION_STORAGE_KEY);
            setActiveReplay(null);
        }
    }, [selectedReplayInfo, fetchCycleChunk]);

    useEffect(() => {
        if (activeReplay && canvasApi.current) {
            setTimeout(() => jumpToCycle(0), 0);
        }
    }, [activeReplay, jumpToCycle]);


    useEffect(() => {
        if (!isPlaying || !activeReplay) {
            cancelAnimationFrame(animationFrameId.current);
            return;
        }

        let lastTime = performance.now();
        let lastDrawTime = 0;

        const run = (currentTime) => {
            animationFrameId.current = requestAnimationFrame(run);
            const deltaTime = currentTime - lastTime;
            lastTime = currentTime;

            const cyclesPerSecond = 30 * speedRef.current;
            const cyclesToAdvance = (deltaTime / 1000) * cyclesPerSecond;
            const step = directionRef.current === 'forward' ? cyclesToAdvance : -cyclesToAdvance;
            playbackTimeRef.current += step;

            const totalCycles = activeReplay.max_rounds || 0;
            const targetCycle = Math.floor(Math.max(0, Math.min(playbackTimeRef.current, totalCycles)));

            setCurrentCycle(targetCycle);
            
            const currentChunkStart = Math.floor(targetCycle / CHUNK_SIZE) * CHUNK_SIZE;

            if (directionRef.current === 'forward' && (targetCycle % CHUNK_SIZE > PREFETCH_THRESHOLD)) {
                fetchCycleChunk(currentChunkStart + CHUNK_SIZE, selectedReplayInfo.filename);
            } else if (directionRef.current === 'backward' && (targetCycle % CHUNK_SIZE < (CHUNK_SIZE - PREFETCH_THRESHOLD))) {
                 fetchCycleChunk(currentChunkStart - CHUNK_SIZE, selectedReplayInfo.filename);
            }

            if (currentTime - lastDrawTime > DRAW_INTERVAL_MS) {
                lastDrawTime = currentTime;
                
                if (directionRef.current === 'backward') {
                    jumpToCycle(targetCycle);
                    return;
                }

                const prevDrawnCycle = lastDrawnCycleRef.current;
                const newEvents = [];
                for (let i = prevDrawnCycle + 1; i <= targetCycle; i++) {
                    const chunkStart = Math.floor(i / CHUNK_SIZE) * CHUNK_SIZE;
                    const chunk = eventsRef.current.get(chunkStart);
                    if (chunk) {
                        for (const event of chunk) {
                            if (event.payload.round === i && event.type === 'MEMORY_WRITE') {
                                newEvents.push(event);
                            }
                        }
                    }
                }
                
                if (canvasApi.current) {
                    const prevChunk = eventsRef.current.get(Math.floor(prevDrawnCycle / CHUNK_SIZE) * CHUNK_SIZE);
                    if(prevChunk) {
                        const prevUpdateEvent = prevChunk.find(e => e.payload.round === prevDrawnCycle && e.type === 'ROUND_UPDATE');
                        if (prevUpdateEvent) {
                            canvasApi.current.applyEvents(prevUpdateEvent.payload.warriorStates.map(ws => ({
                                type: 'MEMORY_WRITE',
                                payload: { address: ws.ip, actorId: -1 } 
                            })));
                        }
    
                    }

                    canvasApi.current.applyEvents(newEvents);
                    
                    const targetChunk = eventsRef.current.get(currentChunkStart);
                    if (targetChunk) {
                        const updateEvent = targetChunk.find(e => e.payload.round === targetCycle && e.type === 'ROUND_UPDATE');
                        if (updateEvent) {
                            canvasApi.current.drawPointers(updateEvent.payload.warriorStates);
                        }
                    }
                }
                lastDrawnCycleRef.current = targetCycle;
            }

            if ((targetCycle >= totalCycles && directionRef.current === 'forward') || 
                (targetCycle <= 0 && directionRef.current === 'backward')) {
                setIsPlaying(false);
            }
        };

        animationFrameId.current = requestAnimationFrame(run);
        return () => cancelAnimationFrame(animationFrameId.current);
    }, [isPlaying, activeReplay, selectedReplayInfo, jumpToCycle, fetchCycleChunk]);
    
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
                            onPlay={handlePlay}
                            onPause={handlePause}
                            jumpToCycle={jumpToCycle}
                            currentCycle={currentCycle}
                            setPlaybackSpeed={handleSetSpeed}
                            playbackDirection={playbackDirection}
                            setPlaybackDirection={handleSetDirection}
                            eventCycles={eventCycles}
                            setEventCycles={handleSetEventCycles}
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