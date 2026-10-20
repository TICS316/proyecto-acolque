# Ideator - Monorepo (Sprint 1)

Ideator es una plataforma asistida por IA para convertir ideas de software en especificaciones técnicas.
Este repositorio es la raíz del monorepo e incluye el backend Spring Boot, el frontend React/Vite y la
orquestación de PostgreSQL con Docker Compose.

## Stack

| Capa | Tecnologías |
| --- | --- |
| Backend | Java 21, Spring Boot 3.4, Spring Web, Spring Data JPA, Spring Security, JWT (JJWT), PostgreSQL, Azure OpenAI SDK, Lombok |
| Frontend | React 18, TypeScript, Vite, Tailwind CSS, React Router, Axios |
| Base de datos | PostgreSQL 15 (Docker) |

## Estructura

```text
.
├── backend/                  # API Spring Boot (Maven)
│   ├── src/main/java/com/ideator/backend/
│   │   ├── config/           # Seguridad, Azure OpenAI, seed
│   │   ├── domain/           # Entidades JPA y enums
│   │   ├── dto/              # Requests/Responses
│   │   ├── exception/        # Manejo global de errores
│   │   ├── repository/       # Repositorios Spring Data
│   │   ├── security/         # JWT, filtro y UserDetails
│   │   ├── service/          # Reglas de negocio
│   │   └── web/              # Controladores REST
│   ├── src/main/resources/application.yml
│   ├── mvnw / mvnw.cmd
│   └── pom.xml
├── frontend/                 # SPA React + Vite
│   ├── src/lib/api.ts        # Cliente Axios + Bearer token
│   ├── src/services/         # Llamadas al backend
│   ├── src/context/          # AuthContext (JWT)
│   ├── src/pages/            # Login, Dashboard, ProjectView
│   └── .env.example
├── docker-compose.yml        # PostgreSQL 15 + volumen persistente
├── .gitignore
└── README.md
```

## Requisitos

- Docker Desktop con Docker Compose v2
- Java 21 (o superior) y Maven 3.9+ (`mvnw` incluido, no requiere Maven instalado)
- Node.js 20+ y npm

## Puesta en marcha

### 1. Base de datos

```bash
docker compose up -d
docker compose ps
```

El contenedor se llama `ideator-postgres` y usa:

| Variable | Valor |
| --- | --- |
| `POSTGRES_DB` | `ideator_db` |
| `POSTGRES_USER` | `ideator_user` |
| `POSTGRES_PASSWORD` | `ideator_pass` |
| Puerto host | `5432` (configurable con `POSTGRES_PORT`) |

> Si ya tienes un PostgreSQL local usando el puerto 5432, ejecuta por ejemplo
> `POSTGRES_PORT=5433 docker compose up -d` y define `SPRING_DATASOURCE_URL` con ese puerto al
> arrancar el backend.

El volumen persistente se llama `ideator_pgdata`. Para detener todo sin borrar datos usa
`docker compose down`; para borrar también el volumen usa `docker compose down -v`.

### 2. Backend

En Windows PowerShell:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```bash
cd backend
./mvnw spring-boot:run
```

La API queda disponible en `http://localhost:8080`.

Si usaste un puerto distinto para PostgreSQL:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5433/ideator_db"
.\mvnw.cmd spring-boot:run
```

Al arrancar se crean las tablas con Hibernate (`ddl-auto=update`) y, si `APP_SEED_ENABLED=true`
(valor por defecto), un usuario y un proyecto demo:

- Correo: `demo@ideator.com`
- Contraseña: `Demo1234!`

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:3000`. El cliente Axios usa `VITE_API_URL`, por defecto
`http://localhost:8080/api`. Puedes copiar `frontend/.env.example` a `frontend/.env` para cambiarlo.

## Probar PostgreSQL

Verificar que el contenedor está sano:

```bash
docker exec ideator-postgres pg_isready -U ideator_user -d ideator_db
```

Abrir una consola SQL:

```bash
docker exec -it ideator-postgres psql -U ideator_user -d ideator_db
```

Comandos útiles dentro de `psql`:

```sql
\dt
select id, email, name, role, created_at from users;
select id, name, status, owner_id, created_at from projects;
select * from project_members;
```

También puedes consultar desde el host si tienes `psql`:

```bash
psql -h localhost -p 5432 -U ideator_user -d ideator_db
```

## Probar los endpoints

### Health general

```bash
curl http://localhost:8080/api/health
```

Respuesta esperada (resumida):

```json
{
  "status": "UP",
  "application": "ideator-api",
  "database": { "status": "UP", "detail": "Conexión a PostgreSQL establecida" },
  "azure": { "status": "NOT_CONFIGURED", "deployment": "gpt-4o-mini" }
}
```

En PowerShell puedes usar `Invoke-RestMethod http://localhost:8080/api/health`.

### Login y proyectos

PowerShell:

```powershell
$base = "http://localhost:8080/api"

$login = Invoke-RestMethod -Method Post -Uri "$base/v1/auth/login" `
  -ContentType "application/json" `
  -Body (@{ email = "demo@ideator.com"; password = "Demo1234!" } | ConvertTo-Json)

$headers = @{ Authorization = "Bearer $($login.token)" }

Invoke-RestMethod -Uri "$base/v1/projects" -Headers $headers

$nuevo = Invoke-RestMethod -Method Post -Uri "$base/v1/projects" -Headers $headers `
  -ContentType "application/json" `
  -Body (@{ name = "Mi primer proyecto"; description = "Prueba Sprint 1" } | ConvertTo-Json)

Invoke-RestMethod -Uri "$base/v1/projects/$($nuevo.id)" -Headers $headers
Invoke-RestMethod -Method Delete -Uri "$base/v1/projects/$($nuevo.id)" -Headers $headers
```

Sin cabecera `Authorization` el endpoint de proyectos responde `401`.

### Registro de usuario

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Ana Perez","email":"ana@ideator.com","password":"Password123"}'
```

## Configurar y probar Azure OpenAI

1. Crea un recurso de **Azure OpenAI** y despliega un modelo (por ejemplo `gpt-4o-mini`).
2. Obtén el **endpoint** del recurso y una **key** desde Azure Portal.
3. Exporta las variables y reinicia el backend.

Windows PowerShell:

```powershell
$env:AZURE_OPENAI_ENDPOINT = "https://<tu-recurso>.openai.azure.com/"
$env:AZURE_OPENAI_KEY = "<tu-key>"
$env:AZURE_OPENAI_DEPLOYMENT = "gpt-4o-mini"
cd backend
.\mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
export AZURE_OPENAI_ENDPOINT="https://<tu-recurso>.openai.azure.com/"
export AZURE_OPENAI_KEY="<tu-key>"
export AZURE_OPENAI_DEPLOYMENT="gpt-4o-mini"
cd backend && ./mvnw spring-boot:run
```

Probar la conectividad real (hace una llamada corta al modelo):

```bash
curl http://localhost:8080/api/health/azure
```

Posibles respuestas:

```json
{ "status": "UP", "detail": "Azure OpenAI respondió: OK", "deployment": "gpt-4o-mini", "latencyMs": 850 }
```

```json
{ "status": "NOT_CONFIGURED", "detail": "Define AZURE_OPENAI_ENDPOINT y AZURE_OPENAI_KEY ..." }
```

```json
{ "status": "DOWN", "detail": "No se pudo completar la llamada a Azure OpenAI", "error": "..." }
```

El backend arranca aunque Azure no esté configurado; el cliente `OpenAIClient` solo se crea cuando
existen `AZURE_OPENAI_ENDPOINT` y `AZURE_OPENAI_KEY` (ver `AzureAIConfig.java`).

## Endpoints del Sprint 1

| Método | Ruta | Autenticación | Descripción |
| --- | --- | --- | --- |
| GET | `/api/health` | Pública | Estado de la API y PostgreSQL |
| GET | `/api/health/azure` | Pública | Diagnóstico real de Azure OpenAI |
| POST | `/api/v1/auth/register` | Pública | Registro con rol `MEMBER` |
| POST | `/api/v1/auth/login` | Pública | Login, devuelve JWT |
| GET | `/api/v1/auth/me` | JWT | Usuario autenticado |
| GET | `/api/v1/projects` | JWT | Proyectos propios o con membresía |
| POST | `/api/v1/projects` | JWT | Crear proyecto |
| GET | `/api/v1/projects/{id}` | JWT | Detalle con control de acceso |
| PUT | `/api/v1/projects/{id}` | JWT (OWNER/EDITOR) | Actualizar proyecto |
| DELETE | `/api/v1/projects/{id}` | JWT (OWNER) | Eliminar proyecto |

## Variables de entorno del backend

| Variable | Valor por defecto | Uso |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/ideator_db` | Conexión JDBC |
| `SPRING_DATASOURCE_USERNAME` | `ideator_user` | Usuario PostgreSQL |
| `SPRING_DATASOURCE_PASSWORD` | `ideator_pass` | Password PostgreSQL |
| `JWT_SECRET` | secreto de desarrollo | Firma HMAC (mínimo 32 bytes) |
| `JWT_EXPIRATION_MS` | `86400000` | Vigencia del token (24 h) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:3000` | Orígenes permitidos, separados por coma |
| `APP_SEED_ENABLED` | `true` | Crea usuario/proyecto demo |
| `AZURE_OPENAI_ENDPOINT` | vacío | Endpoint del recurso Azure OpenAI |
| `AZURE_OPENAI_KEY` | vacío | Key del recurso |
| `AZURE_OPENAI_DEPLOYMENT` | `gpt-4o-mini` | Nombre del deployment |
| `POSTGRES_PORT` | `5432` | Puerto host de Docker Compose |

> En producción cambia `JWT_SECRET`, las credenciales de PostgreSQL y desactiva el seed.

## Verificación realizada

- `backend`: compilación y empaquetado con Maven (`mvn -DskipTests package`) sobre Java 21.
- `backend`: arranque contra PostgreSQL 15 en Docker, health `UP`, login demo, listado/creación/consulta/
  eliminación de proyectos y `401` sin token.
- `frontend`: `npm run build` (TypeScript + Vite) sin errores.

## Alcance y siguientes pasos

Sprint 1 entrega seguridad, persistencia y la base de integración con Azure OpenAI. El flujo
conversacional de especificación (fases A-E), los colaboradores y la generación de documentos se
implementarán en los siguientes sprints. Para producción se recomienda añadir Flyway/Liquibase en lugar
de `ddl-auto=update`, refresh tokens y pruebas de integración con Testcontainers.
