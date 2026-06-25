# Informe técnico de seguridad — GestorTareas

Documento académico de apoyo para presentar el estado de seguridad evaluado del proyecto Android/Kotlin `GestorTareas`, las vulnerabilidades revisadas, las mitigaciones implementadas y la evidencia que debe adjuntarse manualmente.

> **Nota de edición:** los campos marcados como `[Completar]` o `[Insertar captura...]` son placeholders explícitos para personalizar la entrega final. No deben quedar como si fueran contenido definitivo sin completar o sin reemplazar por evidencia real.

## Portada y datos editables

| Campo | Valor editable |
|-------|----------------|
| Institución / asignatura | `[Completar]` |
| Actividad evaluativa | `[Completar]` |
| Estudiante(s) | `[Completar]` |
| Docente | `[Completar]` |
| Fecha de entrega | `[Completar]` |
| Versión del informe | `1.1` |
| Proyecto evaluado | `GestorTareas` |
| Plataforma | `Android / Kotlin` |

## Resumen ejecutivo

La evaluación de seguridad del proyecto `GestorTareas` identificó riesgos principalmente en control de acceso backend, resguardo de datos locales, validación de entradas y manejo de configuración sensible. La revisión confirmó que la aplicación utiliza Firebase Authentication y Firebase Realtime Database, por lo que los hallazgos relacionados con inyección SQL no aplican directamente al diseño actual.

Como resultado de la actividad, se implementaron controles para endurecer reglas de Firebase Realtime Database, deshabilitar respaldo Android no necesario, fortalecer validaciones de login/registro y limitar la entrada de datos en tareas. La herramienta principal de análisis estático fue **Android Lint**, lo cual cubre la exigencia del enunciado porque este permite usar **OWASP ZAP, SonarQube o Android Lint**. La verificación técnica disponible concluyó con `./gradlew lint test` en estado `BUILD SUCCESSFUL`.

El estado final se considera **PASS_WITH_WARNINGS**, sin bloqueantes funcionales o de seguridad para la entrega académica. Permanecen dos advertencias documentadas: la URL de Firebase Realtime Database continúa hardcodeada como deuda técnica no bloqueante y **SonarQube** queda recomendado como mejora futura para ampliar métricas de calidad, mantenibilidad y vulnerabilidades generales más allá del alcance cubierto por Android Lint.

## Retroalimentación previa considerada

La versión actual del informe incorpora de forma explícita la retroalimentación académica recibida en la entrega anterior y la usa como criterio de mejora documental para esta presentación final.

### Fortalezas que se mantienen como base de seguridad

- Se mantiene como fortaleza principal la definición explícita de reglas de **Firebase Realtime Database**, documentadas con enfoque **deny-by-default** y aislamiento por usuario.
- Se conserva el manejo seguro de la **API Key de Google Maps** mediante `local.properties` y el secrets plugin, evitando exponerla en el código fuente.
- Se mantiene el **CRUD completo de tareas** con confirmaciones de usuario en acciones sensibles, lo que mejora control funcional y reduce errores operativos.
- Se conserva la gestión correcta del ciclo de vida del listener de **Firebase**, removido en `onDestroy` para evitar fugas de recursos.

### Ajustes aplicados a partir de la observación docente

- Se añadió una checklist final de entrega para empaquetar únicamente archivos académicos y técnicos necesarios.
- Se agregó una nota formal sobre la restricción de compatibilidad derivada de `minSdk = 31`.
- Se reforzó la advertencia de no dejar instrucciones de plantilla visibles como si fueran parte del informe final.
- Se documentó el tratamiento cuidadoso de `local.properties` y `app/google-services.json` para no exponer secretos ni publicarlos en repositorios públicos.

## Ruta rápida de revisión

1. Revisar la tabla de vulnerabilidades detectadas y su severidad.
2. Validar las correcciones aplicadas y los resultados antes/después.
3. Adjuntar las evidencias pendientes indicadas en la sección final.

## Alcance y metodología

### Alcance evaluado

- Aplicación Android nativa desarrollada en Kotlin.
- Autenticación con Firebase Auth.
- Persistencia de datos con Firebase Realtime Database.
- Formularios de login, registro y gestión de tareas.
- Declaración de permisos y configuración de respaldo Android.
- Manejo local de archivos sensibles de configuración.
- Restricción de compatibilidad por versión mínima de Android.

### Metodología aplicada

| Área | Método |
|------|--------|
| Análisis estático principal | Revisión con Android Lint, aceptado por el enunciado junto con OWASP ZAP o SonarQube. |
| Verificación funcional | Ejecución de `./gradlew lint test`. |
| Revisión de controles móviles | Contraste manual con buenas prácticas OWASP MASVS / Mobile Top 10. |
| Validación de entradas | Pruebas manuales sobre login, registro y formulario de tareas. |
| Configuración Android | Revisión manual de `AndroidManifest.xml` y reglas de backup/data extraction. |
| Backend Firebase | Revisión local de `database.rules.json` y notas operativas. |

### Limitación técnica relevante

- El proyecto declara `minSdk = 31`, por lo que su alcance funcional se limita a dispositivos con **Android 12 o superior**.
- Esta restricción no invalida la seguridad implementada, pero sí debe informarse en la entrega porque deja fuera dispositivos anteriores y puede impactar el alcance esperado según el tipo de aplicación evaluada.

### Herramientas usadas

| Herramienta | Uso en la actividad | Estado |
|-------------|---------------------|--------|
| Android Lint | Detección de problemas de calidad y seguridad Android | Aplicado |
| Gradle (`./gradlew lint test`) | Validación automatizada de lint y pruebas | Aplicado |
| Revisión manual OWASP MASVS / Mobile Top 10 | Evaluación guiada de riesgos móviles | Aplicado |
| Pruebas manuales de validación | Verificación de entradas y flujos de UI | Aplicado |
| SonarQube | Análisis complementario de código | No aplicado en esta evaluación |
| OWASP ZAP | Pruebas dinámicas de tráfico HTTP/API | No aplicado en esta evaluación |

### Notas técnicas sobre herramientas

- **Android Lint** fue la herramienta principal de análisis estático y satisface el requerimiento del enunciado, ya que este habilita el uso de **OWASP ZAP, SonarQube o Android Lint**.
- **Android Lint no reemplaza completamente a SonarQube**. SonarQube queda como mejora futura para ampliar métricas de calidad, mantenibilidad, code smells y vulnerabilidades generales de código.
- **OWASP ZAP no se usó como herramienta principal** porque la aplicación consume principalmente SDKs de Firebase y no expone una API REST propia sobre la que tenga sentido centrar el análisis dinámico. En su lugar, las pruebas dinámicas se realizaron manualmente desde la app sobre autenticación, formularios y flujos funcionales.

## Vulnerabilidades detectadas

### Resumen de hallazgos

| ID | Hallazgo | Severidad | Estado |
|----|----------|-----------|--------|
| V-01 | Reglas backend restrictivas por usuario en Firebase | Alta | Mitigado / evidencia lista para incorporar |
| V-02 | Backup Android habilitado sin exclusiones seguras | Media | Corregido |
| V-03 | Validación insuficiente en login, registro y formulario de tareas | Media | Corregido |
| V-04 | Revisión de permisos de ubicación | Baja | Revisado y justificado |
| V-05 | Presencia local de API keys/configuración sensible | Media | Mitigado operacionalmente |
| V-06 | URL de Firebase Realtime Database hardcodeada | Baja | Documentado como deuda técnica |

### V-01 — Reglas backend Firebase restringidas por usuario

**Severidad:** Alta  
**Situación observada:** inicialmente no había evidencia suficiente en el entorno local que demostrara restricciones backend para aislar `tasks/{uid}` por `auth.uid`.  
**Impacto:** un control de acceso incorrecto en Firebase Realtime Database podría permitir lectura o escritura de tareas de otros usuarios.  
**Solución implementada:** se agregó `database.rules.json` con estrategia **deny-by-default**, autorización exclusiva sobre `/tasks/$uid` cuando `auth.uid == $uid`, validación estricta de `id`, `name` y `description`, y rechazo de campos extra.  
**Estado residual:** control mitigado y con evidencia lista para incorporación documental; permanece la recomendación de conservar trazabilidad de publicación cada vez que las reglas cambien.  
**Evidencia técnica local:** `database.rules.json`.

### V-02 — Respaldo Android no endurecido

**Severidad:** Media  
**Situación observada:** el respaldo automático de Android podía exponer información local si no se limitaba explícitamente.  
**Impacto:** riesgo de copia o transferencia de datos de aplicación/sesión en escenarios de backup o migración del dispositivo.  
**Solución implementada:** se configuró `android:allowBackup="false"` y se alinearon `backup_rules.xml` y `data_extraction_rules.xml` con exclusión total de dominios relevantes.  
**Evidencia técnica local:** `app/src/main/AndroidManifest.xml`, `app/src/main/res/xml/backup_rules.xml`, `app/src/main/res/xml/data_extraction_rules.xml`.

### V-03 — Validación insuficiente de entradas

**Severidad:** Media  
**Situación observada:** los formularios de autenticación y tareas no contaban con validaciones suficientemente estrictas para entradas vacías, formatos inválidos o longitudes excesivas.  
**Impacto:** datos inconsistentes, errores evitables, aumento de superficie para abuso de formularios y mala calidad de información persistida.  
**Solución implementada:** validación de email con `Patterns.EMAIL_ADDRESS`, contraseñas entre 6 y 128 caracteres, `trim()` sobre entradas de tareas y límites de longitud de 100 caracteres para nombre y 500 para descripción.  
**Evidencia técnica local:** validaciones observables en `LoginActivity.kt` y `FormActivity.kt`.

### V-04 — Permisos de ubicación revisados

**Severidad:** Baja  
**Situación observada:** se revisó la necesidad de mantener permisos `ACCESS_COARSE_LOCATION` y `ACCESS_FINE_LOCATION`.  
**Impacto:** un permiso innecesario ampliaría exposición, pero retirarlo incorrectamente puede romper el flujo de ubicación precisa/aproximada en Android moderno.  
**Decisión aplicada:** se conservaron ambos permisos por coherencia con el comportamiento esperado de Android actual y se actualizó el flujo de permisos.  
**Justificación:** no se consideró una vulnerabilidad activa tras la revisión; queda documentado como control revisado.  
**Evidencia técnica local:** `app/src/main/AndroidManifest.xml`, `MapActivity.kt`.

### V-05 — Configuración sensible y API keys locales

**Severidad:** Media  
**Situación observada:** el proyecto utiliza `local.properties` y `app/google-services.json` como parte de la configuración local.  
**Impacto:** si estas credenciales/configuraciones se exponen fuera del entorno controlado, podrían facilitar abuso de servicios o fuga de configuración.  
**Mitigación aplicada:** los archivos sensibles permanecen ignorados por Git y se documentó la necesidad de restringir claves en consola por paquete Android, fingerprint SHA-1 y APIs autorizadas. La clave de Maps se gestiona mediante `local.properties` y el secrets plugin como base operativa segura para la entrega.  
**Estado residual:** requiere validación manual en Google Cloud Console / Firebase Console.  
**Evidencia técnica local:** `SECURITY_NOTES.md`.

### V-06 — URL de Firebase Realtime Database hardcodeada

**Severidad:** Baja  
**Situación observada:** la URL de Firebase Realtime Database permanece hardcodeada en la aplicación.  
**Impacto:** incrementa deuda técnica, dificulta cambios por entorno y expone una configuración que idealmente debería centralizarse.  
**Tratamiento:** documentado como deuda no bloqueante para una futura externalización/configuración por entorno.  
**Estado residual:** pendiente.

## Pruebas realizadas

### Matriz de pruebas

| Prueba | Objetivo | Resultado |
|--------|----------|-----------|
| Validación de email | Rechazar correos inválidos | Correcto |
| Validación de contraseña | Enforce mínimo 6 y máximo 128 | Correcto |
| Validación de tareas | Rechazar vacíos tras `trim()` y longitudes excesivas | Correcto |
| Revisión de permisos de ubicación | Confirmar flujo compatible con Android moderno | Correcto |
| Revisión de reglas Firebase | Verificar deny-by-default y aislamiento por UID | Correcto; existe evidencia lista para pegar de reglas aplicadas en Firebase |
| Revisión de backup Android | Confirmar `allowBackup=false` y exclusiones | Correcto |
| `./gradlew lint test` | Verificación estática y pruebas automatizadas | `BUILD SUCCESSFUL` |

### Datos de prueba manual sugeridos

| Caso | Datos de entrada | Resultado esperado |
|------|------------------|--------------------|
| Email inválido | email: `correo`, password: `123456` | Mensaje de email inválido |
| Contraseña corta | email: `prueba@test.com`, password: `123` | Mensaje de contraseña demasiado corta |
| Nombre de tarea vacío | nombre tarea vacío o solo espacios | Error de nombre requerido |
| Nombre de tarea excedido | nombre con más de 100 caracteres | Error por longitud máxima del nombre |
| Descripción excedida | descripción con más de 500 caracteres | Error por longitud máxima de la descripción |

### Resultado de verificación automatizada

```text
./gradlew lint test
BUILD SUCCESSFUL
```

### Consideración sobre SQL Injection

No se reporta una vulnerabilidad de SQL Injection en esta aplicación porque el proyecto no utiliza SQLite/Room ni consultas SQL directas. La persistencia actual se realiza mediante Firebase Realtime Database, por lo que la superficie de ataque relevante está más relacionada con reglas de acceso, validación de datos y exposición de configuración.

## Resultados antes y después

| Área | Antes | Después |
|------|-------|---------|
| Reglas backend | Sin evidencia local suficiente de control por UID | Reglas definidas con deny-by-default y validaciones por campo |
| Respaldo Android | Riesgo por backup habilitado/sin endurecimiento suficiente | `allowBackup=false` + exclusiones coherentes |
| Validación de autenticación | Controles limitados | Email y contraseña validados con criterios explícitos |
| Validación de tareas | Riesgo de entradas vacías o excesivas | `trim()` y límites máximos definidos |
| Permisos de ubicación | Revisión pendiente | Permisos justificados y flujo actualizado |
| Configuración sensible | Riesgo operativo poco documentado | Riesgo documentado y mitigaciones operativas descritas |

## Checklist final de entrega y paquete ZIP

### Incluir en el ZIP

- `app/`
- `build.gradle.kts` raíz, si existe
- `settings.gradle.kts`
- `gradle/`
- `gradlew`
- `gradlew.bat`
- `database.rules.json`
- `docs/SECURITY_REPORT.docx`
- `SECURITY_NOTES.md`, si aplica

### Excluir del ZIP

- `.git/`
- `.atl/`
- `.gradle/`
- `build/`
- `app/build/`
- `.idea/`, salvo que el docente lo pida explícitamente
- Cachés, temporales y carpetas generadas automáticamente por el entorno

### Tratamiento de archivos sensibles

- `local.properties` **no debe publicarse** en repositorios públicos ni adjuntarse si contiene secretos o claves locales.
- `app/google-services.json` **no debe exponerse** en repositorios públicos; si la entrega necesita compilar, debe indicarse que el archivo puede requerir provisión controlada por el docente o configuración local equivalente.
- No incluir secretos en capturas, anexos ni documentación.

### Verificación rápida antes de comprimir

- [ ] El ZIP contiene solo archivos necesarios para revisión y compilación académica.
- [ ] No se incluyeron `.git/`, cachés, builds ni temporales.
- [ ] `docs/SECURITY_REPORT.docx` está actualizado.
- [ ] Los placeholders `[Completar]` y `[Insertar captura...]` fueron reemplazados o quedaron identificados claramente como pendientes reales.
- [ ] No se expusieron secretos en `local.properties`, `google-services.json`, capturas o anexos.

## Evidencia a adjuntar por el estudiante

> No se incluyen capturas inventadas. Esta sección distingue entre evidencia ya disponible/lista para pegar y evidencia funcional que sigue pendiente de captura.

### Checklist de evidencia visual

| Evidencia | Estado | Detalle |
|----------|--------|---------|
| Captura de Firebase Realtime Database Rules aplicadas | Lista para pegar | Debe mostrar root deny, acceso por `auth.uid == $uid`, validación de `id`, `name`, `description` y rechazo de `$other`. |
| Captura de `./gradlew lint test` con `BUILD SUCCESSFUL` | Lista para pegar | Evidencia principal de ejecución automatizada usada en el informe. |
| Captura de `AndroidManifest.xml` | Lista para pegar | Debe mostrar `android:allowBackup="false"`, referencia a `dataExtractionRules`, `fullBackupContent` y permisos de ubicación. |
| Captura de `backup_rules.xml` y/o `data_extraction_rules.xml` | Opcional complementaria | Útil para ampliar detalle del endurecimiento de backup. |
| Captura de validación de correo inválido en login/registro | Pendiente | Mantener pendiente si el usuario aún no la capturó. |
| Captura de validación de contraseña fuera de rango | Pendiente | Mantener pendiente si el usuario aún no la capturó. |
| Captura de validación de nombre vacío/solo espacios | Pendiente | Mantener pendiente si el usuario aún no la capturó. |
| Captura de validación de nombre o descripción con longitud excedida | Pendiente | Mantener pendiente si el usuario aún no la capturó. |
| Captura del flujo de permisos de ubicación en ejecución | Pendiente/según disponibilidad | Solo adjuntar si se desea reforzar la justificación funcional. |
| Captura o evidencia de que `local.properties` y `google-services.json` no se publican en Git | Pendiente | Puede resolverse con evidencia de `.gitignore` o estado del repositorio. |

### Placeholders sugeridos

| Evidencia | Archivo/captura a insertar |
|----------|-----------------------------|
| Evidencia 1 | `[Insertar captura de ./gradlew lint test con BUILD SUCCESSFUL]` |
| Evidencia 2 | `[Insertar captura de Firebase Realtime Database Rules aplicadas]` |
| Evidencia 3 | `[Insertar captura de AndroidManifest.xml con allowBackup=false y permisos]` |
| Evidencia 4 | `[Insertar captura de validación de email/contraseña inválidos]` |
| Evidencia 5 | `[Insertar captura de validación de nombre/longitud de tarea]` |

### Redacción sugerida debajo de las capturas

- **Firebase Realtime Database Rules:** "Reglas activas en Firebase Realtime Database con enfoque deny-by-default, acceso restringido por `auth.uid == $uid`, validación obligatoria de `id`, `name` y `description`, y rechazo de campos no permitidos mediante `$other`."
- **Gradle (`./gradlew lint test`):** "Ejecución satisfactoria de Android Lint y pruebas automatizadas del proyecto, finalizando con `BUILD SUCCESSFUL` como evidencia de verificación técnica básica sin errores bloqueantes."
- **AndroidManifest.xml:** "Configuración de seguridad del manifiesto Android con `android:allowBackup=\"false\"`, reglas de backup/data extraction declaradas y permisos de ubicación explícitos conforme al flujo funcional revisado."

## Pendientes y deuda técnica

| Pendiente | Prioridad | Observación |
|-----------|-----------|-------------|
| Externalizar la URL de Firebase Realtime Database | Media | Actualmente documentada como deuda no bloqueante. |
| Incorporar SonarQube como análisis complementario | Media | Mejora futura para métricas más amplias de calidad, mantenibilidad y vulnerabilidades generales. |
| Restringir API keys en consola | Alta | Debe verificarse por paquete, SHA-1 y APIs autorizadas. |
| Mantener revisión periódica MASVS | Media | Recomendable para futuras iteraciones académicas o productivas. |
| Confirmar empaquetado final sin archivos no académicos | Alta | Antes de entregar, comprimir solo el contenido solicitado y excluir repositorio, cachés y temporales. |

## Conclusión

La actividad de evaluación permitió mejorar de forma concreta la postura de seguridad de `GestorTareas` en los puntos más relevantes para una aplicación móvil conectada a Firebase: control de acceso por usuario, endurecimiento del almacenamiento local, validación de entradas y manejo responsable de configuración sensible. Las correcciones implementadas son consistentes con el alcance del proyecto y con buenas prácticas razonables para una entrega académica. Además, las fortalezas reconocidas en la retroalimentación previa se mantienen y se usan como base explícita del argumento de seguridad del proyecto.

El resultado final es **favorable con advertencias**, ya que no se identifican bloqueantes críticos pendientes dentro del código revisado. Las acciones aún recomendadas se concentran en reforzar la trazabilidad documental de evidencias funcionales pendientes, restringir claves en consola, declarar con claridad la limitación de `minSdk = 31` y asegurar que el paquete ZIP excluya archivos ajenos a la entrega. Con la incorporación de las capturas ya disponibles y las evidencias funcionales faltantes, el informe queda apto para presentación formal.
