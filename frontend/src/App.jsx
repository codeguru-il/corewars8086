import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Layout from './Layout.jsx';
import Dashboard from './pages/Dashboard.jsx';
import Submissions from './pages/Submissions.jsx';
import Tournaments from "./pages/Tournaments.jsx";
import Simulations from './pages/Simulations.jsx';
import ReplayViewer from './pages/ReplayViewer.jsx';
import { createPageUrl } from './utils/index.js';

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path={createPageUrl("Dashboard")} element={<Dashboard />} />
          <Route path={createPageUrl("Submissions")} element={<Submissions />} />
          <Route path={createPageUrl("Tournaments")} element={<Tournaments />} />
          <Route path={createPageUrl("Simulations")} element={<Simulations />} />
          <Route path={createPageUrl("ReplayViewer")} element={<ReplayViewer />} />
          <Route path="/" element={<Navigate to={createPageUrl("Dashboard")} replace />} />
          <Route path="*" element={<Navigate to={createPageUrl("Dashboard")} replace />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;