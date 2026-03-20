import api from './api';

/**
 * Servicio para gestión de activos financieros.
 */
export const assetService = {
  /**
   * Obtiene todos los activos disponibles.
   * @returns {Promise<Array>} Lista de activos
   */
  getAllAssets: async () => {
    const response = await api.get('/assets');
    return response.data;
  },

  /**
   * Obtiene un activo por su ticker.
   * @param {string} ticker - Símbolo del activo
   * @returns {Promise<Object>} Datos del activo
   */
  getAssetByTicker: async (ticker) => {
    const response = await api.get(`/assets/ticker/${ticker}`);
    return response.data;
  },

  /**
   * Obtiene datos históricos de precios para un activo.
   * @param {string} ticker - Símbolo del activo
   * @returns {Promise<Array>} Lista de datos OHLCV
   */
  getPriceData: async (ticker) => {
    const response = await api.get(`/etl/prices/${ticker}`);
    return response.data;
  },

  /**
   * Obtiene datos de precios en un rango de fechas.
   * @param {string} ticker - Símbolo del activo
   * @param {string} startDate - Fecha inicial (YYYY-MM-DD)
   * @param {string} endDate - Fecha final (YYYY-MM-DD)
   * @returns {Promise<Array>} Lista de datos OHLCV
   */
  getPriceDataByRange: async (ticker, startDate, endDate) => {
    const response = await api.get(`/etl/prices/${ticker}/range`, {
      params: { startDate, endDate }
    });
    return response.data;
  }
};
