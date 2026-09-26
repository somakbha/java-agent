package com.airamatrix.miniagent;

/**
 * BRAIN
 * -----
 * Stands in for "call an LLM". This lab is about the scaffolding around a
 * model (guardrails, eval, trace, human approval), not about wiring a real
 * API key - so this is a tiny deterministic, keyword-based policy that plays
 * the model's role: read the request, propose ONE action as structured data.
 *
 * Swap this class for a real call (e.g. the Claude API) without touching
 * anything else in the package: everything else only depends on the
 * {@link ActionProposal} shape, never on how it was produced.
 */
public final class Brain {

    public ActionProposal propose(CustomerRequest req) {
        String msg = req.message() == null ? "" : req.message().toLowerCase();

        if (msg.contains("refund") || msg.contains("money back")) {
            // Toy policy: offer half the order back, capped by the guardrail later anyway.
            long amount = req.orderTotalCents() / 2;
            return new ActionProposal("refund",
                    "customer asked for a refund; proposing half the order total pending approval",
                    amount, 0.75);
        }
        if (msg.contains("angry") || msg.contains("lawsuit") || msg.contains("manager")) {
            return new ActionProposal("escalate",
                    "message signals strong dissatisfaction; routing to a human agent", 0, 0.9);
        }
        if (msg.isBlank()) {
            return new ActionProposal("none", "nothing to act on", 0, 1.0);
        }
        return new ActionProposal("reply",
                "no refund or escalation signal found; a templated helpful reply is enough", 0, 0.6);
    }
}
