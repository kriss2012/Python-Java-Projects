-- Logic Legend JV-08: Movie or Event Ticket Booking System Schema (SQLite)

CREATE TABLE IF NOT EXISTS movies (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    title TEXT NOT NULL,
    genre TEXT NOT NULL,
    duration_minutes INTEGER NOT NULL,
    language TEXT NOT NULL,
    rating TEXT NOT NULL DEFAULT 'UA',
    synopsis TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shows (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    movie_id INTEGER NOT NULL,
    venue TEXT NOT NULL,
    screen_name TEXT NOT NULL,
    show_date TEXT NOT NULL,
    show_time TEXT NOT NULL,
    silver_price REAL NOT NULL DEFAULT 150.0,
    gold_price REAL NOT NULL DEFAULT 220.0,
    platinum_price REAL NOT NULL DEFAULT 350.0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id)
);

CREATE TABLE IF NOT EXISTS seats (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    show_id INTEGER NOT NULL,
    seat_code TEXT NOT NULL,
    row_name TEXT NOT NULL,
    seat_number INTEGER NOT NULL,
    tier TEXT NOT NULL, -- SILVER, GOLD, PLATINUM
    price REAL NOT NULL,
    status TEXT NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, HOLD, BOOKED
    hold_expiry_ms INTEGER DEFAULT 0,
    hold_user TEXT,
    UNIQUE(show_id, seat_code),
    FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS bookings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_reference TEXT UNIQUE NOT NULL,
    show_id INTEGER NOT NULL,
    movie_title TEXT NOT NULL,
    customer_name TEXT NOT NULL,
    customer_email TEXT NOT NULL,
    customer_phone TEXT NOT NULL,
    seats_csv TEXT NOT NULL,
    subtotal REAL NOT NULL,
    convenience_fee REAL NOT NULL,
    gst_tax REAL NOT NULL,
    discount_amount REAL NOT NULL DEFAULT 0.0,
    grand_total REAL NOT NULL,
    booking_status TEXT NOT NULL DEFAULT 'CONFIRMED', -- CONFIRMED, CANCELLED
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (show_id) REFERENCES shows(id)
);

