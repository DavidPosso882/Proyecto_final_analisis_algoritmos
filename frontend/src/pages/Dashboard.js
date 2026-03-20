import React, { useState, useEffect } from 'react';
import { Container, Card, Form, Alert, Button, Spinner } from 'react-bootstrap';
import CandlestickChart from '../components/charts/CandlestickChart';
import CorrelationHeatmap from '../components/charts/CorrelationHeatmap';
import { assetService } from '../services/assetService';
import { analysisService } from '../services/analysisService';
import api from '../services/api';

/**
 * Página principal del Dashboard.
 * 
 * Muestra:
 * - Selector de activos
 * - Gráfico de velas del activo seleccionado
 * - Heatmap de correlación de todos los activos
 */
const Dashboard = () => {
  const [assets, setAssets] = useState([]);
  const [selectedTicker, setSelectedTicker] = useState('');
  const [priceData, setPriceData] = useState([]);
  const [filteredData, setFilteredData] = useState([]);
  const [correlationData, setCorrelationData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [updating, setUpdating] = useState(false);
  const [updateResult, setUpdateResult] = useState(null);
  
  // Filtros de gráfica
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [showSMA, setShowSMA] = useState(false);
  
  // Filtros de heatmap
  const [selectedAssetsForHeatmap, setSelectedAssetsForHeatmap] = useState([]);

  // Cargar lista de activos al montar el componente
  useEffect(() => {
    loadAssets();
    loadCorrelationMatrix();
  }, []);

  // Inicializar selección de activos para heatmap
  useEffect(() => {
    if (assets.length > 0 && selectedAssetsForHeatmap.length === 0) {
      // Seleccionar todos los activos por defecto
      setSelectedAssetsForHeatmap(assets.map(a => a.ticker));
    }
  }, [assets, selectedAssetsForHeatmap.length]);

  const loadAssets = async () => {
    try {
      const data = await assetService.getAllAssets();
      setAssets(data);
      if (data.length > 0) {
        setSelectedTicker(data[0].ticker);
      }
    } catch (err) {
      setError('Error cargando activos: ' + err.message);
    }
  };

  const loadCorrelationMatrix = async () => {
    try {
      const data = await analysisService.getCorrelationMatrix();
      setCorrelationData(data);
    } catch (err) {
      console.error('Error cargando matriz de correlación:', err);
    }
  };

  // Cargar datos de precios cuando cambia el ticker seleccionado
  useEffect(() => {
    if (selectedTicker) {
      loadPriceData(selectedTicker);
    }
  }, [selectedTicker]);

  // Filtrar datos cuando cambien los datos originales o los filtros de fecha
  useEffect(() => {
    if (priceData.length === 0) {
      setFilteredData([]);
      return;
    }

    let filtered = [...priceData];

    if (startDate) {
      filtered = filtered.filter(d => d.date >= startDate);
    }
    if (endDate) {
      filtered = filtered.filter(d => d.date <= endDate);
    }

    setFilteredData(filtered);
  }, [priceData, startDate, endDate]);

  const loadPriceData = async (ticker) => {
    setLoading(true);
    try {
      const data = await assetService.getPriceData(ticker);
      setPriceData(data);
    } catch (err) {
      setError('Error cargando datos de precios: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  // Filtrar matriz de correlación basada en activos seleccionados
  const getFilteredCorrelationData = () => {
    if (!correlationData || selectedAssetsForHeatmap.length === 0) return null;

    const { tickers, matrix } = correlationData;
    const selectedIndices = selectedAssetsForHeatmap.map(t => tickers.indexOf(t)).filter(i => i !== -1);
    
    if (selectedIndices.length === 0) return null;

    const filteredTickers = selectedIndices.map(i => tickers[i]);
    const filteredMatrix = selectedIndices.map(i => 
      selectedIndices.map(j => matrix[i][j])
    );

    return {
      tickers: filteredTickers,
      matrix: filteredMatrix,
      notes: correlationData.notes
    };
  };

  const handleUpdateData = async () => {
    setUpdating(true);
    setUpdateResult(null);
    setError(null);
    try {
      const response = await api.post('/etl/rebuild');
      setUpdateResult(response.data);
      await loadAssets();
      await loadCorrelationMatrix();
      if (selectedTicker) {
        loadPriceData(selectedTicker);
      }
    } catch (err) {
      setError('Error actualizando datos: ' + err.message);
    } finally {
      setUpdating(false);
    }
  };

  return (
    <Container fluid>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h1>Dashboard Financiero</h1>
        <Button 
          variant={updating ? "secondary" : "primary"}
          onClick={handleUpdateData}
          disabled={updating}
        >
          {updating ? (
            <>
              <Spinner size="sm" className="me-2" />
              Actualizando...
            </>
          ) : (
            '🔄 Actualizar Datos'
          )}
        </Button>
      </div>
      
      {error && (
        <Alert variant="danger" onClose={() => setError(null)} dismissible>
          {error}
        </Alert>
      )}

      {updateResult && (
        <Alert variant="success" onClose={() => setUpdateResult(null)} dismissible>
          ✅ Datos actualizados: {updateResult.totalRecords} registros descargados
        </Alert>
      )}

      {/* Selector de activos */}
      <Card className="card-custom mb-4">
        <Card.Body>
          <Card.Title>Seleccionar Activo</Card.Title>
          <Form.Select
            value={selectedTicker}
            onChange={(e) => setSelectedTicker(e.target.value)}
            className="form-select-custom mb-3"
          >
            {assets.map(asset => (
              <option key={asset.ticker} value={asset.ticker}>
                {asset.ticker} - {asset.name} ({asset.market})
              </option>
            ))}
          </Form.Select>

          {loading && <div className="text-center" style={{ color: 'var(--text-secondary)' }}>Cargando datos...</div>}
        </Card.Body>
      </Card>

      {/* Gráfico de velas */}
      {priceData.length > 0 && (
        <Card className="card-custom mb-4">
          <Card.Body>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <Card.Title className="mb-0">Análisis Técnico - {selectedTicker}</Card.Title>
              <div className="d-flex gap-2 align-items-center">
                <Form.Check 
                  type="checkbox"
                  id="showSMA"
                  label="SMA (20, 50)"
                  checked={showSMA}
                  onChange={(e) => setShowSMA(e.target.checked)}
                  className="mb-0"
                />
              </div>
            </div>
            
            {/* Controles de fecha */}
            <div className="row mb-3">
              <div className="col-md-3">
                <Form.Group controlId="startDate">
                  <Form.Label className="small">Fecha Inicial</Form.Label>
                  <Form.Control
                    type="date"
                    value={startDate}
                    onChange={(e) => setStartDate(e.target.value)}
                    max={endDate || new Date().toISOString().split('T')[0]}
                  />
                </Form.Group>
              </div>
              <div className="col-md-3">
                <Form.Group controlId="endDate">
                  <Form.Label className="small">Fecha Final</Form.Label>
                  <Form.Control
                    type="date"
                    value={endDate}
                    onChange={(e) => setEndDate(e.target.value)}
                    min={startDate}
                    max={new Date().toISOString().split('T')[0]}
                  />
                </Form.Group>
              </div>
              <div className="col-md-6 d-flex align-items-end">
                <Button 
                  variant="outline-secondary" 
                  size="sm"
                  onClick={() => {
                    setStartDate('');
                    setEndDate('');
                  }}
                >
                  Limpiar Filtros
                </Button>
              </div>
            </div>

            <CandlestickChart
              data={filteredData}
              ticker={selectedTicker}
              height={500}
              showSMA={showSMA}
            />
          </Card.Body>
        </Card>
      )}

      {/* Heatmap de correlación */}
      {correlationData && (
        <Card className="card-custom">
          <Card.Body>
            <div className="d-flex justify-content-between align-items-center mb-3">
              <Card.Title className="mb-0">Matriz de Correlación</Card.Title>
              <div className="d-flex gap-2 align-items-center">
                <Form.Select
                  multiple
                  value={selectedAssetsForHeatmap}
                  onChange={(e) => {
                    const selected = Array.from(e.target.selectedOptions, option => option.value);
                    setSelectedAssetsForHeatmap(selected);
                  }}
                  style={{ height: '100px', width: '200px' }}
                >
                  {assets.map(asset => (
                    <option key={asset.ticker} value={asset.ticker}>
                      {asset.ticker}
                    </option>
                  ))}
                </Form.Select>
              </div>
            </div>
            <Alert variant="info" className="small">
              {correlationData.notes}
            </Alert>
            {getFilteredCorrelationData() && (
              <CorrelationHeatmap
                tickers={getFilteredCorrelationData().tickers}
                matrix={getFilteredCorrelationData().matrix}
                height={700}
              />
            )}
          </Card.Body>
        </Card>
      )}
    </Container>
  );
};

export default Dashboard;
