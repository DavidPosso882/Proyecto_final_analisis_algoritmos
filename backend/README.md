# Proyecto Final - Análisis de Algoritmos

Sistema de análisis técnico, estadístico y comparativo de activos financieros con implementación manual de algoritmos.

## 📋 Descripción

Proyecto académico para el curso de Análisis de Algoritmos - Universidad del Quindío (2026-1).

**Objetivo:** Diseñar e implementar algoritmos que permitan analizar series temporales financieras (BVC + activos globales) con énfasis en eficiencia computacional y análisis formal de complejidad.

## 🏗️ Arquitectura

```
backend/
├── src/main/java/com/analisis/algoritmos/
│   ├── BackendApplication.java          # Punto de entrada
│   ├── config/
│   │   └── AppConfig.java              # CORS + RestTemplate
│   ├── controller/                      # API REST
│   │   ├── AssetController.java         # Gestión de activos
│   │   ├── EtlController.java          # Pipeline ETL
│   │   ├── AnalysisController.java     # Correlación, SMA, Retornos
│   │   ├── SimilarityController.java   # Euclidiana, Pearson, Coseno, DTW
│   │   ├── PatternController.java      # Sliding Window + Volatilidad
│   │   ├── SortingController.java      # 12 algoritmos de sorting
│   │   └── ReportController.java       # Generación de PDF
│   ├── model/                           # Entidades JPA
│   │   ├── Asset.java
│   │   └── PriceData.java
│   ├── repository/                      # Spring Data JPA
│   ├── service/                         # Lógica de negocio
│   │   ├── EtlService.java             # Pipeline ETL automatizado
│   │   ├── YahooFinanceClient.java     # HTTP client manual
│   │   ├── SortingService.java         # Benchmark de 12 algoritmos
│   │   └── DataProcessingService.java  # Procesamiento de datos
│   └── algorithms/                      # ✅ TODOS IMPLEMENTADOS
│       ├── sorting/                     # 12 algoritmos de ordenamiento
│       │   ├── SortingAlgorithm.java   # Interfaz
│       │   ├── TimSort.java            # O(n log n) — híbrido runs + insertion
│       │   ├── QuickSort.java          # O(n log n) — partition
│       │   ├── HeapSort.java           # O(n log n) — heapify
│       │   ├── BitonicSort.java        # O(n log² n) — con padding
│       │   ├── CombSort.java           # O(n²) — shrink factor
│       │   ├── TreeSort.java           # O(n log n) — BST manual
│       │   ├── BucketSort.java         # O(n + k) — distribución
│       │   ├── RadixSort.java          # O(d*(n + k))
│       │   ├── PigeonholeSort.java     # O(n + range)
│       │   ├── SelectionSort.java      # O(n²)
│       │   ├── GnomeSort.java          # O(n²)
│       │   └── BinaryInsertionSort.java # O(n²)
│       ├── similarity/                  # 4 algoritmos de similitud
│       │   ├── EuclideanDistance.java   # √Σ(xi - yi)²
│       │   ├── PearsonCorrelation.java # cov(x,y) / (σx * σy)
│       │   ├── CosineSimilarity.java   # (A·B) / (||A|| × ||B||)
│       │   └── DynamicTimeWarping.java # Programación dinámica O(n×m)
│       ├── patterns/
│       │   ├── SlidingWindow.java      # Detección de patrones
│       │   └── VolatilityCalculator.java # σ_anual = σ_diaria × √252
│       └── preprocessing/
│           ├── ReturnsCalculator.java  # r_t = ln(P_t / P_{t-1})
│           └── SimpleMovingAverage.java # Ventana deslizante O(n)
└── pom.xml
```

## 🚀 Tecnologías

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.2
- **Base de Datos:** MariaDB (producción) / H2 (desarrollo)
- **Build Tool:** Maven
- **Frontend:** React + Chart.js + React-Bootstrap
- **Exportación PDF:** OpenPDF

## ⚙️ Configuración

### 1. Base de Datos

Editar `src/main/resources/application.properties`:

```properties
# Para desarrollo (H2 - por defecto)
spring.datasource.url=jdbc:h2:file:./data/analisis_algoritmos_db
spring.h2.console.enabled=true

# Para producción (MariaDB) - descomentar
# spring.datasource.url=jdbc:mariadb://localhost:3306/analisis_algoritmos
# spring.datasource.username=tu_usuario
# spring.datasource.password=tu_password
```

## 🚀 Ejecución

### Backend
```bash
cd backend
./mvnw spring-boot:run         # Ejecutar servidor (puerto 8080)
./mvnw clean compile           # Solo compilar
```

### Frontend
```bash
cd frontend
npm install                    # Instalar dependencias
npm start                      # Ejecutar en desarrollo (puerto 3000)
```

### Acceder a la aplicación

- Frontend: http://localhost:3000
- API REST: http://localhost:8080/api/
- Consola H2: http://localhost:8080/h2-console

## 📡 API Endpoints

### Activos
```
GET    /api/assets                       # Listar todos
GET    /api/assets/{id}                  # Por ID
GET    /api/assets/ticker/{ticker}       # Por ticker
```

### ETL
```
POST   /api/etl/download                # Iniciar descarga
POST   /api/etl/rebuild                 # Reconstruir dataset completo
GET    /api/etl/prices/{ticker}         # Precios por ticker
```

### Análisis
```
GET    /api/analysis/correlation-matrix  # Matriz correlación Pearson
GET    /api/analysis/sma/{ticker}       # Media móvil simple
GET    /api/analysis/returns/{ticker}   # Retornos logarítmicos
```

### Similitud
```
GET    /api/similarity/compare?ticker1=X&ticker2=Y  # Comparar activos
GET    /api/similarity/euclidean        # Distancia euclidiana
GET    /api/similarity/pearson          # Correlación Pearson
GET    /api/similarity/dtw              # Dynamic Time Warping
```

### Sorting
```
POST   /api/sorting/analyze             # Ejecutar benchmark 12 algoritmos
GET    /api/sorting/sorted-data         # Datos ordenados fecha + cierre
GET    /api/sorting/top-volume          # Top 15 mayor volumen
```

### Patrones
```
GET    /api/patterns/analyze?ticker=X   # Análisis de patrones
GET    /api/patterns/volatility?ticker=X # Volatilidad
```

### Reportes
```
GET    /api/reports/pdf                 # Generar reporte PDF
```

## 📝 Restricciones del Proyecto

- ❌ NO usar `yfinance`, `pandas-datareader` ni similares
- ❌ NO usar librerías de alto nivel para implementar algoritmos
- ✅ Implementar algoritmos manualmente desde estructuras básicas
- ✅ Usar Java estándar + Spring Boot + JPA
- ✅ HTTP requests manuales con RestTemplate para ETL

## 👤 Autor

**David** - Full Stack Developer  
Universidad del Quindío  
Ingeniería de Sistemas y Computación  
Análisis de Algoritmos 2026-1

## 📄 Licencia

Proyecto académico - Universidad del Quindío
