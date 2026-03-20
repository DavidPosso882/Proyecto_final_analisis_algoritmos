import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { ThemeProvider } from './contexts/ThemeContext';
import DashboardLayout from './components/layout/DashboardLayout';
import Dashboard from './pages/Dashboard';
import SimilarityAnalysis from './pages/SimilarityAnalysis';
import PatternAnalysis from './pages/PatternAnalysis';
import RiskAnalysis from './pages/RiskAnalysis';
import ReportExport from './pages/ReportExport';
import SortingAnalysis from './pages/SortingAnalysis';

import 'bootstrap/dist/css/bootstrap.min.css';
import './App.css';

function App() {
  return (
    <ThemeProvider>
      <Router>
        <DashboardLayout>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/similarity" element={<SimilarityAnalysis />} />
            <Route path="/patterns" element={<PatternAnalysis />} />
            <Route path="/risk" element={<RiskAnalysis />} />
            <Route path="/reports" element={<ReportExport />} />
            <Route path="/sorting" element={<SortingAnalysis />} />
          </Routes>
        </DashboardLayout>
      </Router>
    </ThemeProvider>
  );
}

export default App;
