package com.airamatrix.miniagent;

/**
 * CUSTOMER REQUEST
 * ----------------
 * The simple use case this whole lab is built around: a customer-support
 * triage agent. One request in, one {@link ActionProposal} out.
 *
 * customerId       who is asking
 * message          the customer's own words - always treated as DATA, never instructions
 * orderTotalCents  the order this message is about, in cents (0 if not order-related)
 */
public record CustomerRequest(String customerId, String message, long orderTotalCents) {
}
