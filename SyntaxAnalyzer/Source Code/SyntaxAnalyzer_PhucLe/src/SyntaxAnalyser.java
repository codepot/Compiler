
import java.util.ArrayList;
import java.util.List;

public class SyntaxAnalyser {

    LexicalAnalyser pro = new LexicalAnalyser();
    int nextTokenPosition = 0;
    Token currentToken = null;
    List<Token> tokens = new ArrayList<>();
    String output = "";
    String errorMessage = "";
    boolean successful = false;
    int errorLoc = -1;
    Token errorToken = new Token(LexicalAnalyser.Type.IDENTIFIER, "error", 0);

    public SyntaxAnalyser() {
    }

    public void input(String source) {
        tokens.clear();
        tokens.addAll(pro.tokenize(source, 1));
    }

    public void reset() {
        nextTokenPosition = 0;
        currentToken = null;
        output = "";
        successful = false;
        errorLoc = -1;
        errorMessage = "";
        errorToken = new Token(LexicalAnalyser.Type.IDENTIFIER, "error", 0);
    }

    public void clearTokens() {
        tokens.clear();
    }

    public void input(String source, int lineNumber) {
        tokens.addAll(pro.tokenize(source, lineNumber));
    }

    public void lexer() {
        if (tokens.size() > 0 && nextTokenPosition < tokens.size()) {
            currentToken = tokens.get(nextTokenPosition);
            if (currentToken.tokenType == LexicalAnalyser.Type.ERROR) {
                errorLoc = nextTokenPosition - 1;
                errorToken = currentToken;
                errorMessage = "UNVALID TOKEN " + currentToken.content;
            }
            nextTokenPosition++;
        } else {
            currentToken = null;
        }
    }

    public String getResult() {
        String result = "";
        if (errorLoc >= 0) {
            for (int i = 0; i <= errorLoc; i++) {
                result += tokens.get(i).toString2() + "\n";
            }
            result += "ERROR AT LINE " + errorToken.lineNumber + ": " + errorMessage;
            if (errorToken.tokenType == LexicalAnalyser.Type.ERROR) {
                result += ", UNVALID TOKEN: " + errorToken.content;
            }
        } else { 
            for (Token token : tokens) {
                result += token.toString2() + "\n";
            }
            if (this.successful) {
                result += "THE SYNTAX IS CORRECT";

            }
        }

        return result;
    }

    private boolean isToken(String token) {
        if (currentToken != null) {
            return currentToken.content.trim().equals(token);
        }
        return false;
    }

   boolean func_rat16f() {
        lexer();
        if (isToken("$$")) {
            currentToken.addRule("<Rat16F> -> $$ <Opt Function Definitions> $$ <Opt Declaration List> <Statement List> $$ ");
            lexer();
            if (isToken("$$")) {
                currentToken.addRule("<Opt Function Definitions> -> <Empty>");

                lexer();
                if (currentToken.content.equals("integer") || currentToken.content.equals("real") || currentToken.content.equals("boolean")) {
                    currentToken.addRule("<Opt Declaration List> -> <Declaration List>");
                } else {
                    currentToken.addRule("<Opt Declaration List> -> <Empty>");
                }
                nextTokenPosition--;

                if (func_opt_declaration_list()) {
                    if (func_statementList()) {
                        lexer();
                        if (isToken("$$")) {
                            lexer();
                            successful = true;
                            return true;
                        } else {

                            return false;
                        }
                    } else {
                        return false;
                    }
                } else {                    
                    return false;
                }

            } else {
                currentToken.addRule("<Opt Function Definitions> -> <Function Definitions>");               
                nextTokenPosition--;
                if (func_opt_function_definitions()) {
                    lexer();
                    if (isToken("$$")) {
                        lexer();
                        if (!(currentToken.content.equals("integer") || currentToken.content.equals("real") || currentToken.content.equals("boolean"))) {
                            currentToken.addRule("<Opt Declaration List> -> <Empty>");
                        }
                        nextTokenPosition--;

                        if (func_opt_declaration_list()) {
                            if (func_statementList()) {
                                lexer();
                                if (isToken("$$")) {
                                    successful = true;
                                    return true;
                                } else {
                                    errorToken = currentToken;
                                    return false;
                                }
                            } else {
                                return false;
                            }
                        } else {
                            return false;
                        }
                    }
                }
            }
        } else {
            errorLoc = 0;
            errorToken = currentToken;
            errorMessage = "$$ is expected at the begining of the source code";
            return false;
        }
        return false;
    }

   boolean func_opt_function_definitions() {
        lexer();
        if (isToken("$$")) {
            currentToken.addRule("<Opt Function Definitions> -><Empty>");
            nextTokenPosition--;
            return true;
        } else {
            nextTokenPosition--;
            return func_function_definitions();
        }
    }

    boolean func_function_definitions() {
        int pointer = nextTokenPosition;
        if (func_function()) {
            
            lexer();
            if (isToken("$$")) {
                nextTokenPosition--;
                return true;
            } else {
                nextTokenPosition--;
                return func_function_definitions();
            }
        } else {
            errorLoc = pointer;
            errorToken = tokens.get(pointer);
            errorMessage = "Function Definition is incorrect";
            return false;
        }
    }

    boolean func_function() {
        lexer();
        if (isToken("function")) {
            currentToken.addRule("<Function> -> function <Identifier> [<Opt Parameter List>] <Opt Declaration List> <Body>");
            lexer();
            if (currentToken.tokenType == LexicalAnalyser.Type.IDENTIFIER) {
                lexer();
                if (isToken("[")) {
                    currentToken.addRule("<Opt Parameter List> -> <Parameter List> | <Empty>");
                    lexer();
                    if (isToken("]")) {
                        currentToken.addRule("<Opt Parameter List> -> <Empty>");
                        return func_opt_declaration_list() && func_body();
                    } else {
                        currentToken.addRule("<Opt Parameter List> -> <Parameter List>");                       
                        nextTokenPosition--;
                        return func_parameter_list() && func_opt_declaration_list() && func_body();
                    }
                } else {
                    nextTokenPosition--;
                    return false;
                }
            } else {
                errorLoc = nextTokenPosition;
                errorToken = currentToken;
                errorMessage = "expecting an identifier";
                nextTokenPosition--;
                return false;
            }
        } else {
            errorLoc = nextTokenPosition;
            errorToken = currentToken;
            errorMessage = "expecting 'function'";
            nextTokenPosition--;
            return false;
        }
    }

    boolean func_opt_parameter_list() {
        lexer();
        if (isToken("]")) {
            nextTokenPosition--;
            return true;
        } else {
            nextTokenPosition--;
            return func_parameter_list();
        }
    }

    boolean func_parameter_list() {
        int pointer = nextTokenPosition;
        boolean isParameter = func_parameter();
        if (isParameter) {
            lexer();
            if (isToken("]")) {
                nextTokenPosition--;
                return true;
            } else {
                nextTokenPosition--;
                lexer();
                if (isToken(",")) {
                    return func_parameter_list();
                } else {
                    nextTokenPosition--;
                    return false;
                }
            }
        } else {
            nextTokenPosition = pointer;
        }
        return false;
    }

    boolean func_parameter() {
        int pointer = nextTokenPosition;
        if (func_IDs()) {
            currentToken.addRule("<Parameter> -> <IDs> : <Qualifier>");
            lexer();
            if (currentToken == null) {
                nextTokenPosition--;
                return false;
            }
            if (isToken(":")) {
                return func_qualifier();
            } else {
                nextTokenPosition--;
                return false;
            }
        } else {
            nextTokenPosition = pointer;
        }
        return false;
    }

    boolean func_qualifier() {
        lexer();
        if (currentToken == null) {
            nextTokenPosition--;
            return false;
        }
        if (isToken("integer") || isToken("boolean") || isToken("real")) {
            currentToken.addRule("<Qualifier> -> " + currentToken.content);
            return true;
        } else {
            nextTokenPosition--;
            return false;
        }
    }
   
    boolean func_body() {
        return func_compound();
    }
   
    boolean func_opt_declaration_list() {
        lexer();
        int pointer = nextTokenPosition;
        if (isToken("]") || isToken("$$")) 
        {
            lexer();
        }    
        if (isToken("{") || currentToken.tokenType == LexicalAnalyser.Type.IDENTIFIER || isToken("if") || isToken("return") || isToken("print") || isToken("read") || isToken("while")) {
            if (isToken("{")) {
                addRule("<Opt Declaration List> - > <Empty>", pointer);
            }
            nextTokenPosition--;
            return true;
        } else {
            currentToken.addRule("<Opt Declaration List> - > <Declaration List>");
            nextTokenPosition--;
            return func_declaration_list();
        }
    }

    boolean func_declaration_list() {
        if (func_declaration()) {
            lexer();
            if (isToken(";")) {
                lexer();
                if (isToken("{") || currentToken.tokenType == LexicalAnalyser.Type.IDENTIFIER || isToken("if") || isToken("return") || isToken("print") || isToken("read") || isToken("while")) {
                    nextTokenPosition--;
                    return true;
                } else {
                    nextTokenPosition--;
                    return func_declaration_list();
                }
            } else {
                nextTokenPosition--;
                return false;
            }
        }
        return false;
    }

    boolean func_declaration() {
        currentToken.addRule("<Declaration> -> <Qualifier> <IDs>");
        return func_qualifier() && func_IDs();
    }

    boolean func_IDs() {
        int pointer = nextTokenPosition;
        lexer();
        if (currentToken == null) {
            return false;
        }
        if (currentToken.tokenType == LexicalAnalyser.Type.IDENTIFIER) {
            lexer();
            if (currentToken == null) {
                return true;
            }
            if (isToken(")") || isToken(";") || isToken(":") || isToken("]")) {
                currentToken.addRule("<IDs> -> <Identifier>");
                nextTokenPosition--;
                return true;
            } else if (isToken(",")) {
                currentToken.addRule("<IDs> -> <Identifier> , <IDs>");
                //lexer();
                return func_IDs();
            } else {
                nextTokenPosition--;
            }
        } else {
            nextTokenPosition = pointer;
        }
        return false;
    }

    boolean func_statementList() {
        int pointer = nextTokenPosition;
        if (func_statement()) {
            lexer();
            if (isToken("}") && nextTokenPosition < (tokens.size())) {
                nextTokenPosition--;
                return true;
            } 
            else if (isToken("$$")) {
                if (nextTokenPosition == (tokens.size())) {
                    nextTokenPosition--;
                    return true;
                } else {                    
                    return false;
                }
            } else {
                nextTokenPosition--;
                return func_statementList();
            }
        } else {
            nextTokenPosition = pointer;
            return false;
        }

    }

    boolean func_statement() {
        int pointer = nextTokenPosition;
        boolean isCompound = func_compound();
        boolean isIf = func_if();
        boolean isReturn = func_return();
        boolean isWrite = func_write();
        boolean isRead = func_read();
        boolean isWhile = func_while();
        boolean isAssign = func_assign();
        boolean result = isCompound || isIf || isReturn || isWrite || isRead || isWhile || isAssign;
        if (result) {
            return true;
        }
        errorLoc = pointer;
        errorToken = tokens.get(pointer);
        errorMessage = "INCORRECT STATEMENT";
        return false;
    }

    boolean func_compound() {
        int pointer = nextTokenPosition;
        lexer();
        if (isToken("{")) {
            currentToken.addRule("<Statement> -> <Compound>");
            if (func_statementList()) {
                lexer();
                if (currentToken == null) {
                    nextTokenPosition--;
                    return false;
                }
                if (isToken("}")) {
                    return true;
                }
            }
        }
        nextTokenPosition = pointer;
        return false;
    }
    
    boolean func_assign() {
        int pointer = nextTokenPosition;
        lexer();
        if (currentToken == null) {
            return false;
        }
        if (currentToken.tokenType == LexicalAnalyser.Type.IDENTIFIER) {
            currentToken.addRule("<Statement> -> <Assign>");
            currentToken.addRule("<Assign> -> <Identifier> := <Expression> ;");
            lexer();
            if (isToken(":=")) {

                if (func_expression()) {

                    lexer();
                    if (isToken(";")) {
                        return true;
                    }
                }
            }
        }
        nextTokenPosition = pointer;

        return false;

    }

    boolean func_return() {
        int pointer = nextTokenPosition;
        lexer();
        if (currentToken == null) {
            nextTokenPosition--;
            return false;
        }
        if (currentToken.tokenType == LexicalAnalyser.Type.KEYWORD && isToken("return")) {
            currentToken.addRule("<Statement> -> <Return>");
            lexer();
            if (isToken(";")) {
                currentToken.addRule("<Return> -> return;");
                return true;
            } else {
                nextTokenPosition--;
                currentToken.addRule("<Return> -> return <Expression>;");
                if (func_expression()) {
                    lexer();
                    if (isToken(";")) {
                        return true;
                    }
                }
            }
        }
        nextTokenPosition = pointer;
        return false;
    }

    boolean func_write() {
        int pointer = nextTokenPosition;
        lexer();
        if (currentToken == null) {
            nextTokenPosition--;
            return false;
        }
        if (currentToken.tokenType == LexicalAnalyser.Type.KEYWORD && isToken("print")) {
            currentToken.addRule("<Statement> ->  <Write>");
            currentToken.addRule("<Write> ->  print (<Expression>);");
            lexer();
            if (isToken("(")) {
                if (func_expression()) {

                    lexer();
                    if (isToken(")")) {
                        lexer();
                        if (isToken(";")) {
                            return true;
                        } else {
                            nextTokenPosition--;
                            return false;
                        }
                    } else {
                        nextTokenPosition--;
                        return false;
                    }
                }
            } else {
                nextTokenPosition--;
                return false;
            }

        } else {
            nextTokenPosition--;
            return false;
        }
        nextTokenPosition = pointer;
        return false;
    }
   
    boolean func_read() {
        int pointer = nextTokenPosition;
        lexer();
        if (isToken("read")) {
            currentToken.addRule("<Statement> -> <Read>");
            currentToken.addRule("<Read> -> read <IDs>");
            lexer();
            if (isToken("(")) {
                if (func_IDs()) {
                    lexer();
                    if (isToken(")")) {
                        lexer();
                        if (isToken(";")) {
                            return true;
                        }
                    }
                }
            }
        } else {
            nextTokenPosition = pointer;
            return false;
        }
        return false;
    }

    boolean func_while() {
        int pointer = nextTokenPosition;
        lexer();
        if (isToken("while")) {
            currentToken.addRule("<Statement> -> <While>");
            currentToken.addRule("<While> -> while (<Condition>) <Statement>");
            lexer();
            if (isToken("(")) {
                if (func_condition()) {
                    lexer();
                    if (isToken(")")) {
                        return func_statement();
                    } else {
                        nextTokenPosition--;
                        return false;
                    }
                }
            } else {
                nextTokenPosition--;
                return false;
            }
        } else {
            nextTokenPosition--;
            return false;
        }
        nextTokenPosition = pointer;
        return false;
    }

    boolean func_condition() {
        currentToken.addRule("<Statement> -> <Condition>");
        currentToken.addRule("<Condition> -> <Expression> <Relop> <Expression> ");
        return func_expression() && func_relop() && func_expression();
    }

    boolean func_relop() {
        lexer();
        if (isToken("=") || isToken("/=") || isToken(">") || isToken("<") || isToken("=>") || isToken("<=")) {
            currentToken.addRule("<Relop> -> " + currentToken.content);
            return true;
        } else {
            nextTokenPosition--;
        }
        nextTokenPosition--;
        return false;
    }
   
    boolean func_if() {
        lexer();
        if (isToken("if")) {
            currentToken.addRule("<Statement> -> <If>");
            currentToken.addRule("<If> -> if (<Condition>) <Statement> <If Prime>");
            currentToken.addRule("<Condition> -> <Expression> <Relop> <Expression>");
            currentToken.addRule("<Relop> ->  = | /= | > | < | => | <=");
            lexer();
            if (isToken("(")) {
                if (func_condition()) {
                    lexer();
                    if (isToken(")")) {
                        return func_statement() && func_if_prime();
                    } else {
                        nextTokenPosition--;
                        return false;
                    }
                }
            } else {
                nextTokenPosition--;
            }
        } else {
            nextTokenPosition--;
        }
        return false;
    }
   
    boolean func_if_prime() {
        lexer();
        if (isToken("endif")) {
            currentToken.addRule("<If Prime> -> endif");
            return true;
        } else if (isToken("else")) {
            if (func_statement()) {
                lexer();
                if (isToken("endif")) {
                    currentToken.addRule("<If Prime> -> else <Statement> endif");
                    return true;
                } else {
                    nextTokenPosition--;
                    return false;
                }
            }
        } else {
            nextTokenPosition--;
            return false;
        }
        nextTokenPosition--;
        return false;
    }
  
    boolean func_expression() {
        if (func_term()) {
            return func_expression_prime();
        }
        return false;
    }
   
    boolean func_expression_prime() {
        lexer();
        if (isToken("+")) {
            currentToken.addRule("<Expression Prime> -> + <Term> <Expression Prime>");
            return func_term() && func_expression_prime();
        } else if (isToken("-")) {
            currentToken.addRule("<Expression Prime> -> - <Term> <Expression Prime>");
            return func_term() && func_expression_prime();
        } else {
            currentToken.addRule("<Expression Prime> -> <Empty>");
            nextTokenPosition--;
            return true;
        }
    }
    
    boolean func_term() {
        if (func_factor()) {
            return func_term_prime();
        } else {
            return false;
        }
    }

     boolean func_term_prime() {
        lexer();
        if (isToken("*")) {
            currentToken.addRule("<Term Prime> -> * <Factor> <Term Prime>");
            if (func_factor()) {
                return func_term_prime();
            }
        } else if (isToken("/")) {
            currentToken.addRule("<Term Prime> -> / <Factor> <Term Prime>");
            if (func_factor()) {
                return func_term_prime();
            }
        } else {
            currentToken.addRule("<Term Prime> -> <Empty>");
            nextTokenPosition--;
            return true;
        }
        return false;
    }

    boolean func_factor() {
        int pointer = nextTokenPosition;
        lexer();
        currentToken.addRule("<Expression> -> <Term> <Expression Prime>");
        currentToken.addRule("<Term> -> <Factor> <Term Prime>");
        if (isToken("-")) {
            currentToken.addRule("<factor> -> - <factor>");
            return func_factor();
        } else if (isToken("(")) {
            currentToken.addRule("<factor> -> (<Expression>)");
            boolean isExpression = func_expression();
            if (isExpression) {
                lexer();
                if (isToken(")")) {
                    return true;
                } else {                   
                    return false;
                }
            }

        } else if (isToken("true")) {
            currentToken.addRule("<factor> -> true");
            return true;
        } else if (isToken("false")) {
            currentToken.addRule("<factor> -> false");
            return true;
        } else if (currentToken.tokenType == LexicalAnalyser.Type.INTEGER) {
            currentToken.addRule("<factor> -> <Integer>");
            return true;
        } else if (currentToken.tokenType == LexicalAnalyser.Type.REAL) {
            currentToken.addRule("<factor> -> <Real>");
            return true;
        } else if (currentToken.tokenType == LexicalAnalyser.Type.IDENTIFIER) {
            lexer();
            if (isToken("[")) {
                addRule("<factor> -> <Identifier>[<IDs>]", pointer);
                addRule("<IDs> -> <Identifier> | <Identifier>, <IDs>", pointer);
                boolean isIDs = func_IDs();
                if (isIDs) {
                    lexer();
                    if (isToken("]")) {
                        return true;
                    } else {
                        nextTokenPosition--;
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                addRule("<factor> -> Identifier", pointer);
                nextTokenPosition--;
                return true;
            }
        }
        nextTokenPosition = pointer;
        return false;
    }

    private void addRule(String rule, int pointer) {
        this.tokens.get(pointer).addRule(rule);
    }
}
