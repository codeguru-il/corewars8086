import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card.jsx";
import { Badge } from "@/components/ui/badge.jsx";
import { Button } from "@/components/ui/button.jsx";
import { X, Users } from "lucide-react";

export default function BracketVisualization({ tournamentType, simulations, onClose }) {
  return (
    <Card className="bg-slate-900/50 border-slate-800 backdrop-blur-xl">
      <CardHeader className="border-b border-slate-800">
        <div className="flex justify-between items-center">
          <CardTitle className="text-xl text-cyan-400">
            {tournamentType === 'high_school' ? 'High School' : 'Middle School'} Brackets
          </CardTitle>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <X className="w-5 h-5" />
          </Button>
        </div>
      </CardHeader>
      <CardContent className="p-6">
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-4">
          {simulations.map((simulation) => (
            <div
              key={simulation.id}
              className="p-4 rounded-lg bg-slate-800/50 border border-slate-700/50 hover:border-cyan-500/50 transition-all"
            >
              <div className="flex items-center justify-between mb-3">
                <h3 className="font-semibold text-white">Bracket #{simulation.bracket_number}</h3>
                <Badge
                  className={
                    simulation.status === 'running' ? 'bg-green-500/20 text-green-400'
                    : simulation.status === 'completed' ? 'bg-blue-500/20 text-blue-400'
                    : 'bg-slate-500/20 text-slate-400'
                  }
                >
                  {simulation.status}
                </Badge>
              </div>

              <div className="space-y-2 mb-3">
                {simulation.teams?.map((team, idx) => (
                  <div key={idx} className="flex items-center gap-2 text-sm">
                    <Users className="w-3 h-3 text-slate-400" />
                    <span className="text-slate-300">{team.team_name}</span>
                  </div>
                ))}
              </div>

              {simulation.status === 'running' && (
                <div className="mt-3 pt-3 border-t border-slate-700">
                  <div className="flex justify-between text-xs text-slate-400">
                    <span>Progress</span>
                    <span>{simulation.completed_rounds || 0} / {simulation.total_rounds}</span>
                  </div>
                  <div className="w-full bg-slate-700 rounded-full h-1 mt-2">
                    <div
                      className="bg-gradient-to-r from-cyan-500 to-blue-600 h-1 rounded-full transition-all"
                      style={{ width: `${((simulation.completed_rounds || 0) / (simulation.total_rounds || 1)) * 100}%` }}
                    />
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  );
}