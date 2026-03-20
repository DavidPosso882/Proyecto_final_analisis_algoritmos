import React, { useState, useEffect, useCallback } from 'react';
import { Container, Card, Table, Alert, Badge } from 'react-bootstrap';
import { assetService } from '../services/assetService';

/**
 * Página de Análisis de Riesgo.
 * 
 * Muestra clasificación de activos por nivel de riesgo basado en volatilidad.
 */
const RiskAnalysis = () => {
  const [assets, setAssets] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadAssets = useCallback(async () => {
    try {
      const response = await fetch('/api/risk/volatility');
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }
      const data = await response.json();
      setAssets(data);
    } catch (err) {
      console.error('Error cargando volatilidad:', err);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadAssets();
  }, [loadAssets]);

  const getRiskVariant = (category) => {
    const variants = {
      'Muy baja': 'success',
      'Baja': 'success',
      'Moderada': 'warning',
      'Alta': 'danger',
      'Muy alta': 'danger'
    };
    return variants[category] || 'secondary';
  };

  return (
    <Container>
      <h1 className="mb-4">Análisis de Riesgo</h1>
      
      <Alert variant="info" className="mb-4">
        <strong>Metodología:</strong> La volatilidad se calcula como la desviación estándar 
        de los retornos logarítmicos anualizados: σ = std(log-returns) × √252
      </Alert>

      <Card className="card-custom">
        <Card.Body>
          <Card.Title>Clasificación por Nivel de Riesgo</Card.Title>



          {loading ? (
            <p style={{ color: 'var(--text-secondary)' }}>Cargando datos...</p>
          ) : (
            <Table striped bordered hover responsive className="table-custom">
              <thead>
                <tr>
                  <th style={{ color: 'var(--text-primary)' }}>Ticker</th>
                  <th style={{ color: 'var(--text-primary)' }}>Nombre</th>
                  <th style={{ color: 'var(--text-primary)' }}>Mercado</th>
                  <th style={{ color: 'var(--text-primary)' }}>Volatilidad Anual (%)</th>
                  <th style={{ color: 'var(--text-primary)' }}>Categoría de Riesgo</th>
                </tr>
              </thead>
              <tbody>
                {assets.sort((a, b) => b.volatility - a.volatility).map(asset => {
                  const variant = getRiskVariant(asset.riskCategory);
                  return (
                    <tr key={asset.ticker}>
                      <td style={{ color: 'var(--text-primary)' }}><strong>{asset.ticker}</strong></td>
                      <td style={{ color: 'var(--text-primary)' }}>{asset.name}</td>
                      <td style={{ color: 'var(--text-secondary)' }}>{asset.market}</td>
                      <td style={{ color: 'var(--text-primary)' }}>{asset.volatility.toFixed(2)}%</td>
                      <td>
                        <Badge bg={variant} className="px-3 py-2">
                          {asset.riskCategory}
                        </Badge>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </Table>
          )}

          <div className="mt-4">
            <h6 style={{ color: 'var(--text-primary)' }}>Leyenda de Riesgo:</h6>
            <div className="d-flex gap-3">
              <Badge bg="success" className="px-3 py-2">Muy Baja</Badge>
              <Badge bg="success" className="px-3 py-2">Baja</Badge>
              <Badge bg="warning" className="px-3 py-2">Moderada</Badge>
              <Badge bg="danger" className="px-3 py-2">Alta</Badge>
              <Badge bg="danger" className="px-3 py-2">Muy Alta</Badge>
            </div>
          </div>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default RiskAnalysis;
