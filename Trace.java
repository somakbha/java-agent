package com.airamatrix.miniagent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TRACE
 * -----
 * The smallest possible tracer: every step the agent takes (guardrail check,
 * decision, eval, approval, action) is recorded as one timestamped event with
 * a name and a bag of fields. Events are printed as they happen (so you see
 * the run live) AND kept in memory so the full trace can be replayed/printed
 * at the end - the same "every hand-off is inspectable" idea used elsewhere
 * in this repo's Python labs, just small enough to read in one file.
 */
public final class Trace {

    /** One traced event: when, what, and whatever fields the caller attached. */
    public record Event(String at, String name, Map<String, Object> fields) {
        @Override
        public String toString() {
            return "[" + at + "] " + name + " " + fields;
        }
    }

    private final String runId;
    private final List<Event> events = new ArrayList<>();

    public Trace(String runId) {
        this.runId = runId;
    }

    /** Record one event now, print it immediately, and keep it for later. */
    public void event(String name, Object... kv) {
        Map<String, Object> fields = new LinkedHashMap<>();
        for (int i = 0; i + 1 < kv.length; i += 2) {
            fields.put(String.valueOf(kv[i]), kv[i + 1]);
        }
        Event e = new Event(Instant.now().toString(), name, fields);
        events.add(e);
        System.out.println("trace(" + runId + ") " + e);
    }

    /** The full ordered list of events for this run - e.g. to show a human before they approve. */
    public List<Event> events() {
        return List.copyOf(events);
    }

    public String runId() {
        return runId;
    }
}
