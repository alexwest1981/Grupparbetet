# Wigell Koncernen - Komplett Systemdokumentation

Denna dokumentation är din ultimata guide till projektet. Den beskriver exakt hur hela systemet hänger ihop (flödet) från start till mål, och förklarar därefter i detalj exakt vad varje enskild fil gör så att du snabbt kan hitta rätt.

---

## 1. Systemets Arkitektur och Flöde (Helhetsbilden)

För att förstå koden måste man först förstå hur datan reser genom systemet. Här är det kompletta flödet för de vanligaste scenarierna:

### Flöde A: En kund skapar en bokning (Valuta-flödet)
1. **Inloggning:** Kunden använder Postman för att skicka sitt användarnamn och lösenord till **Keycloak**. Keycloak verifierar och skickar tillbaka en `access_token` (en "passerbricka").
2. **Anropet:** Kunden skickar en `POST` till Sushi API:et (`/api/v1/bookings`) och bifogar sin token samt en JSON-kropp med vilka rätter de vill ha.
3. **Säkerhetskontroll (Auktorisering):** Sushi API:ets Spring Security-filter (`SecurityConfig.java`) fångar anropet. Den använder `CustomJwtAuthenticationConverter` för att titta inuti Keycloak-tokenen. Hittar den rollen `USER` (eller `ADMIN`), släpps anropet fram till `CustomerController`. Saknas rollen returneras `403 Forbidden`.
4. **Beräkning (SEK):** `CustomerController` loopar igenom alla maträtter kunden skickat in. Den pratar med `DishRepository` för att hämta det *äkta* priset från databasen (så kunden inte kan ljuga om priset) och summerar detta till en total summa i SEK.
5. **Externt API-anrop (JPY):** Innan bokningen sparas, pausar `CustomerController`. Den använder en `RestTemplate` för att skicka totalsumman i SEK till **Kristinas Valutakonverterare (Mikrotjänst 2)**. Kristinas API räknar om detta och skickar tillbaka priset i Japanska Yen (JPY). Om hennes API ligger nere loggas ett fel och JPY-priset sätts till 0.0.
6. **Spara:** `CustomerController` tar både SEK-priset och JPY-priset, lägger in dem i `Booking`-objektet och skickar det till `BookingRepository` som sparar det permanent i MySQL-databasen.
7. **Svar:** Kunden får tillbaka ett `201 Created` och hela den sparade bokningen (inklusive priset i båda valutorna) visas på skärmen.
8. **Loggning:** I samma sekund skriver systemet en rad i filen `logs/sushi-api.log` om att en bokning skapats (VG-krav).

### Flöde B: Övervakning via Dashboarden
1. **Registrering:** När Sushi API:et startar, tittar det i sin `application.properties` och ser att det ska rapportera till en Dashboard. Den skickar ett osynligt anrop till `http://localhost:9090/instances`.
2. **Godkännande:** Dashboarden (som annars är stenhårt låst av Keycloak) har en specifik regel i sin `SecurityConfig` som säger att just anrop till `/instances` är tillåtna utan inloggning. Sushi API:et registreras.
3. **Visning:** När du (Admin) surfar till `http://localhost:9090` tvingas du logga in via Keycloak. Väl inne ser du Sushi API:et, dess minnesanvändning och status tack vare att Dashboarden hela tiden pingar API:ets `/actuator/health`-länkar.

---

## 2. Wigell Sushi API - Vad exakt gör filerna?

Detta är själva hjärtat av din kod. Filerna är uppdelade i mappar (paket) baserat på deras ansvarsområde.

### 📂 Paketet: `controller` (Gränssnittet utåt)
Det är här alla API-anrop (GET, POST, etc.) hamnar först. Controllernas enda jobb är att ta emot trafik, samordna logiken och skicka tillbaka svar.

*   **`CustomerController.java`**: Hanterar allt som en kund (`USER`) och administratör (`ADMIN`) får göra gemensamt.
    *   `getAllDishes()`: Hämtar menyn (Tillåtet för både USER och ADMIN).
    *   `createOrder()` & `createBooking()`: Det är **HÄR** den tunga VG-logiken ligger. Här räknas SEK-priset ut, och här finns `RestTemplate`-anropet som pratar med Kristinas valuta-API för att hämta JPY-priset.
    *   `updateBooking()`: Uppdaterar en bokning. Om kunden ändrar maträtterna, räknar denna metod automatiskt om priset och anropar Kristinas API igen!
    *   `getBookingsByCustomerId()` / `getOrdersByCustomerId()`: Hämtar historik.
*   **`AdminController.java`**: Hanterar allt som *enbart* administratören (`ADMIN`) får göra (skapa/ändra rätter, lokaler, kunder). Den består uteslutande av rena CRUD-operationer (Create, Read, Update, Delete) som pratar direkt med databasen.

### 📂 Paketet: `entity` (Databasens struktur)
Klasserna här motsvarar exakt hur tabellerna i MySQL-databasen ser ut.

*   **`Customer.java`**: Har fält för ID, användarnamn, namn och adress.
*   **`Dish.java`**: Maträtten. Har ett fält `priceSek`.
*   **`Room.java`**: Lokalen. Håller koll på max antal gäster och teknisk utrustning.
*   **`Order.java` & `Booking.java`**: Huvudentiteterna för transaktioner. Båda har fält för `totalPriceSek` och `totalPriceJpy`.
*   **`OrderDish.java` & `BookingDish.java`**: Dessa är så kallade "kopplingstabeller". Eftersom en bokning kan ha många maträtter, och en maträtt kan finnas i många bokningar (ManyToMany), används dessa för att hålla koll på *vilken* rätt som hör till *vilken* bokning, och *hur många* (quantity) kunden beställde.

### 📂 Paketet: `repository` (Databas-kommunikationen)
Här finns ingen egentlig kod, bara "Interfaces". Genom att ärva från `JpaRepository` skapar Spring Boot automatiskt alla SQL-frågor (INSERT, SELECT, DELETE) åt oss i bakgrunden.
*   Innehåller: `BookingRepository`, `CustomerRepository`, `DishRepository`, `OrderRepository`, `RoomRepository`.
*   *Specialare:* I `BookingRepository` och `OrderRepository` har vi lagt till metoden `findByCustomer(Customer customer)`. Detta säger åt Spring att "bygg en SQL-fråga som hämtar alla bokningar för en specifik kund".

### 📂 Paketet: `exception` (Felhanteringen)
Detta är ett VG-krav/Best Practice för att API:et inte ska krascha fult om någon söker på något som inte finns.
*   **`ResourceNotFoundException.java`**: Ett "namngivet" fel som vi kastar i våra controllers (t.ex. `orElseThrow(() -> new ResourceNotFoundException(...))`) när ett ID saknas.
*   **`ErrorResponse.java`**: Mallen för hur ett felmeddelande ska se ut (Vilken tid, statuskod 404, och det specifika felmeddelandet).
*   **`GlobalExceptionHandler.java`**: En övervakare (`@ControllerAdvice`). När någon kod någonstans kastar en `ResourceNotFoundException`, fångar denna klass upp felet i luften, packar in det i vår `ErrorResponse`-mall och skickar tillbaka ett snyggt JSON-svar till Postman.

### 📂 Paketet: `config` (Säkerhet & Bönor)
*   **`SecurityConfig.java`**: Låser API:et. Den definierar att endast `ADMIN` får pilla på `/rooms`, `/customers` och skapa/ändra i `/dishes`. Den tillåter både `ADMIN` och `USER` att läsa `/dishes` och hantera `/bookings` och `/orders`.
*   **`CustomJwtAuthenticationConverter.java`**: En översättare. Eftersom Keycloak lägger sina roller djupt inne i tokenen under `realm_access.roles`, hjälper denna klass Spring Security att hitta dem och lägga till prefixet `ROLE_` (vilket krävs för att `hasRole("ADMIN")` ska fungera).

### ⚙️ Konfiguration & Startdata
*   **`src/main/resources/application.properties`**: Här ställer vi in vilken port API:et körs på (8582), inloggningsuppgifter till MySQL, vilken Keycloak-server som ska användas för säkerhet, och instruktioner för att logga alla händelser till filen `logs/sushi-api.log`. Här säger vi även åt Spring Boot att vänta på att tabellerna skapats innan den kör `data.sql`.
*   **`src/main/resources/data.sql`**: Innehåller färdiga SQL `INSERT IGNORE`-satser. Varje gång API:et startar fylls databasen automatiskt med 5 kunder, 5 rätter, 3 lokaler och 2 beställningar (enligt kravspecifikationen) så att systemet är redo för testning och redovisning direkt.

---

## 3. Wigell Dashboard - Vad exakt gör filerna?

Detta projekt är mycket mindre, eftersom det inte innehåller någon egen affärslogik. Det är en färdigbyggd central (Spring Boot Admin) som vi bara har konfigurerat.

### 📂 Källkoden
*   **`PortalDashboardApplication.java`**: Huvudfilen. Den magiska raden här är `@EnableAdminServer`. Den raden förvandlar en vanlig tom Spring Boot-app till en fullfjädrad övervakningsportal med webbgränssnitt.
*   **`config/SecurityConfig.java`**: Det är här Keycloak-magin sker. Denna fil bestämmer vem som får se Dashboarden.
    *   Den säger åt Spring att använda `oauth2Login()` (vilket är det som byter ut skärmen och tvingar iväg dig till Keycloak när du försöker surfa till localhost:9090).
    *   Den innehåller ett undantag: `.requestMatchers("/instances").permitAll()`. Detta säkerställer att Sushi API:et (och framtida API:er) får skicka sin status till dashboarden *utan* att behöva logga in med Keycloak.

### ⚙️ Konfiguration
*   **`src/main/resources/application.properties`**: Här sätts porten (9090). Det viktigaste här är dock inställningarna för **OAuth2 Client**. Här har vi klistrat in url:erna till `wigell-realm` och angett vår `client-secret`. Det är dessa inställningar som `SecurityConfig.java` använder för att veta exakt vilken Keycloak-server den ska skicka användarna till vid inloggning. Här definieras också `logs/portal-dashboard.log` för fil-loggningen.
*   **`pom.xml`**: Här har vi lagt till beroenden för säkerhet (`spring-boot-starter-security`, `spring-security-oauth2-client`) för att koden i `SecurityConfig` överhuvudtaget ska fungera, samt angett Java 24 och korrekta Spring Boot-versioner (3.4.3).

---

## Sammanfattning för Redovisningen
Om du får en fråga om **var** en specifik sak sker:
*   **Var kollar ni rollerna (ADMIN/USER)?** -> I `config/SecurityConfig.java` inuti Sushi API:et.
*   **Var pratar ni med Keycloak?** -> I `application.properties` (för URL:er) och `SecurityConfig.java` (för att aktivera skyddet). Sushi API kollar inkommande tokens passivt, medan Dashboarden aktivt skickar användaren till Keycloak-inloggningen.
*   **Var pratar ni med Valutatjänsten?** -> Inne i `CustomerController.java` via en `RestTemplate` när en order eller bokning sparas/uppdateras.
*   **Varför kraschar det inte när man söker på fel ID?** -> För att `GlobalExceptionHandler.java` i `exception`-paketet fångar felet och bygger ett standardiserat 404 JSON-svar.
*   **Hur vet Dashboarden om Sushi API:et?** -> Sushi API:et har `spring-boot-admin-starter-client` i sin `pom.xml` och URL:en till dashboarden i `application.properties`, vilket gör att den aktivt "ringer hem" till Dashboarden på port 9090 och anmäler sin existens via den öppna `/instances`-länken.
*   **Var ligger er startdata?** -> I `src/main/resources/data.sql`.
