package com.typesafe.config.impl;

import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigOrigin;
import com.typesafe.config.ConfigValueType;

final class Tokens {
    static private class Value extends Token {

        private AbstractConfigValue value;

        Value(AbstractConfigValue value) {
            super(TokenType.VALUE);
            this.value = value;
        }

        AbstractConfigValue value() {
            return value;
        }

        @Override
        public String toString() {
            String s = tokenType().name() + "(" + value.valueType().name()
                    + ")";
            if (value instanceof ConfigObject || value instanceof ConfigList) {
                return s;
            } else {
                return s + "='" + value().unwrapped() + "'";
            }
        }

        @Override
        protected boolean canEqual(Object other) {
            return other instanceof Value;
        }

        @Override
        public boolean equals(Object other) {
            return super.equals(other) && ((Value) other).value.equals(value);
        }

        @Override
        public int hashCode() {
            return 41 * (41 + super.hashCode()) + value.hashCode();
        }
    }

    static private class Line extends Token {
        private int lineNumber;

        Line(int lineNumber) {
            super(TokenType.NEWLINE);
            this.lineNumber = lineNumber;
        }

        int lineNumber() {
            return lineNumber;
        }

        @Override
        public String toString() {
            return "NEWLINE@" + lineNumber;
        }

        @Override
        protected boolean canEqual(Object other) {
            return other instanceof Line;
        }

        @Override
        public boolean equals(Object other) {
            return super.equals(other)
                    && ((Line) other).lineNumber == lineNumber;
        }

        @Override
        public int hashCode() {
            return 41 * (41 + super.hashCode()) + lineNumber;
        }
    }

    // This is not a Value, because it requires special processing
    static private class UnquotedText extends Token {
        private ConfigOrigin origin;
        private String value;

        UnquotedText(ConfigOrigin origin, String s) {
            super(TokenType.UNQUOTED_TEXT);
            this.origin = origin;
            this.value = s;
        }

        ConfigOrigin origin() {
            return origin;
        }

        String value() {
            return value;
        }

        @Override
        public String toString() {
            return tokenType().name() + "(" + value + ")";
        }

        @Override
        protected boolean canEqual(Object other) {
            return other instanceof UnquotedText;
        }

        @Override
        public boolean equals(Object other) {
            return super.equals(other)
                    && ((UnquotedText) other).value.equals(value);
        }

        @Override
        public int hashCode() {
            return 41 * (41 + super.hashCode()) + value.hashCode();
        }
    }

    // This is not a Value, because it requires special processing
    static private class Substitution extends Token {
        private ConfigOrigin origin;
        private String value;
        private boolean isPath;

        Substitution(ConfigOrigin origin, String s, SubstitutionStyle style) {
            super(TokenType.SUBSTITUTION);
            this.origin = origin;
            this.value = s;
            // if the string is not quoted and contains '.' then
            // it's a path rather than just a key name.

            this.isPath = style == SubstitutionStyle.PATH;
        }

        ConfigOrigin origin() {
            return origin;
        }

        String value() {
            return value;
        }

        boolean isPath() {
            return isPath;
        }

        @Override
        public String toString() {
            return tokenType().name() + "(" + value + ",isPath=" + isPath
                    + ")";
        }

        @Override
        protected boolean canEqual(Object other) {
            return other instanceof Substitution;
        }

        @Override
        public boolean equals(Object other) {
            return super.equals(other)
                    && ((Substitution) other).value.equals(value)
                    && ((Substitution) other).isPath() == this.isPath();
        }

        @Override
        public int hashCode() {
            return 41 * (41 * (41 + super.hashCode()) + value.hashCode())
                    + Boolean.valueOf(isPath()).hashCode();
        }
    }

    static boolean isValue(Token token) {
        return token instanceof Value;
    }

    static AbstractConfigValue getValue(Token token) {
        if (token instanceof Value) {
            return ((Value) token).value();
        } else {
            throw new ConfigException.BugOrBroken(
                    "tried to get value of non-value token " + token);
        }
    }

    static boolean isValueWithType(Token t, ConfigValueType valueType) {
        return isValue(t) && getValue(t).valueType() == valueType;
    }

    static boolean isNewline(Token token) {
        return token instanceof Line;
    }

    static int getLineNumber(Token token) {
        if (token instanceof Line) {
            return ((Line) token).lineNumber();
        } else {
            throw new ConfigException.BugOrBroken(
                    "tried to get line number from non-newline " + token);
        }
    }

    static boolean isUnquotedText(Token token) {
        return token instanceof UnquotedText;
    }

    static String getUnquotedText(Token token) {
        if (token instanceof UnquotedText) {
            return ((UnquotedText) token).value();
        } else {
            throw new ConfigException.BugOrBroken(
                    "tried to get unquoted text from " + token);
        }
    }

    static ConfigOrigin getUnquotedTextOrigin(Token token) {
        if (token instanceof UnquotedText) {
            return ((UnquotedText) token).origin();
        } else {
            throw new ConfigException.BugOrBroken(
                    "tried to get unquoted text from " + token);
        }
    }

    static boolean isSubstitution(Token token) {
        return token instanceof Substitution;
    }

    static String getSubstitution(Token token) {
        if (token instanceof Substitution) {
            return ((Substitution) token).value();
        } else {
            throw new ConfigException.BugOrBroken(
                    "tried to get substitution from " + token);
        }
    }

    static ConfigOrigin getSubstitutionOrigin(Token token) {
        if (token instanceof Substitution) {
            return ((Substitution) token).origin();
        } else {
            throw new ConfigException.BugOrBroken(
                    "tried to get substitution origin from " + token);
        }
    }

    static SubstitutionStyle getSubstitutionStyle(Token token) {
        if (token instanceof Substitution) {
            return ((Substitution) token).isPath() ? SubstitutionStyle.PATH
                    : SubstitutionStyle.KEY;
        } else {
            throw new ConfigException.BugOrBroken(
                    "tried to get substitution style from " + token);
        }
    }

    static Token START = new Token(TokenType.START);
    static Token END = new Token(TokenType.END);
    static Token COMMA = new Token(TokenType.COMMA);
    static Token COLON = new Token(TokenType.COLON);
    static Token OPEN_CURLY = new Token(TokenType.OPEN_CURLY);
    static Token CLOSE_CURLY = new Token(TokenType.CLOSE_CURLY);
    static Token OPEN_SQUARE = new Token(TokenType.OPEN_SQUARE);
    static Token CLOSE_SQUARE = new Token(TokenType.CLOSE_SQUARE);

    static Token newLine(int lineNumberJustEnded) {
        return new Line(lineNumberJustEnded);
    }

    static Token newUnquotedText(ConfigOrigin origin, String s) {
        return new UnquotedText(origin, s);
    }

    static Token newSubstitution(ConfigOrigin origin, String s,
            SubstitutionStyle style) {
        return new Substitution(origin, s, style);
    }

    static Token newValue(AbstractConfigValue value) {
        return new Value(value);
    }

    static Token newString(ConfigOrigin origin, String value) {
        return newValue(new ConfigString(origin, value));
    }

    static Token newInt(ConfigOrigin origin, int value) {
        return newValue(new ConfigInt(origin, value));
    }

    static Token newDouble(ConfigOrigin origin, double value) {
        return newValue(new ConfigDouble(origin, value));
    }

    static Token newLong(ConfigOrigin origin, long value) {
        return newValue(new ConfigLong(origin, value));
    }

    static Token newNull(ConfigOrigin origin) {
        return newValue(new ConfigNull(origin));
    }

    static Token newBoolean(ConfigOrigin origin, boolean value) {
        return newValue(new ConfigBoolean(origin, value));
    }
}
