in Main.java

1. initilizes the brain (the LLM) and evaluate it by comparing the ouput provided by brain and expected output

2. Once successful, initialize the Brain.

3. Brain contains the logic on what action to be proposed based on message content like

angry=>escalation
refund => go for human approval

4. Start providing agent with real customer queries

5. The agent process the queries one by one and enable input violations like if the message is blank or contains some injection markers like
ignore all previous, system prompt

6. if the input validation fails, throw violations

7. Also check the output validation like refund is more than ordered amount