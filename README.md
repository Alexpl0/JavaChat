# JavaChat: Aplicación de Chat con Sockets en Java

## 📜 Descripción del Proyecto
JavaChat es una aplicación de chat de escritorio, robusta y funcional, desarrollada en Java. Este proyecto demuestra la implementación de una arquitectura cliente-servidor utilizando Sockets para la comunicación en red y Threads para la gestión concurrente de múltiples usuarios. La aplicación permite a los usuarios unirse a una sala de chat global, identificarse con un nombre único y comunicarse en tiempo real.

Este proyecto fue desarrollado como parte del curso de Programación Avanzada, con un fuerte énfasis en la aplicación de un flujo de trabajo colaborativo profesional utilizando Git y Git Flow.

## ✒️ Autores
Este proyecto fue desarrollado en colaboración por el siguiente equipo:

*   **Alejandro** - Líder de Proyecto y Desarrollo del Backend (Servidor)
*   **Iber** - Desarrollo de la Lógica del Cliente
*   **Yusmany** - Desarrollo de la Interfaz de Usuario (GUI)

## ✨ Características Implementadas
La aplicación cuenta con un conjunto completo de funcionalidades que aseguran una experiencia de usuario rica y estable:

*   **Servidor Multihilo**: Capaz de gestionar múltiples conexiones de clientes de forma simultánea y eficiente.

*   **Autenticación de Usuarios**:
    *   Solicitud de nombre de usuario al conectar.
    *   Validación en el servidor para prevenir nombres de usuario duplicados.

*   **Chat en Tiempo Real**:
    *   Mensajería pública a todos los usuarios de la sala.
    *   Mensajes Privados: Posibilidad de enviar mensajes directos a un usuario específico usando el comando `/msg <usuario> <mensaje>`.

*   **Notificaciones del Sistema**:
    *   Anuncios automáticos cuando un usuario se une o abandona el chat.
    *   Mensajes de error y confirmación desde el servidor (ej. usuario no encontrado).

*   **Interfaz Gráfica Intuitiva (GUI)**:
    *   Interfaz limpia y funcional desarrollada con Java Swing.
    *   Visualización clara de mensajes públicos, privados y del sistema.
    *   **Timestamps**: Cada mensaje muestra la hora en que fue enviado (`[HH:mm]`).
    *   **Confirmación de Salida**: Un diálogo de advertencia previene el cierre accidental de la aplicación.

*   **Calidad de Vida (QoL)**:
    *   Auto-scroll en el área de chat para mantener la vista en los mensajes más recientes.
    *   Manejo de errores de conexión y desconexión para una experiencia robusta.

## 🛠️ Tecnologías Utilizadas
*   **Lenguaje**: Java (JDK 17+)
*   **Interfaz Gráfica (GUI)**: Java Swing
*   **Comunicación en Red**: Java Sockets (`java.net.Socket`, `java.net.ServerSocket`)
*   **Concurrencia**: Java Threads
*   **Manejo de Fechas**: `java.time.LocalDateTime`
*   **Control de Versiones**: Git, Git Flow

## 🚀 Cómo Empezar
Sigue estas instrucciones para compilar y ejecutar el proyecto en tu máquina local.

### Prerrequisitos
*   Tener instalado el **JDK (Java Development Kit)**, versión 17 o superior.
*   Tener **Git** instalado en tu sistema.

### Compilación y Ejecución
1.  **Clona el repositorio:**
    ```bash
    git clone [URL-de-tu-repositorio-en-GitHub]
    ```

2.  **Navega al directorio del proyecto:**
    ```bash
    cd JavaChat
    ```

3.  **Compila el proyecto:**
    Abre una terminal en la raíz del proyecto y ejecuta el siguiente comando. Esto compilará tanto el servidor como el cliente.
    ```bash
    javac -d bin src/com/server/*.java src/com/client/*.java
    ```
    *(Este comando crea un directorio `bin` para los archivos `.class` compilados)*

4.  **Ejecuta el Servidor:**
    En la misma terminal, ejecuta:
    ```bash
    java -cp bin com.server.Server
    ```
    Verás un mensaje indicando que el servidor está en línea y esperando clientes.

5.  **Ejecuta el Cliente:**
    Abre una **nueva terminal** por cada instancia de cliente que desees. En cada una, ejecuta:
    ```bash
    java -cp bin com.client.ChatClientGUI
    ```
    Aparecerá una ventana pidiéndote un nombre de usuario. ¡Introdúcelo y comienza a chatear!

