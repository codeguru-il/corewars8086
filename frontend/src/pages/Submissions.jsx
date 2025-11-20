import React, { useState } from "react";
import { base44 } from "@/api/base44Client.js";
import { useQuery } from "@tanstack/react-query";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card.jsx";
import { Input } from "@/components/ui/input.jsx";
import { Badge } from "@/components/ui/badge.jsx";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs.jsx";
import { Search, Code, Clock, TrendingUp } from "lucide-react";
import { format } from "date-fns";

export default function Submissions() {
  const [searchQuery, setSearchQuery] = useState("");
  const [tournamentFilter, setTournamentFilter] = useState("all");

  const { data: submissions = [], isLoading } = useQuery({
    queryKey: ['submissions'],
    queryFn: () => base44.entities.Submission.list('-created_date'),
  });

  const filteredSubmissions = submissions.filter(sub => {
    const matchesSearch = sub.team_name.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesTournament = tournamentFilter === 'all' || sub.tournament_type === tournamentFilter;
    return matchesSearch && matchesTournament;
  });

  const avgScore = submissions.length > 0
    ? (submissions.reduce((sum, s) => sum + s.score, 0) / submissions.length).toFixed(1)
    : 0;

  return (
    <div className="p-6 md:p-8 min-h-screen">
      <div className="max-w-7xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl md:text-4xl font-bold text-green-400 mb-2 drop-shadow-[0_0_10px_rgba(0,255,0,0.5)]">
            Code Submissions
          </h1>
          <p className="text-green-600">Real-time feed of team submissions</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          <Card className="bg-black/80 border-green-900/50">
            <CardContent className="p-4">
              <div className="flex items-center gap-3">
                <Code className="w-8 h-8 text-green-400" />
                <div>
                  <p className="text-2xl font-bold text-green-400">{submissions.length}</p>
                  <p className="text-xs text-green-700">Total Submissions</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-black/80 border-green-900/50">
            <CardContent className="p-4">
              <div className="flex items-center gap-3">
                <TrendingUp className="w-8 h-8 text-green-400" />
                <div>
                  <p className="text-2xl font-bold text-green-400">{avgScore}</p>
                  <p className="text-xs text-green-700">Average Score</p>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-black/80 border-green-900/50">
            <CardContent className="p-4">
              <div className="flex items-center gap-3">
                <Clock className="w-8 h-8 text-green-400" />
                <div>
                  <p className="text-2xl font-bold text-green-400">
                    {submissions.length > 0 ? format(new Date(submissions[0].created_date), "HH:mm") : "--:--"}
                  </p>
                  <p className="text-xs text-green-700">Latest Submission</p>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <Card className="bg-black/80 border-green-900/50 backdrop-blur-xl">
          <CardHeader className="border-b border-green-900/50">
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
              <CardTitle className="text-xl text-green-400">All Submissions</CardTitle>
              <div className="flex gap-4 w-full md:w-auto">
                <div className="relative flex-1 md:flex-initial">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-green-600" />
                  <Input
                    placeholder="Search teams..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="pl-10 bg-green-950/30 border-green-900/50 text-green-200 placeholder:text-green-800"
                  />
                </div>
                <Tabs value={tournamentFilter} onValueChange={setTournamentFilter}>
                  <TabsList className="bg-green-950/30 border border-green-900/50">
                    <TabsTrigger value="all" className="data-[state=active]:bg-green-500/20 data-[state=active]:text-green-400">All</TabsTrigger>
                    <TabsTrigger value="high_school" className="data-[state=active]:bg-green-500/20 data-[state=active]:text-green-400">HS</TabsTrigger>
                    <TabsTrigger value="middle_school" className="data-[state=active]:bg-green-500/20 data-[state=active]:text-green-400">MS</TabsTrigger>
                  </TabsList>
                </Tabs>
              </div>
            </div>
          </CardHeader>
          <CardContent className="p-6">
            <div className="space-y-3">
              {filteredSubmissions.map((submission) => (
                <div
                  key={submission.id}
                  className="p-4 rounded-lg bg-green-950/30 border border-green-900/50 hover:border-green-500/50 transition-all"
                >
                  <div className="flex flex-col md:flex-row justify-between gap-4">
                    <div className="flex-1">
                      <div className="flex items-center gap-3 mb-2">
                        <Code className="w-4 h-4 text-green-400" />
                        <h3 className="font-semibold text-green-200">{submission.team_name}</h3>
                        <Badge className="bg-green-900/50 text-green-300 border-green-700">
                          {submission.tournament_type === 'high_school' ? 'High School' : 'Middle School'}
                        </Badge>
                      </div>
                      <p className="text-sm text-green-700">
                        {format(new Date(submission.created_date), "MMM d, yyyy • h:mm a")}
                      </p>
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <p className="text-2xl font-bold text-green-400 drop-shadow-[0_0_8px_rgba(0,255,0,0.5)]">
                          {submission.score}
                        </p>
                        <p className="text-xs text-green-700">Score</p>
                      </div>
                      <Badge
                        className={
                          submission.status === 'approved'
                            ? 'bg-green-500/20 text-green-400 border-green-500/50'
                            : submission.status === 'rejected'
                            ? 'bg-red-500/20 text-red-400 border-red-500/50'
                            : 'bg-yellow-500/20 text-yellow-400 border-yellow-500/50'
                        }
                      >
                        {submission.status}
                      </Badge>
                    </div>
                  </div>
                </div>
              ))}
              {filteredSubmissions.length === 0 && (
                <div className="text-center py-12">
                  <Code className="w-12 h-12 text-green-900 mx-auto mb-3" />
                  <p className="text-green-700">No submissions found</p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}