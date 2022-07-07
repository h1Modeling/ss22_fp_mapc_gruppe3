package de.feu.massim22.group3.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.TruthValue;
import massim.protocol.data.Thing;

public class PerceptUtil {

    public static <T extends Number> T toNumber(List<Parameter> parameters, int index, Class<T> type) {
        Parameter p = parameters.get(index);
        if (!(p instanceof Numeral))
            throw new IllegalArgumentException("Parameter is no Number");
        return type.cast(((Numeral) p).getValue());
    }

    public static String toStr(List<Parameter> parameters, int index) {
        Parameter p = parameters.get(index);
        if (!(p instanceof Identifier))
            throw new IllegalArgumentException("Parameter is no String");
        return (String) ((Identifier) p).getValue();
    }

    public static boolean toBool(List<Parameter> parameters, int index) {
        Parameter p = parameters.get(index);
        if (!(p instanceof TruthValue))
            throw new IllegalArgumentException("Parameter is no Bool");
        return ((TruthValue) p).getValue() == "true";
    }

    public static List<String> toStrList(List<Parameter> parameters, int index) {
        Parameter p = parameters.get(index);
        if (!(p instanceof ParameterList))
            throw new IllegalArgumentException();
        List<String> result = new ArrayList<>();
        for (Parameter para : (ParameterList) p) {
            if (!(para instanceof Identifier))
                throw new IllegalArgumentException();
            String s = (String) ((Identifier) para).getValue();
            result.add(s);
        }
        return result;
    }

    public static List<Integer> toIntList(List<Parameter> parameters, int index) {
        Parameter p = parameters.get(index);
        if (!(p instanceof ParameterList))
            throw new IllegalArgumentException();
        List<Integer> result = new ArrayList<>();
        for (Parameter para : (ParameterList) p) {
            if (!(para instanceof Numeral))
                throw new IllegalArgumentException();
            int s = (int) ((Numeral) para).getValue();
            result.add(s);
        }
        return result;
    }

    public static Set<Thing> toThingSet(List<Parameter> parameters, int index) {
        Parameter p = parameters.get(index);
        if (!(p instanceof ParameterList))
            throw new IllegalArgumentException();
        Set<Thing> result = new HashSet<>();
        for (Parameter para : (ParameterList) p) {
            if (!(para instanceof Function))
                throw new IllegalArgumentException();
            List<Parameter> funcParameter = ((Function) para).getParameters();
            int x = toNumber(funcParameter, 0, Integer.class);
            int y = toNumber(funcParameter, 1, Integer.class);
            String type = toStr(funcParameter, 2);
            String detail = funcParameter.size() > 3 ? toStr(funcParameter, 3) : "";
            result.add(new Thing(x, y, type, detail));
        }
        return result;
    }
}
