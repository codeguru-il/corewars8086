import React, { useEffect } from "react";
import { Link, useLocation } from "react-router-dom";
import { createPageUrl } from "@/utils/index.js";
import { Trophy, Upload, Zap, LayoutDashboard } from "lucide-react";

const navigationItems = [
  { title: "Dashboard", url: createPageUrl("Dashboard"), icon: LayoutDashboard },
  { title: "Submissions", url: createPageUrl("Submissions"), icon: Upload },
  { title: "Tournaments", url: createPageUrl("Tournaments"), icon: Trophy },
  { title: "Simulations", url: createPageUrl("Simulations"), icon: Zap },
];

export default function Layout({ children, sidebar }) {
  const location = useLocation();

  useEffect(() => {
    document.documentElement.classList.add('dark');
    return () => document.documentElement.classList.remove('dark');
  }, []);

  const Header = () => (
    <header className="bg-background/80 border-b border-border backdrop-blur-xl sticky top-0 z-50 flex-shrink-0">
      <div className="px-6">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 bg-gradient-to-br from-primary to-secondary rounded-lg flex items-center justify-center shadow-lg shadow-primary/30">
              <Zap className="w-6 h-6 text-primary-foreground" />
            </div>
            <div>
              <h2 className="font-bold text-xl text-primary">CodeGuru</h2>
              <p className="text-xs text-muted-foreground hidden md:block">Competition Manager</p>
            </div>
          </div>
          <nav className="flex items-center gap-1">
            {navigationItems.map((item) => {
              const isActive = location.pathname === item.url;
              return (
                <Link
                  key={item.title}
                  to={item.url}
                  className={`flex items-center gap-2 px-4 py-2 rounded-lg transition-all duration-200 ${
                    isActive ? 'bg-primary/20 text-primary' : 'text-muted-foreground hover:bg-muted hover:text-foreground'
                  }`}
                >
                  <item.icon className="w-4 h-4" />
                  <span className="font-medium hidden sm:inline">{item.title}</span>
                </Link>
              );
            })}
          </nav>
        </div>
      </div>
    </header>
  );

  return (
    <div className="min-h-screen flex flex-col bg-background text-foreground">
      <Header />
      {/* --- THE FIX IS HERE: added overflow-hidden --- */}
      <div className="flex flex-1 overflow-hidden">
        {sidebar && (
          <aside className="w-80 border-r border-border flex-shrink-0 overflow-y-auto bg-background">
            {sidebar}
          </aside>
        )}
        <main className="flex-1 flex flex-col overflow-auto">
          {children}
        </main>
      </div>
    </div>
  );
}