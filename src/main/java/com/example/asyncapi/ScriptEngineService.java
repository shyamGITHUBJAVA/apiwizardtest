package com.example.asyncapi;

import org.graalvm.polyglot.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScriptEngineService {

    private static final ThreadLocal<Context> jsContext = ThreadLocal.withInitial(() ->
            Context.newBuilder("js")
                    .allowAllAccess(true)
                    .build());

    private static final ThreadLocal<Context> pythonContext = ThreadLocal.withInitial(() ->
            Context.newBuilder("python")
                    .allowAllAccess(true)
                    .build());

    public Object runScript(String language, String script) {
        try {
            String lang = language.toLowerCase();
            Context context = switch (lang) {
                case "js", "javascript" -> jsContext.get();
                case "python" -> pythonContext.get();
                default -> throw new IllegalArgumentException("Unsupported language: " + language);
            };

            Value result = context.eval(lang, script);
            return convertResult(result);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    public Object runScriptFromFile(String language, Path scriptFile) throws IOException {
        String script = Files.readString(scriptFile);
        return runScript(language, script);
    }

    private Object convertResult(Value value) {
        if (value.isHostObject()) {
            return value.asHostObject();
        } else if (value.isBoolean()) {
            return value.asBoolean();
        } else if (value.isNumber()) {
            return value.as(Number.class);
        } else if (value.isString()) {
            return value.asString();
        } else {
            return value.toString();
        }
    }

    // Example usage
    public static void main(String[] args) {
        ScriptEngineService engine = new ScriptEngineService();

        Object jsResult = engine.runScript("js", "Math.sqrt(16)");
        System.out.println("JS Result: " + jsResult);

        Object pyResult = engine.runScript("PYTHON", "import math\nmath.sqrt(25)");
        System.out.println("Python Result: " + pyResult);

        // Example of loading from file
        /*
        try {
            Object fileScriptResult = engine.runScriptFromFile("Python", Path.of("script.py"));
            System.out.println("Result from file: " + fileScriptResult);
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
