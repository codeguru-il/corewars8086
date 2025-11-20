import React, { useEffect, useState } from "react";

export default function FireworkEffect() {
  const [fireworks, setFireworks] = useState([]);

  useEffect(() => {
    const createFirework = () => {
      const x = Math.random() * window.innerWidth;
      const y = Math.random() * (window.innerHeight / 2);
      const color = ['#06b6d4', '#8b5cf6', '#ec4899', '#f59e0b'][Math.floor(Math.random() * 4)];

      return {
        id: Date.now() + Math.random(), x, y, color,
        particles: Array(30).fill(0).map(() => ({
          angle: Math.random() * Math.PI * 2,
          velocity: 2 + Math.random() * 3,
        })),
      };
    };

    const interval = setInterval(() => {
      setFireworks(prev => [...prev, createFirework()]);
    }, 400);

    const cleanupTimeout = setTimeout(() => clearInterval(interval), 4000);
    return () => { clearInterval(interval); clearTimeout(cleanupTimeout); };
  }, []);

  useEffect(() => {
    const cleanupInterval = setInterval(() => {
      setFireworks(prev => prev.filter(f => Date.now() - f.id < 2000));
    }, 100);
    return () => clearInterval(cleanupInterval);
  }, []);

  return (
    <div className="fixed inset-0 pointer-events-none z-50">
      {fireworks.map(firework => (
        <div key={firework.id} className="absolute" style={{ left: firework.x, top: firework.y }}>
          {firework.particles.map((particle, idx) => (
            <div
              key={idx}
              className="absolute w-1 h-1 rounded-full"
              style={{
                backgroundColor: firework.color,
                animation: `firework-particle 1s ease-out forwards`,
                transform: `rotate(${particle.angle}rad)`,
                '--velocity': particle.velocity,
              }}
            />
          ))}
        </div>
      ))}
      <style jsx>{`
        @keyframes firework-particle {
          0% { transform: translate(0, 0) scale(1); opacity: 1; }
          100% { transform: translate(calc(var(--velocity, 3) * 50px), 0) scale(0); opacity: 0; }
        }
      `}</style>
    </div>
  );
}