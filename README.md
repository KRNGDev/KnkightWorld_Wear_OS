# 🛡️ KnightWorld Wear OS

KnightWorld renace como una experiencia nativa para **Wear OS by Google**. El antiguo watchface web para Tizen se ha reescrito desde cero utilizando **Kotlin** y **Jetpack Compose for Wear OS**, manteniendo la estética medieval y convirtiendo tus pasos diarios en progreso dentro de la aventura del caballero.

## ✨ Novedades principales

- **App nativa para Android/Wear OS** con arquitectura moderna y soporte para sensores del reloj.
- **Interfaz en Jetpack Compose** optimizada para pantallas circulares, con barras de energía y salud inspiradas en el proyecto original.
- **Sincronización con sensores**: Lectura en tiempo real de pasos (`TYPE_STEP_COUNTER`) y estado de batería mediante `BroadcastReceiver`.
- **Narrativa dinámica** que convierte tu actividad física en batallas ganadas, pociones recolectadas y misiones completadas.

## 🧱 Arquitectura

La aplicación sigue una estructura simple pero escalable:

```
app/
 ├── src/main/java/com/knightworld/wear
 │   ├── MainActivity.kt        # Punto de entrada y composición de la UI
 │   └── ui/theme/              # Paleta de colores y tipografía personalizada
 └── src/main/res               # Strings y estilos de la app
```

- **MainActivity** solicita permisos para sensores, escucha la batería y pasos, y genera las estadísticas de juego.
- **KnightWorldTheme** define la paleta nocturna, tipografía y estilos reutilizables inspirados en el proyecto HTML original.

## 🛠️ Requisitos

- Android Studio Giraffe (o superior) con el **plugin de Wear OS**.
- Dispositivo o emulador Wear OS con Android 13 (API 33) o superior.
- Java 17 instalado y configurado para Gradle.

## ▶️ Cómo ejecutar

1. **Clona** el repositorio y abre la carpeta `KnigchtWorld` en Android Studio.
2. Sincroniza Gradle; el wrapper incluido descarga Gradle 8.0.2 automáticamente.
3. Crea o conecta un dispositivo Wear OS.
4. Ejecuta `Run > Run 'app'` o, desde la terminal:
   ```bash
   ./gradlew installDebug
   ```
5. Otorga permisos de **reconocimiento de actividad** y **sensores corporales** cuando la app los solicite.

## ♻️ Migración desde Tizen

- Los activos visuales del proyecto original se conservan en `KinghtWorldface/` como referencia histórica.
- La nueva implementación usa Compose, por lo que animaciones e interactividad ahora se renderizan nativamente.
- Las métricas del juego (victorias, derrotas, pociones) se recalculan en base a pasos y batería, listos para ampliarse con servicios de salud de Google.

## 🚀 Próximos pasos

- Integrar objetivos diarios utilizando `Health Services` para Wear OS.
- Sincronizar progreso con una app compañera en Android.
- Añadir watch face nativo basado en `Tiles` o `Complications`.

## 📦 Exportar el proyecto

¿Necesitas compartir el código tal y como está en la rama actual? Ejecuta:

```bash
./scripts/archive_project.sh
```

El script genera `KnightWorld.zip` en la raíz del repositorio utilizando `git archive HEAD`, de modo que obtendrás un paquete listo para descargar sin archivos temporales ni dependencias compiladas.

## 🌐 Publicar en un repositorio nuevo

Si deseas subir esta versión a GitHub bajo un proyecto como `KinghtGearOs`, sigue estos pasos desde la raíz del repo:

1. Inicia sesión en GitHub y crea un **repositorio vacío** llamado `KinghtGearOs` sin README ni archivos adicionales.
2. En la terminal, asegúrate de estar en la carpeta `KnigchtWorld` y verifica el estado:
   ```bash
   git status
   ```
3. Añade el remoto que apunta al nuevo repositorio (reemplaza `TU_USUARIO` por tu nombre de usuario):
   ```bash
   git remote add origin https://github.com/TU_USUARIO/KinghtGearOs.git
   ```
   Usa `git remote set-url origin ...` si el remoto `origin` ya existía y quieres actualizarlo.
4. Sube la rama actual, por ejemplo `work`, o renómbrala a `main` antes de subirla:
   ```bash
   git push -u origin work
   # o
   git branch -M main
   git push -u origin main
   ```
5. Comprueba en GitHub que el commit más reciente aparezca en el repositorio `KinghtGearOs`. A partir de ahí podrás clonar, crear pull requests y seguir trabajando desde tu cuenta.

¡Forja tu leyenda ahora desde cualquier reloj con Wear OS! ⚔️
