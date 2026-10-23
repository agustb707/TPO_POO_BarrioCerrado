# Barrio Cerrado «Los Aromos»

## Descripción del proyecto

Este proyecto corresponde a la **Propuesta A: Gestión de un barrio cerrado** del Trabajo Práctico Obligatorio de Programación Orientada a Objetos.

El barrio cerrado «Los Aromos» cuenta con 150 lotes. Actualmente, parte de la información se administra mediante planillas y mensajes. El objetivo es diseñar un sistema que permita centralizar y organizar la gestión de los lotes, sus residentes, los reclamos, las expensas, el mantenimiento de equipos, el ingreso de visitantes y los reportes de administración.

El trabajo se divide en dos etapas:
- **Etapa 1:** análisis del problema, requerimientos, reglas de negocio, casos de uso, modelado UML y aplicación de principios SOLID y patrones GRASP.
- **Etapa 2:** implementación en Java, pruebas y puesta a punto del sistema.

**Estado actual:** Etapa 1 (diseño). Todavía no hay una aplicación terminada para ejecutar.

## Integrantes

- [Nombre y apellido – integrante 1]
- [Nombre y apellido – integrante 2]
- [Nombre y apellido – integrante 3, si corresponde]

## Funcionalidades previstas

- Registrar y gestionar reclamos de vecinos, asignarlos a empleados y hacer seguimiento de sus estados.
- Emitir expensas mensuales, registrar pagos parciales o totales y calcular intereses por mora.
- Registrar cámaras y luminarias, informar fallas y gestionar órdenes de mantenimiento.
- Autorizar visitas y proveedores, verificar los permisos y registrar ingresos y egresos.
- Emitir reportes de reclamos pendientes, morosidad por lote y equipos fuera de servicio.

Las funcionalidades y sus restricciones se detallan en el documento de diseño de la primera etapa.

## Tecnologías y herramientas

- **Java 17 o superior:** lenguaje previsto para la implementación de la Etapa 2.
- **Git y GitHub:** control de versiones y trabajo colaborativo.
- **Visual Studio Code o IntelliJ IDEA:** entornos para abrir y trabajar con el proyecto.
- **UML:** diagramas de casos de uso, clases y secuencia.

## Cómo obtener y abrir el proyecto desde GitHub

### 1. Clonar el repositorio

1. Ingresar al repositorio del grupo en GitHub.
2. Presionar **Code** y copiar la dirección HTTPS del repositorio.
3. Clonar el repositorio con Git desde una terminal:

   ```bash
   git clone URL_DEL_REPOSITORIO
   ```

4. Entrar a la carpeta que se descargó.

También se puede utilizar **GitHub Desktop** para clonar el repositorio sin escribir comandos.

### 2. Abrirlo en Visual Studio Code

1. Abrir Visual Studio Code.
2. Ir a **Archivo > Abrir carpeta** (File > Open Folder).
3. Seleccionar la carpeta del repositorio clonado.
4. Abrir los archivos de documentación de la primera etapa.

Para la futura implementación en Java se recomienda tener instalado un **JDK 17 o superior** y la extensión **Extension Pack for Java**.

### 3. Abrirlo en IntelliJ IDEA

1. Abrir IntelliJ IDEA.
2. Elegir **Open** y seleccionar la carpeta del repositorio clonado.
3. Abrir los archivos de documentación.
4. Cuando se incorpore el código, configurar un **JDK 17 o superior** como SDK del proyecto.

**Nota:** No hace falta compilar ni instalar dependencias para consultar los documentos de la Etapa 1.

## Estructura del repositorio

Estructura prevista para las dos etapas (algunas carpetas pueden no existir todavía):

```text
Barrio-Los-Aromos/
├── README.md
├── docs/
│   ├── [documento de diseño de Etapa 1]
│   └── uml/
│       └── [diagramas UML y archivos fuente]
├── src/                 # Código Java (Etapa 2)
└── test/                # Pruebas (Etapa 2)
```

Los nombres entre corchetes son descriptivos y deben reemplazarse por los archivos reales que suba el grupo.

## Instrucciones de ejecución

**Etapa 1:** no se ejecuta ningún programa. Para revisar esta entrega, abrir el documento de diseño y los diagramas de la carpeta `docs/`.

**Etapa 2:** se completará esta sección con los pasos para compilar y ejecutar la aplicación Java, el menú o interfaz elegida y los datos de prueba.

## Control de versiones y entregas

El proyecto se desarrolla de forma colaborativa en GitHub. Cada integrante realiza sus propios commits; los cambios se trabajan en ramas y se integran a `main` mediante pull requests revisados por otro integrante.

Las entregas se identifican con los tags:
- `etapa-1`: entrega de análisis, modelado y patrones.
- `etapa-2`: entrega del sistema implementado y probado.

## Alcance de la primera entrega

Incluye el análisis de la Propuesta A, los actores, requerimientos funcionales, reglas de negocio y supuestos, las descripciones de casos de uso, los diagramas UML solicitados y la justificación de **3 principios SOLID y 4 patrones GRASP**, junto con la explicación de los cambios del modelo inicial al final.

La programación del sistema, las pruebas y la puesta a punto corresponden a la segunda entrega.
