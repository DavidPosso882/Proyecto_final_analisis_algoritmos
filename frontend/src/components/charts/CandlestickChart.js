import React, { useEffect, useRef } from 'react';
import Plotly from 'plotly.js-dist-min';
import { useTheme } from '../../contexts/ThemeContext';

/**
 * Componente de gráfico de velas (Candlestick) usando Plotly.js.
 * 
 * Muestra datos OHLC (Open-High-Low-Close) para análisis técnico.
 * Se adapta automáticamente al tema claro u oscuro.
 * 
 * @param {Object} props - Propiedades del componente
 * @param {Array} props.data - Array de datos OHLCV
 * @param {string} props.ticker - Símbolo del activo
 * @param {number} props.height - Altura del gráfico (default: 500)
 * @param {boolean} props.showSMA - Mostrar Media Móvil Simple (default: false)
 */
const CandlestickChart = ({ data, ticker, height = 500, showSMA = false }) => {
  const chartRef = useRef(null);
  const { isDark } = useTheme();

  // Función para calcular SMA
  const calculateSMA = (closingPrices, period) => {
    if (closingPrices.length < period) return [];
    const sma = [];
    for (let i = period - 1; i < closingPrices.length; i++) {
      const sum = closingPrices.slice(i - period + 1, i + 1).reduce((a, b) => a + b, 0);
      sma.push(sum / period);
    }
    return sma;
  };

  useEffect(() => {
    if (!data || data.length === 0) return;

    // Obtener colores según el tema
    const plotBgColor = isDark ? '#16213e' : '#f8f9fa';
    const paperBgColor = isDark ? '#16213e' : '#ffffff';
    const textColor = isDark ? '#eaeaea' : '#212529';
    const gridColor = isDark ? '#2d3561' : '#e9ecef';
    const sma20Color = isDark ? '#fbbf24' : '#f59e0b'; // Amber
    const sma50Color = isDark ? '#60a5fa' : '#3b82f6'; // Blue

    // Preparar datos para Plotly
    const dates = data.map(d => d.date);
    const opens = data.map(d => d.openPrice);
    const highs = data.map(d => d.highPrice);
    const lows = data.map(d => d.lowPrice);
    const closes = data.map(d => d.closePrice);

    // Trace de velas
    const candlestickTrace = {
      x: dates,
      open: opens,
      high: highs,
      low: lows,
      close: closes,
      type: 'candlestick',
      name: ticker,
      increasing: { 
        line: { color: isDark ? '#4ade80' : '#26a69a' }, 
        fillcolor: isDark ? '#4ade80' : '#26a69a' 
      },
      decreasing: { 
        line: { color: isDark ? '#f87171' : '#ef5350' }, 
        fillcolor: isDark ? '#f87171' : '#ef5350' 
      }
    };

    const traces = [candlestickTrace];

    // Agregar SMAs si está habilitado
    if (showSMA) {
      // SMA 20
      if (data.length >= 20) {
        const sma20Data = calculateSMA(closes, 20);
        const sma20Dates = dates.slice(19);
        
        const sma20Trace = {
          x: sma20Dates,
          y: sma20Data,
          type: 'scatter',
          mode: 'lines',
          name: 'SMA (20)',
          line: { color: sma20Color, width: 2 },
          yaxis: 'y'
        };
        traces.push(sma20Trace);
      }

      // SMA 50
      if (data.length >= 50) {
        const sma50Data = calculateSMA(closes, 50);
        const sma50Dates = dates.slice(49);
        
        const sma50Trace = {
          x: sma50Dates,
          y: sma50Data,
          type: 'scatter',
          mode: 'lines',
          name: 'SMA (50)',
          line: { color: sma50Color, width: 2 },
          yaxis: 'y'
        };
        traces.push(sma50Trace);
      }
    }

    // Layout del gráfico
    const layout = {
      title: {
        text: `Gráfico de Velas - ${ticker}`,
        font: { size: 18, color: textColor }
      },
      xaxis: {
        title: {
          text: 'Fecha',
          font: { color: textColor }
        },
        rangeslider: { visible: false },
        type: 'date',
        gridcolor: gridColor,
        tickfont: { color: textColor }
      },
      yaxis: {
        title: {
          text: 'Precio',
          font: { color: textColor }
        },
        autorange: true,
        gridcolor: gridColor,
        tickfont: { color: textColor }
      },
      height: height,
      margin: { t: 50, b: 50, l: 50, r: 50 },
      plot_bgcolor: plotBgColor,
      paper_bgcolor: paperBgColor,
      showlegend: true,
      legend: {
        font: { color: textColor }
      }
    };

    const config = {
      responsive: true,
      displayModeBar: true,
      modeBarButtonsToAdd: ['drawline', 'drawopenpath', 'eraseshape'],
      displaylogo: false
    };

    const chartElement = chartRef.current;
    
    // Usar Plotly.react para mejor rendimiento en actualizaciones
    Plotly.react(chartElement, traces, layout, config);

    // Cleanup
    return () => {
      if (chartElement) {
        Plotly.purge(chartElement);
      }
    };
  }, [data, ticker, height, isDark, showSMA]);

  return (
    <div 
      ref={chartRef} 
      style={{ width: '100%', height: `${height}px` }}
    />
  );
};

export default CandlestickChart;
