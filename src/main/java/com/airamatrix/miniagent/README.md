
---

### System Overview & Execution Flow

1. **Initialization & Evaluation (`Main.java`)**
* **Initialize the Brain (LLM):** Set up the primary LLM instance and perform a baseline evaluation by comparing generated outputs against expected test results.
* **Boot Active Instance:** Upon successful evaluation, initialize the operational `Brain` instance to handle live traffic.


2. **Core Logic (`Brain.java`)**
* Maps incoming message content to appropriate actions (e.g., classifying *angry customer feedback* as an escalation or routing a *refund request* for human approval).


3. **Agent Query Processing & Guardrails**
* **Query Ingestion:** The agent processes real customer queries sequentially.
* **Input Validation & Safety:**
* Intercepts invalid or unsafe inputs (e.g., blank messages, prompt injection attempts like *"ignore all previous instructions"*, or system prompt leaks).
* Raises explicit violations and halts execution if an input check fails.


* **Output Guardrails:**
* Enforces domain rules on generated actions before execution (e.g., ensuring a requested refund amount does not exceed the original order value).





---
