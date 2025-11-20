import React from "react";
import { Card, CardContent } from "@/components/ui/card.jsx";

export default function StatsCard({ title, value, icon: Icon, gradient, subtitle }) {
  return (
    <Card className="bg-black/80 border-green-900/50 backdrop-blur-xl hover:border-green-500/50 transition-all duration-300 hover:shadow-[0_0_20px_rgba(0,255,0,0.2)]">
      <CardContent className="p-6">
        <div className="flex justify-between items-start mb-4">
          <div>
            <p className="text-sm text-green-600 mb-1">{title}</p>
            <p className="text-3xl font-bold text-green-400">{value}</p>
            {subtitle && (
              <p className="text-xs text-green-700 mt-1">{subtitle}</p>
            )}
          </div>
          <div className={`p-3 rounded-xl bg-gradient-to-br ${gradient}`}>
            <Icon className="w-6 h-6 text-black" />
          </div>
        </div>
      </CardContent>
    </Card>
  );
}