# Acceso local de una sola cuenta

El Gestor de Notas ya no utiliza KOVA ni OAuth2. Solo puede iniciar sesión el usuario existente seleccionado por NOTES_LOCAL_USER_ID. No hay registro ni API de administración de usuarios.

## Configuración

- NOTES_LOCAL_USER_ID: UUID del propietario existente de los cuadernos. Por defecto se conserva f1ce8d47-f208-4ab9-932b-8979840887a2, el usuario original de esta aplicación. Verificar este UUID antes del despliegue. No se crean usuarios ni se reasignan notas.
- El nombre de acceso es el campo username de ese usuario en la tabla users.
- La contraseña se valida con BCrypt contra password_hash. Si el hash antiguo no es BCrypt o necesitas cambiar la clave, configurar NOTES_LOCAL_PASSWORD_HASH con un hash BCrypt nuevo en el entorno privado del servicio. No guardar contraseñas ni hashes en git. La variable tiene prioridad sobre el hash de la BD.
- DB_PASSWORD sigue siendo obligatorio para conectar con PostgreSQL.
- NOTES_COOKIE_SECURE es true por defecto. Usar false únicamente al probar mediante HTTP local. En producción usar HTTPS y mantener true.
- Eliminar las antiguas variables de OAuth2/KOVA del servicio.

El navegador obtiene un token CSRF en GET auth/csrf, envía username y password mediante POST auth/login como formulario y consulta GET auth/session. El cierre de sesión requiere POST auth/logout y CSRF. Las peticiones privadas sin sesión devuelven 401. Cambiar el nombre de cookie invalida las sesiones anteriores de KOVA.

La migración V2 y su tabla histórica se conservan para respetar los checksums de Flyway. Ya no se consultan para autenticar. El proxy debe seguir enviando las rutas auth y api de /gestor-notas/ al backend, y eliminar cualquier redirección específica a KOVA si estuviera configurada fuera de estos repositorios.

## Validación

Con Java 21 ejecutar mvn -B -ntp package. Las pruebas de seguridad comprueban acceso anónimo, contraseña correcta e incorrecta, rechazo de otro usuario, CSRF, cierre e invalidación de sesión y conservación del propietario.
