import React, { useState, useEffect } from 'react';
import { Container, Row, Col, Card, Form, Button, Alert, Table } from 'react-bootstrap';
import { assetService } from '../services/assetService';
import { similarityService } from '../services/similarityService';

/**
 * Página de Análisis de Similitud.
 * 
 * Permite comparar dos activos usando los 4 algoritmos de similitud.
 */
const SimilarityAnalysis = () => {
  const [assets, setAssets] = useState([]);
  const [ticker1, setTicker1] = useState('');
  const [ticker2, setTicker2] = useState('');
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
      if (data.length >= 2) {
        setTicker1(data[0].ticker);
        setTicker2(data[1].ticker);
      }
    } catch (err) {
      setError('Error cargando activos: ' + err.message);
    }
  };

  const handleCompare = async () => {
    if (!ticker1 || !ticker2) {
      setError('Selecciona ambos activos');
      return;
    }

    setLoading(true);
    setError(null);
    
    try {
      const data = await similarityService.compareAssets(ticker1, ticker2);
      setResults(data);
    } catch (err) {
      setError('Error en la comparación: ' + err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container>
      <h1 className="mb-4">Análisis de Similitud</h1>
      
      <Card className="card-custom mb-4">
        <Card.Body>
          <Card.Title>Comparar Activos</Card.Title>

          {error && (
            <Alert variant="danger" onClose={() => setError(null)} dismissible className="mb-3">
              {error}
            </Alert>
          )}

          <Row>
            <Col md={5}>
              <Form.Group className="mb-3">
                <Form.Label style={{ color: 'var(--text-primary)' }}>Activo 1</Form.Label>
                <Form.Select
                  value={ticker1}
                  onChange={(e) => setTicker1(e.target.value)}
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

            <Col md={5}>
              <Form.Group className="mb-3">
                <Form.Label style={{ color: 'var(--text-primary)' }}>Activo 2</Form.Label>
                <Form.Select
                  value={ticker2}
                  onChange={(e) => setTicker2(e.target.value)}
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

            <Col md={2} className="d-flex align-items-end">
              <Button
                variant="primary"
                onClick={handleCompare}
                disabled={loading}
                className="w-100 mb-3"
              >
                {loading ? 'Comparando...' : 'Comparar'}
              </Button>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {results && (
        <Card className="card-custom">
          <Card.Body>
            <Card.Title>Resultados: {results.ticker1} vs {results.ticker2}</Card.Title>

            <Alert variant="warning" className="small">
              {results.notes}
            </Alert>

            <Table striped bordered hover className="table-custom">
              <thead>
                <tr>
                  <th style={{ color: 'var(--text-primary)' }}>Algoritmo</th>
                  <th style={{ color: 'var(--text-primary)' }}>Valor</th>
                  <th style={{ color: 'var(--text-primary)' }}>Descripción</th>
                </tr>
              </thead>
              <tbody>
                <tr>
                  <td style={{ color: 'var(--text-primary)' }}>Distancia Euclidiana</td>
                  <td style={{ color: 'var(--text-primary)' }}>{results.euclideanDistance?.toFixed(4) || 'N/A'}</td>
                  <td style={{ color: 'var(--text-secondary)' }}>Menor = más similares</td>
                </tr>
                <tr>
                  <td style={{ color: 'var(--text-primary)' }}>Correlación Pearson</td>
                  <td style={{ color: 'var(--text-primary)' }}>{results.pearsonCorrelation?.toFixed(4) || 'N/A'}</td>
                  <td style={{ color: 'var(--text-secondary)' }}>-1 a 1 (1 = perfecta correlación)</td>
                </tr>
                <tr>
                  <td style={{ color: 'var(--text-primary)' }}>Similitud Coseno</td>
                  <td style={{ color: 'var(--text-primary)' }}>{results.cosineSimilarity?.toFixed(4) || 'N/A'}</td>
                  <td style={{ color: 'var(--text-secondary)' }}>-1 a 1 (1 = misma dirección)</td>
                </tr>
                <tr>
                  <td style={{ color: 'var(--text-primary)' }}>Dynamic Time Warping</td>
                  <td style={{ color: 'var(--text-primary)' }}>{results.dtw?.toFixed(4) || 'N/A'}</td>
                  <td style={{ color: 'var(--text-secondary)' }}>Menor = más similares (invariante en fase)</td>
                </tr>
              </tbody>
            </Table>
          </Card.Body>
        </Card>
      )}
    </Container>
  );
};

export default SimilarityAnalysis;
