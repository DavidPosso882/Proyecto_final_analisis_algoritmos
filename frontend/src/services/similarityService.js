import api from './api';

/**
 * Servicio para algoritmos de similitud.
 */
export const similarityService = {
  /**
   * Compara dos activos usando todos los algoritmos.
   * @param {string} ticker1 - Primer activo
   * @param {string} ticker2 - Segundo activo
   * @returns {Promise<Object>} Resultados de las 4 métricas
   */
  compareAssets: async (ticker1, ticker2) => {
    const response = await api.get('/similarity/compare', {
      params: { ticker1, ticker2 }
    });
    return response.data;
  },

  /**
   * Calcula distancia euclidiana.
   * @param {string} ticker1 - Primer activo
   * @param {string} ticker2 - Segundo activo
   * @returns {Promise<Object>} Resultado del algoritmo
   */
  getEuclideanDistance: async (ticker1, ticker2) => {
    const response = await api.get('/similarity/euclidean', {
      params: { ticker1, ticker2 }
    });
    return response.data;
  },

  /**
   * Calcula correlación de Pearson.
   * @param {string} ticker1 - Primer activo
   * @param {string} ticker2 - Segundo activo
   * @returns {Promise<Object>} Resultado del algoritmo
   */
  getPearsonCorrelation: async (ticker1, ticker2) => {
    const response = await api.get('/similarity/pearson', {
      params: { ticker1, ticker2 }
    });
    return response.data;
  },

  /**
   * Calcula similitud por coseno.
   * @param {string} ticker1 - Primer activo
   * @param {string} ticker2 - Segundo activo
   * @returns {Promise<Object>} Resultado del algoritmo
   */
  getCosineSimilarity: async (ticker1, ticker2) => {
    const response = await api.get('/similarity/cosine', {
      params: { ticker1, ticker2 }
    });
    return response.data;
  },

  /**
   * Calcula Dynamic Time Warping.
   * @param {string} ticker1 - Primer activo
   * @param {string} ticker2 - Segundo activo
   * @returns {Promise<Object>} Resultado del algoritmo
   */
  getDTW: async (ticker1, ticker2) => {
    const response = await api.get('/similarity/dtw', {
      params: { ticker1, ticker2 }
    });
    return response.data;
  }
};
