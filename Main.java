import java.util.Scanner;
import java.util.ArrayList;
public class Main {
    public static void main(String[] args) {
        new Shell();    // Creating a new Shell
    }
}
class Shell {
    private String snippet;     // The current code-snippet grabbed from the Shell interface
    // A list of the tokens created from the current snippet
    private ArrayList<Token> tokens = new ArrayList<>();
    public Shell() {
        Scanner scan = new Scanner(System.in);
        while (true) {
            // Grabbing the snippet from the user
            System.out.print(">>");
            this.snippet = scan.nextLine();

            // Evaluating the snippet
            if (this.snippet.equals("exit")) break;
            // If snippet isn't empty, then we evaluate it
            else if (!(this.snippet.replace(" ", "").equals(""))) eval_snippet();
        }
        scan.close();
    }
    private void eval_snippet() {
        tokenize();
        lex_tokens();
        System.out.println(this.tokens);
    }
    private void tokenize() {

        boolean split = true;   // Whether string is split by spaces and symbols
        char paren_type = ' ';  // The type of parentheses used for the detected string
        StringBuilder token_name = new StringBuilder();
        ArrayList<Token> tokens = new ArrayList<>();
        for (char ch : this.snippet.toCharArray()) {
            if (split) {    // When splitting
                // Looking for the beginning of a string
                if (ch == '"' || ch == '\'') {
                    // Append paren variant to token_name, set split, then continue this tokenization cycle
                    paren_type = ch;
                    split = false;
                }
                // Append all Alphanumeric Characters to token_name
                if (Character.isAlphabetic(ch) || Character.isDigit(ch)) {
                    token_name.append(ch);
                    continue;
                }
                // -- Reached when non-alnum characters is encountered --
                // Doesn't append token of token_name when variable is empty
                if (!(token_name.length() == 0)) {
                    // Variable names are not to be split by underscores
                    if (!(ch == '_')) {
                        // Add token then delete contents of token_name
                        tokens.add(new Token(token_name.toString()));
                        token_name.delete(0, token_name.length());
                    }
                    else token_name.append(ch);
                }
                // Doesn't add token of current char if it matches any of the ones below
                if (!(ch == ' ' || ch == '_' || ch == '"' || ch == '\'')) {
                    tokens.add(new Token(Character.toString(ch)));
                }
                continue;
            }
            // When paren matching paren_type is encountered
            if (ch == paren_type) {
                // Inserts paren_type into first char of token_name, appends ch to token_name
                token_name.insert(0, paren_type);
                token_name.append(ch);
                // Adds token of token_name then deletes contents of token_name
                tokens.add(new Token(token_name.toString()));
                token_name.delete(0, token_name.length());
                split = true;
                continue;
            }
            token_name.append(ch);  // Otherwise append ch to token_name
        }
        // For when loop ends without appending potential token inside of token_name
        if (!(token_name.toString().replace(" ", "").equals(""))) {
            tokens.add(new Token(token_name.toString()));
        }
        this.tokens = tokens;
    }
    private void lex_tokens() {
        ArrayList<Token> tokens = this.tokens;

        for (Token tok : tokens) {
            // Sets Token type to string if fist and last char are '"' or "'"
            if (tok.name.charAt(0) == '"' && tok.name.charAt(tok.name.length() - 1) == '"' ||
                tok.name.charAt(0) == '\'' && tok.name.charAt(tok.name.length() - 1) == '\'') tok.type = "string";
            else {
                switch (tok.name) {
                    case "print":
                    case "println":
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
                    case "|":
                    case "<":
                    case ">":
                    case "~":
                        tok.type = "operator";
                        break;
                    case ",":
                    case ";":
                        tok.type = "separator";
                        break;
                    case "(":
                    case "[":
                    case "{":
                        tok.type = "opening paren";
                        break;
                    case ")":
                    case "]":
                    case "}":
                        tok.type = "closing paren";
                        break;
                    default:
                        // If one alpha char is encountered, item must be a variable
                        for (char ch : tok.name.toCharArray()) {
                            if (Character.isAlphabetic(ch)) {
                                tok.type = "variable";
                                break;
                            }
                        }
                        // If token type is unset, then the token name was entirely numeric, making it an integer
                        if (tok.type.equals("None")) tok.type = "integer";
                        break;
                }
            }
        }
        // Lexing pre-lexed tokens for previously undetectable token types
        ArrayList<Token> fully_lexed = new ArrayList<>(); int skip = 0;
        for (int i = 0; i < tokens.size(); i++) {
            if (!(skip == 0)) {     // Needed to skip re-tokenizing the three tokens that were made into a float
                skip--;
                continue;
            }
            // If current token is integer. And there is a token one after the current token
            if (tokens.get(i).type.equals("integer") && i + 1 < tokens.size()) {
                // If token one after current is '.'. And there is a token two after the current token
                if (tokens.get(i + 1).name.equals(".") && i + 2 < tokens.size()) {
                    // If token two after current is integer
                    if (tokens.get(i + 2).type.equals("integer")) {
                        fully_lexed.add(new Token(
                            // Appends the three tokens from current to 2+ current as one token
                            tokens.get(i).name + tokens.get(i + 1).name + tokens.get(i + 2).name,
                            "float" // With the type 'float'
                        ));
                        skip = 2;
                        continue;
                    }
                }
            }
            // Appends current token
            fully_lexed.add(new Token(tokens.get(i).name, tokens.get(i).type));
        }
        this.tokens = fully_lexed;
    }
    static class Token {
        public String name;
        public String type = "None";
        public Token(String name) {this.name = name;}
        public Token(String name, String type) {this.name = name;this.type = type;}
        
        // TODO:toString is only used for development purposes, nothing should depend on it
        public String toString() {return this.name + " " + this.type;}
    }
}
