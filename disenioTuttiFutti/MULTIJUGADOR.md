# Tutti Frutti - Modo Multijugador Reactivo con WebSockets

## Resumen de Cambios

Se ha implementado un sistema completo de juego multijugador **reactivo en tiempo real** para Tutti Frutti usando:

- **Spring Boot 3.3.0** con JPA y WebSockets
- **H2 Database** (en memoria) para gestión de sesiones
- **WebSockets con STOMP** para comunicación en tiempo real
- **JavaScript** para reactividad en el frontend
- **Timer configurable** que finaliza automáticamente la partida
- **Sistema de reinicio** donde todos los jugadores deben confirmar

## Arquitectura

### Entidades (Base de Datos H2)

1. **Sala**: Representa una sala de juego
   - Nombre, capacidad máxima, estado (ESPERANDO/JUGANDO/FINALIZADA)
   - **tiempoLimiteSegundos**: Tiempo configurable para completar el juego
   - **jugadoresListosParaReiniciar**: Contador para volver a jugar
   - Relación OneToMany con Jugadores
   - Relación OneToOne con PartidaMulti

2. **Jugador**: Jugador dentro de una sala
   - Nombre, sessionId (identificador HTTP), estado listo/no listo
   - Puntaje total
   - **quiereReiniciar**: Si el jugador quiere volver a jugar
   - Relación ManyToOne con Sala
   - Relación OneToMany con RespuestaJugador

3. **PartidaMulti**: Datos de la partida activa
   - Letra y categorías (JSON)
   - Fechas de inicio y fin
   - **tiempoLimite**: Timestamp cuando se agota el tiempo
   - Relación OneToOne con Sala

4. **RespuestaJugador**: Respuestas individuales de cada jugador
   - Categoría, respuesta, validez, puntos, razón
   - Relación ManyToOne con Jugador

### Servicios

**WebSocketServicio**: Servicio de comunicación en tiempo real
- Enviar mensajes a salas específicas
- Notificar actualizaciones globales de salas
- Tipos de mensajes: JUGADOR_UNIDO, PARTIDA_INICIADA, JUGADOR_LISTO, PARTIDA_FINALIZADA, REINICIO_SOLICITADO, SALA_REINICIADA

**SalaServicio**: Servicio principal que gestiona:
- Creación de salas con tiempo límite configurable
- Unión de jugadores a salas (notifica vía WebSocket)
- Inicio de partidas con timer automático
- Validación de respuestas (usa el servicio existente de Gemini AI)
- Finalización automática por tiempo agotado
- Verificación de fin de partida
- Sistema de reinicio con confirmación de todos los jugadores

### Controlador

**MultijugadorController**: Maneja todas las rutas del flujo multijugador:
- `GET /juego/multijugador/`: Lista de salas disponibles
- `POST /juego/multijugador/crear`: Crear nueva sala (con tiempo límite)
- `GET /juego/multijugador/sala/{id}`: Entrar a sala de espera
- `POST /juego/multijugador/sala/{id}/iniciar`: Iniciar partida
- `GET /juego/multijugador/jugar`: Pantalla de juego con timer
- `POST /juego/multijugador/responder`: Enviar respuestas (JSON)
- `POST /juego/multijugador/finalizar-por-tiempo/{id}`: Finalizar por tiempo
- `GET /juego/multijugador/resultados`: Ver resultados finales
- `POST /juego/multijugador/reiniciar`: Solicitar volver a jugar
- `GET /juego/multijugador/api/salas`: API REST para obtener salas
- `GET /juego/multijugador/api/sala/{id}`: API REST para obtener sala específica

### Vistas HTML con JavaScript Reactivo

1. **listaSalas.html**: Lista de salas disponibles reactiva
   - Auto-actualización vía WebSocket cuando se crean/actualizan salas
   - Formulario para crear sala con tiempo límite configurable (30-600 segundos)
   - Botón "VOLVER AL MENU" para regresar al menú principal
   - Conexión WebSocket al topic `/topic/salas`

2. **salaEspera.html**: Sala de espera reactiva
   - Lista de jugadores se actualiza en tiempo real cuando alguien se une
   - Botón "INICIAR PARTIDA" aparece cuando hay ≥2 jugadores
   - Redirección automática cuando la partida inicia
   - Conexión WebSocket al topic `/topic/sala/{salaId}`

3. **multijugadorPantalla.html**: Formulario de juego con timer
   - **Timer visual** que cuenta regresivamente
   - Timer cambia a rojo cuando quedan ≤10 segundos
   - **Botón "TUTTI FRUTTI" deshabilitado** hasta que todos los campos estén completos
   - Validación en tiempo real de campos completos
   - Finalización automática cuando el tiempo se agota
   - Envío de respuestas vía AJAX (JSON)
   - Redirección a pantalla de espera tras enviar

4. **esperandoJugadores.html**: Espera reactiva de resultados
   - Muestra estado LISTO/JUGANDO de cada jugador en tiempo real
   - Auto-redirección cuando todos terminan
   - Conexión WebSocket para actualizaciones instantáneas

5. **multijugadorResultados.html**: Resultados con opciones de reinicio
   - Tabla de posiciones ordenada por puntaje
   - Detalle de respuestas de cada jugador
   - **Botón "VOLVER A JUGAR"**: Solicita reinicio
   - **Botón "VOLVER AL LOBBY"**: Regresa a lista de salas
   - Contador de jugadores que quieren volver a jugar (ej: "2 de 4 jugadores")
   - Reinicio automático cuando TODOS confirman
   - Conexión WebSocket para ver confirmaciones en tiempo real

## Flujo del Juego

1. Usuario ingresa nombre → Menu Principal
2. Selecciona "Multijugador"
3. Ve lista de salas reactiva o crea una nueva (configura capacidad y tiempo límite)
4. Entra a sala de espera (se une automáticamente, otros ven la actualización en vivo)
5. Cuando hay ≥2 jugadores, cualquiera puede iniciar
6. **Timer comienza a contar** (configurado al crear la sala)
7. Cada jugador completa su formulario
   - Botón "TUTTI FRUTTI" se habilita solo cuando todos los campos están completos
   - Timer visible en la parte superior
8. Al enviar o **cuando el tiempo se agota**:
   - Jugadores que no enviaron quedan con puntaje 0
   - Pantalla de espera mostrando quién está listo
9. Cuando todos terminan o el tiempo se agota:
   - Validación automática con Gemini AI
   - Tabla comparativa con puntos de todos los jugadores
10. Opciones finales:
    - **Volver a jugar**: Todos deben confirmar para reiniciar la sala
    - **Volver al lobby**: Regresa a la lista de salas

## Nuevas Características

### 1. Timer Configurable

- Al crear una sala, se define el tiempo límite (30-600 segundos, default 120)
- Timer visible en pantalla de juego
- Cuenta regresiva automática
- Cuando llega a 0, finaliza la partida automáticamente
- Jugadores que no enviaron respuestas quedan con 0 puntos

### 2. Botón "TUTTI FRUTTI" Inteligente

- Deshabilitado por default
- Se habilita solo cuando **todos** los campos están completos
- Mensaje visual indica el estado:
  - "Completa todos los campos..." (deshabilitado)
  - "Todos los campos completos! Puedes enviar..." (habilitado)

### 3. Sistema de Reinicio Colaborativo

- Al finalizar, cada jugador ve:
  - Botón "VOLVER A JUGAR"
  - Botón "VOLVER AL LOBBY"
- Cuando un jugador presiona "VOLVER A JUGAR":
  - Su confirmación se registra
  - Todos ven en tiempo real: "X de Y jugadores quieren volver a jugar"
  - Botón se deshabilita para evitar múltiples clicks
- Cuando **TODOS** confirman:
  - Sala se reinicia automáticamente
  - Vuelven a la sala de espera
  - Puntajes y respuestas se limpian
  - Estado vuelve a ESPERANDO

### 4. Reactividad Completa con WebSockets

**Eventos en tiempo real:**
- `SALAS_ACTUALIZADAS`: Lista de salas se actualiza
- `JUGADOR_UNIDO`: Nuevo jugador en la sala
- `PARTIDA_INICIADA`: Redirige a todos al juego
- `JUGADOR_LISTO`: Actualiza estado de jugador que terminó
- `PARTIDA_FINALIZADA`: Redirige a resultados
- `REINICIO_SOLICITADO`: Muestra contador de confirmaciones
- `SALA_REINICIADA`: Redirige a sala de espera

## Configuración Técnica

### WebSocket

**Endpoint:** `/ws-tuttifrutti` (SockJS + STOMP)

**Topics:**
- `/topic/salas`: Actualizaciones globales de salas
- `/topic/sala/{salaId}`: Eventos específicos de cada sala

**Configuración (WebSocketConfig.java):**
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-tuttifrutti").withSockJS();
    }
}
```

### Librerías JavaScript (CDN)

```html
<script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
```

### H2 Database

```properties
spring.datasource.url=jdbc:h2:mem:tuttifruttidb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

## Cómo Probar

1. **Compilar:** `mvn clean install`
2. **Ejecutar:** `mvn spring-boot:run`
3. **Abrir múltiples navegadores** en `http://localhost:8080`
4. En cada navegador:
   - Ingresar nombre diferente
   - Ir a Menu → Multijugador
5. En navegador 1:
   - Crear sala (nombre, capacidad 4, tiempo 60 segundos)
6. En navegador 2 y 3:
   - Ver la sala aparecer automáticamente
   - Unirse
   - Ver en navegador 1 cómo aparecen en la lista
7. Iniciar partida cuando estén listos
8. Ver timer contar en todos los navegadores
9. Completar campos y ver cómo se habilita "TUTTI FRUTTI"
10. Enviar respuestas y ver actualizaciones en tiempo real
11. Ver resultados y probar "Volver a jugar"

## Notas Importantes

- **Las salas se eliminan al reiniciar** la aplicación (H2 en memoria)
- **Para producción**, cambiar a H2 persistente o PostgreSQL/MySQL
- **API key de Gemini** está hardcodeado (mover a variables de entorno)
- **WebSockets requiere conexión persistente** (no funciona detrás de ciertos proxies sin configuración adicional)
- **Timer es client-side**, sincronizado con server-side para validación
- **Si un jugador cierra el navegador**, su estado permanece hasta que expire la sesión HTTP
