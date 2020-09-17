package kofiko.parsers

import kofiko.TextParser
import java.lang.reflect.Type

class StringParser : TextParser {
    override fun parse(textValue: String, targetType: Type): Any? {
        if (String::class.java == targetType)
            return textValue

        return null
    }
}

class IntParser : TextParser {
    override fun parse(textValue: String, targetType: Type): Any? {
        if (Int::class.java == targetType || targetType.typeName == java.lang.Integer::class.java.name)
            return Integer.parseInt(textValue)

        return null
    }
}

class LongParser : TextParser {
    override fun parse(textValue: String, targetType: Type): Any? {
        if (Long::class.java == targetType || targetType.typeName == java.lang.Long::class.java.name)
            return java.lang.Long.parseLong(textValue)

        return null
    }
}

class DoubleParser : TextParser {
    override fun parse(textValue: String, targetType: Type): Any? {
        if (Double::class.java == targetType || targetType.typeName == java.lang.Double::class.java.name)
            return java.lang.Double.parseDouble(textValue)

        return null
    }
}

class FloatParser : TextParser {
    override fun parse(textValue: String, targetType: Type): Any? {
        if (Float::class.java == targetType || targetType.typeName == java.lang.Float::class.java.name)
            return java.lang.Float.parseFloat(textValue)

        return null
    }
}

class ByteParser : TextParser {
    override fun parse(textValue: String, targetType: Type): Any? {
        if (Byte::class.java == targetType || targetType.typeName == java.lang.Byte::class.java.name)
            return java.lang.Byte.parseByte(textValue)

        return null
    }
}

class ShortParser : TextParser {
    override fun parse(textValue: String, targetType: Type): Any? {
        if (Short::class.java == targetType || targetType.typeName == java.lang.Short::class.java.name)
            return java.lang.Short.parseShort(textValue)

        return null
    }
}
