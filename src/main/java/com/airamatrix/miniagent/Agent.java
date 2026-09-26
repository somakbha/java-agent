package com.airamatrix.miniagent;

import java.util.List;
import java.util.Set;

/**
 * AGENT
 * -----
 * The orchestrator. One call - {@link #handle} - runs the whole pipeline for
 * one customer request:
 *
 *   input guardrails -> brain (propose) -> output guardrails -> human gate -> done
 *                    \____________________ every step traced ____________________/
 *
 * Nothing here calls a model directly and nothing here writes/acts on its own
 * behalf beyond returning the final decision - same separation of concerns as
 * day4/lab5-1's pipeline.py (agents propose, plain code + a human decide).
 */
public final class Agent {

    /** The only actions this agent may ever propose. Anything else is refused, not "mostly done". */
    public static final Set<String> ALLOWED_ACTIONS = Set.of("reply", "refund", "escalate", "none");

    /** Final outcome of one handle() call, kept simple enough to print or assert on in tests. */
    public record Outcome(String status, ActionProposal proposal, List<Guardrails.Violation> violations) {
        public static Outcome blocked(List<Guardrails.Violation> v) {
            return new Outcome("blocked", null, v);
        }
        public static Outcome approved(ActionProposal p) {
            return new Outcome("approved", p, List.of());
        }
        public static Outcome rejected(ActionProposal p) {
            return new Outcome("rejected", p, List.of());
        }
    }

    private final Brain brain;
    private final HumanApproval.Approver approver;

    public Agent(Brain brain, HumanApproval.Approver approver) {
        this.brain = brain;
        this.approver = approver;
    }

    public Outcome handle(CustomerRequest req, Trace trace) {
        trace.event("request.received", "customerId", req.customerId(), "message", req.message());

        // 1) INPUT GUARDRAILS - block before the brain ever sees a bad request.
        List<Guardrails.Violation> inViolations = Guardrails.input(req);
        if (!inViolations.isEmpty()) {
            trace.event("guardrail.input.blocked", "violations", inViolations);
            return Outcome.blocked(inViolations);
        }
        trace.event("guardrail.input.passed");

        // 2) BRAIN - propose exactly one action, as data.
        ActionProposal proposal = brain.propose(req);
        trace.event("brain.proposed", "action", proposal.action(), "reason", proposal.reason(),
                "refundCents", proposal.refundCents(), "confidence", proposal.confidence());

        // 3) OUTPUT GUARDRAILS - block before the proposal can reach the human gate.
        List<Guardrails.Violation> outViolations = Guardrails.output(req, proposal);
        if (!outViolations.isEmpty()) {
            trace.event("guardrail.output.blocked", "violations", outViolations);
            return Outcome.blocked(outViolations);
        }
        trace.event("guardrail.output.passed");

        // 4) HUMAN GATE - skip only for actions small enough to auto-approve.
        if (Guardrails.autoApprovable(proposal)) {
            trace.event("gate.auto_approved", "reason", "no money moves or amount under the auto-approve limit");
            return Outcome.approved(proposal);
        }
        boolean approved = approver.approve(req, proposal);
        trace.event(approved ? "gate.human_approved" : "gate.human_rejected");
        return approved ? Outcome.approved(proposal) : Outcome.rejected(proposal);
    }
}
