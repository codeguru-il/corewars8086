import React, { useCallback } from "react";
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
    onPlay,
    onPause,
    currentCycle,
    jumpToCycle,
    playbackSpeed,
    setPlaybackSpeed,
    playbackDirection,
    setPlaybackDirection,
    eventCycles,
    setEventCycles
}) {

    const handleCanvasReady = useCallback((api) => {
        if (canvasApiRef) {
            canvasApiRef.current = api;
        }
    }, [canvasApiRef]);

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

            {/* --- THIS IS THE MODIFIED LINE --- */}
            {/* We remove 'items-center' and 'justify-center' to allow the child to fill the space */}
            <div className="flex-1 flex bg-black rounded-lg p-2 min-h-0">
                <div className="w-full h-full max-w-full max-h-full aspect-square border-2 border-primary/50 shadow-lg shadow-primary/20 rounded-lg relative">
                    <ReplayCanvas onCanvasReady={handleCanvasReady} />
                </div>
            </div>

            <div className="flex-shrink-0">
                <PlaybackControl
                    isPlaying={isPlaying}
                    onPause={onPause}
                    onPlay={onPlay}
                    onSetSpeed={setPlaybackSpeed}
                    onSetDirection={setPlaybackDirection}
                    direction={playbackDirection}
                    currentCycle={currentCycle}
                    maxCycle={totalCycles}
                    jumpToCycle={jumpToCycle}
                    onSetEventCycles={setEventCycles}
                />
            </div>
        </div>
    );
}