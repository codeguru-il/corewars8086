import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card.jsx";
import { Badge } from "@/components/ui/badge.jsx";
import { format } from "date-fns";
import { Clock, Code, Zap } from "lucide-react";

export default function RecentActivity({ submissions, simulations }) {
  const recentSubmissions = submissions.slice(0, 5);
  const recentSimulations = simulations.slice(0, 3);

  return (
    <Card className="bg-card border-border backdrop-blur-xl">
      <CardHeader className="border-b border-border">
        <CardTitle className="text-xl text-primary flex items-center gap-2">
          <Clock className="w-5 h-5" />
          Recent Activity
        </CardTitle>
      </CardHeader>
      <CardContent className="p-6">
        <div className="flex flex-col gap-6">
          
          {/* Latest Submissions Section */}
          <div>
            <h3 className="text-sm font-semibold text-muted-foreground mb-3">Latest Submissions</h3>
            <div className="space-y-2">
              {recentSubmissions.map((submission) => (
                <div
                  key={submission.id}
                  className="flex items-center justify-between p-3 rounded-lg bg-muted/50 border border-border hover:border-primary/50 transition-all"
                >
                  <div className="flex items-center gap-3">
                    <Code className="w-4 h-4 text-primary" />
                    <div>
                      <p className="font-medium text-foreground">{submission.team_name}</p>
                      <p className="text-xs text-muted-foreground">
                        {format(new Date(submission.created_date), "MMM d, h:mm a")}
                      </p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <Badge className="bg-primary/20 text-primary border-primary/50">
                      Score: {submission.score}
                    </Badge>
                    <Badge className="bg-secondary/50 text-secondary-foreground border-secondary">
                      {submission.tournament_type === 'high_school' ? 'HS' : 'MS'}
                    </Badge>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {recentSimulations.length > 0 && (
            <>
              {/* Dedicated Separator Line */}
              <div className="border-t border-border" />

              {/* Recent Simulations Section */}
              <div>
                <h3 className="text-sm font-semibold text-muted-foreground mb-3">Recent Simulations</h3>
                <div className="space-y-2">
                  {recentSimulations.map((simulation) => (
                    <div
                      key={simulation.id}
                      className="flex items-center justify-between p-3 rounded-lg bg-muted/50 border border-border"
                    >
                      <div className="flex items-center gap-3">
                        <Zap className="w-4 h-4 text-primary" />
                        <div>
                          <p className="font-medium text-foreground">
                            Bracket #{simulation.bracket_number}
                          </p>
                          <p className="text-xs text-muted-foreground">
                            {simulation.teams?.length || 0} teams
                          </p>
                        </div>
                      </div>
                      <Badge
                        className={
                          simulation.status === 'running'
                            ? 'bg-green-500/20 text-green-400 border-green-500/50'
                            : simulation.status === 'completed'
                            ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/50'
                            : 'bg-muted text-muted-foreground border-border'
                        }
                      >
                        {simulation.status}
                      </Badge>
                    </div>
                  ))}
                </div>
              </div>
            </>
          )}
        </div>
      </CardContent>
    </Card>
  );
}