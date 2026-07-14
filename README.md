# PDS Enterprise Centralized Storage Terminal & Secure Ledger Pipeline

A modern, thread-safe Java Swing desktop application designed for managing Public Distribution System (PDS) commodity allocations, verifying beneficiary quotas based on card tiers, and maintaining immutable audit ledgers.

## Features

* **Modern UI Architecture:** Features a sleek Nimbus-inspired look and feel with high-contrast UI component archetypes (Slate 900 & Indigo accents).
* **Thread-Safe Core Engine:** Uses `ReentrantReadWriteLock` mechanisms (`ReadWriteLock`) to ensure concurrent safety across distributed inventory modifications.
* **Dynamic Subsidy Pricing Matrix:** Implements strategy pattern variants via hierarchical enum card tiers (`AYY`, `PHH`, `NPHH`) to accurately dictate localized regulatory values.
* **Automatic Quota Enforcement:** Prevents illegal system states like warehouse deficit stock allocation or maximum category quota distribution via custom exceptions.
* **Virtual Thermal Buffer Terminal:** Includes an integrated digital print stream to visualize physical invoice layouts dynamically before updating system metrics.

## Prerequisites

* **Java Development Kit (JDK):** Version 8 or higher.
* **IDE:** IntelliJ IDEA, Eclipse, NetBeans, or a simple command-line interface.

## Installation & Execution

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/thejatheja537-alt/pds-billing-system.git](https://github.com/thejatheja537-alt/pds-billing-system.git)
   cd pds-billing-system
