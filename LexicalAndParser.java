package compilerFinal;

import java.io.BufferedReader;


import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Pattern;

 

public class LexicalAndParser {
    private static final int TABLE_SIZE = 13;
    private LinkedList<String>[] hashTable;
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z_\\$][a-zA-Z0-9_\\$]*");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[0-9]+(\\.[0-9]+)?");
    private LinkedList<Token> tokens;

    public LexicalAndParser() {
        hashTable = new LinkedList[TABLE_SIZE];
        initializeHashTable();
        tokens = new LinkedList<>();
    }

    private void initializeHashTable() {
        String[] reservedWords = {
            "auto", "break", "case", "char", "const", "continue", 
            "default", "do", "double", "else", "enum", "extern", 
            "float", "for", "goto", "if", "inline", "int", "#include", "stdio.h",
            "long", "register", "restrict", "return", "short", 
            "signed", "sizeof", "static", "struct", "switch", 
            "typedef", "union", "unsigned", "void", "volatile", "while"
        };

        for (String word : reservedWords) {
            addReservedWord(word);
        }
    }

    private void addReservedWord(String word) {
        int index = hashFunction(word);
        if (hashTable[index] == null) {
            hashTable[index] = new LinkedList<>();
        }
        hashTable[index].add(word);
    }

    private int hashFunction(String word) {
        return Math.abs(word.hashCode()) % TABLE_SIZE;
    }

    private boolean isReservedWord(String word) {
        int index = hashFunction(word);
        if (hashTable[index] != null) {
            for (String reservedWord : hashTable[index]) {
                if (word.equals(reservedWord)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void printSymbolTable() {
        try (FileWriter writer = new FileWriter("symbol_table.txt")) {
            System.out.println("Symbol Table Contents:");
            writer.write("Symbol Table Contents:\n");
            for (int i = 0; i < TABLE_SIZE; i++) {
                if (hashTable[i] != null) {
                    System.out.print("Index " + i + ": ");
                    writer.write("Index " + i + ": ");
                    for (String word : hashTable[i]) {
                        System.out.print(word + " ");
                        writer.write(word + " ");
                    }
                    System.out.println();
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class CommentRemover {
        	public void processFile(String inputFile, String outputFile) throws IOException {
                try (BufferedReader br = new BufferedReader(new FileReader(inputFile));
                     FileWriter writer = new FileWriter(outputFile)) {
                    String line;
                    boolean insideCommentBlock = false;

                    while ((line = br.readLine()) != null) {
                        StringBuilder modifiedLine = new StringBuilder();
                        int index = 0;
                        boolean lastWasSpace = false;

                        while (index < line.length()) {
                            char currentChar = line.charAt(index);

                            if (insideCommentBlock) {
                                if (currentChar == '*' && index + 1 < line.length() && line.charAt(index + 1) == '/') {
                                    insideCommentBlock = false;
                                    index++; 
                                }
                            } else {
                                if (currentChar == '/' && index + 1 < line.length()) {
                                    char nextChar = line.charAt(index + 1);
                                    if (nextChar == '/') {
                                        break; 
                                    } else if (nextChar == '*') {
                                        insideCommentBlock = true;
                                        index++; 
                                        continue;
                                    }
                                }

                                if (Character.isWhitespace(currentChar)) {
                                    if (!lastWasSpace) {
                                        modifiedLine.append(' ');
                                        lastWasSpace = true;
                                    }
                                } else {	
                                    modifiedLine.append(currentChar);
                                    lastWasSpace = false;
                                }
                            }
                            index++;
                        }

                        if (modifiedLine.length() > 0) {
                            writer.write(modifiedLine.toString() + "\n");
                        }
                    }
                }
            }
        }

    public void processCodeFile(String input, String lexOutputFileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(input));
             FileWriter lexOutputWriter = new FileWriter(lexOutputFileName)) {

            String line;
            while ((line = br.readLine()) != null) {
                tokenize(line, lexOutputWriter);   

                
                String[] lineTokens = line.split("\\s+|(?<=[{();:,<>+=-])|(?=[{();:,<>+=-])");
                for (String token : lineTokens) {
                    if (isReservedWord(token)) {
                        System.out.println("Reserved word found: " + token);
                    } else if (isDelimiterOrCompoundSymbol(token)) {
                        System.out.println("Delimiter or compound symbol found: " + token);
                    } else if (isIdentifier(token)) {
                        System.out.println("Identifier found: " + token);
                    } else if (isNumber(token)) {
                        System.out.println("Number found: " + token);
                    } else if (isString(token)) {
                        System.out.println("String found: " + token);
                    }
                }
            }

            printSymbolTable(); 

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void printSymbolTable(FileWriter writer) {
        try {
            System.out.println("Symbol Table Contents:");
            writer.write("Symbol Table Contents:\n");
            for (int i = 0; i < TABLE_SIZE; i++) {
                if (hashTable[i] != null) {
                    System.out.print("Index " + i + ": ");
                    writer.write("Index " + i + ": ");
                    for (String word : hashTable[i]) {
                        System.out.print(word + " ");
                        writer.write(word + " ");
                    }
                    System.out.println();
                    writer.write("\n");
                }
            }
            writer.close(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void tokenize(String line, FileWriter writer) throws IOException {
        String[] rawTokens = line.split("\\s+|(?<=[{();:,<>+=-])|(?=[{();:,<>+=-])");
        for (String rawToken : rawTokens) {
            TokenType type = getTokenType(rawToken);
            Token token = new Token(rawToken, type);
            tokens.add(token);
            writer.write(token + "\n");
        }
    }

private TokenType getTokenType(String token) {
        if (isReservedWord(token)) return TokenType.RESERVED;
        if (isDelimiterOrCompoundSymbol(token)) return TokenType.DELIMITER;
        if (isIdentifier(token)) return TokenType.IDENTIFIER;
        if (isNumber(token)) return TokenType.NUMBER;
        if (isString(token)) return TokenType.STRING;
        return TokenType.UNKNOWN;
    }

    private boolean isDelimiterOrCompoundSymbol(String token) {
        String[] delimiters = {"(", ")", "[","%", "]", "{", "}", ";", ",", ".", ":", "!", "?", "+" , "-", "*", "/", "%", "=", "<", ">", "&", "|", "^", "~"};
        for (String delimiter : delimiters) {
            if (token.equals(delimiter)) {
                return true;
            }
        }
        return false;
    }

    private boolean isIdentifier(String token) {
        return IDENTIFIER_PATTERN.matcher(token).matches() && !isReservedWord(token);
    }

    private boolean isNumber(String token) {
        return NUMBER_PATTERN.matcher(token).matches();
    }

    private boolean isString(String token) {
        return token.startsWith("\"") && token.endsWith("\"") && token.length() > 1;
    }
    public class Token {
        String value;
        TokenType type;

        public Token(String value, TokenType type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString() {
            return type + ": " + value;
        }
    }

    public enum TokenType {
        RESERVED, IDENTIFIER, NUMBER, STRING, DELIMITER, UNKNOWN
    }

  
   








    

    class Parser {
        private LinkedList<LexicalAndParser.Token> tokens;
        private int currentTokenIndex;
        private FileWriter parsingResultsWriter;
        private int lineCount;

        public Parser(LinkedList<LexicalAndParser.Token> tokens, FileWriter parsingResultsWriter) {
            this.tokens = tokens;
            this.currentTokenIndex = 0;
            this.lineCount = 1;
            this.parsingResultsWriter = parsingResultsWriter; 
        }

        public void parse() {
            try {
                while (currentTokenIndex < tokens.size()) {
                    LexicalAndParser.Token token = tokens.get(currentTokenIndex);
                    if (token.value.equals("\n")) {
                        lineCount++;
                    }

                    if (isLoopConstruct(token.value)) {
                        parseLoop(token);
                    } else {
                        currentTokenIndex++; 
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private boolean isLoopConstruct(String tokenValue) {
            return tokenValue.equals("for") || tokenValue.equals("while") || tokenValue.equals("do");
        }

        private void parseLoop(LexicalAndParser.Token token) throws IOException {
            switch (token.value) {
                case "for":
                    expectToken("for");
                    expectToken("(");
                    parseCondition();
                    expectToken(";");
                    skipToToken(";");
                    expectToken(";");
                    skipToToken(")");
                    expectToken(")");
                    expectToken("{");
                    skipToToken("}");
                    expectToken("}");
                    writeParsingResult("For Loop");
                    break;
                case "while":
                    expectToken("while");
                    expectToken("(");
                    parseCondition();
                    expectToken(")");
                    expectToken("{");
                    skipToToken("}");
                    expectToken("}");
                    writeParsingResult("While Loop");
                    break;
                case "do":
                    expectToken("do");
                    expectToken("{");
                    skipToToken("}");
                    expectToken("}");
                    expectToken("while");
                    expectToken("(");
                    parseCondition();
                    expectToken(")");
                    expectToken(";");
                    writeParsingResult("Do-While Loop");
                    break;
                default:
                    writeError("Unexpected token in loop parsing: " + token.value);
                    break;
            }
        }

        private void parseCondition() throws IOException {
            LexicalAndParser.Token token = tokens.get(currentTokenIndex);
            while (!token.value.equals(")")) {
                currentTokenIndex++;
                token = tokens.get(currentTokenIndex);
            }
        }

        private void skipToToken(String tokenValue) throws IOException {
            while (currentTokenIndex < tokens.size() && !tokens.get(currentTokenIndex).value.equals(tokenValue)) {
                currentTokenIndex++;
            }
        }

        private void expectToken(String expectedToken) throws IOException {
            LexicalAndParser.Token token = tokens.get(currentTokenIndex);
            if (!token.value.equals(expectedToken)) {
                writeError("Expected " + expectedToken + " but found " + token.value);
            } else {
                currentTokenIndex++;
            }
        }

        private void writeError(String message) throws IOException {
            parsingResultsWriter.write("Error at Line " + lineCount + ": " + message + "\n");
        }

        private void writeParsingResult(String result) throws IOException {
            parsingResultsWriter.write(result + " (Line " + lineCount + ")\n");
        }

    }

public static void main(String[] args) {
    String inputFileName = "input"; 
    String processedFileName = "codeoutput"; 
    String lexOutputFileName = "output"; 
    String parsingOutputFileName = "parse_output"; 

    CommentRemover remover = new CommentRemover();
    try {
        remover.processFile(inputFileName, processedFileName);
    } catch (IOException e) {
        e.printStackTrace();
    }

    LexicalAndParser lexicalAndParser = new LexicalAndParser();
    try (BufferedReader br = new BufferedReader(new FileReader(processedFileName));
         FileWriter lexOutputWriter = new FileWriter(lexOutputFileName)) {

    	String line;
        int lineNumber = 1;  
        while ((line = br.readLine()) != null) {
        	lexicalAndParser.tokenize(line, lexOutputWriter);  
            lineNumber++; 
        }

        try (FileWriter symbolTableWriter = new FileWriter("symbol_table.txt")) {
            lexicalAndParser.printSymbolTable(symbolTableWriter);
        }

    } catch (IOException e) {
        e.printStackTrace();
    }

    try (FileWriter parsingOutputWriter = new FileWriter(parsingOutputFileName)) {
        Parser parser = lexicalAndParser.new Parser(lexicalAndParser.tokens, parsingOutputWriter);
        parser.parse();
    } catch (IOException e) {
        e.printStackTrace();
    

    System.out.println("Lexical analysis, symbol table creation, and parsing complete. Check the output files for results.");
}
}}


