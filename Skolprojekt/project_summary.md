# Systemdokumentation: Wigell Koncernen (Sushi API & Dashboard)

Denna dokumentation beskriver uppbyggnaden, filerna och arkitekturen för två huvudsakliga komponenter i Wigell-projektet: **Wigell Sushi API** och **Wigell Dashboard**. Dokumentationen är utformad för att ge en tydlig och komplett översikt inför genomgångar och redovisningar.

---

## 1. Wigell Sushi API (Mikrotjänst)

Wigell Sushi är en mikrotjänst byggd i Spring Boot som hanterar affärslogiken för en sushirestaurang, inklusive kunder, rätter, rumsbokningar och takeaway-beställningar.

### Teknisk Stack
*   **Språk & Ramverk**: Java 24, Spring Boot 3.x. Lombok är ej använt enligt krav.
*   **Databas**: MySQL via Spring Data JPA. Databasnamn: `wigell_sushi_db`.
*   **Säkerhet**: Spring Security / OAuth2 Resource Server. Redo att validera JWT-tokens från Keycloak.
*   **Övervakning**: Spring Boot Actuator & Spring Boot Admin Client.

### Konfiguration (`application.properties`)
*   **Port**: `8582` (Enligt specifikation).
*   **Databasanslutning**: Konfigurerad att ansluta mot `127.0.0.1:3306/wigell_sushi_db` med automatisk tabell-uppdatering (`update`).
*   **Loggning**: Uppfyller G och VG-krav. Loggar INFO-meddelanden och uppåt till både konsol och filen `logs/sushi-api.log`.
*   **Keycloak**: Innehåller länk till Keycloak-realm (byggs av teammedlem) för att validera inkommande säkerhetstokens.
*   **Dashboard-klient**: Konfigurerad att rapportera in till dashboarden på port `9090`.

### Datamodell (Filer i `entity`-paketet)
Klasserna är annoterade med `@Entity` och mappas till databastabeller. Alla getters/setters är manuellt utskrivna.
*   **`Customer.java`**: Representerar en kund. Innehåller ID, unikt användarnamn, namn och adress.
*   **`Dish.java`**: Representerar en maträtt. Innehåller ID, namn och pris i SEK (`priceSek`).
*   **`Room.java`**: Representerar en lokal för bokning. Innehåller ID, namn, max antal gäster och teknisk utrustning.
*   **`Order.java`**: Representerar en takeaway-beställning. Innehåller prisuppgifter och är kopplad via en relation till `Customer`.
*   **`Booking.java`**: Representerar en rumsreservation. Har relationer till `Customer` (`@ManyToOne`), `Room` (`@ManyToOne`) och maträtter `List<Dish>` (`@ManyToMany`). Innehåller fält för datum, gäster, och totalpris.

### Datalager (Filer i `repository`-paketet)
Interfaces som ärver från `JpaRepository`, vilket ger Spring Boot förmågan att generera SQL-frågor automatiskt (CRUD).
*   `CustomerRepository`, `DishRepository`, `RoomRepository` (Standard CRUD).
*   `BookingRepository` & `OrderRepository`: Innehåller anpassade sökmetoder, ex: `List<Booking> findByCustomer(Customer customer)`, för att hämta rader kopplade till specifika kunder.

### API Endpoints (Filer i `controller`-paketet)
Klasserna är annoterade med `@RestController`. Varje endpoint som skapar, uppdaterar eller tar bort något loggar händelsen (ex. "Admin created dish X"), vilket uppfyller loggningskravet.

#### `CustomerController.java` (Kundfunktioner)
Hanterar det en kund kan göra. Förväntar sig USER-roll vid anrop.
*   `GET /api/v1/dishes`: Lista alla rätter.
*   `POST /api/v1/orders`: Beställ takeaway. Skapar en Order i databasen.
*   `POST /api/v1/bookings`: Boka bord/lokal. Skapar en Booking i databasen.
*   `PATCH /api/v1/bookings/{bookingId}`: Ändra delar av en befintlig bokning (datum, gäster, lokal, rätter).
*   `GET /api/v1/bookings?customerId={id}`: Se kundens tidigare och aktiva bokningar.
*   `GET /api/v1/orders?customerId={id}`: Se kundens tidigare beställningar.

#### `AdminController.java` (Adminfunktioner)
Hanterar administratörens rättigheter (komplett CRUD). Förväntar sig ADMIN-roll.
*   **Kunder**: 
    *   `GET /api/v1/customers` (Lista)
    *   `POST /api/v1/customers` (Skapa)
    *   `PUT /api/v1/customers/{id}` (Uppdatera)
    *   `DELETE /api/v1/customers/{id}` (Ta bort)
    *   `POST /api/v1/customers/{id}/addresses` (Lägg till adress)
    *   `DELETE /api/v1/customers/{id}/addresses/{addressId}` (Rensa adress)
*   **Rätter (`/dishes`)**: `GET` (alla), `GET` (specifik), `POST` (skapa), `PUT` (uppdatera), `DELETE` (ta bort).
*   **Lokaler (`/rooms`)**: `GET` (alla), `GET` (specifik), `POST` (skapa), `PUT` (uppdatera), `DELETE` (ta bort).
*   **Bokningar/Beställningar**: `GET /api/v1/orders` (alla), `GET /api/v1/orders/{id}` (specifik), `GET /api/v1/bookings` (alla).

---

## 2. Wigell Dashboard (Övervakningsportal)

Dashboarden är ett webbgränssnitt som övervakar hälsan och statusen för koncernens mikrotjänster (som Wigell Sushi).

### Teknisk Stack
*   **Språk & Ramverk**: Java 24, Spring Boot 3.x. Lombok ej använt.
*   **Spring Boot Admin Server**: Kärnkomponenten som ritar upp gränssnittet och samlar in hälso-data från andra tjänster.
*   **Säkerhet**: Spring Security konfigurerad för inloggning via Keycloak.

### Konfiguration (`application.properties`)
*   **Port**: `9090`.
*   **Säkerhet (Keycloak)**: Innehåller all konfiguration för att applikationen ska agera klient mot Keycloak (`issuer-uri`, `client-id`, `client-secret`). Användaren omdirigeras till Keycloak för att logga in.
*   **Loggning**: Uppfyller G och VG-krav. Loggar skrivs både till konsolen och till filen `logs/portal-dashboard.log`.

### Filer och Struktur

#### `PortalDashboardApplication.java`
*   Innehåller annoteringen `@EnableAdminServer`. Detta är den viktigaste raden i projektet då den aktiverar hela Spring Boot Admin-gränssnittet.

#### `SecurityConfig.java` (Säkerhetskonfiguration)
*   Tvingar alla användare att logga in för att se Dashboarden (`anyRequest().authenticated()`).
*   Inloggning sker via OAuth2 och Keycloak (`oauth2ResourceServer` & `formLogin`).
*   Mappar automatiskt Keycloaks rollsystem så att det fungerar med Spring Securitys förväntade `ROLE_`-prefix.
*   Stänger av CSRF-skydd specifikt för `/logout` och Actuator-endpoints för att möjliggöra korrekta nätverksanrop bakom kulisserna.

---

## Hur Systemen Samarbetar (Helhetsbild)

1.  **Inloggning (Säkerhet)**: När administratören surfar till Dashboarden på port 9090 avkrävs de en inloggning. De slussas till Keycloak-servern, loggar in, och släpps in i Dashboarden med en JWT-token. Samma Keycloak används när API Gateway eller Postman skickar anrop till Sushi API (port 8582) – API:et verifierar token i headern mot samma Keycloak-server.
2.  **Övervakning (Actuator & Admin)**: Sushi API är en "klient" till Dashboarden. Genom `spring-boot-admin-starter-client` vet Sushi API om att Dashboarden finns på port 9090. När Sushi API startar pingar den Dashboarden och säger "Här är jag". Dashboarden använder därefter Actuator-endpoints (`/actuator/health` och `/actuator/info`) på Sushi API:et för att hämta CPU-användning, databasstatus, och loggar direkt in i webbgränssnittet.
3.  **Fil-Loggning**: Om ett fel inträffar eller något raderas, skriver Sushi API:et ner detta i `logs/sushi-api.log`. Om något sker i övervakningssystemet sparas det i `logs/portal-dashboard.log`. Detta gör systemet spårbart och stabilt (VG-krav).
4.  **Framtidssäkrat**: Koden är nu helt redo att kopplas ihop med teamets gemensamma API Gateway och Valutakonverterare, då endpoints och datamodell följer specifikation och Returnerar rätt HTTP-koder.