# Labor Management Module Documentation

## Overview

The **Labor Management Module** handles the complete lifecycle of factory laborers, including profile management, daily attendance tracking, cash advances, and weekly payout processing. It supports multiple wage types and provides automated calculations for earned amounts and deductions.

---

## 1. Laborer Profiles

The system maintains profiles for all laborers. A critical attribute of each laborer is their **Wage Type**, which determines how their earnings are calculated.

### Wage Types 
| Wage Type      | Description                                                                                   |
|----------------|-----------------------------------------------------------------------------------------------|
| **HOURLY**     | Earnings are based on hours worked. The hourly rate is derived from a 8-hour daily wage base. |
| **PIECE_RATE** | Earnings are based on the number of pieces completed.                                         |
| **DAILY**      | Fixed rate per day (Standard base for hourly calculations).                                   |

---

## 2. Attendance Management

Attendance is logged daily for each laborer. The system automatically calculates the **Earned Amount** for each attendance record based on the laborer's wage type.

### Earned Amount Calculation Logic

#### For Hourly Workers:
1. **Hours Worked**: Calculated as the duration between `checkInTime` and `checkOutTime`.
2. **Hourly Rate**: `dailyWage / 8` (Assumes an 8-hour standard shift).
3. **Earned Amount**: `hoursWorked × hourlyRate`.

#### For Piece Rate Workers:
1. **Earned Amount**: `piecesCompleted × pieceRate`.

---

## 3. Advance Management

The module tracks cash advances (loans) given to laborers and handles their recovery during the payout process.

### Transactions
- **GIVEN**: Recorded when a laborer is granted a cash advance.
- **DEDUCTED**: Recorded when an advance is recovered (usually automatically during payout generation).

### Outstanding Balance
The system calculates the outstanding balance for any laborer as:
`Sum(GIVEN amounts) - Sum(DEDUCTED amounts)`

---

## 4. Payout Management

The system processes **Weekly Payouts** to consolidate earnings and settle advances.

### Weekly Payout Calculation
1. **Gross Payout**: Sum of `earnedAmount` from all attendance records within the selected week.
2. **Advance Deduction**:
    - The system checks the laborer's **Outstanding Advance Balance**.
    - **Deduction Amount** = `min(Gross Payout, Outstanding Balance)`.
    - This ensures that the system automatically recovers loans without making the net payout negative.
3. **Net Payout**: `Gross Payout - Advance Deduction`.

### Payout Status
- **PENDING**: Payout generated but not yet disbursed.
- **PAID**: Payout has been disbursed to the laborer with a payment date and reference number.

---

## 5. Reports & Analytics

The Labor module provides comprehensive financial and operational insights.

### Report Types
- **Weekly Report**: Aggregates expenses for a specific week (ISO Week format).
- **Monthly Report**: Summary of labor costs and hours for a calendar month.
- **Yearly Report**: Annual summary of labor expenditure.

### Report Contents
Each report provides the following data points:
- **Period**: The timeframe (e.g., "2026-W12", "2026-03").
- **Total Hours**: Cumulative hours worked by all laborers in the period.
- **Total Workers**: Distinct count of laborers who worked during the period.
- **Total Labor Cost**: The sum of gross payouts/earned amounts.

### Export Features
Administrators can export labor expense reports to **Excel (.xlsx)** format for further auditing and accounting.

---

## 6. Database Schema

### `laborers`
| Column     | Description                          |
|------------|--------------------------------------|
| id         | Primary Key                          |
| name       | Laborer's full name                  |
| wage_type  | HOURLY / PIECE_RATE / DAILY          |
| daily_wage | Base wage for hourly workers         |
| piece_rate | Rate per unit for piece-rate workers |

### `attendance`
| Column           | Description                     |
|------------------|---------------------------------|
| laborer_id       | FK to laborers                  |
| work_date        | Date of work                    |
| check_in_time    | Time in                         |
| check_out_time   | Time out                        |
| hours_worked     | Calculated duration             |
| pieces_completed | Units produced                  |
| earned_amount    | Calculated earnings for the day |

### `advance_transactions`
| Column           | Description         |
|------------------|---------------------|
| laborer_id       | FK to laborers      |
| transaction_date | Date of transaction |
| amount           | Transaction value   |
| transaction_type | GIVEN / DEDUCTED    |

### `weekly_payouts`
| Column            | Description                      |
|-------------------|----------------------------------|
| laborer_id        | FK to laborers                   |
| week_start_date   | Monday of the payout week        |
| week_end_date     | Sunday of the payout week        |
| gross_payout      | Total earnings before deductions |
| advance_deduction | Recovered loan amount            |
| net_payout        | Final payable amount             |
| payment_status    | PENDING / PAID                   |

---

## 7. API Endpoints

### Laborers
- `POST /api/labors`: Create laborer
- `GET /api/labors`: List all laborers

### Attendance
- `POST /api/attendance`: Log single attendance
- `POST /api/attendance/bulk`: Log attendance for multiple laborers at once

### Advances
- `POST /api/advances/grant`: Give advance to laborer
- `GET /api/advances/balance/{id}`: Get outstanding debt

### Payouts
- `POST /api/payouts/generate`: Generate weekly payout record
- `POST /api/payouts/{id}/disburse`: Mark payout as paid

### Reports
- `GET /api/labor-reports/weekly`: Weekly cost summary
- `GET /api/labor-reports/monthly`: Monthly cost summary
- `GET /api/labor-reports/export`: Export data to Excel
