# Interfaz de clientes comerciales

La misma aplicación conserva su instalación personal y funciona en `/apps/gestor-notas/gn-<32 caracteres hexadecimales>/`. En esa ruta todas las API de negocio y la protección CSRF permanecen dentro de la instancia; nunca se usa la API raíz personal ni la de otro cliente.

`auth/info` indica LOCAL o KOVA. En una instancia comercial sin sesión, el ingreso continúa mediante OIDC con la cuenta KOVA. Si el permiso fue retirado se muestra el regreso a Mis productos, sin ofrecer una contraseña local. Salir de la instancia vuelve al portal de KOVA.

El pipeline comercial compila con `npm run build -- --base-href /gestor-notas-prueba/` y empaqueta esta salida en el JAR verificado de Notas. El instalador de KOVA ajusta su base a la instancia concreta. El despliegue personal conserva `/gestor-notas/` y su API existente.
