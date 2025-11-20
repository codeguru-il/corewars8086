import React from "react";
import { Zap, ArrowLeft } from "lucide-react";
import { Button } from "@/components/ui/button.jsx";
import PlaybackControl from "../components/replay/PlaybackControl.jsx";
import ReplayCanvas from "../components/replay/ReplayCanvas.jsx";
import "../components/replay/PlaybackControl.css";

export default function ReplayViewer({
    canvasApiRef,
    replayData,
    onClose,
    isPlaying,
    setIsPlaying,
    currentCycle,
    jumpToCycle,
    playbackSpeed,
    setPlaybackSpeed,
    playbackDirection,
    setPlaybackDirection,
    eventCycles,
    setEventCycles
}) {
    // This component no longer contains the animation loop or complex useEffect hooks.
    // It simply renders the UI based on the props passed down from Simulations.jsx.

    if (!replayData) {
        return (
            <div className="p-6 md:p-8 text-center h-full flex flex-col justify-center items-center">
                <Zap className="w-24 h-24 text-muted-foreground/30 mx-auto mb-4 animate-pulse" />
                <h2 className="text-xl font-bold text-foreground">Loading Replay Data...</h2>
            </div>
        );
    }
    
    const totalCycles = replayData.duration_cycles || 0;

    return (
        <div className="flex flex-col h-full p-6 gap-6">
            <div className="flex items-center gap-4 flex-shrink-0">
                <Button variant="ghost" size="icon" onClick={onClose} className="hover:bg-muted">
                    <ArrowLeft className="w-5 h-5" />
                </Button>
                <h1 className="text-2xl font-bold text-primary">
                    Replay: Round #{replayData.round_number}
                </h1>
            </div>

            <div className="flex-1 flex items-center justify-center bg-black rounded-lg border border-border p-2 min-h-0">
                <ReplayCanvas onCanvasReady={(api) => {
                    if (canvasApiRef) {
                        canvasApiRef.current = api;
                    }
                }} />
            </div>

            <div className="flex-shrink-0">
                <PlaybackControl
                    isPlaying={isPlaying}
                    direction={playbackDirection}
                    currentCycle={currentCycle}
                    maxCycle={totalCycles}
                    onPlay={(detail) => { 
                        setPlaybackDirection(detail.direction); 
                        setPlaybackSpeed(detail.speed); 
                        setIsPlaying(true); 
                    }}
                    onPause={() => setIsPlaying(false)}
                    onSetSpeed={(speed) => setPlaybackSpeed(speed)}
                    onSetDirection={(dir) => setPlaybackDirection(dir)}
                    onJumpTo={(cycle) => jumpToCycle(cycle)}
                    onSetEventCycles={setEventCycles}
                />
            </div>
        </div>
    );
}