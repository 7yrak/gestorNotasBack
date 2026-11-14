# Clientes comerciales de Gestor de Notas

El perfil `kova-customer` abre una instancia y una base nuevas por compra pagada. El perfil predeterminado mantiene el acceso personal existente. No se migran ni comparten cuadernos de la instalación personal.

El preparador de KOVA genera el identificador `gn-<UUID del pedido sin guiones>`, la ruta `/apps/gestor-notas/<instancia>/`, la conexión privada de PostgreSQL y `notes.kova.owner-id` desde el comprador del pedido. La base debe ser `kova_gn_<identificador>` en modo nativo o `notes_customer` en un puerto reservado. Se rechaza conectar este perfil a la base personal. Una identidad persistida impide cambiar el comprador o reutilizar otra instancia.

Las migraciones `db/customer` crean el esquema desde cero. No se usa baseline ni el historial de la base personal. La cuenta interna no permite contraseñas locales: el cliente entra mediante su cuenta KOVA con OIDC y PKCE. Cada petición privada consulta el permiso vigente; vencimiento, suspensión, revocación, otra organización o instancia y caída de KOVA bloquean el acceso. El propietario de la compra y el administrador global autorizado trabajan sobre el mismo espacio de esa instancia.

Los clientes reciben `legacyUserId` para compatibilidad con las rutas de cuadernos. La API siempre fija el propietario desde la identidad verificada y mantiene CSRF para escrituras. La interfaz comercial debe compilarse desde la revisión fijada de gestorNotasFront con base `/gestor-notas-prueba/`; el preparador cambia solo esa base al instalar el JAR.

Pruebas: `mvn package` comprueba el acceso local y comercial. Con `KOVA_NOTES_TEST_DB_PASSWORD` se ejecutan además las pruebas HTTP/JPA contra la base PostgreSQL efímera `notes_customer` en 127.0.0.1:15548 (usuario kova_test). Nunca apuntarlas a datos personales.

El código no acredita un despliegue ni un pago real. La venta exige instalar el artefacto y el trabajador de clientes de Notas mediante el pipeline de KOVA, comprobar su capacidad y el acceso del comprador. El administrador publica y habilita compras del plan en el asistente de KOVA; no activa cobros de producción desde esa pantalla.
