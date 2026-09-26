package com.airamatrix.miniagent;

/**
 * ACTION PROPOSAL
 * ---------------
 * What the "brain" (see {@link Brain}) hands back for one request: a single
 * proposed action plus the data a human or guardrail needs to judge it. This
 * is the contract between the brain and everything downstream - guardrails,
 * eval and the human gate all read this shape, never free text.
 *
 * action      one of Agent.ALLOWED_ACTIONS ("reply", "refund", "escalate", "none")
 * reason      short human-readable justification, shown to the approver
 * refundCents only meaningful for action=refund; 0 otherwise
 * confidence  0.0-1.0, how sure the brain is
 */
public record ActionProposal(String action, String reason, long refundCents, double confidence) {
}
