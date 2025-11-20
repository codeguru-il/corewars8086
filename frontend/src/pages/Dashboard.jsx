import React from "react";
import { base44 } from "@/api/base44Client.js";
import { useQuery } from "@tanstack/react-query";
import { Trophy, Upload, Zap, Users } from "lucide-react";
import { Link } from "react-router-dom";
import { createPageUrl } from "@/utils/index.js";
import { Button } from "@/components/ui/button.jsx";

import StatsCard from "../components/dashboard/StatsCard.jsx";
import RecentActivity from "../components/dashboard/RecentActivity.jsx";
import TournamentOverview from "../components/dashboard/TournamentOverview.jsx";

export default function Dashboard() {
  const { data: teams = [] } = useQuery({
    queryKey: ['teams'],
    queryFn: () => base44.entities.Team.list(),
  });

  const { data: submissions = [] } = useQuery({
    queryKey: ['submissions'],
    queryFn: () => base44.entities.Submission.list('-created_date'),
  });

  const { data: simulations = [] } = useQuery({
    queryKey: ['simulations'],
    queryFn: () => base44.entities.Simulation.list('-created_date'),
  });

  const { data: replays = [] } = useQuery({
    queryKey: ['replays'],
    queryFn: () => base44.entities.Replay.list('-created_date'),
  });

  const middleSchoolTeams = teams.filter(t => t.tournament_type === 'middle_school').length;
  const highSchoolTeams = teams.filter(t => t.tournament_type === 'high_school').length;
  const activeSimulations = simulations.filter(s => s.status === 'running').length;

  return (
    <div className="p-6 md:p-8 min-h-screen">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-green-400 mb-2 drop-shadow-[0_0_10px_rgba(0,255,0,0.5)]">
            Competition Dashboard
          </h1>
          <p className="text-green-600">Monitor all CodeGuru tournament activity</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <StatsCard
            title="Total Teams"
            value={teams.length}
            icon={Users}
            gradient="from-green-500 to-green-600"
            subtitle={`${middleSchoolTeams} MS • ${highSchoolTeams} HS`}
          />
          <StatsCard
            title="Submissions"
            value={submissions.length}
            icon={Upload}
            gradient="from-green-400 to-emerald-500"
            subtitle="Total code submissions"
          />
          <StatsCard
            title="Active Simulations"
            value={activeSimulations}
            icon={Zap}
            gradient="from-lime-500 to-green-600"
            subtitle="Currently running"
          />
          <StatsCard
            title="Total Replays"
            value={replays.length}
            icon={Trophy}
            gradient="from-emerald-500 to-green-600"
            subtitle="Recorded matches"
          />
        </div>

        <div className="grid lg:grid-cols-3 gap-6">
          <div className="lg:col-span-2">
            <RecentActivity submissions={submissions} simulations={simulations} />
          </div>
          <div>
            <TournamentOverview teams={teams} simulations={simulations} />
          </div>
        </div>

        <div className="mt-6 flex gap-4">
          <Link to={createPageUrl("Tournaments")} className="flex-1">
            <Button className="w-full bg-gradient-to-r from-green-500 to-green-600 hover:from-green-600 hover:to-green-700 text-black font-semibold">
              <Trophy className="w-4 h-4 mr-2" />
              Manage Tournaments
            </Button>
          </Link>
          <Link to={createPageUrl("Simulations")} className="flex-1">
            <Button className="w-full bg-gradient-to-r from-emerald-500 to-green-600 hover:from-emerald-600 hover:to-green-700 text-black font-semibold">
              <Zap className="w-4 h-4 mr-2" />
              View Simulations
            </Button>
          </Link>
        </div>
      </div>
    </div>
  );
}