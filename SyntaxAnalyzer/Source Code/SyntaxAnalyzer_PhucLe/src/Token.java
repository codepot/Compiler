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

    public final LexicalAnalyser.Type tokenType;
    public final String content;
    public final int lineNumber;
    public  String rules = "";

    public Token(LexicalAnalyser.Type tokenType, String content, int lineNumber) {
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
