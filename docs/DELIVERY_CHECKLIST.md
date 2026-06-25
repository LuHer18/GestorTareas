# Checklist breve de entrega — GestorTareas

Usa esta lista inmediatamente antes de generar el ZIP final de entrega.

## Incluir

- [ ] `app/`
- [ ] `build.gradle.kts` raíz, si existe
- [ ] `settings.gradle.kts`
- [ ] `gradle/`
- [ ] `gradlew`
- [ ] `gradlew.bat`
- [ ] `database.rules.json`
- [ ] `docs/SECURITY_REPORT.docx`
- [ ] `SECURITY_NOTES.md`, si aplica

## Excluir

- [ ] `.git/`
- [ ] `.atl/`
- [ ] `.gradle/`
- [ ] `build/`
- [ ] `app/build/`
- [ ] `.idea/`, salvo solicitud expresa del docente
- [ ] Cachés y temporales

## Revisión final

- [ ] `docs/SECURITY_REPORT.docx` abre correctamente y refleja la retroalimentación previa considerada.
- [ ] Los placeholders `[Completar]` y `[Insertar captura...]` fueron completados o quedaron marcados como pendientes reales.
- [ ] No se exponen secretos en `local.properties`, `app/google-services.json`, capturas o anexos.
- [ ] Si la compilación requiere `local.properties` o `app/google-services.json`, se explica esa dependencia sin publicar secretos.
- [ ] La limitación `minSdk = 31` quedó mencionada como alcance técnico de la app.
