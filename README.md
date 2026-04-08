# Wigell Koncernen - Mikrotjänster & Dashboard

Detta repository innehåller källkoden för **Wigell Sushi API** (en mikrotjänst) och **Wigell Dashboard** (en övervakningsportal), utvecklade som en del av ett skolprojekt.

## Projektöversikt

Systemet består av två huvudkomponenter:

1.  **Wigell Sushi API**: En Spring Boot-mikrotjänst som hanterar affärslogiken för en sushirestaurang. Detta inkluderar hantering av kunder, rätter, bokningar och beställningar via ett REST API.
2.  **Wigell Dashboard**: En Spring Boot Admin Server som tillhandahåller ett webbgränssnitt för att övervaka hälsan och statusen för anslutna mikrotjänster som Wigell Sushi API.

En mer detaljerad systemdokumentation finns i filen `project_summary.md`.

## Teknisk Stack

*   **Språk**: Java 24
*   **Ramverk**: Spring Boot 3.x
*   **Databas**: MySQL (via Spring Data JPA)
*   **Säkerhet**: Spring Security med OAuth2 / OIDC för Keycloak-integration.
*   **Övervakning**: Spring Boot Admin (Server & Klient) och Spring Boot Actuator.
*   **Byggverktyg**: Maven

## Hur man kör systemet

### Förutsättningar
*   Java 24 JDK
*   Maven
*   MySQL-databas
*   En körande Keycloak-instans (för säkerhet)

### 1. Kör Wigell Sushi API
Navigera till `Wigell Sushi/sushi-api` och kör:
```sh
mvn spring-boot:run
```
API:et startar på port `8582` och försöker ansluta till databasen `wigell_sushi_db` samt dashboarden på port `9090`.

### 2. Kör Wigell Dashboard
Navigera till `Wigell Dashboard/portal-dashboard` och kör:
```sh
mvn spring-boot:run
```
Dashboarden startar på port `9090`. Surfa till `http://localhost:9090` och logga in via Keycloak för att se övervakningsgränssnittet.

**Notera**: Konfiguration för databas, Keycloak och dashboard-anslutningar finns i respektive `application.properties`-fil.
