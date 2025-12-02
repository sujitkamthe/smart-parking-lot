# The Smart Parking Lot Rate Calculator

## 1. The Scenario

You are designing the billing engine for a smart, multi-level parking complex. The lot serves various types of vehicles and offers dynamic pricing based on the time of day, the day of the week, and the duration of the stay.

The management wants a system that is completely automated: a driver inserts their ticket upon exit, and the system calculates the final fee. The complexity lies in the fact that multiple rate policies might apply to a single stay, and the system must always calculate the **lowest possible valid fare** for the customer to ensure transactional integrity and satisfaction.

## 2. Business Requirements

### The Domain and Pricing Basis

The parking facility operates under specific constraints detailed below:

- **Vehicle Classification:** The system accommodates three distinct vehicle types: Motorcycles, Cars, and Buses.

- **Spots:** Different spots accommodate different vehicles (e.g., Compact spots for Motorcycles and Cars, Large for Buses).

- **Tickets:** Issued upon entry, serving as the foundational record and bearing a precise timestamp of entry.


### Vehicle Rate Multipliers

All base rates below are defined relative to the **CAR** rate. All other vehicle classes are subject to a corresponding multiplier applied to the base rate:

|**Vehicle Type**|**Multiplier**|
|---|---|
|Motorcycle|0.8x|
|Car|1.0x|
|Bus|2.0x|

### Pricing Policies (Based on Car Rate)

The system must accurately implement the following active rate policies. Note that **Policies 2 and 3** share underlying dependency on time-window validation and loyalty discount application logic, which should inform the architecture for reusable components:

1. **Standard Hourly Rate with Peak Hour Surcharge:** This policy applies a progressive rate based on duration, incorporating time-of-day multipliers.

    - **Base Car Rate:** The fee structure is established as $5.00 for the initial hour, $3.00 for the subsequent second hour, and $2.00 for every hour thereafter.

    - **Peak Hour Surcharge:** Hours that overlap, even partially, with designated peak periods (7:00 AM – 10:00 AM OR 4:00 PM – 7:00 PM on weekdays) shall incur a **1.5x multiplier** applied to that specific hour's calculated rate.

    - **Calculation Protocol:** Total parking duration shall be consistently rounded upward to the nearest full hour. Each resulting hourly segment must be individually assessed for peak time overlap.

2. **Early Bird Special:** A flat rate designated for commuter traffic, subject to loyalty discounts.

    - **Applicability Condition:** Requires vehicle entry between 6:00 AM – 9:00 AM AND vehicle exit between 3:30 PM – 7:00 PM on the **same calendar day**.

    - **Duration Constraint:** The total parking session must not exceed 15 hours.

    - **Base Car Rate:** A flat fee of **$15.00**.

    - **Loyalty Discount Application:** The base rate is reduced based on the customer's loyalty tier:

   | Tier | Discount |
       |---|---|
   | NONE | 0% | 
   | SILVER | 10% |
   | GOLD | 20% |
   | PLATINUM | 30% |


3. **Night Owl Special:** A flat rate designated for overnight parking, subject to loyalty discounts.

    - **Applicability Condition:** Requires vehicle entry between 6:00 PM – 11:59 PM AND vehicle exit between 5:00 AM – 10:00 AM on the **next consecutive calendar day**.

    - **Duration Constraint:** The total parking session must not exceed 18 hours.

    - **Base Car Rate:** A flat fee of **$8.00**.

    - **Loyalty Discount Application:** Uses the identical percentage structure defined in the Early Bird Special policy.


### Conflict Resolution: Best Value Logic

A key functional requirement is the resolution of scenarios where a single parking session satisfies the criteria for multiple simultaneous rates.

- _Scenario:_ A Car customer enters on a weekday at 6:30 AM and leaves at 4:00 PM. This transaction may meet the criteria for **Standard Hourly** and **Early Bird**.

- **Mandate:** The engine is required to perform an exhaustive evaluation of all valid and applicable rate policies (Policies 1, 2, and 3), select the one yielding the **minimum derived amount**, and apply that final charge.


### Ambiguity Resolution Constraint

To maintain system integrity and ensure predictable, deterministic outcomes, all developed solutions must adhere to the following behavioral constraint regarding extended parking sessions:

- _Critical Edge Case:_ If a vehicle stays for more than 24 hours, both the **Early Bird Special** and the **Night Owl Special** are automatically rendered **invalid**. The system must revert to evaluating the Standard Hourly Rate exclusively. These special rates are strictly restricted to single-day or consecutive overnight stays.