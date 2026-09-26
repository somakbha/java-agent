package com.airamatrix.miniagent;

import java.util.ArrayList;
import java.util.List;

/**
 * EVAL
 * ----
 * Grades a finished proposal against a fixed set of test cases with a known
 * right answer - deterministic, no model in the loop, run offline or in CI
 * to catch a regression before it reaches a real customer. This is the same
 * idea as day4/lab5-2's graders.py, shrunk to fit one file: define cases,
 * run the agent's brain on each, compare, report a pass rate.
 */
public final class Eval {

    /** One labelled example: an input and the action we expect for it. */
    public record Case(String name, CustomerRequest input, String expectedAction) {}

    /** The verdict for one case. */
    public record CaseResult(Case testCase, ActionProposal actual, boolean passed) {}

    private static final List<Case> CASES = List.of(
            new Case("refund_request",
                    new CustomerRequest("cust-1", "I want a refund, this is broken", 10_00), "refund"),
            new Case("angry_customer",
                    new CustomerRequest("cust-2", "I am furious, get me your manager", 0), "escalate"),
            new Case("plain_question",
                    new CustomerRequest("cust-3", "When will my order ship?", 0), "reply"),
            new Case("empty_message",
                    new CustomerRequest("cust-4", "", 0), "none")
    );

    private Eval() {}

    /** Run every case through a fresh Brain and report pass/fail per case. */
    public static List<CaseResult> run(Brain brain) {
        List<CaseResult> results = new ArrayList<>();
        for (Case c : CASES) {
            ActionProposal actual = brain.propose(c.input());
            boolean passed = c.expectedAction().equals(actual.action());
            results.add(new CaseResult(c, actual, passed));
        }
        return results;
    }

    /** Print a short report; returns true only if every case passed (useful as a CI gate). */
    public static boolean report(List<CaseResult> results) {
        long passed = results.stream().filter(CaseResult::passed).count();
        System.out.println("eval: " + passed + "/" + results.size() + " cases passed");
        for (CaseResult r : results) {
            String mark = r.passed() ? "PASS" : "FAIL";
            System.out.println("  [" + mark + "] " + r.testCase().name() + " expected="
                    + r.testCase().expectedAction() + " actual=" + r.actual().action());
        }
        return passed == results.size();
    }
}
