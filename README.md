
> # 🎯 ABOUT
> This is a 'best practices' template project. However, it is an opinionated take on that.
>
> DISCLAIMER: I'm by no means an expert on Spring Boot (it's not even my preferred tool), one reason
> to do this is to learn. Opinions are welcomed (with proper reasoning), personal opinions (like
> naming, libraries, etc.) should be posted in the discussions section.
>
> To share feedback, you can use the following tools:
> * Issues: to report bugs, or request new features
> * Discussions: to raise questions about the implementation decisions, propose alternatives, etc.
> * Pull Requests: to fix problems (i.e.: implementing TODOs or fixing bugs)
>
> The project is mirrored on [GitLab](https://gitlab.com/jaguililla/hexagonal_spring) for CI
> demonstration purposes.
>
> Have fun!

# 🗓️ Appointments
Application to create appointments (REST API). Appointments are stored in a relational DB
(Postgres), and their creation/deletion is published to a Kafka broker.

## 📘 Architecture
* [Hexagonal]/[Onion]/[Clean] Architecture
* OpenAPI code generation (server and client)

[Hexagonal]: https://alistair.cockburn.us/hexagonal-architecture
[Onion]: https://jeffreypalermo.com/2008/07/the-onion-architecture-part-1
[Clean]: https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html

## 🧰 Stack
* Java 21
* Spring 3.3 (configurable server, 'undertow' by default)
  * Actuator (healthcheck, etc.)
* Flyway (chosen over Liquibase for its simplicity)
* Postgres
* Kafka

## 🏎️ Runtime
* Cloud Native Buildpacks (building)
* Docker Compose (local environment with the infrastructure)

## 🧪 Test
* ArchUnit (preferred over Java modules: it allows naming checks, etc.)
* Testcontainers (used to provide a test instance of Postgres and Kafka)

## ⚒️ Development
* SDKMAN (allows to use simpler runners on CI)
* Maven Wrapper (Maven can be provided by SDKMAN, however, Maven Wrapper has better IDE support)
* Editorconfig (supported by a lot of editors, rules limited though)
* CI pipelines for GitHub and GitLab

## 📑 Requirements
* Docker Compose
* JDK 21+
* SDKMAN (optional, recommended)

## 📖 Terms
* **Port:** interface to set a boundary among application logic and implementation details.
* **Adapter:**: port implementation to bridge the application's domain with the tools used in
  the system.
* **Domain:**: application logic and
* **Service:**
* **UseCase/Case:**
* **Input/Driver Adapter:**
* **Output/Driven Adapter:**

## 🤔 Design Decisions
* Minimal: don't use libraries to implement easy stuff (even if that's boring).
* Prefer flat structure (avoid empty parent packages).
* Less coupling with Spring (easier to migrate, to other frameworks/toolkits).
* Not use Spring integrations if a library can be used directly.
* No Spring profiles (settings are loaded from the environment).
* Split API spec in different files for future modularity.
* Prefer service independence over code reuse (sharing libraries among microservices), less
  coupling foster evolution among services and favor scalability when more teams/services are added.
* Take out the common (general) part of the `pom.xml` to `parent.xml`, however, it should not be
  moved to another repository (because of avoid coupling rule above).
* Docker Compose profiles are used to separate infrastructure from a complete environment including
  a container for this application.
* Atomicity in notifiers (with outbox pattern) should be done with a different notifier adapter.

## 📚 Design
* The REST API controller and client are generated from the OpenAPI spec at build time.
* Hexagonal Architecture: domain, ports, and adapters.
* Use cases are 'one responsibility services'. Start with services, split when they get bigger.
* `domain` holds business logic (services and/or use cases) and driven ports (interfaces).
* `domain.model` keeps the structures relevant to the application's domain. The more logic added to
  an entity, the better (it could be easily accessed by many different services, or use cases).
* `output.{notifiers,repositories}` driven adapters (implementations of driven ports).
* `input.controllers` driver adapter (adapters without interface).
* There are no 'input/driver ports', as they don't need to be decoupled from anything they just use
  the domain (and that's acceptable).
* Subpackages can be created for different adapter implementations (to isolate their code).
* Code structure and access rules:
  - **appointments**: holds the Spring configuration (dependency injection) and contains the
    starting class for the application.
  - **appointments.output.{notifiers,repositories}**: contains domain ports' actual implementations.
    These are implementation details and must not be used directly (except DI and tests).
  - **appointments.input.controllers**: contains the REST controllers of the application (driver
    adapter). Classes on this package cannot use any other application layer apart from domain.
  - **appointments.domain**: contains the business rules. Must not reference implementation details
    (storage, frameworks, etc.) directly, these features should be accessed by abstract
    interchangeable interfaces. It's not a problem to reference this package from Controllers or
    Repositories.
  - **appointments.domain.model**: holds the business entities. These are the data structures used
    by the business logic. Follows the same access rules as its parent package.

## 🎚️ Set up
* `sdk env install`

## ▶️ Commands
All commands assume a Unix like OS.

The most important commands to operate the project are:

* Build: `./mvnw package`
* Documentation: `./mvnw site`
* Run: `./mvnw spring-boot:run`
* Build image: `./mvnw spring-boot:build-image`

To run or deploy the application:

* Run JAR locally: `java -jar target/appointments-0.1.0.jar`
* Run container: `docker-compose --profile local up`

## 🤖 Service Management
* You can check the API spec using [Swagger UI](http://localhost:8080/swagger-ui/index.html).

### Docker
At the `docker-compose.yml` you can find the information on how to run the application as a
container, adjusting the configuration for running it on different environments.

Two Docker compose profiles are used:
- Default profile (no profile parameter): to allow starting only the infrastructure, this is useful
  to start the application from the IDE
- Local profile (--profile local): which also starts a container using the image of this application

### Testing
The verification requests can be executed with: `src/test/resources/requests.sh`, or
`PORT=9090 src/test/resources/requests.sh` if you want to run them to a different port.

The health check endpoint is: http://localhost:18080/actuator/health
