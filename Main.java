import java.util.Scanner;
import java.util.Arrays;
import java.util.ArrayList;
public class Main {
    public static void main(String[] args) {
        // Starting a new shell process
        new Shell();
    }
}
class Shell {
    private String snippet;
    private ArrayList<Token> tokens = new ArrayList<>();
    public Shell() {
        Scanner scan = new Scanner(System.in);
        while (true) {
            // Grabbing the snippet from the user
            System.out.print(">>");
            this.snippet = scan.nextLine();

            // Evaluating the snippet
            if (this.snippet.equals("exit()")) break;
            else if (!(this.snippet.replace(" ", "").equals(""))) eval_snippet();
        }
        scan.close();
    }
    private void eval_snippet() {
        tokenize();
        lex_tokens();
    }
    private void tokenize() {
        boolean split = true; char paren_type = ' ';
        StringBuilder token_name = new StringBuilder();
        ArrayList<Token> tokens = new ArrayList<>();
        for (char ch : this.snippet.toCharArray()) {
            if (split) {
                if (ch == '"' || ch == '\'') {
                    paren_type = ch;
                    split = false;
                }
                if (Character.isAlphabetic(ch) || Character.isDigit(ch)) {
                    token_name.append(ch);
                    continue;
                }
                if (!(token_name.length() == 0)) {
                    if (!(ch == '_')) {
                        tokens.add(new Token(token_name.toString()));
                        token_name.delete(0, token_name.length());
                    }
                    else token_name.append(ch);
                }
                if (!(ch == ' ' || ch == '_' || ch == '"' || ch == '\'')) {
                    tokens.add(new Token(Character.toString(ch)));
                }
                continue;
            }
            if (ch == paren_type) {
                token_name.insert(0, paren_type);
                token_name.append(ch);
                tokens.add(new Token(token_name.toString()));
                token_name.delete(0, token_name.length());
                split = true;
                continue;
            }
            token_name.append(ch);
        }
        if (!(token_name.toString().replace(" ", "").equals(""))) {
            tokens.add(new Token(token_name.toString()));
        }
        this.tokens = tokens;
    }
    private void lex_tokens() {
        ArrayList<Token> tokens = this.tokens;

        for (Token tok : tokens) {
            if (tok.name.charAt(0) == '"' && tok.name.charAt(tok.name.length() - 1) == '"' ||
                tok.name.charAt(0) == '\'' && tok.name.charAt(tok.name.length() - 1) == '\'') tok.type = "string";
            else {
                switch (tok.name) {
                    case "print":
                    case "exit":
                        tok.type = "instruction";
                        break;
                    case "var":
                    case "const":
                        tok.type = "declaration";
                        break;
                    case "=":
                    case "+":
                    case "-":
                    case "*":
                    case "%":
                        tok.type = "operator";
                        break;
                    default:
                        for (char ch : tok.name.toCharArray()) {
                            if (Character.isAlphabetic(ch)) {
                                tok.type = "variable";
                                break;
                            }
                        }
                        if (tok.type.equals("None")) tok.type = "integer";
                        break;
                }
            }
        }
        System.out.println(tokens);
    }
    static class Token {
        public String name;
        public String type = "None";
        public Token(String name) {this.name = name;}
        public Token(String name, String type) {this.name = name;this.type = type;}
        public String toString() {return this.name + " " + this.type;}
    }
}
