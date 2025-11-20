import React from "react";
import { Badge } from "@/components/ui/badge.jsx";
import { Zap, Target, AlertCircle, Trophy } from "lucide-react";

export default function EventLog({ events }) {
  const getEventIcon = (eventType) => {
    switch (eventType) {
      case 'spawn': return <Zap className="w-3 h-3" />;
      case 'attack': return <Target className="w-3 h-3" />;
      case 'crash': return <AlertCircle className="w-3 h-3" />;
      case 'victory': return <Trophy className="w-3 h-3" />;
      default: return <Zap className="w-3 h-3" />;
    }
  };

  const getEventColor = (eventType) => {
    switch (eventType) {
      case 'spawn': return 'bg-green-500/20 text-green-400';
      case 'attack': return 'bg-orange-500/20 text-orange-400';
      case 'crash': return 'bg-red-500/20 text-red-400';
      case 'victory': return 'bg-yellow-500/20 text-yellow-400';
      default: return 'bg-cyan-500/20 text-cyan-400';
    }
  };

  return (
    // We will use a Tailwind class for the scrollbar styling
    <div className="h-[500px] overflow-y-auto space-y-2 pr-2 custom-scrollbar">
      {events.map((event, idx) => (
        <div
          key={idx}
          className="p-3 rounded-lg bg-slate-800/50 border border-slate-700/50 hover:border-cyan-500/30 transition-all"
        >
          <div className="flex items-start gap-3">
            <div className={`p-1.5 rounded ${getEventColor(event.event_type)}`}>
              {getEventIcon(event.event_type)}
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 mb-1">
                <span className="text-xs font-mono text-slate-500">
                  Cycle {event.cycle}
                </span>
                <Badge variant="outline" className="text-xs">
                  {event.team_name}
                </Badge>
              </div>
              <p className="text-sm text-slate-300">{event.description}</p>
            </div>
          </div>
        </div>
      ))}

      {events.length === 0 && (
        <div className="text-center py-12">
          <Zap className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <p className="text-slate-400 text-sm">No events recorded yet</p>
        </div>
      )}
    </div>
  );
}