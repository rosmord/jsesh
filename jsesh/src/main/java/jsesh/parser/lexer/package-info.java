/// Lexical analysis for the Manuel de Codage format.
/// 
/// # Using the lexer
/// Gets the [MdcLexicon] singleton using [MdcLexicon#instance()] and use it to build a [MdcLexer].
/// 
/// Typically:
/// 
/// ~~~
/// MdcLexer lexer = MdcLexicon.instance().newLexer("p*t:pt");
/// Optional<MdcSymbol> next = lexer.nextSymbol();
/// ~~~
/// 
/// # Extending the lexicon
/// 
/// If you need to add new symbols to the lexicon:
/// 
/// 1. add an entry in the [MDCToken] enum, being aware of that the order of the entries matters in case of ties.
/// 2. add a rule in [MdcLexicon] (in the private `build` method) to describe the token
/// 3. add a mirror MdcSymbolCode entry in [MdcSymbolCode] enum - those are the enums seen by the parser.
/// 4. possibly modify the [jsesh.parser.lexer.MdcLexer] to produce the correct symbol.
package jsesh.parser.lexer;
