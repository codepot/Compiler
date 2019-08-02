/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Dell
 */
public class Token {

    public final LexicalAnalyzer.Type tokenType;
    public final String content;
    public final int lineNumber;
    public  String rules = "";

    public Token(LexicalAnalyzer.Type tokenType, String content, int lineNumber) {
        this.tokenType = tokenType;
        this.content = content;
        this.lineNumber = lineNumber;
    }

    public String toString() {
        return String.format("%-12s %-30s", tokenType.toString(), this.content);
    }

    public String toString2() {
       // return String.format("%-20s %-36s", "Token: " + tokenType.toString(), "Lexeme: " + this.content + "\t\t\t  line:"+lineNumber+rules);
        return String.format("%-20s %-36s", "Token: " + tokenType.toString(), "Lexeme: " + this.content +rules+"\n");
       
    }
    
     
    public void addRule(String rule){
        this.rules+="\n\t"+rule;
    }
}

class Symbol{
    private String identifier;
    private int memoryLocation;
    private String type;
    private int line;
    
    public Symbol(){
        
    }

    public Symbol(String identifier, int memoryLocation, String type, int line) {
        this.identifier = identifier;
        this.memoryLocation = memoryLocation;
        this.type = type;
        this.line = line;
    }

    @Override
    public boolean equals(Object obj) {
        return identifier.equals(((Symbol)obj).identifier);        
    } 

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getMemoryLocation() {
        return memoryLocation;
    }

    public void setMemoryLocation(int memoryLocation) {
        this.memoryLocation = memoryLocation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return String.format("%-12s %-8s %-8s", identifier, memoryLocation, type);
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line = line;
    }    
}

class Instruction {
    public int address;
    public String operator;
    public String operand;

    public Instruction(int address, String operator, String operand) {
        this.address = address;
        this.operator = operator;
        this.operand = operand;
    }
    
    public Instruction(){
        
    }

    public int getAddress() {
        return address;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOperand() {
        return operand;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }   
    
    public String toString() {
        return String.format("%-4s %-8s %-8s", address, operator, operand);
    }
}


