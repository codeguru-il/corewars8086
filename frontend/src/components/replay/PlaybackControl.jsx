import React, { useState } from 'react';

const ICONS = {
    playForward: '/icons/play-forward.png',
    playBackward: '/icons/play-backward.png',
    pause: '/icons/pause.png',
    rewind: '/icons/rewind.png',
    fastForward: '/icons/fast-forward.png',
    jumpStart: '/icons/jump-start.png',
    jumpEnd: '/icons/jump-end.png'
};

export default function PlaybackControl({
    isPlaying, onPlay, onPause, onSetSpeed, onSetDirection, jumpToCycle, onSetEventCycles,
    maxCycle, currentCycle, direction
}) {
    const [speedIndex, setSpeedIndex] = useState(0);
    const speedLevels = [1, 2, 4, 8, 16];

    const handlePlayPause = () => {
        if (isPlaying) {
            onPause();
        } else {
            onSetDirection(direction);
            onPlay();
        }
    };

    const handleSpeedUp = () => {
        if (!isPlaying) {
            if (direction !== 'forward') {
                onSetDirection('forward');
            }
        } else {
            if (speedIndex < speedLevels.length - 1) {
                const newIndex = speedIndex + 1;
                setSpeedIndex(newIndex);
                onSetSpeed(speedLevels[newIndex]);
            }
        }
    };

    const handleSlowDown = () => {
        if (!isPlaying) {
            if (direction !== 'backward') {
                onSetDirection('backward');
            }
        } else {
            if (speedIndex > 0) {
                const newIndex = speedIndex - 1;
                setSpeedIndex(newIndex);
                onSetSpeed(speedLevels[newIndex]);
            }
        }
    };

    const getCenterIcon = () => {
        if (isPlaying) return ICONS.pause;
        return direction === 'forward' ? ICONS.playForward : ICONS.playBackward;
    };

    return (
        <div className="playback-control">
            <div className="event-input-section">
                <label className="event-label">Event Cycles (comma-separated)</label>
                <input 
                    type="text" 
                    className="event-input" 
                    placeholder="e.g., 100, 500, 1000"
                    onChange={(e) => {
                        const cycles = e.target.value.split(',').map(s => parseInt(s.trim())).filter(n => !isNaN(n));
                        onSetEventCycles([...new Set(cycles)].sort((a,b)=>a-b));
                    }}
                />
            </div>
            <div className="round-counter">
                {/* --- THIS IS THE FIX --- */}
                {/* Use a ternary operator to show either "RUNNING" or "CYCLE" */}
                {isPlaying ? (
                    <span className="round-label text-primary animate-pulse">RUNNING</span>
                ) : (
                    <span className="round-label">CYCLE</span>
                )}
                
                <span className="cycle-value">{(Math.floor(currentCycle) + 1).toLocaleString()}</span>
                <span className="cycle-max">/ {(maxCycle > 0 ? maxCycle : 1).toLocaleString()}</span>
            </div>
            <div className="controls">
                <button onClick={() => jumpToCycle(0)} className="control-btn" title="Jump to Start"><img src={ICONS.jumpStart} alt="Jump Start" /></button>
                <button onClick={handleSlowDown} className="control-btn" title="Slow Down / Reverse"><img src={ICONS.rewind} alt="Rewind" /></button>
                <button onClick={handlePlayPause} className={`control-btn center ${isPlaying ? 'playing' : ''}`} title="Play/Pause"><img src={getCenterIcon()} alt="Play/Pause" /></button>
                <button onClick={handleSpeedUp} className="control-btn" title="Speed Up / Forward"><img src={ICONS.fastForward} alt="Fast Forward" /></button>
                <button onClick={() => jumpToCycle(maxCycle)} className="control-btn" title="Jump to End"><img src={ICONS.jumpEnd} alt="Jump End" /></button>
            </div>
        </div>
    );
}