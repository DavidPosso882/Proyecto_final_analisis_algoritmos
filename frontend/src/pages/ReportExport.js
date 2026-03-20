import React, { useState } from 'react';
import { Container, Card, Button, Alert, Spinner } from 'react-bootstrap';
import api from '../services/api';

/**
 * Página de Exportación de Reportes.
 * 
 * Permite descargar un reporte técnico en formato PDF.
 */
const ReportExport = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleDownloadReport = async () => {
    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      const response = await api.get('/reports/pdf', {
        responseType: 'blob' // Importante para recibir archivos binarios
      });

      // Crear URL del blob
      const url = window.URL.createObjectURL(new Blob([response.data]));
      
      // Crear elemento <a> temporal para descargar
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', 'reporte-analisis-algoritmos.pdf');
      document.body.appendChild(link);
      link.click();
      
      // Cleanup
      link.remove();
      window.URL.revokeObjectURL(url);
      
      setSuccess(true);
    } catch (err) {
      setError('Error generando el reporte: ' + (err.message || 'Error desconocido'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container>
      <h1 className="mb-4">Exportación de Reportes</h1>

      <Card className="card-custom">
        <Card.Body>
          <Card.Title>Reporte Técnico en PDF</Card.Title>

          <Card.Text style={{ color: 'var(--text-secondary)' }}>
            Descarga un reporte técnico profesional en PDF que incluye:
          </Card.Text>

          <ul style={{ color: 'var(--text-secondary)' }}>
            <li><strong>Portada profesional</strong> con título, metadatos y diseño elegante</li>
            <li><strong>Índice de contenidos</strong> con navegación por secciones</li>
            <li><strong>Metodología y descripción</strong> del proyecto</li>
            <li><strong>Fórmulas matemáticas</strong> de los algoritmos implementados</li>
            <li><strong>Análisis de complejidad</strong> computacional (Big-O)</li>
            <li><strong>Lista completa de activos</strong> analizados (22 activos)</li>
            <li><strong>Resultados de algoritmos</strong> de ordenamiento</li>
            <li><strong>Decisiones de diseño</strong> y optimizaciones implementadas</li>
            <li><strong>Conclusiones</strong> y recomendaciones futuras</li>
            <li><strong>Información académica</strong> y declaración de uso de IA</li>
          </ul>

          {error && (
            <Alert variant="danger" onClose={() => setError(null)} dismissible className="mb-3">
              {error}
            </Alert>
          )}

          {success && (
            <Alert variant="success" onClose={() => setSuccess(false)} dismissible className="mb-3">
              Reporte descargado exitosamente
            </Alert>
          )}

          <div className="d-grid gap-2">
            <Button
              variant="primary"
              size="lg"
              onClick={handleDownloadReport}
              disabled={loading}
            >
              {loading ? (
                <>
                  <Spinner
                    as="span"
                    animation="border"
                    size="sm"
                    role="status"
                    aria-hidden="true"
                    className="me-2"
                  />
                  Generando reporte...
                </>
              ) : (
                '📄 Descargar Reporte PDF'
              )}
            </Button>
          </div>
        </Card.Body>
      </Card>

      <Card className="card-custom mt-4">
        <Card.Body>
          <Card.Title>Formato del Reporte</Card.Title>
          <Card.Text style={{ color: 'var(--text-muted)' }}>
            El reporte se genera en formato PDF utilizando OpenPDF, una librería de código abierto.
            El documento incluye todas las secciones requeridas para la evaluación académica,
            incluyendo la fundamentación matemática de cada algoritmo y el análisis formal de
            complejidad computacional.
          </Card.Text>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default ReportExport;
