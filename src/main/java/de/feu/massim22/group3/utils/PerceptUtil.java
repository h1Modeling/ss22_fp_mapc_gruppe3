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
import eis.iilang.Percept;
import eis.iilang.TruthValue;
import massim.protocol.data.Thing;
import java.awt.Point;

/**
 * The Class <code>PerceptUtil</code> contains static methods to serialize and deserialize <code>Percepts</code>.
 *
 * @author Heinz Stadler
 */
public class PerceptUtil {

    /**
     * Gets a numeric value from a list of percept parameters.
     * 
     * @param <T> the type of the numeric value
     * @param parameters the percept parameters
     * @param index the index of the parameter in the parameter list
     * @param type the class of the type of the numeric value
     * @return the numeric value
     */
    public static <T extends Number> T toNumber(List<Parameter> parameters, int index, Class<T> type) {
        Parameter p = parameters.get(index);
        if (!(p instanceof Numeral))
            throw new IllegalArgumentException("Parameter is no Number");
        return type.cast(((Numeral) p).getValue());
    }

    /**
     * Gets a string value form a list of percept parameters.
     * 
     * @param parameters the percept parameters
     * @param index the index of the parameter in the parameter list
     * @return the string value
     */
    public static String toStr(List<Parameter> parameters, int index) {
        Parameter p = parameters.get(index);
        if (!(p instanceof Identifier))
            throw new IllegalArgumentException("Parameter is no String");
        return (String) ((Identifier) p).getValue();
    }

    /**
     * Gets a boolean value from a list of percept parameters.
     * 
     * @param parameters the percept parameters
     * @param index the index of the parameter in the parameter list
     * @return the boolean value
     */
    public static boolean toBool(List<Parameter> parameters, int index) {
        Parameter p = parameters.get(index);
        if (!(p instanceof TruthValue))
            throw new IllegalArgumentException("Parameter is no Bool");
        return ((TruthValue) p).getValue() == "true";
    }

    /**
     * Gets a list of strings from a list of percept parameters.
     * 
     * @param parameters the percept parameters
     * @param index the index of the parameter in the parameter list
     * @return the list of strings
     */
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

    /**
     * Gets a list of integers from a list of percept parameters.
     * 
     * @param parameters the percept parameters
     * @param index the index of the parameter in the parameter list
     * @return the list of integers
     */
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

    /**
     * Gets a Set of Things from a list of percept parameters.
     * 
     * @param parameters the percept parameters
     * @param index the index of the parameter in the parameter list
     * @return the Set of Things
     */
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

    /**
     * Creates a Percept from a Thing
     * @param t the Thing
     * @return the Percept
     */
    public static Percept fromThing(Thing t) {
        return new Percept("thing", new Numeral(t.x), new Numeral(t.y), new Identifier(t.type), new Identifier(t.details));
    }

    /**
     * Creates a Percept from a Point
     * @param p the Point
     * @return the Percept
     */
    public static Percept fromAttachedPoint(Point p) {
        return new Percept("attached", new Numeral(p.x), new Numeral(p.y));
    }
}
