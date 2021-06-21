package General_Purpose_IDE.Packages;

import General_Purpose_IDE.Exceptions.ConversionException;
import General_Purpose_IDE.Exceptions.ReservedNameException;
import General_Purpose_IDE.Exceptions.UnsupportedTypeException;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class Parameter {
    final static List<String> legalParameterTypes = Arrays.asList(new String[]
            {"int", "java.lang.String", "float", "boolean"});
    final static List<String> reservedKeywords = Arrays.asList(new String[]
            {"default"});


    final static String strArr = "java.lang.String[]";

    private String name;
    private String value;

    Parameter(String name) throws ReservedNameException {
        nameIsLegal(name);
        this.name = name;
        this.value = "";
    }

    Parameter(String name, String value) throws ReservedNameException {
        nameIsLegal(name);
        this.name = name;
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public Object getConvertedValue() {
        return convert(this.value);
    }

    public Type getValueType() {
        return getType(this);
    }

    static Object convert(String value, String typeName) throws ConversionException, NumberFormatException {
        Object obj = null;
        switch (legalParameterTypes.indexOf(typeName)) {
            //int
            case 0:
                obj = Integer.decode(value);
                break;
            //string
            case 1:
                obj = value;
                break;
            //float
            case 2:
                obj = Float.parseFloat(value);
                break;
            //boolean
            case 3:
                obj = Boolean.parseBoolean(value);
                break;
            default:
                throw new ConversionException("Type " + typeName + " is unsupported");
        }
        return obj;
    }

    /**
     * Convert a string to a variable type automatically.
     * This is achieved by attempting to convert the value from the most stringent type to the least stringent
     * Order:
     *  - int
     *  - float
     *  - boolean
     *  - string
     * @param value value to convert
     * @return the converted value, cast to an Object
     */
    static Object convert(String value) {

        //int
        try {
            return Integer.decode(value);
        } catch (Exception e) { }

        //float
        try {
            return Float.parseFloat(value);
        } catch (Exception e) { }

        //boolean
        if (value.toLowerCase().compareTo("true") == 0 || value.toLowerCase().compareTo("false") == 0) {
            return Boolean.parseBoolean(value);
        }

        //string
        return value;
    }

    static Type getType(Parameter p) {
        return getType(p.value);
    }


    static Type getType(String value) {
        //int
        try {
            Integer.decode(value);
            return int.class;
        } catch (Exception e) { }

        //float
        try {
            Float.parseFloat(value);
            return float.class;
        } catch (Exception e) { }

        //boolean
        if (value.toLowerCase().compareTo("true") == 0 || value.toLowerCase().compareTo("false") == 0) {
            return boolean.class;
        }

        //string
        return String.class;
    }

    private static void nameIsLegal(String name) throws ReservedNameException {
        for (String s : reservedKeywords) {
            if (s.toLowerCase().compareTo(name.toLowerCase()) == 0) {
                throw new ReservedNameException("Keyword \'" + name + "\' is reserved");
            }
        }
    }
}
