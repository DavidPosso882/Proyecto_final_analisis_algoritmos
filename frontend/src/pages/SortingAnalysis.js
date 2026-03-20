import React, { useState, useEffect } from 'react';
import { Bar } from 'react-chartjs-2';
import { Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const SortingAnalysis = () => {
  const [analysisResult, setAnalysisResult] = useState(null);
  const [sortedData, setSortedData] = useState([]);
  const [topVolume, setTopVolume] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeTab, setActiveTab] = useState('analysis');

  const runAnalysis = async () => {
    setLoading(true);
    setAnalysisResult(null);
    try {
      const response = await fetch('/api/sorting/analyze', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
      });
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      setAnalysisResult(data);
    } catch (error) {
      console.error('Error running analysis:', error);
      alert(`Error ejecutando el análisis: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  const loadSortedData = async () => {
    try {
      const response = await fetch('/api/sorting/sorted-data');
      const data = await response.json();
      setSortedData(data);
    } catch (error) {
      console.error('Error loading sorted data:', error);
    }
  };

  const loadTopVolume = async () => {
    try {
      const response = await fetch('/api/sorting/top-volume');
      const data = await response.json();
      setTopVolume(data);
    } catch (error) {
      console.error('Error loading top volume:', error);
    }
  };

  useEffect(() => {
    loadSortedData();
    loadTopVolume();
  }, []);

  // Asegurar que los datos del gráfico estén ordenados ascendentemente por tiempo
  const sortedAlgorithms = analysisResult
    ? [...analysisResult.algorithms].sort((a, b) => a.timeMs - b.timeMs)
    : [];

  const chartData = analysisResult ? {
    labels: sortedAlgorithms.map(a => a.name),
    datasets: [{
      label: 'Tiempo de ejecución (ms)',
      data: sortedAlgorithms.map(a => a.timeMs),
      backgroundColor: sortedAlgorithms.map((_, i) => {
        const hue = (i / sortedAlgorithms.length) * 240;
        return `hsla(${hue}, 70%, 55%, 0.7)`;
      }),
      borderColor: sortedAlgorithms.map((_, i) => {
        const hue = (i / sortedAlgorithms.length) * 240;
        return `hsla(${hue}, 70%, 45%, 1)`;
      }),
      borderWidth: 1
    }]
  } : null;

  const chartOptions = {
    responsive: true,
    plugins: {
      legend: { position: 'top' },
      title: {
        display: true,
        text: 'Comparación de Algoritmos de Ordenamiento (Tiempos Ascendentes)',
        font: { size: 16 }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        title: {
          display: true,
          text: 'Tiempo (ms)'
        }
      },
      x: {
        title: {
          display: true,
          text: 'Algoritmo'
        }
      }
    }
  };

  return (
    <div className="container-fluid py-4">
      <h1 className="mb-4">Análisis de Algoritmos de Ordenamiento</h1>
      
      <div className="row mb-4">
        <div className="col-12">
          <button 
            className="btn btn-primary me-2"
            onClick={runAnalysis}
            disabled={loading}
          >
            {loading ? 'Ejecutando...' : 'Ejecutar Análisis de 12 Algoritmos'}
          </button>
          {loading && (
            <div className="alert alert-info mt-2" role="alert">
              <strong>Ejecutando análisis de algoritmos...</strong>
              <br />
              <small>Este proceso puede tardar varios minutos debido a la complejidad de algunos algoritmos.</small>
            </div>
          )}
        </div>
      </div>

      {analysisResult && (
        <div className="row mb-4">
          <div className="col-md-4">
            <div className="card">
              <div className="card-body text-center">
                <h6 className="card-subtitle mb-2 text-muted">Tamaño del Dataset</h6>
                <h3 className="card-title">{analysisResult.dataSize.toLocaleString()}</h3>
                <p className="card-text text-muted">registros</p>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card">
              <div className="card-body text-center">
                <h6 className="card-subtitle mb-2 text-muted">Algoritmos Evaluados</h6>
                <h3 className="card-title">{analysisResult.algorithms.length}</h3>
                <p className="card-text text-muted">algoritmos</p>
              </div>
            </div>
          </div>
          <div className="col-md-4">
            <div className="card">
              <div className="card-body text-center">
                <h6 className="card-subtitle mb-2 text-muted">Tiempo Total</h6>
                <h3 className="card-title">{analysisResult.totalExecutionTimeMs.toLocaleString()}</h3>
                <p className="card-text text-muted">ms</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {chartData && (
        <div className="row mb-4">
          <div className="col-md-12">
            <div className="card">
              <div className="card-body">
                <Bar data={chartData} options={chartOptions} />
              </div>
            </div>
          </div>
        </div>
      )}

      {analysisResult && (
        <div className="row mb-4">
          <div className="col-md-12">
            <div className="card">
              <div className="card-header">
                <ul className="nav nav-tabs card-header-tabs">
                  <li className="nav-item">
                    <button 
                      className={`nav-link ${activeTab === 'analysis' ? 'active' : ''}`}
                      onClick={() => setActiveTab('analysis')}
                    >
                      Tabla 1 — Resultados
                    </button>
                  </li>
                  <li className="nav-item">
                    <button 
                      className={`nav-link ${activeTab === 'sorted' ? 'active' : ''}`}
                      onClick={() => setActiveTab('sorted')}
                    >
                      Datos Ordenados (Fecha + Cierre)
                    </button>
                  </li>
                  <li className="nav-item">
                    <button 
                      className={`nav-link ${activeTab === 'volume' ? 'active' : ''}`}
                      onClick={() => setActiveTab('volume')}
                    >
                      Top 15 Mayor Volumen
                    </button>
                  </li>
                </ul>
              </div>
              <div className="card-body">
                {activeTab === 'analysis' && (
                  <div className="table-responsive">
                    <h5 className="mb-3">Tabla 1. Análisis de datos enteros</h5>
                    <table className="table table-striped table-hover">
                      <thead className="table-dark">
                        <tr>
                          <th>#</th>
                          <th>Método de Ordenamiento</th>
                          <th>Complejidad O( )</th>
                          <th>Mejor Caso</th>
                          <th>Peor Caso</th>
                          <th>Tamaño</th>
                          <th>Tiempo (ms)</th>
                          <th>Estable</th>
                          <th>In-Place</th>
                        </tr>
                      </thead>
                      <tbody>
                        {sortedAlgorithms.map((algo, index) => (
                          <tr key={index}>
                            <td>{index + 1}</td>
                            <td><strong>{algo.name}</strong></td>
                            <td><code>{algo.complexity}</code></td>
                            <td><code>{algo.bestCase}</code></td>
                            <td><code>{algo.worstCase}</code></td>
                            <td>{(algo.dataSize || analysisResult.dataSize).toLocaleString()}</td>
                            <td>{algo.timeMs >= 0 ? algo.timeMs.toFixed(4) : 'Error'}</td>
                            <td>{algo.isStable ? '✅ Sí' : '❌ No'}</td>
                            <td>{algo.isInPlace ? '✅ Sí' : '❌ No'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}

                {activeTab === 'sorted' && (
                  <div className="table-responsive" style={{ maxHeight: '500px', overflowY: 'auto' }}>
                    <p className="text-muted mb-2">
                      Registros ordenados ascendentemente por fecha de cotización.
                      Cuando la fecha es igual, se ordena por precio de cierre.
                    </p>
                    <table className="table table-sm table-striped">
                      <thead className="table-dark sticky-top">
                        <tr>
                          <th>Fecha</th>
                          <th>Activo</th>
                          <th>Open</th>
                          <th>High</th>
                          <th>Low</th>
                          <th>Close</th>
                          <th>Volumen</th>
                        </tr>
                      </thead>
                      <tbody>
                        {sortedData.slice(0, 200).map((item, index) => (
                          <tr key={index}>
                            <td>{item.date}</td>
                            <td><strong>{item.ticker || item.asset?.ticker || 'N/A'}</strong></td>
                            <td>{Number(item.openPrice).toFixed(2)}</td>
                            <td>{Number(item.highPrice).toFixed(2)}</td>
                            <td>{Number(item.lowPrice).toFixed(2)}</td>
                            <td>{Number(item.closePrice).toFixed(2)}</td>
                            <td>{Number(item.volume).toLocaleString()}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                    {sortedData.length > 200 && (
                      <p className="text-muted mt-2">
                        Mostrando los primeros 200 de {sortedData.length.toLocaleString()} registros
                      </p>
                    )}
                  </div>
                )}

                {activeTab === 'volume' && (
                  <div className="table-responsive">
                    <p className="text-muted mb-2">
                      Los 15 días con mayor volumen de negociación, ordenados ascendentemente por volumen.
                    </p>
                    <table className="table table-striped table-hover">
                      <thead className="table-dark">
                        <tr>
                          <th>#</th>
                          <th>Fecha</th>
                          <th>Activo</th>
                          <th>Close</th>
                          <th>Volumen</th>
                        </tr>
                      </thead>
                      <tbody>
                        {topVolume.map((item, index) => (
                          <tr key={index}>
                            <td>{index + 1}</td>
                            <td>{item.date}</td>
                            <td><strong>{item.ticker || item.asset?.ticker || 'N/A'}</strong></td>
                            <td>{Number(item.closePrice).toFixed(2)}</td>
                            <td>{Number(item.volume).toLocaleString()}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default SortingAnalysis;
