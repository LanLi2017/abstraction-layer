/** Code Generated by NADEEF.*/
import qa.qcri.nadeef.core.datamodel.*;
import java.util.*;

public class Rule1916557363 extends PairTupleRule {
    protected List<Column> leftHandSide = new ArrayList();
    protected List<Column> rightHandSide = new ArrayList();

    public Rule1916557363() {}

    @Override
    public void initialize(String id, List<String> tableNames) {
        super.initialize(id, tableNames);
        leftHandSide.add(new Column("TB_TEMP.po_city_state_zip")); 

        rightHandSide.add(new Column("TB_TEMP.zip")); 

    }

    /**
     * Default horizontal scope operation.
     * @param tables input tables.
     * @return filtered tables.
     */
    @Override
    public Collection<Table> horizontalScope(Collection<Table> tables) {
        tables.iterator().next().project(leftHandSide).project(rightHandSide);
        return tables;
    }

    /**
     * Default block operation.
     * @param tables a collection of tables.
     * @return a collection of blocked tables.
     */
    @Override
    public Collection<Table> block(Collection<Table> tables) {
        Table table = tables.iterator().next();
        Collection<Table> groupResult = table.groupOn(leftHandSide);
        return groupResult;
    }

    /**
     * Default group operation.
     *
     * @param tables input tables
     */
    @Override
   	public void iterator(Collection<Table> tables, IteratorResultHandler output) {
        Table table = tables.iterator().next();
        ArrayList<TuplePair> result = new ArrayList();
        table.orderBy(rightHandSide);
        int pos1 = 0, pos2 = 0;
        boolean findViolation = false;

        // ---------------------------------------------------
        // two pointer loop via the block. Linear scan
        // ---------------------------------------------------
        while (pos1 < table.size()) {
            findViolation = false;
            for (pos2 = pos1 + 1; pos2 < table.size(); pos2 ++) {
                Tuple left = table.get(pos1);
                Tuple right = table.get(pos2);

                findViolation = !left.hasSameValue(right);

                // generates all the violations between pos1 - pos2.
                if (findViolation) {
                    for (int i = pos1; i < pos2; i ++) {
                        for (int j = pos2; j < table.size(); j++) {
                           TuplePair pair = new TuplePair(table.get(i), table.get(j));
                           output.handle(pair);
                        }
                    }
                    break;
                }
            }
            pos1 = pos2;
        }
    }

    /**
     * Detect method.
     * @param tuplePair tuple pair.
     * @return violation set.
     */
    @Override
    public Collection<Violation> detect(TuplePair tuplePair) {
        List<Violation> result = new ArrayList();
        Tuple left = tuplePair.getLeft();
        Tuple right = tuplePair.getRight();

        // Comparing tuples is redundant, but needed for correctness of detect function,
		// when used in incremental detection
		if(!left.hasSameValue(right)){
	        Violation violation = new Violation(getRuleName());
	        violation.addTuple(left);
	        violation.addTuple(right);
	        result.add(violation);
	    }

        return result;
    }

    /**
     * Repair of this rule.
     *
     * @param violation violation input.
     * @return a candidate fix.
     */
    @Override
    public Collection<Fix> repair(Violation violation) {
        List<Fix> result = new ArrayList();
        Collection<Cell> cells = violation.getCells();
        HashMap<Column, Cell> candidates = new HashMap<Column, Cell>();
        int vid = violation.getVid();
        Fix fix;
        Fix.Builder builder = new Fix.Builder(violation);
        for (Cell cell : cells) {
            Column column = cell.getColumn();
            if (rightHandSide.contains(column)) {
                if (candidates.containsKey(column)) {
                    // if the right hand is already found out in another tuple
                    Cell right = candidates.get(column);
                    fix = builder.left(cell).right(right).build();
                    result.add(fix);
                } else {
                    // it is the first time of this cell shown up, put it in the
                    // candidate and wait for the next one shown up.
                    candidates.put(column, cell);
                }
            }
        }
        return result;
    }
}