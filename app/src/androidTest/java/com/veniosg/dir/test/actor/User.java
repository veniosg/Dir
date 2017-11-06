package com.veniosg.dir.test.actor;

import com.veniosg.dir.test.actor.action.SelectsActions;
import com.veniosg.dir.test.actor.action.TypesActions;
import com.veniosg.dir.test.actor.assertion.CannotSeeAssertions;
import com.veniosg.dir.test.actor.assertion.SeesAssertions;

public class User {
    public User() {}

    private final SeesAssertions sees = new SeesAssertions();
    private final CannotSeeAssertions cannotSeeAssertions = new CannotSeeAssertions();
    private final SelectsActions selects = new SelectsActions();
    private final TypesActions types = new TypesActions();

    public SeesAssertions sees() {
        return sees;
    }

    public SelectsActions selects() {
        return selects;
    }

    public CannotSeeAssertions cannotSee() {
        return cannotSeeAssertions;
    }

    public TypesActions types() {
        return types;
    }
}
