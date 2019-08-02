

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LexicalAnalyser {
    String[] operators = {"+", "-", "*"};
    String[] compoundOperators = {"/", "<"}; // with equal sign, ":" is an
    // exception as ":= or => is an operator, but ":" is not an operator
    String[] separators = {"$", "{", "}", "(", ")", "[", "]", ",", ";"};
    String[] keywords = {"function", "while", "if", "else", "endif", "return", "print", "read", "write", "integer", "boolean",
        "real", "true" , "false"};
    
    
    /*
	 accepting states (rows): Number: 1 and 7 -- Letter: 2,3,4
     */
    private static final int[][] machine = {{2, 1, 8, 8}, {8, 1, 8, 6}, {3, 4, 5, 8}, {3, 4, 5, 8},
    {3, 4, 5, 8}, {3, 4, 5, 8}, {8, 7, 8, 8}, {8, 7, 8, 8}, {8, 8, 8, 8}};

    private static final int[] acceptedStates = {1, 2, 3, 4, 7};

    /*
	 0:letter, 1:digit, 2:_ (underscore), 3:. (period)
     */
    public static enum Type {
        IDENTIFIER, INTEGER, REAL, SEPARATOR, OPERATOR, KEYWORD, ERROR;
    }


    int currentIndex = 0;
    List<Token> tokens = new ArrayList<>();
    int currentState = 0;
    String tokenBuffer = "";

    public void analyze(String line, int lineNumber) {
        currentIndex = 0;
        tokens = new ArrayList<>();
        lexer(line, lineNumber);

        for (Token token : tokens) {
            System.out.println(token);
        }
    }

    public List<String> analyzeSource(String line, int lineNumber) {
        List<String> result = new ArrayList<>();
        tokens = new ArrayList<>();
        lexer(line, lineNumber);
        for (Token token : tokens) {
            result.add(token.toString2());
        }
        return result;
    }
    
    public List<Token> tokenize(String input,int lineNumber){
        List<Token> result = new ArrayList<>();
        tokens = new ArrayList<>();
        lexer(input, lineNumber);
        for (Token token : tokens) {
            result.add(token);
        }
        return result;
    }

    public void lexer(String line, int lineNumber) {
        char[] chars = line.toCharArray();
        currentIndex = 0;
        if (chars.length > 0) {
            char c = chars[currentIndex];

            if (!isSpace(c)) {
                // Check SEPARATOR
                if (!isAcceptedCharacters(c)) {
                    if (tokenBuffer.length() == 0) {
                        if (Arrays.asList(separators).contains(c + "")) {
                            if (c == '$') {
                                if (chars.length > 1) {
                                    if (chars[currentIndex + 1] == '$') {
                                        tokens.add(new Token(Type.SEPARATOR, "$$", lineNumber));
                                        currentIndex = 2;
                                    } else {
                                        tokens.add(new Token(Type.ERROR, "$", lineNumber));
                                        currentIndex = 1;
                                    }
                                } else {
                                    tokens.add(new Token(Type.ERROR, "$", lineNumber));
                                    currentIndex = 1;
                                }
                            } else {
                                tokens.add(new Token(Type.SEPARATOR, String.valueOf(c), lineNumber));
                                currentIndex = 1;
                            }
                            String remain = line.trim().substring(currentIndex);
                            lexer(remain, lineNumber);
                        } 
                        // CHECK OPERATOR
                        else if (Arrays.asList(operators).contains(c + "") || c == '>' || c == '=') {

                            if (c == '>') {
                                tokens.add(new Token(Type.OPERATOR, String.valueOf(c), lineNumber));
                                currentIndex = 1;
                            } else if (chars.length > 1 && c == '=' && chars[currentIndex + 1] == '>') {
                                tokens.add(new Token(Type.OPERATOR, c + ">", lineNumber));
                                currentIndex = 2;
                            } else {
                                tokens.add(new Token(Type.OPERATOR, String.valueOf(c), lineNumber));
                                currentIndex = 1;
                            }
                            String remain = line.trim().substring(currentIndex);

                            lexer(remain, lineNumber);

                        } else if (String.valueOf(c).equals(":")) {
                            if (chars.length > 1) {
                                if (chars[currentIndex + 1] == '=') {
                                    tokens.add(new Token(Type.OPERATOR, c + "=", lineNumber));
                                    currentIndex = 2;
                                } else {
                                    tokens.add(new Token(Type.OPERATOR, String.valueOf(c), lineNumber));
                                    currentIndex = 1;
                                }
                            } else {
                                tokens.add(new Token(Type.ERROR, String.valueOf(c), lineNumber));
                                currentIndex = 1;
                            }
                            String remain = line.trim().substring(currentIndex);
                            lexer(remain, lineNumber);
                        } else if (Arrays.asList(compoundOperators).contains(c + "")) {
                            if (chars.length > 1 && chars[currentIndex + 1] == '=') {
                                tokens.add(new Token(Type.OPERATOR, c + "=", lineNumber));
                                currentIndex = 2;
                            } else {
                                tokens.add(new Token(Type.OPERATOR, String.valueOf(c), lineNumber));
                                currentIndex = 1;
                            }
                            String remain = line.trim().substring(currentIndex);
                            lexer(remain, lineNumber);
                        } else {
                            tokens.add(new Token(Type.ERROR, String.valueOf(c), lineNumber));
                            currentIndex = 1;
                            String remain = line.trim().substring(currentIndex);
                            lexer(remain, lineNumber);
                        }
                    } // buffer Length >0
                    else {

                        if (isStateAccepted(currentState)) {
                            tokens.add(createToken(tokenBuffer, lineNumber));
                        } else {
                            tokens.add(new Token(Type.ERROR, tokenBuffer, lineNumber));
                        }
                        tokenBuffer = "";
                        currentState = 0;
                        lexer(line, lineNumber);

                    }

                } // input is letter or digits or underscore
                else {
                    int nextState = nextState(currentState, c);
                    tokenBuffer += c;
                    currentState = nextState;

                    String remain = line.substring(1);
                    lexer(remain, lineNumber);
                }
                // IS space/tab/newline
            } else {
                if (tokenBuffer.length() > 0) {
                    if (isStateAccepted(currentState)) {
                        tokens.add(createToken(tokenBuffer, lineNumber));
                    } else {
                        tokens.add(new Token(Type.ERROR, tokenBuffer, lineNumber));
                    }
                }
                tokenBuffer = "";
                currentState = 0;
                String remain = line.substring(1);
                lexer(remain, lineNumber);

            }
            // ---

            // CHECK LETTER & DIGITS
        } // no more chars, check the token buffer
        else if (tokenBuffer.length() > 0) {
            if (isStateAccepted(currentState)) {
                tokens.add(createToken(tokenBuffer, lineNumber));
            } else {
                tokens.add(new Token(Type.ERROR, tokenBuffer, lineNumber));
            }
        }
        tokenBuffer = "";
        currentState = 0;

    }

    private int nextState(int fromState, char c) {
        if (Character.isAlphabetic(c)) {
            return machine[fromState][0];
        } else if (Character.isDigit(c)) {
            return machine[fromState][1];
        } else if (c == '_') {
            return machine[fromState][2];
        } else {
            return machine[fromState][3];
        }
    }

    private Token createToken(String tokenBuffer,int lineNumber) {
        if (Arrays.asList(keywords).contains(tokenBuffer)) {
            return new Token(Type.KEYWORD, tokenBuffer, lineNumber);
        } else if (isInteger(tokenBuffer)) {
            return new Token(Type.INTEGER, tokenBuffer, lineNumber);
        } else if (isReal(tokenBuffer)) {
            return new Token(Type.REAL, tokenBuffer, lineNumber);
        } else {
            return new Token(Type.IDENTIFIER, tokenBuffer, lineNumber);
        }
    }

    private boolean isLetter(char c) {
        return (Character.isAlphabetic(c) || c == '_');
    }

    private boolean isAcceptedCharacters(char c) {
        return (Character.isAlphabetic(c) || Character.isDigit(c) || c == '_' || c == '.');
    }

    private boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    private boolean isReal(String input) {
        try {
            Double.parseDouble(input);
        } catch (NumberFormatException ex) {
            return false;
        }
        return true;
    }

    private boolean isStateAccepted(int state) {
        return Arrays.binarySearch(acceptedStates, state) >= 0;
    }

    private boolean isSpace(char c) {
        return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
    } 

}
