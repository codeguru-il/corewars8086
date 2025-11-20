import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card.jsx";
import { Trophy } from "lucide-react";

export default function TournamentOverview({ teams, simulations }) {
  const msTeams = teams.filter(t => t.tournament_type === 'middle_school').length;
  const hsTeams = teams.filter(t => t.tournament_type === 'high_school').length;

  const msSimulations = simulations.filter(s => s.tournament_type === 'middle_school');
  const hsSimulations = simulations.filter(s => s.tournament_type === 'high_school');

  return (
    <Card className="bg-card border-border backdrop-blur-xl">
      <CardHeader className="border-b border-border">
        <CardTitle className="text-xl text-primary flex items-center gap-2">
          <Trophy className="w-5 h-5" />
          Tournament Overview
        </CardTitle>
      </CardHeader>
      <CardContent className="p-6">
        {/* --- CORRECTED STRUCTURE --- */}
        <div className="flex flex-col gap-6">
          
          {/* High School Section */}
          <div>
            <div className="flex items-center gap-2 mb-3">
              <div className="w-2 h-2 rounded-full bg-primary"></div>
              <h3 className="font-semibold text-foreground">High School</h3>
            </div>
            <div className="space-y-2 ml-4">
              <div className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">Teams</span>
                <span className="font-medium text-foreground">{hsTeams}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">Simulations</span>
                <span className="font-medium text-foreground">{hsSimulations.length}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">Running</span>
                <span className="font-medium text-primary">
                  {hsSimulations.filter(s => s.status === 'running').length}
                </span>
              </div>
            </div>
          </div>

          {/* Dedicated Separator Line */}
          <div className="border-t border-border" />

          {/* Middle School Section */}
          <div>
            <div className="flex items-center gap-2 mb-3">
              <div className="w-2 h-2 rounded-full bg-secondary"></div>
              <h3 className="font-semibold text-foreground">Middle School</h3>
            </div>
            <div className="space-y-2 ml-4">
              <div className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">Teams</span>
                <span className="font-medium text-foreground">{msTeams}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">Simulations</span>
                <span className="font-medium text-foreground">{msSimulations.length}</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm text-muted-foreground">Running</span>
                <span className="font-medium text-primary">
                  {msSimulations.filter(s => s.status === 'running').length}
                </span>
              </div>
            </div>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}