# Tutti Frutti - Modo Multijugador

## Resumen de Cambios

Se ha implementado un sistema completo de juego multijugador para Tutti Frutti usando:

- **Spring Boot 3.3.0** con JPA
- **H2 Database** (en memoria) para gestión de sesiones
- **HTML puro con Thymeleaf** (sin CSS ni JavaScript)
- **Meta refresh** para auto-actualización de páginas

## Arquitectura

### Entidades (Base de Datos H2)

1. **Sala**: Representa una sala de juego
   - Nombre, capacidad máxima, estado (ESPERANDO/JUGANDO/FINALIZADA)
   - Relación OneToMany con Jugadores
   - Relación OneToOne con PartidaMulti

2. **Jugador**: Jugador dentro de una sala
   - Nombre, sessionId (identificador HTTP), estado listo/no listo
   - Puntaje total
   - Relación ManyToOne con Sala
   - Relación OneToMany con RespuestaJugador

3. **PartidaMulti**: Datos de la partida activa
   - Letra y categorías (JSON)
   - Fechas de inicio y fin
   - Relación OneToOne con Sala

4. **RespuestaJugador**: Respuestas individuales de cada jugador
   - Categoría, respuesta, validez, puntos, razón
   - Relación ManyToOne con Jugador

### Servicios

**SalaServicio**: Servicio principal que gestiona:
- Creación de salas
- Unión de jugadores a salas
- Inicio de partidas (genera letra y categorías con IA)
- Validación de respuestas (usa el servicio existente de Gemini AI)
- Verificación de fin de partida

### Controlador

**MultijugadorController**: Maneja todas las rutas del flujo multijugador:
- `GET /juego/multijugador/`: Lista de salas disponibles
- `POST /juego/multijugador/crear`: Crear nueva sala
- `GET /juego/multijugador/sala/{id}`: Entrar a sala de espera
- `POST /juego/multijugador/sala/{id}/iniciar`: Iniciar partida
- `GET /juego/multijugador/jugar`: Pantalla de juego o espera
- `POST /juego/multijugador/responder`: Enviar respuestas
- `GET /juego/multijugador/resultados`: Ver resultados finales

### Vistas HTML

1. **listaSalas.html**: Lista de salas disponibles + formulario crear sala
   - Auto-recarga cada 5 segundos

2. **salaEspera.html**: Sala de espera antes de iniciar
   - Muestra jugadores conectados
   - Botón para iniciar cuando hay mínimo 2 jugadores
   - Auto-recarga cada 3 segundos

3. **multijugadorPantalla.html**: Formulario de juego
   - Muestra letra y categorías
   - Formulario para ingresar respuestas

4. **esperandoJugadores.html**: Espera mientras otros terminan
   - Muestra estado de cada jugador (LISTO/JUGANDO)
   - Auto-recarga cada 3 segundos

5. **multijugadorResultados.html**: Resultados finales
   - Tabla de posiciones ordenada por puntaje
   - Detalle de respuestas de cada jugador con validaciones

## Flujo del Juego

1. Usuario ingresa nombre → Menu Principal
2. Selecciona "Multijugador"
3. Ve lista de salas o crea una nueva
4. Entra a sala de espera (se une automáticamente)
5. Cuando hay ≥2 jugadores, cualquiera puede iniciar
6. Todos reciben la misma letra y categorías (generadas por IA)
7. Cada jugador completa su formulario y envía
8. Pantalla de espera hasta que todos terminen
9. Validación automática con Gemini AI
10. Tabla comparativa con puntos de todos los jugadores

## Características Técnicas

- **Sin JavaScript**: Todo se maneja con formularios HTML y recargas de página
- **Auto-actualización**: Meta refresh en páginas de espera
- **Gestión de sesiones**: HttpSession identifica jugadores
- **Validación IA**: Reutiliza el servicio existente de Gemini
- **Base de datos temporal**: H2 en memoria (se reinicia con la app)
- **Console H2**: Disponible en `/h2-console` para debug

## Configuración

### application.properties
```properties
# H2 Database
spring.datasource.url=jdbc:h2:mem:tuttifruttidb
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

### Dependencias Agregadas (pom.xml)
```xml
<!-- H2 Database -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
</dependency>

<!-- JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
```

## Cómo Probar

1. Compilar: `mvn clean install`
2. Ejecutar: `mvn spring-boot:run`
3. Abrir múltiples navegadores/pestañas en `http://localhost:8080`
4. En cada navegador:
   - Ingresar nombre diferente
   - Ir a Menu → Multijugador
5. Crear sala en un navegador
6. Unirse a la misma sala desde otros navegadores
7. Iniciar partida cuando todos estén listos
8. Completar respuestas en cada navegador
9. Ver resultados comparativos

## Notas

- Las salas se eliminan al reiniciar la aplicación (H2 en memoria)
- Para producción, cambiar a H2 persistente o PostgreSQL/MySQL
- Las páginas se auto-recargan, no son reactivas
- El API key de Gemini está hardcodeado (mover a variables de entorno en producción)
