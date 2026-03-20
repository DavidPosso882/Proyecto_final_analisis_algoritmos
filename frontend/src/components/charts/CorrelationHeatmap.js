import React, { useEffect, useRef } from 'react';
import Plotly from 'plotly.js-dist-min';
import { useTheme } from '../../contexts/ThemeContext';

/**
 * Componente de mapa de calor de correlación usando Plotly.js.
 * 
 * Muestra una matriz de correlación entre activos financieros.
 * Se adapta automáticamente al tema claro u oscuro.
 * 
 * @param {Object} props - Propiedades del componente
 * @param {Array} props.tickers - Array de símbolos de activos
 * @param {Array} props.matrix - Matriz de correlación (2D array)
 * @param {number} props.height - Altura del gráfico (default: 600)
 */
const CorrelationHeatmap = ({ tickers, matrix, height = 600 }) => {
  const chartRef = useRef(null);
  const { isDark } = useTheme();

  useEffect(() => {
    if (!tickers || !matrix || tickers.length === 0) return;

    // Obtener colores según el tema
    const plotBgColor = isDark ? '#16213e' : '#ffffff';
    const paperBgColor = isDark ? '#16213e' : '#ffffff';
    const textColor = isDark ? '#eaeaea' : '#212529';

    // Trace del heatmap
    const heatmapTrace = {
      z: matrix,
      x: tickers,
      y: tickers,
      type: 'heatmap',
      colorscale: [
        [0, '#d73027'],      // Rojo fuerte (correlación -1)
        [0.25, '#fc8d59'],   // Rojo claro
        [0.5, '#fee08b'],    // Amarillo (correlación 0)
        [0.75, '#91cf60'],   // Verde claro
        [1, '#1a9850']       // Verde fuerte (correlación 1)
      ],
      zmid: 0,
      zmin: -1,
      zmax: 1,
      showscale: true,
      colorbar: {
        title: {
          text: 'Correlación',
          font: { color: textColor }
        },
        titleside: 'right',
        tickfont: { size: 10, color: textColor }
      },
      hovertemplate: 
        '<b>%{x}</b> vs <b>%{y}</b><br>' +
        'Correlación: %{z:.3f}<br>' +
        '<extra></extra>'
    };

    // Layout del gráfico
    const layout = {
      title: {
        text: 'Matriz de Correlación de Activos',
        font: { size: 18, color: textColor }
      },
      xaxis: {
        title: '',
        tickangle: -45,
        tickfont: { size: 10, color: textColor },
        automargin: true
      },
      yaxis: {
        title: '',
        tickfont: { size: 10, color: textColor },
        autorange: 'reversed',
        automargin: true
      },
      height: height,
      margin: { t: 80, b: 100, l: 100, r: 80 },
      plot_bgcolor: plotBgColor,
      paper_bgcolor: paperBgColor
    };

    const config = {
      responsive: true,
      displayModeBar: true,
      displaylogo: false
    };

    const chartElement = chartRef.current;
    Plotly.newPlot(chartElement, [heatmapTrace], layout, config);

    // Cleanup
    return () => {
      if (chartElement) {
        Plotly.purge(chartElement);
      }
    };
  }, [tickers, matrix, height, isDark]);

  return (
    <div 
      ref={chartRef} 
      style={{ width: '100%', height: `${height}px` }}
    />
  );
};

export default CorrelationHeatmap;
