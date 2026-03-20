import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Alert } from 'react-bootstrap';
import { assetService } from '../services/assetService';
import api from '../services/api';

const PatternAnalysis = () => {
  const [assets, setAssets] = useState([]);
  const [selectedAsset, setSelectedAsset] = useState('');
  const [windowSize, setWindowSize] = useState(5);
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadAssets();
  }, []);

  const loadAssets = async () => {
    try {
      const data = await assetService.getAllAssets();
      setAssets(data);
      if (data.length > 0) {
        setSelectedAsset(data[0].ticker);
      }
    } catch (err) {
      setError('Error cargando activos: ' + err.message);
    }
  };

  const handleAnalyze = async () => {
    if (!selectedAsset) {
      setError('Selecciona un activo');
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const response = await api.get('/patterns/analyze', {
        params: { 
          ticker: selectedAsset, 
          windowSize: windowSize 
        }
      });
      setResults(response.data);
    } catch (err) {
      setError('Error en el análisis: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container>
      <h1 className="mb-4">Análisis de Patrones</h1>
      
      <Card className="card-custom mb-4">
        <Card.Body>
          <Card.Title>Detección de Patrones con Sliding Window</Card.Title>
          
          {error && (
            <Alert variant="danger" onClose={() => setError(null)} dismissible className="mb-3">
              {error}
            </Alert>
          )}

          <Row>
            <Col md={4}>
              <Form.Group className="mb-3">
                <Form.Label style={{ color: 'var(--text-primary)' }}>Activo</Form.Label>
                <Form.Select
                  value={selectedAsset}
                  onChange={(e) => setSelectedAsset(e.target.value)}
                  className="form-select-custom"
                >
                  {assets.map(asset => (
                    <option key={asset.ticker} value={asset.ticker}>
                      {asset.ticker} - {asset.name}
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group className="mb-3">
                <Form.Label style={{ color: 'var(--text-primary)' }}>
                  Tamaño de Ventana (días)
                </Form.Label>
                <Form.Control
                  type="number"
                  min={2}
                  max={20}
                  value={windowSize}
                  onChange={(e) => setWindowSize(parseInt(e.target.value))}
                  className="form-control-custom"
                />
              </Form.Group>
            </Col>
            <Col md={4}>
              <Form.Group className="mb-3 d-flex align-items-end">
                <Button 
                  variant="primary" 
                  onClick={handleAnalyze}
                  disabled={loading}
                  className="w-100"
                >
                  {loading ? 'Analizando...' : 'Detectar Patrones'}
                </Button>
              </Form.Group>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {results && (
        <>
          <Card className="card-custom mb-4">
            <Card.Body>
              <Card.Title>Resultados del Análisis</Card.Title>
              <Row>
                <Col md={3}>
                  <div className="text-center">
                    <h5>{results.upwardPatterns}</h5>
                    <p className="text-muted">Patrones Alcistas</p>
                  </div>
                </Col>
                <Col md={3}>
                  <div className="text-center">
                    <h5>{results.downwardPatterns}</h5>
                    <p className="text-muted">Patrones Bajistas</p>
                  </div>
                </Col>
                <Col md={3}>
                  <div className="text-center">
                    <h5>{results.localMaxima}</h5>
                    <p className="text-muted">Máximos Locales</p>
                  </div>
                </Col>
                <Col md={3}>
                  <div className="text-center">
                    <h5>{results.localMinima}</h5>
                    <p className="text-muted">Mínimos Locales</p>
                  </div>
                </Col>
              </Row>
            </Card.Body>
          </Card>

          <Card className="card-custom">
            <Card.Body>
              <Card.Title>Información del Algoritmo</Card.Title>
              <Alert variant="info">
                <strong>Sliding Window Algorithm:</strong>
                <ul className="mb-0 mt-2">
                  <li>Complejidad: O(n × k) donde n = tamaño de datos, k = tamaño de ventana</li>
                  <li>Patrón Alcista: {windowSize} días consecutivos de precios crecientes</li>
                  <li>Patrón Bajista: {windowSize} días consecutivos de precios decrecientes</li>
                  <li>Máximo Local: Punto más alto en una ventana de {windowSize} días</li>
                  <li>Mínimo Local: Punto más bajo en una ventana de {windowSize} días</li>
                </ul>
              </Alert>
            </Card.Body>
          </Card>
        </>
      )}

      {!results && !loading && (
        <Card className="card-custom">
          <Card.Body>
            <Alert variant="secondary">
              <p className="mb-0">
                Selecciona un activo y el tamaño de ventana para analizar patrones de precios.
              </p>
            </Alert>
          </Card.Body>
        </Card>
      )}
    </Container>
  );
};

export default PatternAnalysis;
