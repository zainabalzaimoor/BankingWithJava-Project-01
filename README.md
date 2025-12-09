# Banking With Java-Project-01
User Stories Link: https://zainabalzaimoor.atlassian.net/jira/software/projects/SCRUM/boards/1
## 📄 Project Overview

The goal of this project is to design a file-based database for **ACME Bank** and implement a complete Java program that meets the following functional requirements:

- **User management**: Abstract `User` class with `Admin` and `Customer` subclasses  
- **Account management**: Abstract `Account` class with `BankingAccount` and `SavingAccount` subclasses  
- **Debit cards**: `DebitCard` interface implemented by `MasterCard`, `MasterPlatinum`, and `MasterTitan`  
- **Core banking operations**: Deposits, withdrawals (including overdraft handling), transfers between accounts  
- **Account status handling**: Automatic deactivation/reactivation based on overdraft and deposits  
- **File database**: Persist user and account data using file handling  
- **Unit testing**: Validate core functionality with JUnit tests

Text-based ERD:
    +-----------------+
    |     User        |<<abstract>>
    +-----------------+
    | userId          |
    | username        |
    | password        |
    +-----------------+
       ^         ^
       |         |
  +--------+  +---------+
  | Admin  |  | Customer|
  +--------+  +---------+
       |           |
       +-----------+
               |
    +-----------------+
    |     Account     |<<abstract>>
    +-----------------+
    | accountNumber   |
    | balance         |
    | isActive        |
    +-----------------+
       ^         ^
       |         |

