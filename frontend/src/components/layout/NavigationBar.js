import React from 'react';
import { Navbar, Nav, Container, Form } from 'react-bootstrap';
import { Link, useLocation } from 'react-router-dom';
import { useTheme } from '../../contexts/ThemeContext';

/**
 * Barra de navegación principal.
 * 
 * Muestra el título del proyecto, enlaces a las diferentes secciones
 * y un toggle para cambiar entre tema claro y oscuro.
 */
const NavigationBar = () => {
  const location = useLocation();
  const { toggleTheme, isDark } = useTheme();

  return (
    <Navbar className="navbar-custom mb-4" expand="lg">
      <Container>
        <Navbar.Brand as={Link} to="/">
          📈 Análisis de Algoritmos - Finanzas
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link 
              as={Link} 
              to="/" 
              active={location.pathname === '/'}
            >
              Dashboard
            </Nav.Link>
            <Nav.Link 
              as={Link} 
              to="/similarity" 
              active={location.pathname === '/similarity'}
            >
              Similitud
            </Nav.Link>
            <Nav.Link 
              as={Link} 
              to="/patterns" 
              active={location.pathname === '/patterns'}
            >
              Patrones
            </Nav.Link>
            <Nav.Link 
              as={Link} 
              to="/risk" 
              active={location.pathname === '/risk'}
            >
              Riesgo
            </Nav.Link>
            <Nav.Link 
              as={Link} 
              to="/reports" 
              active={location.pathname === '/reports'}
            >
              Reportes
            </Nav.Link>
            <Nav.Link 
              as={Link} 
              to="/sorting" 
              active={location.pathname === '/sorting'}
            >
              Ordenamiento
            </Nav.Link>
          </Nav>
          <Nav className="align-items-center">
            {/* Toggle de tema */}
            <div className="theme-switch me-3">
              <span className="theme-icon">{isDark ? '🌙' : '☀️'}</span>
              <Form.Check
                type="switch"
                id="theme-switch"
                checked={isDark}
                onChange={toggleTheme}
                aria-label="Cambiar tema"
              />
            </div>
            <Navbar.Text>
              <small>Universidad del Quindío 2026-1</small>
            </Navbar.Text>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default NavigationBar;
