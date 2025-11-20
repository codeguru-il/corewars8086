import React, { useState } from "react";
import { base44 } from "@/api/base44Client.js";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card.jsx";
import { Button } from "@/components/ui/button.jsx";
import { Badge } from "@/components/ui/badge.jsx";
import { Trophy, Play, AlertCircle } from "lucide-react";
import { Alert, AlertDescription } from "@/components/ui/alert.jsx";

import BracketVisualization from "../components/tournaments/BracketVisualization.jsx";

export default function Tournaments() {
  const [selectedTournament, setSelectedTournament] = useState(null);
  const queryClient = useQueryClient();

  const { data: submissions = [] } = useQuery({
    queryKey: ['submissions'],
    queryFn: () => base44.entities.Submission.list(),
  });

  const { data: simulations = [] } = useQuery({
    queryKey: ['simulations'],
    queryFn: () => base44.entities.Simulation.list('-created_date'),
  });

  const startTournamentMutation = useMutation({
    mutationFn: async (tournamentType) => {
      const tournamentSubmissions = submissions.filter(
        s => s.tournament_type === tournamentType && s.status === 'approved'
      );

      if (tournamentSubmissions.length < 2) {
        throw new Error('Need at least 2 teams to start tournament');
      }

      const brackets = [];
      let bracketNumber = 1;
      for (let i = 0; i < tournamentSubmissions.length; i += 4) {
        const bracketTeams = tournamentSubmissions.slice(i, i + 4).map(sub => ({
          team_name: sub.team_name,
          submission_id: sub.id,
        }));

        await base44.entities.Simulation.create({
          tournament_type: tournamentType,
          bracket_number: bracketNumber++,
          teams: bracketTeams,
          status: 'pending',
          total_rounds: 10000,
        });
      }
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['simulations'] });
    },
  });

  const hsSubmissions = submissions.filter(s => s.tournament_type === 'high_school' && s.status === 'approved');
  const msSubmissions = submissions.filter(s => s.tournament_type === 'middle_school' && s.status === 'approved');
  const hsSimulations = simulations.filter(s => s.tournament_type === 'high_school');
  const msSimulations = simulations.filter(s => s.tournament_type === 'middle_school');

  return (
    <div className="p-6 md:p-8 min-h-screen">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-500 mb-2">
            Tournament Management
          </h1>
          <p className="text-slate-400">Create and manage competition brackets</p>
        </div>

        <div className="grid md:grid-cols-2 gap-6 mb-8">
          <Card className="bg-slate-900/50 border-slate-800 backdrop-blur-xl">
            <CardHeader className="border-b border-slate-800">
              <CardTitle className="flex items-center gap-2 text-cyan-400">
                <Trophy className="w-5 h-5" />
                High School Tournament
              </CardTitle>
            </CardHeader>
            <CardContent className="p-6">
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-slate-400">Approved Submissions</span>
                  <Badge className="bg-cyan-500/20 text-cyan-400">{hsSubmissions.length}</Badge>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-slate-400">Active Brackets</span>
                  <Badge className="bg-purple-500/20 text-purple-400">{hsSimulations.length}</Badge>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-slate-400">Running Simulations</span>
                  <Badge className="bg-green-500/20 text-green-400">{hsSimulations.filter(s => s.status === 'running').length}</Badge>
                </div>

                <Button
                  onClick={() => startTournamentMutation.mutate('high_school')}
                  disabled={hsSubmissions.length < 2 || startTournamentMutation.isPending}
                  className="w-full mt-4 bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-600 hover:to-blue-700"
                >
                  <Play className="w-4 h-4 mr-2" />
                  {startTournamentMutation.isPending ? 'Creating Brackets...' : 'Start Tournament'}
                </Button>

                {hsSubmissions.length < 2 && (
                  <Alert className="bg-yellow-500/10 border-yellow-500/50">
                    <AlertCircle className="w-4 h-4 text-yellow-400" />
                    <AlertDescription className="text-yellow-400 text-sm">
                      Need at least 2 approved submissions to start
                    </AlertDescription>
                  </Alert>
                )}

                {hsSimulations.length > 0 && (
                  <Button
                    variant="outline"
                    onClick={() => setSelectedTournament('high_school')}
                    className="w-full border-cyan-500/50 text-cyan-400 hover:bg-cyan-500/10"
                  >
                    View Brackets
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>

          <Card className="bg-slate-900/50 border-slate-800 backdrop-blur-xl">
            <CardHeader className="border-b border-slate-800">
              <CardTitle className="flex items-center gap-2 text-purple-400">
                <Trophy className="w-5 h-5" />
                Middle School Tournament
              </CardTitle>
            </CardHeader>
            <CardContent className="p-6">
              <div className="space-y-4">
                 <div className="flex justify-between items-center">
                  <span className="text-slate-400">Approved Submissions</span>
                  <Badge className="bg-purple-500/20 text-purple-400">{msSubmissions.length}</Badge>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-slate-400">Active Brackets</span>
                  <Badge className="bg-cyan-500/20 text-cyan-400">{msSimulations.length}</Badge>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-slate-400">Running Simulations</span>
                  <Badge className="bg-green-500/20 text-green-400">{msSimulations.filter(s => s.status === 'running').length}</Badge>
                </div>

                <Button
                  onClick={() => startTournamentMutation.mutate('middle_school')}
                  disabled={msSubmissions.length < 2 || startTournamentMutation.isPending}
                  className="w-full mt-4 bg-gradient-to-r from-purple-500 to-pink-600 hover:from-purple-600 hover:to-pink-700"
                >
                  <Play className="w-4 h-4 mr-2" />
                  {startTournamentMutation.isPending ? 'Creating Brackets...' : 'Start Tournament'}
                </Button>
                
                {msSubmissions.length < 2 && (
                  <Alert className="bg-yellow-500/10 border-yellow-500/50">
                    <AlertCircle className="w-4 h-4 text-yellow-400" />
                    <AlertDescription className="text-yellow-400 text-sm">
                      Need at least 2 approved submissions to start
                    </AlertDescription>
                  </Alert>
                )}

                {msSimulations.length > 0 && (
                  <Button
                    variant="outline"
                    onClick={() => setSelectedTournament('middle_school')}
                    className="w-full border-purple-500/50 text-purple-400 hover:bg-purple-500/10"
                  >
                    View Brackets
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>
        </div>

        {selectedTournament && (
          <BracketVisualization
            tournamentType={selectedTournament}
            simulations={simulations.filter(s => s.tournament_type === selectedTournament)}
            onClose={() => setSelectedTournament(null)}
          />
        )}
      </div>
    </div>
  );
}