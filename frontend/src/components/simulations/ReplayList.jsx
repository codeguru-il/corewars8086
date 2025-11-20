import React from "react";
import { Badge } from "@/components/ui/badge.jsx";
import { Button } from "@/components/ui/button.jsx";
import { PlaySquare, Trophy, Star } from "lucide-react";

export default function ReplayList({ replays, onReplayClick }) {
  const sortedReplays = [...replays].sort((a, b) => {
    if (a.is_featured && !b.is_featured) return -1;
    if (!a.is_featured && b.is_featured) return 1;
    return b.round_number - a.round_number;
  });

  return (
    <div className="space-y-3">
      {sortedReplays.map((replay) => (
        <div
          key={replay.id}
          className="p-4 rounded-lg bg-slate-800/50 border border-slate-700/50 hover:border-cyan-500/50 transition-all"
        >
          <div className="flex items-start justify-between mb-3">
            <div className="flex-1">
              <div className="flex items-center gap-2 mb-1">
                <span className="font-semibold text-white">Round #{replay.round_number}</span>
                {replay.is_featured && (
                  <Badge className="bg-yellow-500/20 text-yellow-400 border-yellow-500/50">
                    <Star className="w-3 h-3 mr-1" /> Featured
                  </Badge>
                )}
              </div>
              <div className="flex items-center gap-2 text-sm text-slate-400">
                <Trophy className="w-4 h-4 text-cyan-400" />
                <span>Winner: <span className="text-cyan-400 font-medium">{replay.winner_team_name}</span></span>
              </div>
              <p className="text-xs text-slate-500 mt-1">
                {replay.duration_cycles} cycles • {replay.events?.length || 0} events
              </p>
            </div>
            <Button size="sm" onClick={() => onReplayClick(replay)} className="bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-600 hover:to-blue-700">
              <PlaySquare className="w-4 h-4 mr-1" /> Watch
            </Button>
          </div>
        </div>
      ))}
      {replays.length === 0 && (
        <div className="text-center py-12">
          <PlaySquare className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <p className="text-slate-400">No replays available</p>
        </div>
      )}
    </div>
  );
}