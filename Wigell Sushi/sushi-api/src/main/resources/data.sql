-- Använder INSERT IGNORE så att databasen inte kraschar om raderna redan finns vid nästa omstart.
-- Kunder
INSERT IGNORE INTO customers (id, username, name, address) VALUES (1, 'user1', 'Anders Andersson', 'Gatan 1');
INSERT IGNORE INTO customers (id, username, name, address) VALUES (2, 'user2', 'Berta Bengtsson', 'Gatan 2');
INSERT IGNORE INTO customers (id, username, name, address) VALUES (3, 'user3', 'Carl Carlsson', 'Gatan 3');
INSERT IGNORE INTO customers (id, username, name, address) VALUES (4, 'user4', 'Daniella Davidsson', 'Gatan 4');
INSERT IGNORE INTO customers (id, username, name, address) VALUES (5, 'user5', 'Erik Eriksson', 'Gatan 5');

-- Lokaler
INSERT IGNORE INTO room (id, name, max_guests, technical_equipment) VALUES (1, 'Lilla rummet', 4, 'Ingen');
INSERT IGNORE INTO room (id, name, max_guests, technical_equipment) VALUES (2, 'Mellanrummet', 10, 'Projektor');
INSERT IGNORE INTO room (id, name, max_guests, technical_equipment) VALUES (3, 'Stora rummet', 30, 'Ljudsystem och Projektor');

-- Rätter
INSERT IGNORE INTO dishes (id, name, price_sek) VALUES (1, 'Lax Nigiri', 20.0);
INSERT IGNORE INTO dishes (id, name, price_sek) VALUES (2, 'Avokado Maki', 15.0);
INSERT IGNORE INTO dishes (id, name, price_sek) VALUES (3, 'Spicy Tuna', 25.0);
INSERT IGNORE INTO dishes (id, name, price_sek) VALUES (4, 'Sashimi Mix', 150.0);
INSERT IGNORE INTO dishes (id, name, price_sek) VALUES (5, 'Miso Soppa', 30.0);

-- Beställningar
INSERT IGNORE INTO orders (id, customer_id, total_price_sek, total_price_jpy) VALUES (1, 1, 100.0, 1400.0);
INSERT IGNORE INTO orders (id, customer_id, total_price_sek, total_price_jpy) VALUES (2, 2, 200.0, 2800.0);

-- Order Rätter (Kopplingstabell)
INSERT IGNORE INTO order_dishes (id, order_id, dish_id, quantity) VALUES (1, 1, 1, 5);
INSERT IGNORE INTO order_dishes (id, order_id, dish_id, quantity) VALUES (2, 2, 4, 1);
INSERT IGNORE INTO order_dishes (id, order_id, dish_id, quantity) VALUES (3, 2, 5, 2);
