# PiePayOffers

A backend service to fetch, store, and serve the best payment offers — inspired by Flipkart’s payment options experience.

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Setup & Run](#-setup--run)
- [API Usage](#-api-usage)
- [Design Decision](#-design-decision)
- [Example Database Schema](#-example-database-schema)
- [Scalability](#-scalability)
- [Future Improvement](#-future-improvement)
- [Sample Flipkart Payload](#-sample-flipkart-payload)
- [Assumptions](#-assumptions)
- [Development Branch](#-development-branch)
- [Author](#-author)

---

## 📌 Overview

This service fetches offer data from flipkart payment page payloads, parses relevant details like discount type, value, banks, and payment instruments, and stores them in a relational database.

It also provides an API to calculate the **best applicable discount** for a given order amount, bank, and payment instrument.

---

## ✅ Features

- **POST `/offer`**
    - Accepts JSON payload in Flipkart payment page format
    - Extracts all offer sections and saves offers to the DB
    - Ignores duplicates based on `adjustment_id`

- **GET `/highest-discount`**
    - Calculates the highest possible discount for:
        - Amount to pay
        - Bank name
        - Payment instrument
    - Supports flat discounts, % discounts, and fallback parsing from summary text.
    - Handles missing or incomplete data gracefully.

---

## 🗂️ Tech Stack

- Java 17+
- Spring Boot 3
- Spring Data JPA
- MySQL
- Maven

---

## ✅ Setup & Run

1. **Clone the repo**
   ```bash
   git clone https://github.com/khushusharma/piepay_offers.git
   cd piepay_offers

2. **Create src/main/resources/application.properties with your local DB credentials**
   ```bash
   # Spring Datasource Configuration
   spring.datasource.url=jdbc:mysql://localhost:3306/piepay
   spring.datasource.username=root
   spring.datasource.password=your_password
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

   # Hibernate Properties
   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true

3. **Build the Project**
   ```bash
   mvn clean install

4. **Run the Application**
   ```bash
   mvn spring-boot:run

5. **By default backend will start on.**
   ```bash 
    http://localhost:8080

---

## ✅ API Usage

**1. POST API `/offer`**

**Description:** Save offers from Flipkart payload JSON.

**Example:**
  ```bash
    curl -X POST http://localhost:8080/offer \
    -H "Content-Type: application/json" \
    -d @flipkart-sample.json
    
   ```

**2. GET API `/highest-discount`**

**Description:** Calculate and return the highest possible discount for an order.

**Query Params:**

- `amountToPay` - Order Amount
- `bankName` - Bank Name (ICICI, HDFC etc. )
- `paymentInstrument` - CREDIT, DEBIT, UPI etc.

**Example:**
  ```bash 
            curl "http://localhost:8080/offer/highest-discount?amountToPay=10000&bankName=HDFC&paymentInstrument=CREDIT"
  ```

**Sample Response**
  ```json
    {
      "highestDiscountAmount": 1500.0
    }
   ```

---

##  📌 Design Decision

**✅️ Framework & Tech Stack**
- Spring Boot was chosen for its simple, powerful REST support, fast setup, and clear layered structure.
- MySQL is used for reliable, structured storage and good performance for normalized relational data.

**✅ Database Schema**
- Each offer uses offerId (adjustment_id) as a unique business key.
- discountType, discountValue, percentage, and minAmount are parsed from the summary text because Flipkart’s API does not provide them as explicit fields.
- banks, paymentInstruments, and emiMonths use **@ElementCollection** to create normalized link tables, ensuring efficient filtering by bank/payment instrument.

**✅ Exception Handling**
- Instead of cluttering logic with repetitive try-catch blocks, a @ControllerAdvice is used to handle bad input or system errors globally.
- This keeps controllers and services clean and focused on business logic.

---

## ✅ Example Database Schema

Example `offer` table structure:

   ```sql 
     CREATE TABLE `offers` (
     `id` bigint NOT NULL AUTO_INCREMENT,
     `adjustment_type` varchar(255) DEFAULT NULL,
     `discount_type` varchar(255) DEFAULT NULL,
     `discount_value` double NOT NULL,
     `min_amount` double NOT NULL,
     `offer_id` varchar(255) NOT NULL,
     `percentage` bit(1) NOT NULL,
     `summary` text,
      PRIMARY KEY (`id`),
      UNIQUE KEY `UKiwrjr30jn4468samtpvs89va8` (`offer_id`)
   ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

```

---

## ✅ Scalability

**How to handle ~1,000 requests per second for /highest-discount:**
1. **Efficient Queries:**
   The current implementation uses proper indexed relational tables (offers, offer_banks, offer_payment_instruments).
   To scale, ensure indexes on frequently filtered fields like bankName and paymentInstruments so the DB query remains fast.
2. **Read Replicas:**
   If traffic grows further, add read replicas for the database.
   Since the GET endpoint only reads, you can distribute read load across replicas to reduce pressure on the primary DB.
3. **Caching Layer:**
   For frequently repeated lookups with the same bankName + paymentInstrument + amountToPay (e.g., common combinations), add an in-memory cache like Redis or use Spring’s built-in **@Cacheable**.
   This reduces redundant DB hits for the same queries.
4. **Horizontal Scaling:**
   Deploy multiple instances of the backend service behind a load balancer (e.g., NGINX, AWS ELB).
   Spring Boot apps are stateless for this purpose — so they can handle traffic spikes linearly by adding more pods/containers.

--- 
## ✅ Future Improvement

If I had more time to work on this assignment, I would improve it by:

- Add unit and integration tests for parsing logic and APIs.
- Will add comment in the application for better understanding.
- Improve summary parsing to handle more edge cases robustly.
- Use **@Valid** request validation for clean input checks.
- Expand exception handling for DB errors and JSON parse issues.
- Add Swagger for easy testing and documentation.
- Add caching for hot queries.

---

## ✅ Sample Flipkart Payload

```json
{
  "flipkartOfferApiResponse": {
    "title": "Partner offers",
    "offers": [
      {
        "adjustment_type": "INSTANT_DISCOUNT",
        "adjustment_id": "FPO250717182707WJBDJ",
        "summary": "Additional ₹1500 Off On Credit and Debit Card Transactions",
        "contributors": {
          "payment_instrument": ["CREDIT", "EMI_OPTIONS"],
          "banks": ["HDFC", "ICICI", "SBI"],
          "emi_months": ["3", "6"]
        }
      }
    ]
  }
}
```

---

## ✅ Assumptions

- This service expects Flipkart-style payment offer payloads.
- Fallback summary text parsing is used if explicit values are missing.
- The `application.properties` file is not pushed — developers configure their own.

---

## ✅ Development Branch
Development happens under a separate branch (e.g., `feature`).

---

## ✅ Author
Khushi Dokwal
[GitHub](https://github.com/khushusharma)





