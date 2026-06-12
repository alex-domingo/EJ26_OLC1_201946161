package olc1.golite.symbols;

import java.util.HashMap;
import java.util.Map;

import olc1.golite.reports.GoliteError;
import olc1.golite.reports.SemanticException;

public class Environment {

    private final Environment parent;
    private final Map<String, SymbolEntry> symbols = new HashMap<>();

    public Environment(Environment parent) {
        this.parent = parent;
    }

    public Environment getParent() {
        return parent;
    }

    // Declara en el ambito ACTUAL, error si ya existe en este mismo ambito
    public void declare(SymbolEntry entry) {
        if (symbols.containsKey(entry.getName())) {
            throw new SemanticException(new GoliteError(
                    "Semantico",
                    "La variable '" + entry.getName() + "' ya fue declarada en este ambito",
                    entry.getLine(), entry.getColumn()));
        }
        symbols.put(entry.getName(), entry);
    }

    // Busca recorriendo la cadena de ambitos, error si no existe en ninguno
    public SymbolEntry get(String name, int line, int column) {
        Environment env = this;
        while (env != null) {
            SymbolEntry entry = env.symbols.get(name);
            if (entry != null) {
                return entry;
            }
            env = env.parent;
        }
        throw new SemanticException(new GoliteError(
                "Semantico",
                "La variable '" + name + "' no esta definida en este contexto",
                line, column));
    }
}
