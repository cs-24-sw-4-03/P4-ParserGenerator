package org.RaceGuard;
// import ANTLR's runtime libraries
import org.RaceGuard.AstNodes.AstNode;
import org.RaceGuard.AstNodes.InitNode;
import org.RaceGuard.symbols.SymbolTable;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RaceGuard {
    /***
     *
     * @param args - the file path to the source code. Default is '/input/'
     * @throws Exception
     */
    private static final String DEFAULT_INPUT_PATH = System.getProperty("user.dir") + "/input/";

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected one argument with file path.");
        }
        Path source = Paths.get(DEFAULT_INPUT_PATH, args[0]);
        validateSource(source);

        CharStream input = CharStreams.fromPath(source); // Load the input file
        RaceGuardLexer lexer = new RaceGuardLexer(input); // Create a lexer that feeds off of input CharStream
        CommonTokenStream tokens = new CommonTokenStream(lexer);  // Create a buffer of tokens pulled from the lexer
        RaceGuardParser parser = new RaceGuardParser(tokens);   // Create a parser that feeds off the tokens buffer
        ParseTree tree = parser.init(); // Begin parsing at init node

        if (parser.getNumberOfSyntaxErrors() > 0) {
            System.err.println("Syntax errors detected.");
            System.exit(1);
        }

        TypeContainer typeContainer = new TypeContainer();
        RaceGuardBaseVisitor<AstNode> visitor = new AstVisitor(typeContainer);
        InitNode ast=(InitNode) tree.accept(visitor);

        printAST(ast, args);
        printCST(tree, parser);

        SymbolTable symbolTable = new SymbolTable();

        SymbolTableVisitor symbolTableVisitor = new SymbolTableVisitor(symbolTable);
        ast.accept(symbolTableVisitor);

        MethodVisitor methodVisitor = new MethodVisitor(symbolTable);
        ast.accept(methodVisitor);

        TypeVisitor typeVisitor = new TypeVisitor(symbolTable, typeContainer);
        ast.accept(typeVisitor);
        printAST(ast, args);


        boolean hasErrors = false;

        if (!symbolTableVisitor.getExceptions().isEmpty()) {
            System.err.println("Errors detected in variable scoping");
            printExceptions(symbolTableVisitor.getExceptions());
            hasErrors = true;
        }

        if (!methodVisitor.getExceptions().isEmpty()) {
            System.err.println("Errors detected in method scoping");
            printExceptions(methodVisitor.getExceptions());
            hasErrors = true;
        }

        if(!typeVisitor.getExceptions().isEmpty()) {
            System.err.println("Errors detected in type checking.");
            printExceptions(typeVisitor.getExceptions());
            hasErrors = true;
        }
        if (hasErrors) {
            System.exit(1);
        }

        generateCode(ast,symbolTable);

    }
    private static void validateSource(Path source) throws IOException {
        if (!Files.exists(source)) {
            throw new IOException("File source not found at path: " + source);
        }
        String extension = getFileExtension(source);
        if (!"par".equals(extension)) {
            throw new IOException("Wrong file extension, expected .par");
        }
    }

    /**
     * Get the file extension of a path
     * @param path
     * @return the file extension or an empty string if no extension
     */
    private static String getFileExtension(Path path) {
        String fileName = path.getFileName().toString();
        int lastDot = fileName.lastIndexOf(".");
        return lastDot > 0 ? fileName.substring(lastDot + 1) : "";
    }

    private static void printAST(AstNode ast, String[] args) {
        System.out.println("AST:");
        AstPrintVisitor astPrintVisitor = new AstPrintVisitor();
        String printOption = args.length > 0 ? args[0] : "";
        astPrintVisitor.visit(0, ast, printOption); // Optionally print parent node info
    }

    private static void printCST(ParseTree tree, RaceGuardParser parser) {
        System.out.println("CST:");
        System.out.println(tree.toStringTree(parser)); // Print LISP-style tree
    }

    private static void printExceptions(List<RuntimeException> exceptions){
        System.out.println("\nExceptions:");
        for(RuntimeException e : exceptions){
            System.out.println(e.toString());
        }

    }

    private static void generateCode(InitNode ast, SymbolTable symbolTable) throws IOException {
        CodeGenVisitor codeGenVisitor = new CodeGenVisitor(symbolTable);
        ast.accept(codeGenVisitor);
    }

}
