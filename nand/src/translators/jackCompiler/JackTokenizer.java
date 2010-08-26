package translators.jackCompiler;

import java.io.*;
import java.util.*;

/**
 * JackTokenizer object: Reads input from a reader and produces a stream of
 * tokens for the compiler.
 */
public class JackTokenizer {

    /**
     * Keyword token type
     */
    public static final int TYPE_KEYWORD		= 1;

    /**
     * Symbol token type
     */
    public static final int TYPE_SYMBOL			= 2;

    /**
     * Identifier token type
     */
    public static final int TYPE_IDENTIFIER		= 3;

    /**
     * Int constant token type
     */
    public static final int TYPE_INT_CONST		= 4;

    /**
     * String constant token type
     */
    public static final int TYPE_STRING_CONST	        = 5;

    // Keywords of the language

    /**
     * Class keyword
     */
    public static final int KW_CLASS           = 1;

    /**
     * Method keyword
     */
    public static final int KW_METHOD          = 2;

    /**
     * Function keyword
     */
    public static final int KW_FUNCTION        = 3;

    /**
     * Constructor keyword
     */
    public static final int KW_CONSTRUCTOR     = 4;

    /**
     * Int keyword
     */
    public static final int KW_INT             = 5;

    /**
     * Boolean keyword
     */
    public static final int KW_BOOLEAN         = 6;

    /**
     * Char keyword
     */
    public static final int KW_CHAR            = 7;

    /**
     * Void keyword
     */
    public static final int KW_VOID            = 8;

    /**
     * Var keyword
     */
    public static final int KW_VAR             = 9;

    /**
     * Static keyword
     */
    public static final int KW_STATIC          = 10;

    /**
     * Field keyword
     */
    public static final int KW_FIELD           = 11;

    /**
     * Let keyword
     */
    public static final int KW_LET             = 12;

    /**
     * Do keyword
     */
    public static final int KW_DO              = 13;

    /**
     * If keyword
     */
    public static final int KW_IF              = 14;

    /**
     * Else keyword
     */
    public static final int KW_ELSE            = 15;

    /**
     * While keyword
     */
    public static final int KW_WHILE           = 16;

    /**
     * Return keyword
     */
    public static final int KW_RETURN          = 17;

    /**
     * True keyword
     */
    public static final int KW_TRUE            = 18;

    /**
     * False keyword
     */
    public static final int KW_FALSE           = 19;

    /**
     * Null keyword
     */
    public static final int KW_NULL            = 20;

    /**
     * This keyword
     */
    public static final int KW_THIS            = 21;

    // The parser
    private StreamTokenizer parser;

    // Hashtable containing the keywords of the language
    private Hashtable keywords;

    // Hashtable containing the symbols of the language
    private Hashtable symbols;

    // The type of the current token
    private int tokenType;

    // The type of the current keyword
    private int keyWordType;

    // The current symbol
    private char symbol;

    // The current int value
    private int intValue;

	// The current line number
	private int lineNumber;

    // The current string value
    private String stringValue;

    // The current identifier
    private String identifier;

    /**
     * Constructs a new JackTokenizer with the given input Reader.
     * @param input The input Reader
     */
    public JackTokenizer(Reader input) {
        try {
            parser = new StreamTokenizer(input);
            parser.parseNumbers();
            parser.slashSlashComments(true);
            parser.slashStarComments(true);
            parser.ordinaryChar('.');
            parser.ordinaryChar('-');
            parser.ordinaryChar('<');
            parser.ordinaryChar('>');
            parser.ordinaryChar('/');
            parser.wordChars('_','_');
            parser.nextToken();
            initKeywords();
            initSymbols();
        } catch (IOException ioe) {
            System.out.println
                ("JackTokenizer failed during initialization operation");
            System.exit(-1);
        }
    }

    /**
     * Advances the parser to the next token
     * May only be called when hasMoreToken() == true
     */
    public void advance() {
        try {
            switch (parser.ttype) {
                case StreamTokenizer.TT_NUMBER:
                    tokenType = TYPE_INT_CONST;
                    intValue = (int)parser.nval;
                    break;
                case StreamTokenizer.TT_WORD:
                    String word = parser.sval;
                    if (keywords.containsKey(word)) {
                        tokenType = TYPE_KEYWORD;
                        keyWordType = ((Integer)keywords.get(word)).intValue();
                    }
                    else {
                        tokenType = TYPE_IDENTIFIER;
                        identifier = word;
                    }
                    break;
                case '"':
                    tokenType = TYPE_STRING_CONST;
                    stringValue = parser.sval;
                    break;
                default:
                    tokenType = TYPE_SYMBOL;
                    symbol = (char)parser.ttype;
                    break;
            }
        	lineNumber = parser.lineno();
            parser.nextToken();
        } catch (IOException ioe) {
            System.out.println
                ("JackTokenizer failed during advance operation");
            System.exit(-1);
        }
    }

    /**
     * Returns the current token type
     */
    public int getTokenType() {
        return tokenType;
    }

    /**
     * Returns the keyword type of the current token
     * May only be called when getTokenType() == KEYWORD
     */
    public int getKeywordType() {
        return keyWordType;
    }

    /**
     * Returns the symbol of the current token
     * May only be called when getTokenType() == SYMBOL
     */
    public char getSymbol() {
        return symbol;
    }

    /**
     * Returns the int value of the current token
     * May only be called when getTokenType() == INT_CONST
     */
    public int getIntValue() {
        return intValue;
    }

    /**
     * Returns the string value of the current token
     * May only be called when getTokenType() == STRING_CONST
     */
    public String getStringValue() {
        return stringValue;
    }

    /**
     * Returns the identifier value of the current token
     * May only be called when getTokenType() == IDENTIFIER
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Returns if there are more tokens in the stream
     */
    public boolean hasMoreTokens() {
        return (parser.ttype != parser.TT_EOF);
    }

    /**
     * Returns the line number of the current token
     */
    public int getLineNumber() {
		return lineNumber;
    }

    // Initializes the keywords hashtable
    private void initKeywords() {
        keywords = new Hashtable();
        keywords.put("class",new Integer(KW_CLASS));
        keywords.put("method",new Integer(KW_METHOD));
        keywords.put("function",new Integer(KW_FUNCTION));
        keywords.put("constructor",new Integer(KW_CONSTRUCTOR));
        keywords.put("int",new Integer(KW_INT));
        keywords.put("boolean",new Integer(KW_BOOLEAN));
        keywords.put("char",new Integer(KW_CHAR));
        keywords.put("void",new Integer(KW_VOID));
        keywords.put("var",new Integer(KW_VAR));
        keywords.put("static",new Integer(KW_STATIC));
        keywords.put("field",new Integer(KW_FIELD));
        keywords.put("let",new Integer(KW_LET));
        keywords.put("do",new Integer(KW_DO));
        keywords.put("if",new Integer(KW_IF));
        keywords.put("else",new Integer(KW_ELSE));
        keywords.put("while",new Integer(KW_WHILE));
        keywords.put("return",new Integer(KW_RETURN));
        keywords.put("true",new Integer(KW_TRUE));
        keywords.put("false",new Integer(KW_FALSE));
        keywords.put("null",new Integer(KW_NULL));
        keywords.put("this",new Integer(KW_THIS));
    }

    // Initializes the symbols hashtable
    private void initSymbols() {
        symbols = new Hashtable();
        symbols.put("(","(");
        symbols.put(")",")");
        symbols.put("[","[");
        symbols.put("]","]");
        symbols.put("{","{");
        symbols.put("}","}");
        symbols.put(",",",");
        symbols.put(";",";");
        symbols.put("=","=");
        symbols.put(".",".");
        symbols.put("+","+");
        symbols.put("-","-");
        symbols.put("*","*");
        symbols.put("/","/");
        symbols.put("&","&");
        symbols.put("|","|");
        symbols.put("~","~");
        symbols.put("<","<");
        symbols.put(">",">");
    }

}
