package com.sos.joc.classes.filters;

import java.util.List;
import java.util.regex.Pattern;

public class FilterAfterResponse {
    
    public static boolean matchRegex(String regex, String path) {
        if (regex == null || regex.isEmpty()) {
            return true;
        }
        if (path == null) {
            return false;
        }
        return Pattern.compile(regex).matcher(path).find();
    }
    
    public static boolean filterStateHasState(List<? extends Enum<?>> filterStates, Enum<?> state) {
        if (filterStates == null || filterStates.isEmpty()) {
            return true;
        }
        boolean filterStatesContainsState = false;
        for (Enum<?> filterState : filterStates) {
           if (filterState.name().equals(state.name())) {
               filterStatesContainsState = true;
               break;
           }
        }
        return filterStatesContainsState;
    }
}
