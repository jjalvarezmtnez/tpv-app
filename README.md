# TPV App

Aplicación de escritorio tipo TPV orientada a pequeños establecimientos de hostelería, que permite gestionar comandas, mesas, productos, usuarios y control de stock. Desarrollada en Java con JavaFX, arquitectura MVC, persistencia en SQLite y acceso a datos con JDBC. Incluye gestión de roles, control de inventario e informes gráficos básicos.

---

## Objetivo del proyecto

El objetivo de esta aplicación es digitalizar y simplificar la gestión interna de pequeños negocios de hostelería, donde habitualmente se utilizan métodos manuales o herramientas no especializadas.

Estos enfoques generan problemas como:

- Errores en el cálculo de comandas
- Falta de control sobre el stock
- Dificultad para consultar ventas anteriores
- Ausencia de información para la toma de decisiones

El sistema propuesto centraliza toda esta información en una única aplicación.

---

## Tecnologías utilizadas

- **Java 17**
- **JavaFX**: interfaz gráfica
- **SQLite**: base de datos local
- **JDBC**: acceso a datos
- **Maven**: gestión del proyecto

---

## Arquitectura

La aplicación sigue el patrón **MVC (Modelo-Vista-Controlador)**:

- **Modelo**: representa las entidades y la lógica de negocio.
- **Vista**: contiene la interfaz gráfica desarrollada con JavaFX y FXML.
- **Controlador**: gestiona la interacción entre usuario y sistema.
- **Capa de persistencia**: centraliza el acceso a la base de datos mediante JDBC y SQLite.

Este enfoque permite separar responsabilidades, facilitando el mantenimiento, la organización del código y la evolución futura del sistema.

---

## Funcionalidades principales

- Autenticación de usuarios con roles: administrador y camarero.
- Gestión de usuarios.
- Gestión de productos y categorías.
- Gestión del estado de mesas.
- Creación, edición y cierre de comandas.
- Añadir y eliminar productos en comandas.
- Cálculo automático de totales.
- Control de stock.
- Registro del historial de ventas.
- Visualización de informes gráficos básicos.

---

## Ejecución del proyecto

1. Clonar el repositorio:

```bash
git clone https://github.com/jjalvarezmtnez/tpv-app.git
```

2. Abrir el proyecto en **IntelliJ IDEA**.

3. Ejecutar la clase principal:

```text
MainJavaFX
```

La base de datos se genera automáticamente en el primer arranque de la aplicación.

---

## Estructura del proyecto

```text
src/
 ├── main/
 │   ├── java/
 │   │   └── com/jhonatan/tfg/tpv/
 │   │       ├── controller/
 │   │       ├── dao/
 │   │       ├── database/
 │   │       ├── model/
 │   │       │   ├── enums/
 │   │       │   └── informe/
 │   │       ├── service/
 │   │       ├── util/
 │   │       ├── ConsoleTestRunner.java
 │   │       ├── Main.java
 │   │       └── MainJavaFX.java
 │   └── resources/
 │       ├── css/
 │       └── fxml/
```

---

## Estado del proyecto

Versión funcional completa previa a la entrega del Proyecto Final de Desarrollo de Aplicaciones Multiplataforma (DAM).

---

## Mejoras futuras

- Exportación de informes a PDF.
- Auten cación avanzada (OAuth).
- Aplicación móvil complementaria.
- Panel web para consulta remota.
- Estadís cas avanzadas con compara vas temporales.
- Despliegue en entorno cliente-servidor con base de datos remota.
- Pasarela de pagos real.
- Impresión real de ckets.
- Mul usuario concurrente en red.
- Predicción avanzada de ventas.

---

## Autor

Jhonatan Álvarez Martínez  
Técnico en Desarrollo de Aplicaciones Multiplataforma (DAM)  
GitHub: https://github.com/jjalvarezmtnez

