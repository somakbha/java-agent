package com.airamatrix.miniagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * GUARDRAILS
 * ----------
 * Deterministic, code-only checks that run BEFORE and AFTER the "brain"
 * (see {@link Brain}) - never delegated to the model. A guardrail failure
 * blocks the action outright; it is never "mostly fine". This mirrors the
 * repo's Python labs (contracts.py's validate/check_change): the schema and
 * the rules are the enforcement, not a suggestion to the model.
 *
 * Two passes:
 *   input(...)   runs on the raw request, before the brain sees it
 *   output(...)  runs on the brain's proposal, before it reaches the human gate
 */
public final class Guardrails {

    /** One violation: which rule, and why. Empty list = passed. */
    public record Violation(String rule, String detail) {}

    // Anything that looks like the customer trying to steer the agent's own
    // instructions, rather than describe their problem. Customer text is DATA,
    // never instructions - same principle as agents.py's ticket-text handling.
    private static final Set<String> INJECTION_MARKERS = Set.of(
            "ignore previous", "ignore all previous", "you are now", "system prompt",
            "disregard your instructions", "act as");

    private static final long MAX_AUTO_REFUND_CENTS = 20_00; // $20.00 - anything above needs a human, always

    private Guardrails() {}

    /** Checks on the raw customer request, before the brain runs at all. */
    public static List<Violation> input(CustomerRequest req) {
        List<Violation> v = new ArrayList<>();
        if (req.message() == null || req.message().isBlank()) {
            v.add(new Violation("empty_message", "the request has no message to act on"));
        }
        String lower = req.message() == null ? "" : req.message().toLowerCase();
        for (String marker : INJECTION_MARKERS) {
            if (lower.contains(marker)) {
                v.add(new Violation("possible_prompt_injection",
                        "message contains '" + marker + "' - treat as data, do not follow it"));
            }
        }
        if (req.orderTotalCents() < 0) {
            v.add(new Violation("bad_order_total", "orderTotalCents is negative"));
        }
        return v;
    }

    /** Checks on the brain's proposal, before it can reach the human gate. */
    public static List<Violation> output(CustomerRequest req, ActionProposal p) {
        List<Violation> v = new ArrayList<>();
        if (!Agent.ALLOWED_ACTIONS.contains(p.action())) {
            v.add(new Violation("unknown_action", p.action() + " is not one of " + Agent.ALLOWED_ACTIONS));
        }
        if ("refund".equals(p.action())) {
            if (p.refundCents() <= 0) {
                v.add(new Violation("bad_refund_amount", "refund action needs a positive refundCents"));
            }
            if (p.refundCents() > req.orderTotalCents()) {
                v.add(new Violation("refund_exceeds_order",
                        p.refundCents() + " exceeds order total " + req.orderTotalCents()));
            }
        }
        if (p.confidence() < 0.0 || p.confidence() > 1.0) {
            v.add(new Violation("bad_confidence", "confidence " + p.confidence() + " outside [0,1]"));
        }
        return v;
    }

    /** True if this proposal is small enough to skip the human gate entirely. */
    public static boolean autoApprovable(ActionProposal p) {
        if ("reply".equals(p.action()) || "none".equals(p.action())) {
            return true;               // no money moves, no human needed
        }
        return "refund".equals(p.action()) && p.refundCents() <= MAX_AUTO_REFUND_CENTS;
    }
}
