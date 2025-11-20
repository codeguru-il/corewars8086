import React from "react";
import { Badge } from "@/components/ui/badge.jsx";
import { ChevronRight, Zap } from "lucide-react";

export default function SimulationTree({ simulations, selectedSimulation, onSelectSimulation }) {
  const groupedSimulations = simulations.reduce((acc, sim) => {
    if (!acc[sim.tournament_type]) {
      acc[sim.tournament_type] = [];
    }
    acc[sim.tournament_type].push(sim);
    return acc;
  }, {});

  return (
    <div className="space-y-4">
      {Object.entries(groupedSimulations).map(([tournamentType, sims]) => (
        <div key={tournamentType}>
          <h3 className="text-sm font-semibold text-slate-300 mb-2 uppercase tracking-wide">
            {tournamentType === 'high_school' ? 'High School' : 'Middle School'}
          </h3>
          <div className="space-y-1">
            {sims.map((simulation) => (
              <button
                key={simulation.id}
                onClick={() => onSelectSimulation(simulation)}
                className={`w-full text-left p-3 rounded-lg transition-all ${
                  selectedSimulation?.id === simulation.id
                    ? 'bg-cyan-500/20 border border-cyan-500/50'
                    : 'bg-slate-800/50 border border-slate-700/50 hover:border-cyan-500/30'
                }`}
              >
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <Zap className="w-4 h-4 text-cyan-400" />
                    <span className="font-medium text-white">Bracket #{simulation.bracket_number}</span>
                  </div>
                  <ChevronRight className="w-4 h-4 text-slate-400" />
                </div>
                <div className="flex items-center gap-2 mt-2">
                  <Badge className={ `text-xs ${
                      simulation.status === 'running' ? 'bg-green-500/20 text-green-400'
                      : simulation.status === 'completed' ? 'bg-blue-500/20 text-blue-400'
                      : 'bg-slate-500/20 text-slate-400'}`
                    }
                  >
                    {simulation.status}
                  </Badge>
                  <span className="text-xs text-slate-400">
                    {simulation.teams?.length || 0} teams
                  </span>
                </div>
              </button>
            ))}
          </div>
        </div>
      ))}
      {simulations.length === 0 && (
        <div className="text-center py-8">
          <Zap className="w-12 h-12 text-slate-600 mx-auto mb-3" />
          <p className="text-slate-400 text-sm">No simulations yet</p>
        </div>
      )}
    </div>
  );
}