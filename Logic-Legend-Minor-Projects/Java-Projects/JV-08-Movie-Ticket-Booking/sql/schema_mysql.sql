-- Logic Legend JV-08: Movie or Event Ticket Booking System Schema (MySQL)

CREATE TABLE IF NOT EXISTS movies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    genre VARCHAR(60) NOT NULL,
    duration_minutes INT NOT NULL,
    language VARCHAR(40) NOT NULL,
    rating VARCHAR(10) NOT NULL DEFAULT 'UA',
    synopsis TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS shows (
    id INT AUTO_INCREMENT PRIMARY KEY,
    movie_id INT NOT NULL,
    venue VARCHAR(100) NOT NULL,
    screen_name VARCHAR(50) NOT NULL,
    show_date VARCHAR(20) NOT NULL,
    show_time VARCHAR(20) NOT NULL,
    silver_price DECIMAL(10,2) NOT NULL DEFAULT 150.00,
    gold_price DECIMAL(10,2) NOT NULL DEFAULT 220.00,
    platinum_price DECIMAL(10,2) NOT NULL DEFAULT 350.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (movie_id) REFERENCES movies(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS seats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    show_id INT NOT NULL,
    seat_code VARCHAR(10) NOT NULL,
    row_name VARCHAR(5) NOT NULL,
    seat_number INT NOT NULL,
    tier VARCHAR(20) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    hold_expiry_ms BIGINT DEFAULT 0,
    hold_user VARCHAR(100),
    UNIQUE KEY uq_show_seat (show_id, seat_code),
    FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    booking_reference VARCHAR(50) UNIQUE NOT NULL,
    show_id INT NOT NULL,
    movie_title VARCHAR(150) NOT NULL,
    customer_name VARCHAR(100) NOT NULL,
    customer_email VARCHAR(120) NOT NULL,
    customer_phone VARCHAR(30) NOT NULL,
    seats_csv VARCHAR(255) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    convenience_fee DECIMAL(10,2) NOT NULL,
    gst_tax DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    grand_total DECIMAL(10,2) NOT NULL,
    booking_status VARCHAR(30) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE
) ENGINE=InnoDB;

