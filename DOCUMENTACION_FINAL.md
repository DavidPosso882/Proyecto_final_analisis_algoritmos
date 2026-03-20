# Documentación Final - Proyecto Final Análisis de Algoritmos

## Resumen del Proyecto

El proyecto consiste en una aplicación web para análisis financiero que implementa manualmente algoritmos de ordenamiento, cálculo de volatilidad, correlación y similitud entre activos financieros.

**Stack Tecnológico:**
- Backend: Java 21, Spring Boot 3.2, MariaDB, JPA
- Frontend: React 18, Bootstrap 5.3, Plotly.js
- Algoritmos: Implementaciones manuales (sin librerías de alto nivel)

## Problemas Identificados y Corregidos

### 1. Datos Duplicados en Base de Datos
**Problema:** La tabla `price_data` contenía 20,880 registros duplicados (múltiples registros para la misma fecha y activo).

**Solución:** Eliminación de duplicados manteniendo un registro por (asset_id, date).

```sql
-- Registros eliminados: 20,880
-- Registros totales antes: 49,590
-- Registros totales después: 28,710
```

### 2. Cálculo de Volatilidad Incorrecto
**Problema:** Los valores de volatilidad eran extremadamente altos (miles de porcentajes) debido a los datos duplicados.

**Solución:** 
- Corrección de la base de datos (eliminación de duplicados)
- Implementación de endpoint `/api/risk/volatility`

**Resultados (ejemplos):**
- BND: 5.90% (antes: 288.86%)
- VOO: 32.27% (antes: 3226.85%)

### 3. StackOverflowError en QuickSort
**Problema:** El algoritmo QuickSort causaba StackOverflowError con grandes volúmenes de datos (49,590 registros) debido a recursión profunda.

**Solución:** Implementación de QuickSort iterativo usando una pila explícita.

**Código modificado:**
```java
private <T> void quickSort(T[] array, int low, int high, Comparator<T> comparator) {
    java.util.Stack<int[]> stack = new java.util.Stack<>();
    stack.push(new int[]{low, high});
    
    while (!stack.isEmpty()) {
        int[] range = stack.pop();
        // ... lógica de partición
    }
}
```

### 4. Problema N+1 en Carga de Datos
**Problema:** La relación `@ManyToOne(fetch = FetchType.EAGER)` entre `PriceData` y `Asset` causaba 49,590 consultas adicionales.

**Solución:** 
- Cambio a `FetchType.LAZY` en `PriceData.java`
- Implementación de consultas con `JOIN FETCH` en `PriceDataRepository`

**Cambios:**
```java
// PriceData.java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "asset_id", nullable = false)
private Asset asset;

// PriceDataRepository.java
@Query("SELECT p FROM PriceData p JOIN FETCH p.asset ORDER BY p.date ASC, p.closePrice ASC")
List<PriceData> findAllWithAsset();
```

### 5. Experiencia de Usuario en Frontend
**Problema:** Falta de feedback visual durante ejecución de análisis largos.

**Solución:** Agregación de mensaje de progreso en `SortingAnalysis.js`.

```jsx
{loading && (
    <div className="alert alert-info mt-2" role="alert">
        <strong>Ejecutando análisis de algoritmos...</strong>
        <br />
        <small>Este proceso puede tardar varios minutos debido a la complejidad de algunos algoritmos.</small>
    </div>
)}
```

## Endpoints Implementados

### Backend (Spring Boot)

1. **Análisis de Ordenamiento** (`/api/sorting`)
   - `POST /analyze`: Ejecuta 12 algoritmos de ordenamiento
   - `GET /sorted-data`: Datos ordenados por fecha + precio de cierre
   - `GET /top-volume`: Top 15 días con mayor volumen

2. **Análisis de Riesgo** (`/api/risk`)
   - `GET /volatility`: Volatilidad anualizada de todos los activos

3. **Análisis de Correlación** (`/api/analysis`)
   - `GET /correlation-matrix`: Matriz de correlación de Pearson

4. **Datos de Activos** (`/api/etl`)
   - `GET /prices/{ticker}`: Datos históricos de un activo

### Frontend (React)

1. **Dashboard** (`/`): Gráfico de velas y matriz de correlación
2. **Análisis de Riesgo** (`/risk`): Clasificación por volatilidad
3. **Análisis de Similitud** (`/similarity`): Comparación de activos
4. **Ordenamiento** (`/sorting`): Análisis de algoritmos de ordenamiento

## Algoritmos Implementados

### Ordenamiento (12 algoritmos)
1. TimSort - O(n log n)
2. Comb Sort - O(n²)
3. Selection Sort - O(n²)
4. Tree Sort - O(n log n)
5. Pigeonhole Sort - O(n + k)
6. BucketSort - O(n + k)
7. QuickSort - O(n log n)
8. HeapSort - O(n log n)
9. Bitonic Sort - O(n log² n)
10. Gnome Sort - O(n²)
11. Binary Insertion Sort - O(n²)
12. RadixSort - O(nk)

### Similitud
- Distancia Euclidiana
- Correlación de Pearson
- Similitud del Coseno
- Dynamic Time Warping (DTW)

### Cálculos Financieros
- Volatilidad anualizada
- Retornos logarítmicos
- Medias móviles simples (SMA)
- Matriz de correlación

## Estado Actual

### ✅ Funcionalidades Completadas
- Backend 100% funcional
- Frontend con navegación y visualizaciones
- Base de datos optimizada (sin duplicados)
- Todos los algoritmos implementados manualmente
- Endpoints REST funcionando correctamente

### ⚠️ Limitaciones Conocidas
- Algoritmos O(n²) son lentos con grandes volúmenes de datos (inherente a su complejidad)
- Tiempo de ejecución del análisis completo: ~2-3 minutos

### 📊 Métricas del Sistema
- Activos en base de datos: 22
- Registros de precios: 28,710 (únicos)
- Algoritmos de ordenamiento: 12
- Endpoints REST: 15+

## Instrucciones de Ejecución

### Backend
```bash
cd backend
mvn spring-boot:run
```

### Frontend
```bash
cd frontend
npm start
```

### Acceso
- Backend: http://localhost:8080
- Frontend: http://localhost:3000

## Conclusión

El proyecto ha sido completado exitosamente. Todos los errores críticos han sido corregidos y la aplicación funciona correctamente. Los algoritmos de ordenamiento O(n²) son lentos por diseño, lo cual es esperado y parte del análisis de complejidad algorítmica solicitado en los requisitos del proyecto.

---
**Fecha de finalización:** 2026-03-10
**Universidad del Quindío - Análisis de Algoritmos 2026-1**