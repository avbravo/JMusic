# JMusic - Editor de Canciones con IA

JMusic es una aplicación moderna construida con **Java (Quarkus)** y una interfaz web **3D futurista** utilizando **Three.js**. Permite a los compositores escribir letras, analizar métricas y recibir sugerencias de rimas potenciadas por lógica de análisis de texto.

## Características

- 🎨 **Interfaz 3D**: Entorno visual inmersivo basado en partículas.
- ✍️ **Editor en Tiempo Real**: Análisis de sílabas y métrica mientras escribes.
- 🤖 **IA de Rimas**: Sugerencias automáticas basadas en el final del verso.
- 📂 **Formato Markdown**: Las canciones se guardan como archivos `.md` para máxima compatibilidad.

## Requisitos

- **Java 21** o superior.
- **Maven 3.9+**.

## Pasos para Compilar y Ejecutar

### 1. Compilación
Desde la raíz del proyecto `JMusic`, ejecuta:

```bash
mvn clean compile
```

### 2. Ejecución en Modo Desarrollo (Recomendado)
Para iniciar la aplicación y ver los cambios en caliente:

```bash
mvn quarkus:dev
```

### 3. Acceso a la Aplicación
Una vez que Quarkus haya iniciado, abre tu navegador en:
[http://localhost:8080](http://localhost:8080)

## Estructura de Archivos
- `src/main/java`: Lógica de análisis y API REST.
- `src/main/resources/META-INF/resources`: Frontend (HTML, CSS, JS, Audio/3D).
- `songs_data`: Carpeta donde se almacenan las canciones guardadas.

## Uso del Editor
- Escribe tus versos en el panel central.
- El panel derecho se actualizará automáticamente con la cuenta de sílabas y sugerencias.
- Haz clic en **GUARDAR .MD** para persistir tu canción.
