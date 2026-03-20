import React from 'react';
import { Container, Row, Col } from 'react-bootstrap';
import NavigationBar from './NavigationBar';

/**
 * Layout principal del dashboard.
 * 
 * Envuelve todas las páginas con la barra de navegación
 * y el contenedor principal. Soporta tema claro y oscuro.
 * 
 * @param {Object} props - Propiedades del componente
 * @param {React.ReactNode} props.children - Contenido de la página
 */
const DashboardLayout = ({ children }) => {
  return (
    <div className="dashboard-layout min-vh-100">
      <NavigationBar />
      <Container fluid className="py-4">
        <Row>
          <Col>
            {children}
          </Col>
        </Row>
      </Container>
    </div>
  );
};

export default DashboardLayout;
