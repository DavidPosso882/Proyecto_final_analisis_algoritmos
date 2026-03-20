import api from './api';

/**
 * Servicio para análisis de datos financieros.
 */
export const analysisService = {
  /**
   * Obtiene la matriz de correlación entre activos.
   * @returns {Promise<Object>} Matriz de correlación con tickers
   */
  getCorrelationMatrix: async () => {
    const response = await api.get('/analysis/correlation-matrix');
    return response.data;
  },

  /**
   * Calcula medias móviles simples (SMA) para un activo.
   * @param {string} ticker - Símbolo del activo
   * @param {number} periods - Períodos para SMA (default: 20)
   * @returns {Promise<Object>} Datos SMA calculados
   */
  getSMA: async (ticker, periods = 20) => {
    const response = await api.get(`/analysis/sma/${ticker}`, {
      params: { periods }
    });
    return response.data;
  },

  /**
   * Calcula retornos diarios para un activo.
   * @param {string} ticker - Símbolo del activo
   * @param {string} startDate - Fecha inicial opcional
   * @param {string} endDate - Fecha final opcional
   * @returns {Promise<Object>} Lista de retornos diarios
   */
  getReturns: async (ticker, startDate, endDate) => {
    const params = {};
    if (startDate) params.startDate = startDate;
    if (endDate) params.endDate = endDate;
    
    const response = await api.get(`/analysis/returns/${ticker}`, { params });
    return response.data;
  }
};
