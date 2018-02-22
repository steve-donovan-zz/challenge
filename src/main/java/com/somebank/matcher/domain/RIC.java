package com.somebank.matcher.domain;

public final class RIC {
    private final String ric;

    public RIC(String ric) {
        this.ric = ric;
    }

    public String getRIC() {
        return ric;
    }

    @Override
    public int hashCode() {
        return ric.hashCode();
    }

    @Override
    public boolean equals(Object ric) {

        if (ric == null || !(ric instanceof RIC)) {
            return false;
        }

        return this.ric.equals(((RIC)ric).ric);
    }
}
