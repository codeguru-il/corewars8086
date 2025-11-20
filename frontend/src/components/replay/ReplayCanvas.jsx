import React, { useRef, useEffect } from 'react';

export default function ReplayCanvas({ onCanvasReady }) {
    const canvasRef = useRef(null);

    useEffect(() => {
        const canvas = canvasRef.current; 
        const ctx = canvas.getContext('2d');
        const boardWidth = 256;
        const dotSize = 3;
        canvas.width = boardWidth * dotSize; 
        canvas.height = (65536 / boardWidth) * dotSize;
        
        const warriorColors = ['#4285F4', '#DB4437', '#F4B400', '#0F9D58', '#e91e63', '#9c27b0'];
        const pointerColors = warriorColors.map(c => {
            const r = parseInt(c.slice(1, 3), 16), g = parseInt(c.slice(3, 5), 16), b = parseInt(c.slice(5, 7), 16);
            return `rgb(${Math.min(255, r + 90)}, ${Math.min(255, g + 90)}, ${Math.min(255, b + 90)})`;
        });

        const clear = () => {
            ctx.fillStyle = '#000000';
            ctx.fillRect(0, 0, canvas.width, canvas.height);
        };

        const applyEvents = (events) => {
            if (!events) return;
            events.forEach(event => {
                if (event.type === 'MEMORY_WRITE') {
                    const payload = event.payload; 
                    const arenaOffset = payload.address - 0x10000;
                    const x = (arenaOffset % boardWidth) * dotSize; 
                    const y = Math.floor(arenaOffset / boardWidth) * dotSize;
                    ctx.fillStyle = warriorColors[payload.actorId % warriorColors.length];
                    ctx.fillRect(x, y, dotSize, dotSize);
                }
            });
        };

        const drawPointers = (warriorStates) => {
            if (!warriorStates) return;
            warriorStates.forEach(state => {
                const arenaOffset = state.ip - 0x10000;
                const x = (arenaOffset % boardWidth) * dotSize; 
                const y = Math.floor(arenaOffset / boardWidth) * dotSize;
                ctx.fillStyle = pointerColors[state.id % pointerColors.length];
                ctx.fillRect(x, y, dotSize, dotSize);
            });
        };

        onCanvasReady({ clear, applyEvents, drawPointers });
    }, [onCanvasReady]);

    return (
        <canvas 
            id="replay-canvas" 
            ref={canvasRef} 
            style={{ maxWidth: '100%', maxHeight: '100%', objectFit: 'contain' }} 
        />
    );
}