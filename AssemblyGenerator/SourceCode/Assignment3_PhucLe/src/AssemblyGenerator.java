
import java.util.ArrayList;
import java.util.List;

public class AssemblyGenerator {
    LexicalAnalyzer pro = new LexicalAnalyzer();
    List<Token> tokens = new ArrayList<>();
    List<Symbol> symbols = new ArrayList<>();
    List<Instruction> instructions = new ArrayList<>();
    int nextTokenPosition = 0;
    Token currentToken = null;
    int errorPosition = -1;
    String errorMessage = "";
    String qualifier_temp = "";
    boolean isRead = false;
    int jumpstack = -1;
    String acceptedType = "";
   

    public AssemblyGenerator() {        
    }

    public List<Symbol> getSymbols() {
        return symbols;
    }

    public boolean doesSymbolExist(String identifier) {
        return this.symbols.contains(new Symbol(identifier, 0, null, 0));
    }

    public int findSymbol(String identifier) {
        for (int i = 0; i < symbols.size(); i++) {
            if (symbols.get(i).getIdentifier().equals(identifier)) {
                return i;
            }
        }
        return -1;
    }

    public int insertSymbol(Symbol symbol) {
        int index = findSymbol(symbol.getIdentifier());
        if (index >= 0) {
            if (errorMessage.equals("")) {
                errorMessage ="ERROR at line "+symbol.getLine()+": variable '"+ symbol.getIdentifier() + "' has been declared before";
            }
            return -1;
        } else {
            symbol.setMemoryLocation(6000 + symbols.size());
            this.symbols.add(symbol);
            return symbols.size() - 1;
        }
    }

    public int insertInstruction(Instruction instruction) {
        instruction.setAddress(instructions.size() + 1);
        this.instructions.add(instruction);
        return instructions.size() - 1;
    }

    public List<Instruction> getInstructions() {
        return instructions;
    }

    public void input(String source, int lineNumber) {
        tokens.addAll(pro.tokenize(source, lineNumber));
    }

    public void lexer() {
        if (tokens.size() > 0 && nextTokenPosition < tokens.size()) {
            currentToken = tokens.get(nextTokenPosition);
            nextTokenPosition++;
        } else {
            currentToken = null;
        }
    }

    public String getResult() {
        String result = "";
        for (Token token : tokens) {
            result += token.toString() + "\n";
        }
        return result;
    }

    public void func_rat16f() {
        lexer();
        if (isToken("$$")) {
            lexer();
            if (isToken("$$")) {
                func_opt_declaration_list();
                func_statement_list();
                lexer();
                if (isToken("$$")) {
                    
                }
            }
        }
    }

    // can combine with declaration
    private void func_qualifiers() {
        lexer();
        if (currentToken == null) {
            nextTokenPosition--;
        }
        if (isToken("integer") || isToken("boolean") || isToken("real")) {
            qualifier_temp = currentToken.content;
        } else {
            nextTokenPosition--;
        }
    }

    private void func_opt_declaration_list() {
        int pointer = nextTokenPosition;
        lexer();
        if (!(isToken("integer") || isToken("boolean") || isToken("real"))) {
            nextTokenPosition--;
        } else {
            nextTokenPosition--;
            func_declaration_list();
        }
    }

    private void func_declaration_list() {
        func_declaration();
        lexer();
        if (isToken(";")) {
            lexer();
            if (currentToken != null) {
                if (!(currentToken.tokenType == LexicalAnalyzer.Type.IDENTIFIER || isToken("if") || isToken("return") || isToken("print") || isToken("read") || isToken("while"))) {
                    nextTokenPosition--;
                    func_declaration_list();
                } else {
                    nextTokenPosition--;
                }
            }
        }
    }

    private void func_declaration() {
        func_qualifiers();
        func_ids();
    }

    private void func_ids() {
        int pointer = nextTokenPosition;
        lexer();
        if (currentToken.tokenType == LexicalAnalyzer.Type.IDENTIFIER) {
            Token save = currentToken;
            lexer();
            if (isToken(")") || isToken(";") || isToken(":") || isToken("]")) {
                nextTokenPosition--;
                if (isRead) {
                    int index = findSymbol(save.content);
                    if (index < 0) {
                        errorMessage = "ERROR at line " + save.lineNumber + " - variable " + save.content + " had not been declared before used";
                        return;
                    } else {
                        insertInstruction(new Instruction(0, "STDIN", ""));
                        insertInstruction(new Instruction(0, "POPM", getAddress(save.content) + ""));
                        isRead = false;
                    }
                } else if (qualifier_temp.trim().equals("integer") || qualifier_temp.trim().equals("real") || qualifier_temp.trim().equals("boolean")) {
                    int insertedIndex = insertSymbol(new Symbol(tokens.get(nextTokenPosition - 1).content, 0, qualifier_temp, tokens.get(nextTokenPosition - 1).lineNumber));

                }

                //
            } else if (isToken(",")) {
                // nextTokenPosition--;
                if (isRead) {
                    int index = findSymbol(save.content);
                    if (index < 0) {
                        errorMessage = "ERROR at line " + save.lineNumber + " - variable " + save.content + " had not been declared before used";
                        return;
                    } else {
                        insertInstruction(new Instruction(0, "STDIN", ""));
                        insertInstruction(new Instruction(0, "POPM", getAddress(save.content) + ""));
                        func_ids();
                    }
                } else if (qualifier_temp.trim().equals("integer") || qualifier_temp.trim().equals("real") || qualifier_temp.trim().equals("boolean")) {
                    int insertedIndex = insertSymbol(new Symbol(tokens.get(nextTokenPosition - 2).content, 0, qualifier_temp, tokens.get(nextTokenPosition - 2).lineNumber));
                    func_ids();
                } else {
                    nextTokenPosition--;
                }
            }
        } else {
            nextTokenPosition = pointer;
        }

    }

    private void func_statement_list() {
        int pointer = nextTokenPosition;
        func_statement();
        lexer();
        if ((isToken("}") && nextTokenPosition < (tokens.size())) || (isToken("$$") && nextTokenPosition == (tokens.size()))) {
            nextTokenPosition--;
// ok
        } else {
            nextTokenPosition--;
            func_statement_list();
        }

    }

    private void func_statement() {
        lexer();
        Token save = currentToken;
        nextTokenPosition--;
        if (save == null) {
            return;
        }
        if (save.content.equals("{")) {
            func_compound();
        } else if (save.tokenType == LexicalAnalyzer.Type.IDENTIFIER) {
            func_assign();
        } else if (save.content.equals("if")) {
            func_if();
        } else if (save.content.equals("print")) {
            func_write();
        } else if (save.content.equals("read")) {
            func_read();
        } else if (save.content.equals("while")) {
            System.out.println("WHILE");
            func_while();
        } else {
            return;
        }

    }

    private void func_compound() {
        lexer();
        if (isToken("{")) {
            func_statement_list();
            lexer();
            if (isToken("}")) {
                return;
            }
        }
    }

    private void func_assign() {
        int pointer = nextTokenPosition;
        lexer();
        if (currentToken.tokenType == LexicalAnalyzer.Type.IDENTIFIER) {
            Token save = currentToken;
            int index = findSymbol(save.content);
            if (index < 0) {
                nextTokenPosition = tokens.size();
                if (errorMessage.equals("")) {
                    errorMessage = "ERROR at line " + save.lineNumber + " - variable " + save.content + " had not been declared before used";
                }
                return;
            } else {
                acceptedType = symbols.get(index).getType();
                lexer();
                if (isToken(":=")) {
                    func_expression();
                    insertInstruction(new Instruction(0, "POPM", "" + getAddress(save.content)));
                    getAddress(save.content);
                    lexer();
                    if (isToken(";")) {
                        // return true;
                        return;
                    }
                }
            }
        }
        nextTokenPosition = pointer;

        //return false;
    }

    private void func_return() {

    }

    private void func_write() {
        int pointer = nextTokenPosition;
        lexer();
        if (currentToken.tokenType == LexicalAnalyzer.Type.KEYWORD && isToken("print")) {

            lexer();
            if (isToken("(")) {
                func_expression();
                lexer();
                if (isToken(")")) {
                    lexer();
                    if (isToken(";")) {
                        insertInstruction(new Instruction(0, "STDOUT", ""));
                        // successfully
                    }
                }
            }
        } else {
            nextTokenPosition = pointer;
        }
    }

    private void func_read() {
        int pointer = nextTokenPosition;
        lexer();
        if (isToken("read")) {
            lexer();
            if (isToken("(")) {
                isRead = true;
                func_ids();
                lexer();
                if (isToken(")")) {
                    lexer();
                    if (isToken(";")) {
                        return;
                    }
                }
            }
            isRead = false;
        } else {
            nextTokenPosition = pointer;
            isRead = false;
            //return false;
        }
        //return false;
    }

    private void func_while() {
        int pointer = nextTokenPosition;
        lexer();
        if (isToken("while")) {
            insertInstruction(new Instruction(0, "LABEL", ""));
            int addr = this.instructions.size();
            lexer();
            if (isToken("(")) {
                func_condition();
                lexer();
                if (isToken(")")) {
                    func_statement();
                    insertInstruction(new Instruction(0, "JUMP", "" + addr));
                    back_patch();
                } else {
                    nextTokenPosition--;
                    return;
                }
            }
        }

    }

    private void func_condition() {
        acceptedType = "";
        func_expression();
        lexer();
        Token save = currentToken;
        String token = currentToken.content;
        func_expression();

        if (token.equals("<")) {
            insertInstruction(new Instruction(0, "LES", ""));

        } else if (token.equals(">")) {
            insertInstruction(new Instruction(0, "GRT", ""));
        } else if (token.equals(">")) {
            insertInstruction(new Instruction(0, "GRT", ""));
        } else if (token.equals("=")) {
            insertInstruction(new Instruction(0, "EQU", ""));
        } else if (token.equals("=>")) {
            insertInstruction(new Instruction(0, "GET", ""));
        } else if (token.equals("<=")) {
            insertInstruction(new Instruction(0, "LET", ""));
        } else if (token.equals("!=")) {
            insertInstruction(new Instruction(0, "NEQ", ""));
        } else {

        }
        insertInstruction(new Instruction(0, "JUMPZ", "TO-BE-UPDATED"));
        push_jumpstack();
    }

    private void func_relop() {
        lexer();
        if (isToken("=")) {

        } else if (isToken(">")) {

        } else if (isToken("<")) {

        } else if (isToken("=>")) {

        } else if (isToken("<=")) {

        } else if (isToken("!=")) {

        } else {
            nextTokenPosition--;
            return;
        }

    }

    private void func_factor() {
        int pointer = nextTokenPosition;
        lexer();

        if (isToken("-")) {
            func_factor();
        } else if (isToken("(")) {
            func_expression();
            lexer();
            if (isToken(")")) {
                //return true;
            } else {
                //return false;
            }

        } else if (isToken("true") || isToken("false")) {
            if (acceptedType.equals("")) {
                acceptedType = "boolean";
            }
            if (!acceptedType.equals("boolean")) {
                errorMessage = "ERROR at line " + currentToken.lineNumber + " TOKEN: '" + currentToken.content + "' - Types boolean and " + acceptedType + " are mismatched";
                return;
            }

        } else if (currentToken.tokenType == LexicalAnalyzer.Type.INTEGER) {
            if (acceptedType.equals("")) {
                acceptedType = "integer";
            }
            if (!(acceptedType.equals("integer"))) {
                errorMessage = "ERROR at line " + currentToken.lineNumber + " TOKEN: '" + currentToken.content + "' - Types integer and " + acceptedType + " are mismatched";
                return;
            }
            insertInstruction(new Instruction(0, "PUSHI", currentToken.content + ""));
        } else if (currentToken.tokenType == LexicalAnalyzer.Type.REAL) {
            if (acceptedType.equals("")) {
                acceptedType = "real";
            }
            if (!(acceptedType.equals("real"))) {
                errorMessage = "ERROR at line " + currentToken.lineNumber + " TOKEN: '" + currentToken.content + "' - Types real and " + acceptedType + " are mismatched";
                return;
            }
            insertInstruction(new Instruction(0, "PUSHI", currentToken.content + ""));
        } else if (currentToken.tokenType == LexicalAnalyzer.Type.IDENTIFIER) {
            int index = findSymbol(currentToken.content);
            if (index < 0) {
                errorMessage = "ERROR at line " + currentToken.lineNumber + " - variable '" + currentToken.content + "' had not been declared before used";
                return;
            } else {
                String type = symbols.get(index).getType();
                 if (acceptedType.equals("")) {
                acceptedType = type;
                   }
                if (acceptedType.equals("real")) {

                    if (!(type.equals("real"))) {
                        errorMessage = "ERROR at line " + currentToken.lineNumber + " TOKEN: '" + currentToken.content + "' - Types " + type + " and " + acceptedType + " are mismatched";
                        return;
                    }
                } else if (acceptedType.equals("integer")) {

                    if (!(type.equals("integer"))) {
                        errorMessage = "ERROR at line " + currentToken.lineNumber + " TOKEN: '" + currentToken.content + "' - Types " + type + " and " + acceptedType + " are mismatched";
                        return;
                    }
                } else if (!(type.equals("boolean"))) {
                    errorMessage = "ERROR at line " + currentToken.lineNumber + " TOKEN: '" + currentToken.content + "' - Types " + type + " and " + acceptedType + " are mismatched";
                    return;
                }
                insertInstruction(new Instruction(0, "PUSHM", getAddress(currentToken.content) + ""));
            }
        }
    }

    private void func_if() {
        lexer();
        if (isToken("if")) {
            lexer();
            if (isToken("(")) {
                func_condition();
                lexer();
                if (isToken(")")) {
                    func_statement();
                    func_if_prime();
                    back_patch();

                } else {
                    nextTokenPosition--;
                    return;
                }
            }
        }

    }

    private void func_if_prime() {
        lexer();
        if (isToken("endif")) {
            return;
        } else if (isToken("else")) {
            func_statement();
            lexer();
            if (isToken("endif")) {
                return;
                // return true;
            } else {
                nextTokenPosition--;
                return;
            }
        }
    }

    private void func_expression() {
        func_term();
        func_expression_prime();
    }

    private void func_expression_prime() {
        lexer();
        if (isToken("+")) { //ADD
            func_term();
            insertInstruction(new Instruction(0, "ADD", ""));
            func_expression_prime();
        } else if (isToken("-")) { //SUB
            func_term();
            insertInstruction(new Instruction(0, "SUB", ""));
            func_expression_prime();
        } else {
            nextTokenPosition--;
        }
    }

    private void func_term() {
        func_factor();
        func_term_prime();
    }

    private void func_term_prime() {
        lexer();
        if (isToken("*")) {
            func_factor();
            func_term_prime();
            insertInstruction(new Instruction(0, "MUL", ""));
        } else if (isToken("/")) {
            //DIV
            func_factor();
            func_term_prime();
            insertInstruction(new Instruction(0, "DIV", ""));
        } else {
            nextTokenPosition--;
        }

    }

    private boolean isToken(String token) {
        if (currentToken != null) {
            return currentToken.content.trim().equals(token);
        }
        return false;
    }

    private int getAddress(String identifier) {
        for (Symbol s : symbols) {
            if (s.getIdentifier().equals(identifier)) {
                return s.getMemoryLocation();
            }
        }
        return -1;
    }

    private void printInstructions() {
        int i = 1;
        for (Instruction instruction : instructions) {
            System.out.println(instruction);
            i++;
        }   
    }

    private void push_jumpstack() {
        jumpstack = instructions.size() - 1;
    }

    private int pop_jumstack() {
        return jumpstack;
    }

    private void back_patch() {
        instructions.get(jumpstack).setOperand(instructions.size() + 1 + "");
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public static void main(String[] args) {

        AssemblyGenerator assemblyGenerator = new AssemblyGenerator();
        //System.out.println(assemblyGenerator.insertSymbol(new Symbol("sumD", 0, null)));
        //System.out.println(assemblyGenerator.symbols.get(3));
        //System.out.println("" + assemblyGenerator.getSymbols().contains(new Symbol("sumD", 0, null)));
        //System.out.println("" + assemblyGenerator.getSymbols().s);

        //assemblyGenerator.input("integer x,y,z, bc, nd; boolean h,n; ", 1); //func_opt_declaration_list()
        //assemblyGenerator.input("read (a,b,c); print(a+b);$$", 1); //func_statement_list
        // assemblyGenerator.func_statement_list();
        // assemblyGenerator.func_opt_declaration_list();
        //assemblyGenerator.input("$$ $$ integer a,b,c;a:=(c+1); $$", 1);
        //assemblyGenerator.input("$$ $$ integer a,b,c; if(a<b) a:=c; $$", 1);
        //assemblyGenerator.input("$$ $$ integer i,max; {read(i,max); while(i!=max){ i:=i+max; print(i);  } $$", 1);
        assemblyGenerator.input("$$ $$ integer i, max, sum; real yes;  yes:=(max/2); max:=(1+sum);  while (i <max)  { sum := sum + i; i:= i + 1; } print (sum+max); $$    ", 1);
        assemblyGenerator.func_rat16f();

        String error_msg = assemblyGenerator.getErrorMessage();
        if (error_msg.equals("")) {
           
            assemblyGenerator.printInstructions();
        } else {
           
            System.out.println("\n" + assemblyGenerator.getErrorMessage());
        }
        /*
         System.out.println("\n" + assemblyGenerator.getErrorMessage());

         assemblyGenerator.printSymbols();
         System.out.println("\n----------------------------");
         assemblyGenerator.printInstructions(); */

    }
}
