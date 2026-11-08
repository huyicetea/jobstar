---
name: scaffold-microservice
description: Scaffolds a new Spring Boot service under backend/services/ with the same structure and config files as auth-service. Use when asked to create, scaffold or generate a service such as job-service, candidate-service, application-service or notification-service.
argument-hint: <service-name, e.g. job-service>
allowed-tools: Read, Write, Edit, Bash, Glob
---

## Instructions
Create the service named in the argument exactly like `backend/services/auth-service`. Read `backend/pom.xml` and `backend/docs/TECH-SPEC.md` first. Do not invent layer names, ports or env vars. Create files with the Write tool (a long bash heredoc script failed to parse). Never write code comments.

Not supported: `api-gateway` (reactive, no JPA/Flyway) and `ai-service` (Python). Tell the user to scaffold those by hand.

## Inputs
| service | `<pkg>` | `<port>` | `<db>` | `<Name>` |
|---------|---------|----------|--------|----------|
| auth-service | `auth` | 8081 | `auth_db` | `Auth` |
| job-service | `job` | 8082 | `job_db` | `Job` |
| candidate-service | `candidate` | 8083 | `candidate_db` | `Candidate` |
| application-service | `application` | 8084 | `application_db` | `Application` |
| notification-service | `notification` | 8085 | `notification_db` | `Notification` |

## Target Tree
```
backend/services/<service>/
  pom.xml
  Dockerfile
  src/main/java/com/jobstar/<pkg>/
    <Name>ServiceApplication.java
    controller/ service/ repository/
    model/{entity,enums,dto/{request,response}}/
  src/main/resources/{application.yml, db/migration/}
  src/test/java/com/jobstar/<pkg>/
```
Empty folders get a `.gitkeep`. Create `security/ config/ exception/ mapper/ scheduler/` only if the user asks.

## Steps
1. Resolve `<pkg>`, `<port>`, `<db>`, `<Name>` from the table; stop if the service is unknown or already exists.
2. `mkdir -p` the tree and `touch .gitkeep` in empty folders (one short Bash call).
3. Write `pom.xml`, `Dockerfile`, `application.yml` and the main class from the templates below.
4. Add `<module>services/<service></module>` to `backend/pom.xml` (after the last module).
5. From `backend/` run `mvn -q -DskipTests package`; confirm `services/<service>/target/<service>-0.0.1-SNAPSHOT.jar` exists.
6. Report created files and any open items.

## Templates

### pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>com.jobstar</groupId>
        <artifactId>jobstar-backend</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <relativePath>../../pom.xml</relativePath>
    </parent>
    <artifactId><service></artifactId>
    <name><service></name>
    <dependencies>
        <dependency>
            <groupId>com.jobstar</groupId>
            <artifactId>common-lib</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-flyway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```
Use `spring-boot-starter-flyway` (Boot 4), not `flyway-core` alone. `common-lib` needs no version (managed by the parent).

### application.yml
```yaml
server:
  port: <port>

spring:
  application:
    name: <service>
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/<db>}
    username: ${DB_USER:jobstar}
    password: ${DB_PASSWORD:jobstar}
  jpa:
    open-in-view: false
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration

management:
  endpoints:
    web:
      exposure:
        include: health
```

### Dockerfile (build context is `backend/`)
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY common-lib common-lib
COPY services/<service> services/<service>
RUN mvn -q -pl services/<service> -am package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /build/services/<service>/target/<service>-*.jar app.jar
EXPOSE <port>
ENTRYPOINT ["java", "-jar", "app.jar"]
```
Build with `docker build -f services/<service>/Dockerfile .` from `backend/`.

### <Name>ServiceApplication.java
```java
package com.jobstar.<pkg>;

import com.jobstar.common.error.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class <Name>ServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(<Name>ServiceApplication.class, args);
    }
}
```
`@Import` is required: the service package is not under `com.jobstar.common`, so the shared handler is not scanned.

## Rules
- Env vars are `DB_URL`, `DB_USER`, `DB_PASSWORD` (TECH-SPEC), never `DB_HOST` or `DB_PASS`.
- No Spring context test in the scaffold (it needs a database). No migrations either; the first feature adds `V1__...`.
- Ports and database names come only from the table and `backend/docs/TECH-SPEC.md`.

## Example
**Input:** `/scaffold-microservice job-service`
**Output:**
- Created `backend/services/job-service/` (package `com.jobstar.job`, port 8082, DB `job_db`): `pom.xml`, `Dockerfile`, `application.yml`, `JobServiceApplication.java`, layer folders with `.gitkeep`.
- Added module `services/job-service` to `backend/pom.xml`.
- `mvn -q -DskipTests package`: OK, `job-service-0.0.1-SNAPSHOT.jar` built.
- Open: first Flyway migration and entities.
