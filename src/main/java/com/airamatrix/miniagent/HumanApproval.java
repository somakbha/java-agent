package com.airamatrix.miniagent;

import java.util.function.Predicate;

/**
 * HUMAN APPROVAL
 * --------------
 * The gate. Any proposal that Guardrails.autoApprovable() does not clear
 * must be approved by a human before Agent will act on it - the agent
 * itself never approves its own action. In a real system this would be a
 * queue a person reads from; here it is a pluggable {@link Approver} so the
 * lab can run non-interactively (auto-approve/deny for demos and tests) or
 * interactively (read a y/n from the console).
 */
public final class HumanApproval {

    /** Something that can approve or reject a proposal. Implement this to plug in a real human. */
    public interface Approver {
        boolean approve(CustomerRequest req, ActionProposal proposal);
    }

    /** Approves everything - only for demos/tests. Never wire this into anything real. */
    public static final Approver AUTO_APPROVE = (req, p) -> true;

    /** Approves based on a caller-supplied rule - handy for tests that need a specific answer. */
    public static Approver rule(Predicate<ActionProposal> rule) {
        return (req, p) -> rule.test(p);
    }

    /** Reads one y/n line from the console - the "a real person is deciding" path. */
    public static final Approver CONSOLE = (req, p) -> {
        System.out.println("APPROVAL NEEDED for customer " + req.customerId() + ":");
        System.out.println("  action=" + p.action() + " reason=" + p.reason()
                + " refundCents=" + p.refundCents() + " confidence=" + p.confidence());
        System.out.print("approve? [y/N]: ");
        System.out.flush();
        String line = new java.util.Scanner(System.in).nextLine().trim().toLowerCase();
        return line.equals("y") || line.equals("yes");
    };

    private HumanApproval() {}
}
