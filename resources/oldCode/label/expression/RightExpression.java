/*
 * This file is distributed according to the GNU Lesser Public Licence.
 * Created on 31 mars 2005
 *
 */
package jsesh.transducer.label.expression;

/**
 * An expression on the right of a rewriting rule.
 * <p> Note on OO representation : 
 * There is a strong structural difference about what can be on 
 * the left and on the right of a rule, hence the two classes.
 * Now, a Variable might be found on both sides, with virtually no 
 * difference. However, this would require multiple inheritance (or using interfaces).
 * As, in fact, variables are the only type of expression which can occur on both sides,
 * it's simpler to have two separate hierarchies.
 * @author Serge Rosmorduc
 */
public class RightExpression {

}
