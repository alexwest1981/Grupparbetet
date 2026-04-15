# Postman Tester - Wigell Sushi API

Här är en komplett samling av alla anrop du kan göra mot ditt Sushi API (som körs på port `8582`). Alla anrop under "/api/v1" kräver att du skickar med en giltig JWT-token från Keycloak.

## 0. Förberedelser (Innan du testar API:et)

Innan du kan göra något av anropen nedan måste du hämta en token.

**Hämta Token från Keycloak:**
*   **Metod:** `POST`
*   **URL:** `http://localhost:8080/realms/wigell-realm/protocol/openid-connect/token`
*   **Authorization:** `No Auth`
*   **Body (`x-www-form-urlencoded`):**
    *   `client_id`: `dashboard-client`
    *   `client_secret`: `[Din Secret]`
    *   `username`: `api_user_2` (eller din admin-användare)
    *   `password`: `[Ditt lösenord]`
    *   `grant_type`: `password`

**Kopiera värdet för `access_token` från svaret.**

För ALLA anrop nedan gäller:
*   Gå till fliken **Authorization** i Postman.
*   Välj **Bearer Token**.
*   Klistra in din kopierade token.
*   (Under fliken **Headers** bör Postman automatiskt sätta `Content-Type: application/json` om du skickar en Body).

---

## 1. ADMIN - Hantera Kunder (`/customers`)

### Skapa Kund
*   **Metod:** `POST`
*   **URL:** `http://localhost:8582/api/v1/customers`
*   **Body (raw, JSON):**
    ```json
    {
        "username": "kalle_kund",
        "name": "Kalle Anka",
        "address": "Paradisäppelvägen 111"
    }
    ```

### Lista Kunder
*   **Metod:** `GET`
*   **URL:** `http://localhost:8582/api/v1/customers`

### Uppdatera Kund (Byt ut `{id}` mot ett nummer)
*   **Metod:** `PUT`
*   **URL:** `http://localhost:8582/api/v1/customers/{id}`
*   **Body (raw, JSON):**
    ```json
    {
        "name": "Karl Anka",
        "address": "Ankeborg 1"
    }
    ```

### Lägg till / Uppdatera Adress
*   **Metod:** `POST`
*   **URL:** `http://localhost:8582/api/v1/customers/{customerId}/addresses`
*   **Body (raw, Text):**
    ```text
    Ny adressgatan 42
    ```

### Ta bort Adress
*   **Metod:** `DELETE`
*   **URL:** `http://localhost:8582/api/v1/customers/{customerId}/addresses/1`

### Ta bort Kund
*   **Metod:** `DELETE`
*   **URL:** `http://localhost:8582/api/v1/customers/{id}`

---

## 2. ADMIN - Hantera Rätter (`/dishes`)

### Skapa Maträtt
*   **Metod:** `POST`
*   **URL:** `http://localhost:8582/api/v1/dishes`
*   **Body (raw, JSON):**
    ```json
    {
        "name": "Spicy Tuna Roll",
        "priceSek": 149.0
    }
    ```

### Lista Rätter
*   **Metod:** `GET`
*   **URL:** `http://localhost:8582/api/v1/dishes`

### Hämta specifik Rätt
*   **Metod:** `GET`
*   **URL:** `http://localhost:8582/api/v1/dishes/{id}`

### Uppdatera Rätt
*   **Metod:** `PUT`
*   **URL:** `http://localhost:8582/api/v1/dishes/{id}`
*   **Body (raw, JSON):**
    ```json
    {
        "name": "Spicy Tuna Roll (Extra Spicy)",
        "priceSek": 159.0
    }
    ```

### Ta bort Rätt
*   **Metod:** `DELETE`
*   **URL:** `http://localhost:8582/api/v1/dishes/{id}`

---

## 3. ADMIN - Hantera Lokaler (`/rooms`)

### Skapa Lokal
*   **Metod:** `POST`
*   **URL:** `http://localhost:8582/api/v1/rooms`
*   **Body (raw, JSON):**
    ```json
    {
        "name": "Tokyo Room",
        "maxGuests": 12,
        "technicalEquipment": "Projektor och Whiteboard"
    }
    ```

### Lista Lokaler
*   **Metod:** `GET`
*   **URL:** `http://localhost:8582/api/v1/rooms`

### Hämta specifik Lokal
*   **Metod:** `GET`
*   **URL:** `http://localhost:8582/api/v1/rooms/{id}`

### Uppdatera Lokal
*   **Metod:** `PUT`
*   **URL:** `http://localhost:8582/api/v1/rooms/{id}`
*   **Body (raw, JSON):**
    ```json
    {
        "name": "Tokyo Room Premium",
        "maxGuests": 15,
        "technicalEquipment": "85-tums TV och ljudsystem"
    }
    ```

### Ta bort Lokal
*   **Metod:** `DELETE`
*   **URL:** `http://localhost:8582/api/v1/rooms/{id}`

---

## 4. ADMIN - Visa Ordrar & Bokningar

### Lista alla Ordrar
*   **Metod:** `GET`
*   **URL:** `http://localhost:8582/api/v1/orders`

### Hämta specifik Order
*   **Metod:** `GET`
*   **URL:** `http://localhost:8582/api/v1/orders/{id}`

### Lista alla Bokningar
*   **Metod:** `GET`
*   **URL:** `http://localhost:8582/api/v1/bookings`

---

## 5. CUSTOMER - Kundens API

### Lista Rätter (Samma som Admin)
*   **Metod:** `GET`
*   **URL:** `http://localhost:8582/api/v1/dishes`

### Skapa Takeaway Beställning (Order)
*Denna endpoint kommer automatiskt räkna ut SEK-priset och försöka anropa Valuta-API:et för JPY.*
*   **Metod:** `POST`
*   **URL:** `http://localhost:8582/api/v1/orders`
*   **Body (raw, JSON):** (Byt ut ID:n mot rätter och kunder som faktiskt finns i din databas)
    ```json
    {
        "customer": {
            "id": 1
        },
        "orderDishes": [
            {
                "dish": { "id": 1 },
                "quantity": 2
            },
            {
                "dish": { "id": 2 },
                "quantity": 1
            }
        ]
    }
    ```

### Skapa Rumsbokning
*Denna endpoint räknar också ut SEK och JPY automatiskt.*
*   **Metod:** `POST`
*   **URL:** `http://localhost:8582/api/v1/bookings`
*   **Body (raw, JSON):**
    ```json
    {
        "customer": { "id": 1 },
        "room": { "id": 1 },
        "bookingDate": "2024-05-20T18:00:00",
        "numberOfGuests": 4,
        "technicalEquipment": "Behöver en mikrofon",
        "bookingDishes": [
            {
                "dish": { "id": 1 },
                "quantity": 4
            }
        ]
    }
    ```

### Uppdatera en Bokning (T.ex. byta rätter eller datum)
*   **Metod:** `PATCH`
*   **URL:** `http://localhost:8582/api/v1/bookings/{id}`
*   **Body (raw, JSON):**
    ```json
    {
        "numberOfGuests": 6,
        "bookingDishes": [
            {
                "dish": { "id": 2 },
                "quantity": 6
            }
        ]
    }
    ```

### Hämta Kundens Bokningar
*   **Metod:** `GET`
*   **URL:** `http://localhost:8582/api/v1/bookings?customerId=1`

### Hämta Kundens Beställningar (Ordrar)
*   **Metod:** `GET`
*   **URL:** `http://localhost:8582/api/v1/orders?customerId=1`

---

## Tips för Felhanteringen (Testa ditt VG-krav)
För att visa upp att ni har en bra global felhanterare, prova att göra ett GET-anrop mot en resurs som inte finns.
T.ex: `GET http://localhost:8582/api/v1/customers/999`
Du bör då få tillbaka ett snyggt JSON-felmeddelande (404 Not Found) med tidpunkt, status och texten "Hittade ingen matchande kund med id: 999".
