package com.cognitionis.jtimegraph.timegraph;

import java.text.*;
import java.util.*;
import com.cognitionis.utils_basickit.*;

/**
 * TimeGraph implementation
 *
 * TODO: The problem with this implementation is that cross-chain relations are
 * limited to before/after. If we add equal then we can first add a relation in
 * dry mode (no chain collapses) and if it is consistent then add it and
 * collapse chains if needed. TODO
 *
 * @author hector
 */
public class TimeGraph {

    public static final DateFormat iso = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static final String granul_days = "yyyy-MM-dd";
    public static final DateFormat granul_days2 = new SimpleDateFormat("yyyy-MM-dd");
    public static final String upper_bound = "9999-12-31T23:59:59";
    public static final String lower_bound = "0000-01-01T00:00:00";
    // metagraph contains a list of pointchains, if some chain becomes empty it can be either reused or deleted.
    public ArrayList<Chain> metagraph;
    // reference TimePoint
    public HashMap<String, TimePoint> entity_tp_map;
    // reference Entities (only used after the creation of the timegraph)
    //DEPRECATED: private HashMap<TimePoint, String> tp_entity_map; (INCLUDED IN TimePoint)
    //timex-refdate HashMap to easyly locate them in the map
    // useful for answering before 1999 (and 1999 is not in the graph) so we need to find all refs before 1999
    // ej: e1_s 1999
    public TreeMap<Date, String> date_entitypoint_map;

    public TimeGraph() {
        metagraph = new ArrayList<Chain>();
        entity_tp_map = new HashMap<String, TimePoint>();
        date_entitypoint_map = new TreeMap<Date, String>();
        //tp_entity_map = new HashMap<TimePoint, String>();
    }

    /*        ArrayList<Chain> metagraph_copy;
     TreeMap<Date, String> date_entitypoint_map_copy;
        
     public static ArrayList<Chain> copy_metagraph(ArrayList<Chain> orig_metagraph){
     ArrayList<Chain> metagraph_copy;
     return null;
     }

     public static HashMap<String, TimePoint> copy_entity_tp_map(HashMap<String, TimePoint> orig_entity_tp_map){
     HashMap<String, TimePoint> entity_tp_map_copy;
     return null;
     }
    
     public static TreeMap<Date, String> copy_date_entitypoint_map(TreeMap<Date, String> orig_date_entitypoint_map){
     TreeMap<Date, String> date_entitypoint_map_copy;
     return null;
     }    
    
  
     public static TimeGraph copy_timegraph_by_value(TimeGraph orig_tg){
     TimeGraph tg=new TimeGraph();
     tg.metagraph=TimeGraph.copy_metagraph(orig_tg.metagraph);        
     tg.entity_tp_map=TimeGraph.copy_entity_tp_map(orig_tg.entity_tp_map);        
     tg.date_entitypoint_map=TimeGraph.copy_date_entitypoint_map(orig_tg.date_entitypoint_map);        
     return tg;
     }

     public TimeGraph get_deep_copy(){
     TimeGraph tg=new TimeGraph();
     tg.metagraph=TimeGraph.copy_metagraph(metagraph);        
     tg.entity_tp_map=TimeGraph.copy_entity_tp_map(entity_tp_map);        
     tg.date_entitypoint_map=TimeGraph.copy_date_entitypoint_map(date_entitypoint_map);        
     return tg;
     }*/
    
    
    /**
     * Check if relation can be added in the timegraph (consistent). 
     * Add relation in a new chain or chains, check graph consistency, remove relation
     *
     * @param lid
     * @param entity1
     * @param greginterval1_s
     * @param greginterval1_e
     * @param entity2
     * @param greginterval2_s
     * @param greginterval2_e
     * @param relation
     * @return true if the relation was successfully added
     */
    /*
    public boolean IsAddRelationConsistent(String lid, String entity1, String greginterval1_s, String greginterval1_e, String entity2, String greginterval2_s, String greginterval2_e, String relation) {
        try {
            if (entity1 == null || entity2 == null || relation == null) {
                throw new Exception("ERROR: entity1 entity2 and relation must be not NULL.");
            }
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entities " + entity1 + "-" + entity2 + " relation " + relation + " (lid " + lid + ")\n");
            }
            String x1 = entity1 + "_s";
            String x2 = entity1 + "_e";
            String y1 = entity2 + "_s";
            String y2 = entity2 + "_e";

            // greg interval reference ommitted since this is jut a check

            if (greginterval1_s != null && greginterval1_e != null && greginterval2_s != null && greginterval2_e != null) {
                // TODO: check relation correspondence with the dates
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.err.println(entity1 + " (" + greginterval1_s + "-" + greginterval1_e + ") and " + entity2 + " (" + greginterval2_s + "-" + greginterval2_e + ") are both intervals.");
                }
            }
            if (relation.matches("(DURING|DURING_INV|IDENTITY)")) {
                relation = "SIMULTANEOUS";
            }

            // basic integrity check e.g., e1-e1 before
            if (entity1.equals(entity2) && !(relation.equals("IDENTITY") || relation.equals("SIMULTANEOUS"))) {
                System.err.println("Closure Violation: " + lid + " " + entity1 + "-" + entity2 + "(" + relation + ")");
            }

            // none of the entities exist
            if (!entity_tp_map.containsKey(x1) && !entity_tp_map.containsKey(x2) && !entity_tp_map.containsKey(y1) && !entity_tp_map.containsKey(y2)) {
                if (relation.matches("(BEFORE|IBEFORE|BEGINS|ENDS|OVERLAPS|IS_INCLUDED|SIMULTANEOUS)")) {
                    TODO ALL BELOW JUST ADD ALWAYS IN NEW CHAIN IF THERE IS ANY ENTITY THAT IS NEW.
                            IF IS JUST A NEW RELATION AMONG EXISTING ENTITIES PUT AND TRACK CONNECTIONS BUT DO NOT COLLAPSE (WE NEED TO ADD EQUALS CROSS-CHAIN-RELATION...)
                    could_be_added = addBothEntitiesInNewChain(x1, x2, y1, y2, relation);
                } else {
                    could_be_added = addBothEntitiesInNewChain(y1, y2, x1, x2, reverseRelationCategory(relation));
                }
            } else {
                // both entities exist
                if (entity_tp_map.containsKey(x1) && entity_tp_map.containsKey(x2) && entity_tp_map.containsKey(y1) && entity_tp_map.containsKey(y2)) {
                    if (relation.matches("(BEFORE|IBEFORE|BEGINS|ENDS|OVERLAPS|IS_INCLUDED|SIMULTANEOUS)")) {
                        could_be_added = addOnlyRelation(x1, x2, y1, y2, relation);
                    } else {
                        could_be_added = addOnlyRelation(y1, y2, x1, x2, reverseRelationCategory(relation));
                    }
                } // only one exists
                else {
                    // if it is the first make it the second one for simplification
                    if (entity_tp_map.containsKey(x1) && entity_tp_map.containsKey(x2) && !entity_tp_map.containsKey(y1) && !entity_tp_map.containsKey(y2)) {
                        relation = reverseRelationCategory(relation);
                        x1 = entity2 + "_s";
                        x2 = entity2 + "_e";
                        y1 = entity1 + "_s";
                        y2 = entity1 + "_e";
                    }
                    could_be_added = addOnlyFirstEntity(x1, x2, y1, y2, relation);
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (TimeGraph):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return false;
        }
        return true; // return could_be_added to avoid inconsitencies or true to only to break in uncaptured exceptions
    }*/

    
    /**
     * Add a relation between two entities (time intervals) in the timegraph
     *
     * @param lid
     * @param entity1
     * @param greginterval1_s
     * @param greginterval1_e
     * @param entity2
     * @param greginterval2_s
     * @param greginterval2_e
     * @param relation
     * @return true if the relation was successfully added
     */
    public boolean addRelation(String lid, String entity1, String greginterval1_s, String greginterval1_e, String entity2, String greginterval2_s, String greginterval2_e, String relation) {
        boolean can_be_added;
        try {
            // store a copy to restore timegraph if something fails, it is easier to implement a dry add and then collapse strategy
            // add equal cross chain relation if necessary
            // metagraph_copy.=Collections.cop;             entity_tp_map_copy;             date_entitypoint_map_copy;            

            if (entity1 == null || entity2 == null || relation == null) {
                throw new Exception("ERROR: entity1 entity2 and relation must be not NULL.");
            }

            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entities " + entity1 + "-" + entity2 + " relation " + relation + " (lid " + lid + ")\n");
            }

            String x1 = entity1 + "_s";
            String x2 = entity1 + "_e";
            String y1 = entity2 + "_s";
            String y2 = entity2 + "_e";

            if (greginterval1_s != null && greginterval1_e != null) {
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.err.println(entity1 + " is an interval (" + greginterval1_s + "-" + greginterval1_e + ")");
                }
                Date d1 = (Date) iso.parse(greginterval1_s);
                Date d2 = (Date) iso.parse(greginterval1_e);
                if (!d1.before(d2)) {
                    throw new Exception("Event " + entity1 + " associated begin date is posterior to end date.");
                }
                date_entitypoint_map.put((Date) iso.parse(greginterval1_s), x1);
                date_entitypoint_map.put((Date) iso.parse(greginterval1_e), x2);
            }

            if (greginterval2_s != null && greginterval2_e != null) {
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.err.println(entity2 + " is an interval (" + greginterval2_s + "-" + greginterval2_e + ")");
                }
                Date d1 = (Date) iso.parse(greginterval2_s);
                Date d2 = (Date) iso.parse(greginterval2_e);
                if (!d1.before(d2)) {
                    throw new Exception("Event " + entity2 + " associated begin date is posterior to end date.");
                }
                date_entitypoint_map.put((Date) iso.parse(greginterval2_s), y1);
                date_entitypoint_map.put((Date) iso.parse(greginterval2_e), y2);
            }

            if (greginterval1_s != null && greginterval1_e != null && greginterval2_s != null && greginterval2_e != null) {
                // check relation correspondence with the dates
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    System.err.println(entity1 + " (" + greginterval1_s + "-" + greginterval1_e + ") and " + entity2 + " (" + greginterval2_s + "-" + greginterval2_e + ") are both intervals.");
                }
            }

            // Allen's OVERLAP (o)  x1 < y1 < x2 < y2 and OVERLAPED_BY are not included in TimeML, although they could be INCLUDED and INCLUDED_BY...
            // This makes IDENTITY, DURING AND DURING_INV, EQUIVALENT TO SIMULTANEOUS
            // Then there are only 11 relations (6 with the inversion)
            // TODO: in the final timegraph use Allen relations...
            // TODO: in the wrapper take care of translating relations (however they are) to Allen names
            if (relation.matches("(DURING|DURING_INV|IDENTITY)")) {
                relation = "SIMULTANEOUS";
            }

            // basic integrity check e.g., e1-e1 before
            if (entity1.equals(entity2) && !(relation.equals("IDENTITY") || relation.equals("SIMULTANEOUS"))) {
                System.err.println("Closure Violation: " + lid + " " + entity1 + "-" + entity2 + "(" + relation + ")");
            }

            // none of the entities exist
            if (!entity_tp_map.containsKey(x1) && !entity_tp_map.containsKey(x2) && !entity_tp_map.containsKey(y1) && !entity_tp_map.containsKey(y2)) {
                if (relation.matches("(BEFORE|IBEFORE|BEGINS|ENDS|OVERLAPS|IS_INCLUDED|SIMULTANEOUS)")) {
                    can_be_added=addBothEntitiesInNewChain(x1, x2, y1, y2, relation);
                } else {
                    can_be_added=addBothEntitiesInNewChain(y1, y2, x1, x2, reverseRelationCategory(relation));
                }
            } else {
                // both entities exist
                if (entity_tp_map.containsKey(x1) && entity_tp_map.containsKey(x2) && entity_tp_map.containsKey(y1) && entity_tp_map.containsKey(y2)) {
                    if (relation.matches("(BEFORE|IBEFORE|BEGINS|ENDS|OVERLAPS|IS_INCLUDED|SIMULTANEOUS)")) {
                        can_be_added=addOnlyRelation(x1, x2, y1, y2, relation);
                    } else {
                        can_be_added=addOnlyRelation(y1, y2, x1, x2, reverseRelationCategory(relation));
                    }
                } // only one exists
                else {
                    // if it is the first make it the second one for simplification
                    if (entity_tp_map.containsKey(x1) && entity_tp_map.containsKey(x2) && !entity_tp_map.containsKey(y1) && !entity_tp_map.containsKey(y2)) {
                        relation = reverseRelationCategory(relation);
                        x1 = entity2 + "_s";
                        x2 = entity2 + "_e";
                        y1 = entity1 + "_s";
                        y2 = entity1 + "_e";
                    }
                    can_be_added=addOnlyFirstEntity(x1, x2, y1, y2, relation);
                }
            }

        } catch (Exception e) {
            System.err.println("Errors found (TimeGraph AddRelation):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return false;
        }
        //System.out.println("Can be added: "+can_be_added);
        return can_be_added; // return can_be_added to avoid inconsitencies or true to only to break in uncaptured exceptions
    }

    /**
     * Calculate positions according to the relation for new chain...
     *
     * @param tpx1
     * @param tpx2
     * @param tpy1
     * @param tpy2
     * @param relation
     */
    public boolean addBothEntitiesInNewChain(String x1, String x2, String y1, String y2, String rel) {
        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
            System.err.println("addBothEntitiesInNewChain");
        }
        try {
            int c = createOrSelectNewChain();
            if (rel.equals("BEFORE")) { // Allen (<)  x1 < x2 < y1 < y2
                TimePoint tp1 = new TimePoint(c, x1);
                TimePoint tp2 = new TimePoint(c, tp1.getPositionAfter(), x2);
                TimePoint tp3 = new TimePoint(c, tp2.getPositionAfter(), y1);
                TimePoint tp4 = new TimePoint(c, tp3.getPositionAfter(), y2);
                entity_tp_map.put(x1, tp1);
                entity_tp_map.put(x2, tp2);
                entity_tp_map.put(y1, tp3);
                entity_tp_map.put(y2, tp4);
                metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                metagraph.get(c).getTimePoints().put(tp2.getPosition(), tp2);
                metagraph.get(c).getTimePoints().put(tp3.getPosition(), tp3);
                metagraph.get(c).getTimePoints().put(tp4.getPosition(), tp4);
            }

            if (rel.equals("IBEFORE")) { // Allen's meets (m) x1 < (x2 = y1) < y2
                TimePoint tp1 = new TimePoint(c, x1);
                TimePoint tp2 = new TimePoint(c, tp1.getPositionAfter(), x2 + "," + y1);
                TimePoint tp3 = new TimePoint(c, tp2.getPositionAfter(), y2);
                entity_tp_map.put(x1, tp1);
                entity_tp_map.put(x2, tp2);
                entity_tp_map.put(y1, tp2);
                entity_tp_map.put(y2, tp3);
                metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                metagraph.get(c).getTimePoints().put(tp2.getPosition(), tp2);
                metagraph.get(c).getTimePoints().put(tp3.getPosition(), tp3);
            }

            if (rel.equals("BEGINS")) { // Allen's starts (s) (x1 = y1) < x2 < y2
                TimePoint tp1 = new TimePoint(c, x1 + "," + y1);
                TimePoint tp2 = new TimePoint(c, tp1.getPositionAfter(), x2);
                TimePoint tp3 = new TimePoint(c, tp2.getPositionAfter(), y2);
                entity_tp_map.put(x1, tp1);
                entity_tp_map.put(y1, tp1);
                entity_tp_map.put(x2, tp2);
                entity_tp_map.put(y2, tp3);
                metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                metagraph.get(c).getTimePoints().put(tp2.getPosition(), tp2);
                metagraph.get(c).getTimePoints().put(tp3.getPosition(), tp3);
            }

            if (rel.equals("ENDS")) { // Allen's Finishes (f) y1 < x1 < (x2 = y2)
                TimePoint tp1 = new TimePoint(c, y1);
                TimePoint tp2 = new TimePoint(c, tp1.getPositionAfter(), x1);
                TimePoint tp3 = new TimePoint(c, tp2.getPositionAfter(), x2 + "," + y2);
                entity_tp_map.put(y1, tp1);
                entity_tp_map.put(x1, tp2);
                entity_tp_map.put(x2, tp3);
                entity_tp_map.put(y2, tp3);
                metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                metagraph.get(c).getTimePoints().put(tp2.getPosition(), tp2);
                metagraph.get(c).getTimePoints().put(tp3.getPosition(), tp3);
            }

            if (rel.equals("OVERLAPS")) { // Allen's Overlaps (o) x1 < y1 < x2 < y2
                TimePoint tp1 = new TimePoint(c, x1);
                TimePoint tp2 = new TimePoint(c, tp1.getPositionAfter(), y1);
                TimePoint tp3 = new TimePoint(c, tp2.getPositionAfter(), x2);
                TimePoint tp4 = new TimePoint(c, tp3.getPositionAfter(), y2);
                entity_tp_map.put(x1, tp1);
                entity_tp_map.put(y1, tp2);
                entity_tp_map.put(x2, tp3);
                entity_tp_map.put(y2, tp4);
                metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                metagraph.get(c).getTimePoints().put(tp2.getPosition(), tp2);
                metagraph.get(c).getTimePoints().put(tp3.getPosition(), tp3);
                metagraph.get(c).getTimePoints().put(tp4.getPosition(), tp4);
            }

            if (rel.equals("IS_INCLUDED")) { // Allen's during (d) y1 < x1 < x2 < y2
                TimePoint tp1 = new TimePoint(c, y1);
                TimePoint tp2 = new TimePoint(c, tp1.getPositionAfter(), x1);
                TimePoint tp3 = new TimePoint(c, tp2.getPositionAfter(), x2);
                TimePoint tp4 = new TimePoint(c, tp3.getPositionAfter(), y2);
                entity_tp_map.put(y1, tp1);
                entity_tp_map.put(x1, tp2);
                entity_tp_map.put(x2, tp3);
                entity_tp_map.put(y2, tp4);
                metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                metagraph.get(c).getTimePoints().put(tp2.getPosition(), tp2);
                metagraph.get(c).getTimePoints().put(tp3.getPosition(), tp3);
                metagraph.get(c).getTimePoints().put(tp4.getPosition(), tp4);
            }

            if (rel.equals("SIMULTANEOUS")) { //  # Allen's equal (=) (x1 = y1) < (x2 = y2)
                TimePoint tp1 = new TimePoint(c, x1 + "," + y1);
                TimePoint tp2 = new TimePoint(c, tp1.getPositionAfter(), x2 + "," + y2);
                entity_tp_map.put(x1, tp1);
                entity_tp_map.put(y1, tp1);
                entity_tp_map.put(x2, tp2);
                entity_tp_map.put(y2, tp2);
                metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                metagraph.get(c).getTimePoints().put(tp2.getPosition(), tp2);
            }
        } catch (Exception e) {
            System.err.println("Errors found (TimeMLGraph addBothEntitiesInNewChain):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return false;
        }
        return true;
    }

    /**
     * Check if a new chain is needed to add the relation with a new entity
     *
     * @param tpx1
     * @param tpx2
     * @param tpy1
     * @param tpy2
     * @param relation
     */
    public boolean addOnlyFirstEntity(String x1, String x2, String y1, String y2, String rel) {
        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
            System.err.println("addOnlyFirstEntity");
        }
        try {
            // Y(y1,y2) is already known. Add only X(x1,x2).
            TimePoint tpy1 = entity_tp_map.get(y1);
            TimePoint tpy2 = entity_tp_map.get(y2);

            if (rel.equals("BEFORE")) { // x1 < x2 < y1 < y2
                if (!metagraph.get(tpy1.getChain()).hasPrevious(tpy1)) { // add in the same chain
                    TimePoint tp2 = new TimePoint(tpy1.getChain(), tpy1.getPositionBefore(), x2);
                    TimePoint tp1 = new TimePoint(tpy1.getChain(), tp2.getPositionBefore(), x1);
                    entity_tp_map.put(x2, tp2);
                    entity_tp_map.put(x1, tp1);
                    metagraph.get(tpy1.getChain()).getTimePoints().put(tp2.getPosition(), tp2);
                    metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                } else { // create or select new chain
                    int c = createOrSelectNewChain();
                    TimePoint tp2 = new TimePoint(c, x2);
                    TimePoint tp1 = new TimePoint(c, tp2.getPositionBefore(), x1);
                    entity_tp_map.put(x2, tp2);
                    entity_tp_map.put(x1, tp1);
                    metagraph.get(c).getTimePoints().put(tp2.getPosition(), tp2);
                    metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                    // add point-chain connections 2 metagraph
                    metagraph.get(c).addConnection(tp2.getPosition(), tpy1, "<");
                    metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp2, ">");
                }
            }

            if (rel.equals("AFTER")) { // y1 < y2 < x1 < x2
                if (!metagraph.get(tpy2.getChain()).hasNext(tpy2)) { // add in the same chain
                    TimePoint tp1 = new TimePoint(tpy2.getChain(), tpy2.getPositionAfter(), x1);
                    TimePoint tp2 = new TimePoint(tpy2.getChain(), tp1.getPositionAfter(), x2);
                    entity_tp_map.put(x1, tp1);
                    entity_tp_map.put(x2, tp2);
                    metagraph.get(tpy2.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                    metagraph.get(tpy2.getChain()).getTimePoints().put(tp2.getPosition(), tp2);
                } else { // create or select new chain
                    int c = createOrSelectNewChain();
                    TimePoint tp1 = new TimePoint(c, x1);
                    TimePoint tp2 = new TimePoint(c, tp1.getPositionAfter(), x2);
                    entity_tp_map.put(x1, tp1);
                    entity_tp_map.put(x2, tp2);
                    metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                    metagraph.get(c).getTimePoints().put(tp2.getPosition(), tp2);
                    // add point-chain connections 2 metagraph
                    metagraph.get(c).addConnection(tp1.getPosition(), tpy2, ">");
                    metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, "<");
                }
            }

            if (rel.equals("IBEFORE")) { // Allen's MEET (m) x1 < (x2 = y1) < y2
                if (!metagraph.get(tpy1.getChain()).hasPrevious(tpy1)) { // add in the same chain
                    TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPositionBefore(), x1);
                    tpy1.associateEntities(x2);
                    entity_tp_map.put(x1, tp1);
                    entity_tp_map.put(x2, tpy1);
                    metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                } else { // create a new chain
                    int c = createOrSelectNewChain();
                    TimePoint tp1 = new TimePoint(c, x1);
                    tpy1.associateEntities(x2);
                    entity_tp_map.put(x1, tp1);
                    entity_tp_map.put(x2, tpy1);
                    metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                    // add point-chain connections 2 metagraph
                    metagraph.get(c).addConnection(tp1.getPosition(), tpy1, "<");
                    metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, ">");
                }
            }

            if (rel.equals("IAFTER")) { // Allen's METBY (mi) y1 < (y2 = x1) < x2
                if (!metagraph.get(tpy2.getChain()).hasNext(tpy2)) { // add in the same chain
                    TimePoint tp1 = new TimePoint(tpy2.getChain(), tpy2.getPositionAfter(), x2);
                    tpy2.associateEntities(x1);
                    entity_tp_map.put(x1, tpy2);
                    entity_tp_map.put(x2, tp1);
                    metagraph.get(tpy2.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                } else { // create or select new chain
                    int c = createOrSelectNewChain();
                    TimePoint tp1 = new TimePoint(c, x2);
                    tpy2.associateEntities(x1);
                    entity_tp_map.put(x1, tpy2);
                    entity_tp_map.put(x2, tp1);
                    metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                    // add point-chain connections 2 metagraph
                    metagraph.get(c).addConnection(tp1.getPosition(), tpy2, ">");
                    metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, "<");
                }
            }

            if (rel.equals("BEGINS")) { // Allen's START (s) (x1 = y1) < x2 < y2
                entity_tp_map.put(x1, tpy1);
                tpy1.associateEntities(x1);
                if (metagraph.get(tpy1.getChain()).isNextFor(tpy1, tpy2)) { // add in the same chain
                    TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPosition() + ((tpy2.getPosition() - tpy1.getPosition()) / 2), x2);
                    entity_tp_map.put(x2, tp1);
                    metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                } else {
                    // tpy1 and tpy2 are in the same chain or different chains but already related, create new chain for x2
                    if (tpy1.getChain() == tpy2.getChain() || (tpy1.getChain() != tpy2.getChain() && metagraph.get(tpy1.getChain()).hasNext(tpy1) && metagraph.get(tpy2.getChain()).hasPrevious(tpy2))) {
                        int c = createOrSelectNewChain();
                        TimePoint tp1 = new TimePoint(c, x2);
                        entity_tp_map.put(x2, tp1);
                        metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                        // add point-chain connections to metagraph
                        metagraph.get(c).addConnection(tp1.getPosition(), tpy1, ">");
                        metagraph.get(c).addConnection(tp1.getPosition(), tpy2, "<");
                        metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, "<");
                        metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, ">");
                    } else {
                        // chose chain to put tp1
                        if (!metagraph.get(tpy1.getChain()).hasNext(tpy1)) {
                            TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPositionAfter(), x2);
                            entity_tp_map.put(x2, tp1);
                            metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                            // add point-chain connections to metagraph
                            metagraph.get(tpy1.getChain()).addConnection(tp1.getPosition(), tpy2, "<");
                            metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, ">");
                        } else {
                            TimePoint tp1 = new TimePoint(tpy2.getChain(), tpy2.getPositionBefore(), x2);
                            entity_tp_map.put(x2, tp1);
                            metagraph.get(tpy2.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                            // add point-chain connections to metagraph
                            metagraph.get(tpy2.getChain()).addConnection(tp1.getPosition(), tpy1, ">");
                            metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, "<");
                        }
                    }
                }
            }


            if (rel.equals("BEGUN_BY")) { // Allen's STARTED_BY (si) (x1 = y1) < y2 < x2
                entity_tp_map.put(x1, tpy1);
                tpy1.associateEntities(x1);
                if (metagraph.get(tpy2.getChain()).hasNext(tpy2)) { // add in the same chain (it doesn't matter if they are in diff chains)
                    TimePoint tp1 = new TimePoint(tpy2.getChain(), tpy2.getPositionAfter(), x2);
                    entity_tp_map.put(x2, tp1);
                    metagraph.get(tpy2.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                } else {
                    int c = createOrSelectNewChain();
                    TimePoint tp1 = new TimePoint(c, x2);
                    entity_tp_map.put(x2, tp1);
                    metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                    // add point-chain connections to metagraph
                    metagraph.get(c).addConnection(tp1.getPosition(), tpy2, ">");
                    metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, "<");
                }
            }


            if (rel.equals("ENDS")) { // Allen's FINISHES (f) y1 < x1 < (x2 = y2)
                entity_tp_map.put(x2, tpy2);
                tpy2.associateEntities(x2);
                if (metagraph.get(tpy1.getChain()).isNextFor(tpy1, tpy2)) { // add in the same chain
                    TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPosition() + ((tpy2.getPosition() - tpy1.getPosition()) / 2), x1);
                    entity_tp_map.put(x1, tp1);
                    metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                } else {
                    // tpy1 and tpy2 are in the same chain or different chains but already related, create new chain for x2
                    if (tpy1.getChain() == tpy2.getChain() || (tpy1.getChain() != tpy2.getChain() && metagraph.get(tpy1.getChain()).hasNext(tpy1) && metagraph.get(tpy2.getChain()).hasPrevious(tpy2))) {
                        int c = createOrSelectNewChain();
                        TimePoint tp1 = new TimePoint(c, x1);
                        entity_tp_map.put(x1, tp1);
                        metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                        // add point-chain connections to metagraph
                        metagraph.get(c).addConnection(tp1.getPosition(), tpy1, ">");
                        metagraph.get(c).addConnection(tp1.getPosition(), tpy2, "<");
                        metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, "<");
                        metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, ">");
                    } else {
                        // chose chain to put tp1
                        if (!metagraph.get(tpy1.getChain()).hasNext(tpy1)) {
                            TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPositionAfter(), x1);
                            entity_tp_map.put(x1, tp1);
                            metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                            // add point-chain connections to metagraph
                            metagraph.get(tpy1.getChain()).addConnection(tp1.getPosition(), tpy2, "<");
                            metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, ">");
                        } else {
                            TimePoint tp1 = new TimePoint(tpy2.getChain(), tpy2.getPositionBefore(), x1);
                            entity_tp_map.put(x1, tp1);
                            metagraph.get(tpy2.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                            // add point-chain connections to metagraph
                            metagraph.get(tpy2.getChain()).addConnection(tp1.getPosition(), tpy1, ">");
                            metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, "<");
                        }
                    }
                }
            }

            if (rel.equals("ENDED_BY")) { // Allen's ENDED_BY (fi) x1 < y1 < (y2 = x2)
                entity_tp_map.put(x2, tpy2);
                tpy2.associateEntities(x2);
                if (metagraph.get(tpy1.getChain()).hasPrevious(tpy1)) { // add in the same chain (it doesn't matter if they are in diff chains)
                    TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPositionBefore(), x1);
                    entity_tp_map.put(x1, tp1);
                    metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                } else {
                    int c = createOrSelectNewChain();
                    TimePoint tp1 = new TimePoint(c, x1);
                    entity_tp_map.put(x1, tp1);
                    metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                    // add point-chain connections to metagraph
                    metagraph.get(c).addConnection(tp1.getPosition(), tpy1, "<");
                    metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, ">");
                }
            }

            if (rel.equals("OVERLAPS")) { // Allen's (o)  x1 < y1 < x2 < y2
                // x1
                if (!metagraph.get(tpy1.getChain()).hasPrevious(tpy1)) { // add in the same chain
                    TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPositionBefore(), x1);
                    entity_tp_map.put(x1, tp1);
                    metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                } else {
                    int c = createOrSelectNewChain();
                    TimePoint tp1 = new TimePoint(c, x1);
                    entity_tp_map.put(x1, tp1);
                    metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                    // add point-chain connections to metagraph
                    metagraph.get(c).addConnection(tp1.getPosition(), tpy1, "<");
                    metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, ">");
                }

                //x2
                if (metagraph.get(tpy1.getChain()).isNextFor(tpy1, tpy2)) { // add in the same chain
                    TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPosition() + ((tpy2.getPosition() - tpy1.getPosition()) / 2), x2);
                    entity_tp_map.put(x2, tp1);
                    metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                } else {
                    // tpy1 and tpy2 are in the same chain or different chains but already related, create new chain for x2
                    if (tpy1.getChain() == tpy2.getChain() || (tpy1.getChain() != tpy2.getChain() && metagraph.get(tpy1.getChain()).hasNext(tpy1) && metagraph.get(tpy2.getChain()).hasPrevious(tpy2))) {
                        int c = createOrSelectNewChain();
                        TimePoint tp1 = new TimePoint(c, x2);
                        entity_tp_map.put(x2, tp1);
                        metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                        // add point-chain connections to metagraph
                        metagraph.get(c).addConnection(tp1.getPosition(), tpy1, ">");
                        metagraph.get(c).addConnection(tp1.getPosition(), tpy2, "<");
                        metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, "<");
                        metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, ">");
                    } else {
                        // chose chain to put tp1
                        if (!metagraph.get(tpy1.getChain()).hasNext(tpy1)) {
                            TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPositionAfter(), x2);
                            entity_tp_map.put(x2, tp1);
                            metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                            // add point-chain connections to metagraph
                            metagraph.get(tpy1.getChain()).addConnection(tp1.getPosition(), tpy2, "<");
                            metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, ">");
                        } else {
                            TimePoint tp1 = new TimePoint(tpy2.getChain(), tpy2.getPositionBefore(), x2);
                            entity_tp_map.put(x2, tp1);
                            metagraph.get(tpy2.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                            // add point-chain connections to metagraph
                            metagraph.get(tpy2.getChain()).addConnection(tp1.getPosition(), tpy1, ">");
                            metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, "<");
                        }
                    }
                }
            }

            if (rel.equals("OVERLAPPED_BY")) { // Allen's (oi)  y1 < x1 < y2 < x2
                // x2
                if (!metagraph.get(tpy2.getChain()).hasNext(tpy2)) { // add in the same chain
                    TimePoint tp1 = new TimePoint(tpy2.getChain(), tpy2.getPositionAfter(), x2);
                    entity_tp_map.put(x2, tp1);
                    metagraph.get(tpy2.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                } else {
                    int c = createOrSelectNewChain();
                    TimePoint tp1 = new TimePoint(c, x2);
                    entity_tp_map.put(x2, tp1);
                    metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                    // add point-chain connections to metagraph
                    metagraph.get(c).addConnection(tp1.getPosition(), tpy2, ">");
                    metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, "<");
                }

                // x1
                // tpy1 and tpy2 are in the same chain or different chains but already related, create new chain for x2
                if (tpy1.getChain() == tpy2.getChain() || (tpy1.getChain() != tpy2.getChain() && metagraph.get(tpy1.getChain()).hasNext(tpy1) && metagraph.get(tpy2.getChain()).hasPrevious(tpy2))) {
                    int c = createOrSelectNewChain();
                    TimePoint tp1 = new TimePoint(c, x1);
                    entity_tp_map.put(x1, tp1);
                    metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                    // add point-chain connections to metagraph
                    metagraph.get(c).addConnection(tp1.getPosition(), tpy1, ">");
                    metagraph.get(c).addConnection(tp1.getPosition(), tpy2, "<");
                    metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, "<");
                    metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, ">");
                } else {
                    // chose chain to put tp1
                    if (!metagraph.get(tpy1.getChain()).hasNext(tpy1)) {
                        TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPositionAfter(), x1);
                        entity_tp_map.put(x1, tp1);
                        metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                        // add point-chain connections to metagraph
                        metagraph.get(tpy1.getChain()).addConnection(tp1.getPosition(), tpy2, "<");
                        metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, ">");
                    } else {
                        TimePoint tp1 = new TimePoint(tpy2.getChain(), tpy2.getPositionBefore(), x1);
                        entity_tp_map.put(x1, tp1);
                        metagraph.get(tpy2.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                        // add point-chain connections to metagraph
                        metagraph.get(tpy2.getChain()).addConnection(tp1.getPosition(), tpy1, ">");
                        metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, "<");
                    }
                }
            }


            if (rel.equals("IS_INCLUDED")) { // Allen's During (d) y1 < x1 < x2 < y2
                if (metagraph.get(tpy1.getChain()).isNextFor(tpy1, tpy2)) { // add in the same chain
                    TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPosition() + ((tpy2.getPosition() - tpy1.getPosition()) / 3), x1);
                    TimePoint tp2 = new TimePoint(tpy1.getChain(), tpy1.getPosition() + ((tpy2.getPosition() - tpy1.getPosition()) / 3) * 2, x2);
                    entity_tp_map.put(x1, tp1);
                    entity_tp_map.put(x2, tp2);
                    metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                    metagraph.get(tpy1.getChain()).getTimePoints().put(tp2.getPosition(), tp2);
                } else {
                    // tpy1 and tpy2 are in the same chain or different chains but already related, create new chain for x2
                    if (tpy1.getChain() == tpy2.getChain() || (tpy1.getChain() != tpy2.getChain() && metagraph.get(tpy1.getChain()).hasNext(tpy1) && metagraph.get(tpy2.getChain()).hasPrevious(tpy2))) {
                        int c = createOrSelectNewChain();
                        TimePoint tp1 = new TimePoint(c, x1);
                        TimePoint tp2 = new TimePoint(c, tp1.getPositionAfter(), x2);
                        entity_tp_map.put(x1, tp1);
                        entity_tp_map.put(x2, tp2);
                        metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                        metagraph.get(c).getTimePoints().put(tp2.getPosition(), tp2);
                        // add point-chain connections to metagraph
                        metagraph.get(c).addConnection(tp1.getPosition(), tpy1, ">");
                        metagraph.get(c).addConnection(tp2.getPosition(), tpy2, "<");
                        metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, "<");
                        metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp2, ">");
                    } else {
                        // chose chain to put tp1 and tp2
                        if (!metagraph.get(tpy1.getChain()).hasNext(tpy1)) {
                            TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPositionAfter(), x1);
                            TimePoint tp2 = new TimePoint(tpy1.getChain(), tp1.getPositionAfter(), x2);
                            entity_tp_map.put(x1, tp1);
                            entity_tp_map.put(x2, tp2);
                            metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                            metagraph.get(tpy1.getChain()).getTimePoints().put(tp2.getPosition(), tp2);
                            // add point-chain connections to metagraph
                            metagraph.get(tpy1.getChain()).addConnection(tp2.getPosition(), tpy2, "<");
                            metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp2, ">");
                        } else {
                            TimePoint tp2 = new TimePoint(tpy2.getChain(), tpy2.getPositionBefore(), x2);
                            TimePoint tp1 = new TimePoint(tpy2.getChain(), tp2.getPositionBefore(), x1);
                            entity_tp_map.put(x2, tp2);
                            entity_tp_map.put(x1, tp1);
                            metagraph.get(tpy2.getChain()).getTimePoints().put(tp2.getPosition(), tp2);
                            metagraph.get(tpy2.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                            // add point-chain connections to metagraph
                            metagraph.get(tpy2.getChain()).addConnection(tp1.getPosition(), tpy1, ">");
                            metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, "<");
                        }
                    }
                }
            }

            if (rel.equals("INCLUDES")) { // Allen's contains (di) x1 < y1 < y2 < x2 (two independent point relations)
                // x1
                if (!metagraph.get(tpy1.getChain()).hasPrevious(tpy1)) { // add in the same chain
                    TimePoint tp1 = new TimePoint(tpy1.getChain(), tpy1.getPositionBefore(), x1);
                    entity_tp_map.put(x1, tp1);
                    metagraph.get(tpy1.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                } else {
                    int c = createOrSelectNewChain();
                    TimePoint tp1 = new TimePoint(c, x1);
                    entity_tp_map.put(x1, tp1);
                    metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                    // add point-chain connections to metagraph
                    metagraph.get(c).addConnection(tp1.getPosition(), tpy1, "<");
                    metagraph.get(tpy1.getChain()).addConnection(tpy1.getPosition(), tp1, ">");
                }

                // x2
                if (!metagraph.get(tpy2.getChain()).hasNext(tpy2)) { // add in the same chain
                    TimePoint tp1 = new TimePoint(tpy2.getChain(), tpy2.getPositionAfter(), x2);
                    entity_tp_map.put(x2, tp1);
                    metagraph.get(tpy2.getChain()).getTimePoints().put(tp1.getPosition(), tp1);
                } else {
                    int c = createOrSelectNewChain();
                    TimePoint tp1 = new TimePoint(c, x2);
                    entity_tp_map.put(x2, tp1);
                    metagraph.get(c).getTimePoints().put(tp1.getPosition(), tp1);
                    // add point-chain connections to metagraph
                    metagraph.get(c).addConnection(tp1.getPosition(), tpy2, ">");
                    metagraph.get(tpy2.getChain()).addConnection(tpy2.getPosition(), tp1, "<");
                }
            }

            if (rel.equals("SIMULTANEOUS")) { //  # Allen's equal (=) (x1 = y1) < (x2 = y2)
                entity_tp_map.put(x1, tpy1);
                if (!tpy1.associateEntities(x1)) {
                    return false;
                }
                entity_tp_map.put(x2, tpy2);
                if (!tpy2.associateEntities(x2)) {
                    return false;
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TimeMLGraph addOnlyFirstEntity):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return false;
        }
        return true;
    }

    /**
     * Check if the relation is in the graph or is inconsistent and only if it
     * can be added -> add it. Adding new relations can produce chain collapses
     * that reduce the graph.
     *
     * @param x1
     * @param x2
     * @param y1
     * @param y2
     * @param rel
     */
    public boolean addOnlyRelation(String x1, String x2, String y1, String y2, String rel) {
        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
            System.err.println("addOnlyRelation " + x1 + x2 + " " + rel + " " + y1 + y2);
        }
        try {
            // TimePoints are already known
            TimePoint tpx1 = entity_tp_map.get(x1);
            TimePoint tpx2 = entity_tp_map.get(x2);
            TimePoint tpy1 = entity_tp_map.get(y1);
            TimePoint tpy2 = entity_tp_map.get(y2);

            if (rel.equals("BEFORE")) { // Allen (<)  x1 < x2 < y1 < y2
                String pointrel = getPointRelation(tpx2, tpy1);
                if (pointrel.equals("<")) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.out.println("already in graph");
                    }
                } else {
                    //offense
                    if (!pointrel.equals("UNKNOWN")) {
                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                            System.out.println("offense not x2<y1 (" + x2 + pointrel + y1 + ") in " + rel);
                        }
                        // break? ignore?
                    } //unknown
                    else {
                        //System.out.println("add new relation");
                        add_after_crosschain(tpy1, tpx2);
                    }
                }
            }

            if (rel.equals("IBEFORE")) { // Allen's meets (m) x1 < (x2 = y1) < y2
                String pointrel = getPointRelation(tpx2, tpy1);
                if (pointrel.equals("=")) { // same as tpx2.equals(tpy1)
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.out.println("already in graph");
                    }
                } else {
                    //offense
                    if (!pointrel.equals("UNKNOWN")) {
                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                            System.out.println("offense x2!=y1 (" + x2 + pointrel + y1 + ") in " + rel);
                        }
                        // break? ignore?
                    } //unknown
                    else {
                        //System.out.println("add new relation");
                        make_equal_from_crosschain(tpy1, tpx2); // NOOOOOOOOO EQUAL RELATION ---> can be a function
                    }
                }

            }

            if (rel.equals("BEGINS")) { // Allen's starts (s) (x1 = y1) < x2 < y2
                //x1=y1
                String pointrel = getPointRelation(tpx1, tpy1);
                if (pointrel.equals("=")) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.out.println("already in graph");
                    }
                } else {
                    //offense
                    if (!pointrel.equals("UNKNOWN")) {
                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                            System.out.println("offense x1!=y1 (" + x1 + pointrel + y1 + ") in " + rel);
                        }
                        // break? ignore?
                    } //unknown
                    else {
                        //System.out.println("add new relation");
                        make_equal_from_crosschain(tpy1, tpx1); // NOOOOOOOOO EQUAL RELATION ---> can be a function
                    }
                }

                // x2<y2
                tpx2 = entity_tp_map.get(x2);
                tpy2 = entity_tp_map.get(y2);
                pointrel = getPointRelation(tpx2, tpy2);
                if (pointrel.equals("<")) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.out.println("already in graph");
                    }
                } else {
                    //offense
                    if (!pointrel.equals("UNKNOWN")) {
                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                            System.out.println("offense not x2<y2 (" + x2 + pointrel + y2 + ") in " + rel);
                        }
                        // break? ignore?
                    } //unknown
                    else {
                        //System.out.println("add new relation");
                        add_after_crosschain(tpy2, tpx2);
                    }
                }



            }

            if (rel.equals("ENDS")) { // Allen's Finishes (f) y1 < x1 < (x2 = y2)

                //x2=y2
                String pointrel = getPointRelation(tpx2, tpy2);
                if (pointrel.equals("=")) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.out.println("already in graph");
                    }
                } else {
                    //offense
                    if (!pointrel.equals("UNKNOWN")) {
                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                            System.out.println("offense x2!=y2 (" + x2 + pointrel + y2 + ") in " + rel);
                        }
                        // break? ignore?
                    } //unknown
                    else {
                        //System.out.println("add new relation");
                        make_equal_from_crosschain(tpy2, tpx2);
                    }
                }

                // y1 < x1
                tpx1 = entity_tp_map.get(x1);
                tpy1 = entity_tp_map.get(y1);
                pointrel = getPointRelation(tpy1, tpx1);
                if (pointrel.equals("<")) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.out.println("already in graph");
                    }
                } else {
                    //offense
                    if (!pointrel.equals("UNKNOWN")) {
                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                            System.out.println("offense not y1<x1 (" + y1 + pointrel + x1 + ") in " + rel);
                        }
                        // break? ignore?
                    } //unknown
                    else {
                        //System.out.println("add new relation");
                        add_after_crosschain(tpx1, tpy1);
                    }
                }

            }

            if (rel.equals("OVERLAPS")) { // Allen's Overlaps (o) x1 < y1 < x2 < y2
                // x1 < y1
                String pointrel = getPointRelation(tpx1, tpy1);
                if (pointrel.equals("<")) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.out.println("already in graph");
                    }
                } else {
                    //offense
                    if (!pointrel.equals("UNKNOWN")) {
                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                            System.out.println("offense not x1 < y1 (" + x1 + pointrel + y1 + ") in " + rel);
                        }
                        // break? ignore?
                    } //unknown
                    else {
                        //System.out.println("add new relation");
                        add_after_crosschain(tpy1, tpx1);
                    }
                }

                // y1<x2
                tpx2 = entity_tp_map.get(x2);
                tpy2 = entity_tp_map.get(y2);
                pointrel = getPointRelation(tpy1, tpx2);
                if (pointrel.equals("<")) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.out.println("already in graph");
                    }
                } else {
                    //offense
                    if (!pointrel.equals("UNKNOWN")) {
                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                            System.out.println("offense not y1<x2 (" + y1 + pointrel + x2 + ") in " + rel);
                        }
                        // break? ignore?
                    } //unknown
                    else {
                        //System.out.println("add new relation");
                        add_after_crosschain(tpx2, tpy1);
                    }
                }
            }

            if (rel.equals("IS_INCLUDED")) { // Allen's during (d) y1 < x1 < x2 < y2
                // y1 < x1
                String pointrel = getPointRelation(tpy1, tpx1);
                if (pointrel.equals("<")) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.out.println("already in graph");
                    }
                } else {
                    //offense
                    if (!pointrel.equals("UNKNOWN")) {
                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                            System.out.println("offense not y1<x1 (" + y1 + pointrel + x1 + ") in " + rel);
                        }
                        // break? ignore?
                    } //unknown
                    else {
                        //System.out.println("add new relation");
                        add_after_crosschain(tpx1, tpy1);
                    }
                }

                // x2<y2
                tpx2 = entity_tp_map.get(x2);
                tpy2 = entity_tp_map.get(y2);
                pointrel = getPointRelation(tpx2, tpy2);
                if (pointrel.equals("<")) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.out.println("already in graph");
                    }
                } else {
                    //offense
                    if (!pointrel.equals("UNKNOWN")) {
                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                            System.out.println("offense not x2<y2 (" + x2 + pointrel + y2 + ") in " + rel);
                        }
                        // break? ignore?
                    } //unknown
                    else {
                        //System.out.println("add new relation");
                        add_after_crosschain(tpy2, tpx2);
                    }
                }
            }

            if (rel.equals("SIMULTANEOUS")) { //  # Allen's equal (=) (x1 = y1) < (x2 = y2)
                //x1=y1
                String pointrel = getPointRelation(tpx1, tpy1);
                if (pointrel.equals("=")) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.out.println("already in graph");
                    }
                } else {
                    //offense
                    if (!pointrel.equals("UNKNOWN")) {
                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                            System.out.println("offense x1!=y1 (" + x1 + pointrel + y1 + ") in " + rel);
                        }
                        // break? ignore?
                    } //unknown
                    else {
                        //System.out.println("add new relation");
                        make_equal_from_crosschain(tpy1, tpx1); // NOOOOOOOOO EQUAL RELATION ---> can be a function
                    }
                }

                //x2=y2
                tpx2 = entity_tp_map.get(x2);
                tpy2 = entity_tp_map.get(y2);
                pointrel = getPointRelation(tpx2, tpy2);
                if (pointrel.equals("=")) {
                    if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                        System.out.println("already in graph");
                    }
                } else {
                    //offense
                    if (!pointrel.equals("UNKNOWN")) {
                        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                            System.out.println("offense x2!=y2 (" + x2 + pointrel + y2 + ") in " + rel);
                        }
                        // break? ignore?
                    } //unknown
                    else {
                        //System.out.println("add new relation");
                        make_equal_from_crosschain(tpy2, tpx2); // NOOOOOOOOO EQUAL RELATION ---> can be a function
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TimeGraph addOnlyRelation):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return false;
        }
        return true;
    }

    public void make_equal_from_crosschain(TimePoint p1, TimePoint p2) throws Exception {
            // chose which point is easier to merge
            int p1score = 0;
            int p2score = 0;

            if (!metagraph.get(p1.getChain()).hasNext(p1)) {
                p1score++;
            }
            if (!metagraph.get(p1.getChain()).hasPrevious(p1)) {
                p1score++;
            }
            if (!metagraph.get(p2.getChain()).hasNext(p2)) {
                p2score++;
            }
            if (!metagraph.get(p2.getChain()).hasPrevious(p2)) {
                p2score++;
            }

            // by default p2 is moved to p1, switch if p1 is easier
            if (p1score > p2score) {
                TimePoint aux = p1;
                p1 = p2;
                p2 = aux;
            }

            // merge p2 in p1
            TimePoint p2next = metagraph.get(p2.getChain()).getNextTimePoint(p2);
            TimePoint p2prev = metagraph.get(p2.getChain()).getPreviousTimePoint(p2);


            // copy everything to p1
            p1.associateEntities(p2.getAssociatedEntities());
            String[] associated_entities = p2.getAssociatedEntities().split(",");
            for (int aen = 0; aen < associated_entities.length; aen++) {
                entity_tp_map.put(associated_entities[aen], p1);
            }

            // remove connections between the unified point and the chain and vice-versa
            if (metagraph.get(p2.getChain()).getAfterConnections(p2.getPosition()) != null && metagraph.get(p2.getChain()).getAfterConnections(p2.getPosition()).containsKey(p1.getChain())) {
                TimePoint aux = metagraph.get(p2.getChain()).getAfterConnections(p2.getPosition()).get(p1.getChain());
                metagraph.get(p2.getChain()).removeConnection(p2.getPosition(), aux, ">");
                metagraph.get(aux.getChain()).removeConnection(aux.getPosition(), p2, "<");
            }
            if (metagraph.get(p2.getChain()).getBeforeConnections(p2.getPosition()) != null && metagraph.get(p2.getChain()).getBeforeConnections(p2.getPosition()).containsKey(p1.getChain())) {
                TimePoint aux = metagraph.get(p2.getChain()).getBeforeConnections(p2.getPosition()).get(p1.getChain());
                metagraph.get(p2.getChain()).removeConnection(p2.getPosition(), aux, "<");
                metagraph.get(aux.getChain()).removeConnection(aux.getPosition(), p2, ">");
            }



            // update connkeys
            Set<Integer> connkeys = null;
            if (metagraph.get(p2.getChain()).getAfterConnections(p2.getPosition()) != null) {
                connkeys = new HashSet<Integer>();
                connkeys.addAll(metagraph.get(p2.getChain()).getAfterConnections(p2.getPosition()).keySet());
            }
            if (connkeys != null) {
                for (Integer key : connkeys) {
                    TimePoint dest_point = metagraph.get(p2.getChain()).getAfterConnections(p2.getPosition()).get(key); // connection to move to p1

                    // remove from p2
                    metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), p2, "<");
                    metagraph.get(p2.getChain()).removeConnection(p2.getPosition(), dest_point, ">");

                    //add_after_crosschain(p1, dest_point);
                    TimePoint conflictpoint_dest_point = null;
                    // dest_point already conected to p1 < conflict
                    if (metagraph.get(dest_point.getChain()).getBeforeConnections(dest_point.getPosition()) != null) {
                        conflictpoint_dest_point = metagraph.get(dest_point.getChain()).getBeforeConnections(dest_point.getPosition()).get(p1.getChain());
                    }
                    // p1 already connected to destpoint chain > conflict
                    TimePoint conflictpoint_p1 = null;
                    if (metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()) != null) {
                        conflictpoint_p1 = metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()).get(dest_point.getChain());
                    }
                    if (conflictpoint_dest_point != null || conflictpoint_p1 != null) {
                        // normal conflict
                        if (conflictpoint_p1 == null && metagraph.get(dest_point.getChain()).isMoreInformative(dest_point.getPosition(), p1, "<")) {
                            metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), conflictpoint_dest_point, "<");
                            metagraph.get(conflictpoint_dest_point.getChain()).removeConnection(conflictpoint_dest_point.getPosition(), dest_point, ">");
                            metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), p1, "<");
                            metagraph.get(p1.getChain()).addConnection(p1.getPosition(), dest_point, ">");
                        }
                        // inverse conflict
                        if (conflictpoint_dest_point == null && metagraph.get(p1.getChain()).isMoreInformative(p1.getPosition(), dest_point, ">")) {
                            metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), conflictpoint_p1, ">");
                            metagraph.get(conflictpoint_p1.getChain()).removeConnection(conflictpoint_p1.getPosition(), p1, "<");
                            metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), p1, "<");
                            metagraph.get(p1.getChain()).addConnection(p1.getPosition(), dest_point, ">");
                        }
                        // double conflict
                        if (conflictpoint_dest_point != null && conflictpoint_p1 != null
                                && metagraph.get(dest_point.getChain()).isMoreInformative(dest_point.getPosition(), p1, "<")
                                && metagraph.get(p1.getChain()).isMoreInformative(p1.getPosition(), dest_point, ">")) {
                            metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), conflictpoint_dest_point, "<");
                            metagraph.get(conflictpoint_dest_point.getChain()).removeConnection(conflictpoint_dest_point.getPosition(), dest_point, ">");
                            metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), conflictpoint_p1, ">");
                            metagraph.get(conflictpoint_p1.getChain()).removeConnection(conflictpoint_p1.getPosition(), p1, "<");
                            metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), p1, "<");
                            metagraph.get(p1.getChain()).addConnection(p1.getPosition(), dest_point, ">");
                        }
                    } else {
                        metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), p1, "<");
                        metagraph.get(p1.getChain()).addConnection(p1.getPosition(), dest_point, ">");
                    }
                }
            }
            connkeys = null;
            if (metagraph.get(p2.getChain()).getBeforeConnections(p2.getPosition()) != null) {
                connkeys = new HashSet<Integer>();
                connkeys.addAll(metagraph.get(p2.getChain()).getBeforeConnections(p2.getPosition()).keySet());
            }
            if (connkeys != null) {
                for (Integer key : connkeys) {
                    TimePoint dest_point = metagraph.get(p2.getChain()).getBeforeConnections(p2.getPosition()).get(key); // connection to move to p1

                    // remove from p2
                    metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), p2, ">");
                    metagraph.get(p2.getChain()).removeConnection(p2.getPosition(), dest_point, "<");

                    //add_after_crosschain(dest_point, p1);
                    TimePoint conflictpoint_dest_point = null;
                    // dest_point already conected to p1 < conflict
                    if (metagraph.get(dest_point.getChain()).getAfterConnections(dest_point.getPosition()) != null) {
                        conflictpoint_dest_point = metagraph.get(dest_point.getChain()).getAfterConnections(dest_point.getPosition()).get(p1.getChain());
                    }
                    // p1 already connected to destpoint chain > conflict
                    TimePoint conflictpoint_p1 = null;
                    if (metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()) != null) {
                        conflictpoint_p1 = metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()).get(dest_point.getChain());
                    }
                    if (conflictpoint_dest_point != null || conflictpoint_p1 != null) {
                        // normal conflict
                        if (conflictpoint_p1 == null && metagraph.get(dest_point.getChain()).isMoreInformative(dest_point.getPosition(), p1, ">")) {
                            metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), conflictpoint_dest_point, ">");
                            metagraph.get(conflictpoint_dest_point.getChain()).removeConnection(conflictpoint_dest_point.getPosition(), dest_point, "<");
                            metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), p1, ">");
                            metagraph.get(p1.getChain()).addConnection(p1.getPosition(), dest_point, "<");
                        }
                        // inverse conflict
                        if (conflictpoint_dest_point == null && metagraph.get(p1.getChain()).isMoreInformative(p1.getPosition(), dest_point, "<")) {
                            metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), conflictpoint_p1, "<");
                            metagraph.get(conflictpoint_p1.getChain()).removeConnection(conflictpoint_p1.getPosition(), p1, ">");
                            metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), p1, ">");
                            metagraph.get(p1.getChain()).addConnection(p1.getPosition(), dest_point, "<");
                        }
                        // double conflict
                        if (conflictpoint_dest_point != null && conflictpoint_p1 != null
                                && metagraph.get(dest_point.getChain()).isMoreInformative(dest_point.getPosition(), p1, ">")
                                && metagraph.get(p1.getChain()).isMoreInformative(p1.getPosition(), dest_point, "<")) {
                            metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), conflictpoint_dest_point, ">");
                            metagraph.get(conflictpoint_dest_point.getChain()).removeConnection(conflictpoint_dest_point.getPosition(), dest_point, "<");
                            metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), conflictpoint_p1, "<");
                            metagraph.get(conflictpoint_p1.getChain()).removeConnection(conflictpoint_p1.getPosition(), p1, ">");
                            metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), p1, ">");
                            metagraph.get(p1.getChain()).addConnection(p1.getPosition(), dest_point, "<");
                        }
                    } else {
                        metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), p1, ">");
                        metagraph.get(p1.getChain()).addConnection(p1.getPosition(), dest_point, "<");
                    }
                }
            }

            if (p2next == null && p2prev == null) {
                metagraph.get(p2.getChain()).clear();
                return;
            }

            if (p2next != null && p2prev != null) {
                // new chain needed for next
                int c = createOrSelectNewChain();
                // replace with last one
                for (TimePoint tp : metagraph.get(p2.getChain()).getTimePointsGreaterOrEqual(p2next).values()) {
                    // add new point to metagraph
                    TimePoint np = new TimePoint(c, tp.getPosition(), tp.getAssociatedEntities());
                    metagraph.get(c).getTimePoints().put(np.getPosition(), np);

                    Set<Integer> connkeys2 = null;
                    if (metagraph.get(tp.getChain()).getAfterConnections(tp.getPosition()) != null) {
                        connkeys2 = new HashSet<Integer>();
                        connkeys2.addAll(metagraph.get(tp.getChain()).getAfterConnections(tp.getPosition()).keySet());
                    }
                    if (connkeys2 != null) {
                        for (Integer key : connkeys2) {
                            TimePoint dest_point = metagraph.get(tp.getChain()).getAfterConnections(tp.getPosition()).get(key);
                            metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), tp, "<");
                            metagraph.get(tp.getChain()).removeConnection(tp.getPosition(), dest_point, ">");
                            metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, "<");
                            metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, ">");
                        }
                    }
                    connkeys2 = null;
                    if (metagraph.get(tp.getChain()).getBeforeConnections(tp.getPosition()) != null) {
                        connkeys2 = new HashSet<Integer>();
                        connkeys2.addAll(metagraph.get(tp.getChain()).getBeforeConnections(tp.getPosition()).keySet());
                    }
                    if (connkeys2 != null) {
                        for (Integer key : connkeys2) {
                            TimePoint dest_point = metagraph.get(tp.getChain()).getBeforeConnections(tp.getPosition()).get(key);
                            metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), tp, ">");
                            metagraph.get(tp.getChain()).removeConnection(tp.getPosition(), dest_point, "<");
                            metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, ">");
                            metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, "<");
                        }
                    }
                    // search and update entity-tp references (change to new point)
                    String[] associated_entities2 = tp.getAssociatedEntities().split(",");
                    for (int aen = 0; aen < associated_entities2.length; aen++) {
                        entity_tp_map.put(associated_entities2[aen], np);
                    }
                }
                // leave only prev in the original chain
                metagraph.get(p2.getChain()).setTimePoints(metagraph.get(p2.getChain()).getTimePointsLowerOrEqual(p2prev));
                // relate chains to the merged point
                metagraph.get(c).addConnection(p2next.getPosition(), p1, ">");
                metagraph.get(p1.getChain()).addConnection(p1.getPosition(), metagraph.get(c).getTimePoints().firstEntry().getValue(), "<");
                checkChainFusion(metagraph.get(c).getTimePoints().firstEntry().getValue(), p1);
                metagraph.get(p1.getChain()).addConnection(p1.getPosition(), p2prev, ">");
                metagraph.get(p2prev.getChain()).addConnection(p2prev.getPosition(), p1, "<");
                checkChainFusion(p1, p2prev);
                return;
            }

            if (p2next != null) {
                // leave only prev in the original chain
                metagraph.get(p2.getChain()).setTimePoints(metagraph.get(p2.getChain()).getTimePointsGreaterOrEqual(p2next));
                // relate chains to the merged point

                TimePoint conflictpoint_p2next = null;
                if (metagraph.get(p2next.getChain()).getAfterConnections(p2next.getPosition()) != null) {
                    conflictpoint_p2next = metagraph.get(p2next.getChain()).getAfterConnections(p2next.getPosition()).get(p1.getChain());
                }
                TimePoint conflictpoint_p1 = null;
                if (metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()) != null) {
                    conflictpoint_p1 = metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()).get(p2next.getChain());
                }

                if (conflictpoint_p2next != null || conflictpoint_p1 != null) {
                    // normal conflict
                    if (conflictpoint_p1 == null && metagraph.get(p2next.getChain()).isMoreInformative(p2next.getPosition(), p1, ">")) {
                        metagraph.get(p2next.getChain()).removeConnection(p2next.getPosition(), conflictpoint_p2next, ">");
                        metagraph.get(conflictpoint_p2next.getChain()).removeConnection(conflictpoint_p2next.getPosition(), p2next, "<");
                        metagraph.get(p2next.getChain()).addConnection(p2next.getPosition(), p1, ">");
                        metagraph.get(p1.getChain()).addConnection(p1.getPosition(), p2next, "<");
                    }
                    // inverse conflict
                    if (conflictpoint_p2next == null && metagraph.get(p1.getChain()).isMoreInformative(p1.getPosition(), p2next, "<")) {
                        metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), conflictpoint_p1, "<");
                        metagraph.get(conflictpoint_p1.getChain()).removeConnection(conflictpoint_p1.getPosition(), p1, ">");
                        metagraph.get(p2next.getChain()).addConnection(p2next.getPosition(), p1, ">");
                        metagraph.get(p1.getChain()).addConnection(p1.getPosition(), p2next, "<");
                    }
                    // double conflict
                    if (conflictpoint_p2next != null && conflictpoint_p1 != null
                            && metagraph.get(p2next.getChain()).isMoreInformative(p2next.getPosition(), p1, ">")
                            && metagraph.get(p1.getChain()).isMoreInformative(p1.getPosition(), p2next, "<")) {
                        metagraph.get(p2next.getChain()).removeConnection(p2next.getPosition(), conflictpoint_p2next, ">");
                        metagraph.get(conflictpoint_p2next.getChain()).removeConnection(conflictpoint_p2next.getPosition(), p2next, "<");
                        metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), conflictpoint_p1, "<");
                        metagraph.get(conflictpoint_p1.getChain()).removeConnection(conflictpoint_p1.getPosition(), p1, ">");
                        metagraph.get(p2next.getChain()).addConnection(p2next.getPosition(), p1, ">");
                        metagraph.get(p1.getChain()).addConnection(p1.getPosition(), p2next, "<");
                    }

                } else {
                    metagraph.get(p2next.getChain()).addConnection(p2next.getPosition(), p1, ">");
                    metagraph.get(p1.getChain()).addConnection(p1.getPosition(), p2next, "<");
                }

                checkChainFusion(p2next, p1);
                return;
            }

            if (p2prev != null) {
                // leave only prev in the original chain
                metagraph.get(p2.getChain()).setTimePoints(metagraph.get(p2.getChain()).getTimePointsLowerOrEqual(p2prev));
                // relate chains to the merged point
                TimePoint conflictpoint_p2prev = null;
                // dest_point already conected to p1 < conflict
                if (metagraph.get(p2prev.getChain()).getBeforeConnections(p2prev.getPosition()) != null) {
                    conflictpoint_p2prev = metagraph.get(p2prev.getChain()).getBeforeConnections(p2prev.getPosition()).get(p1.getChain());
                }
                // p1 already connected to destpoint chain > conflict
                TimePoint conflictpoint_p1 = null;
                if (metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()) != null) {
                    conflictpoint_p1 = metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()).get(p2prev.getChain());
                }
                if (conflictpoint_p2prev != null || conflictpoint_p1 != null) {
                    // normal conflict
                    if (conflictpoint_p1 == null && metagraph.get(p2prev.getChain()).isMoreInformative(p2prev.getPosition(), p1, "<")) {
                        metagraph.get(p2prev.getChain()).removeConnection(p2prev.getPosition(), conflictpoint_p2prev, "<");
                        metagraph.get(conflictpoint_p2prev.getChain()).removeConnection(conflictpoint_p2prev.getPosition(), p2prev, ">");
                        metagraph.get(p2prev.getChain()).addConnection(p2prev.getPosition(), p1, "<");
                        metagraph.get(p1.getChain()).addConnection(p1.getPosition(), p2prev, ">");
                    }
                    // inverse conflict
                    if (conflictpoint_p2prev == null && metagraph.get(p1.getChain()).isMoreInformative(p1.getPosition(), p2prev, ">")) {
                        metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), conflictpoint_p1, ">");
                        metagraph.get(conflictpoint_p1.getChain()).removeConnection(conflictpoint_p1.getPosition(), p1, "<");
                        metagraph.get(p2prev.getChain()).addConnection(p2prev.getPosition(), p1, "<");
                        metagraph.get(p1.getChain()).addConnection(p1.getPosition(), p2prev, ">");
                    }
                    // double conflict
                    if (conflictpoint_p2prev != null && conflictpoint_p1 != null
                            && metagraph.get(p2prev.getChain()).isMoreInformative(p2prev.getPosition(), p1, "<")
                            && metagraph.get(p1.getChain()).isMoreInformative(p1.getPosition(), p2prev, ">")) {
                        metagraph.get(p2prev.getChain()).removeConnection(p2prev.getPosition(), conflictpoint_p2prev, "<");
                        metagraph.get(conflictpoint_p2prev.getChain()).removeConnection(conflictpoint_p2prev.getPosition(), p2prev, ">");
                        metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), conflictpoint_p1, ">");
                        metagraph.get(conflictpoint_p1.getChain()).removeConnection(conflictpoint_p1.getPosition(), p1, "<");
                        metagraph.get(p2prev.getChain()).addConnection(p2prev.getPosition(), p1, "<");
                        metagraph.get(p1.getChain()).addConnection(p1.getPosition(), p2prev, ">");
                    }
                } else {
                    metagraph.get(p2prev.getChain()).addConnection(p2prev.getPosition(), p1, "<");
                    metagraph.get(p1.getChain()).addConnection(p1.getPosition(), p2prev, ">");
                }
                checkChainFusion(p1, p2prev);
                return;
            }
        /*} catch (Exception e) {
            System.err.println("Errors found (TimeGraph):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            throw e;
        }*/

    }

    /**
     * Returns the point relation between two points in the graph
     *
     * @param x
     * @param y
     * @return
     */
    public String getPointRelation(TimePoint x, TimePoint y) {
        if (x.getChain() == y.getChain()) {
            return getPositionRelation(x.getPosition(), y.getPosition());
        } else {
            return getMultiChainRelation(x, y);
        }
    }

    /**
     * Returns =,<,> depending on the tp relation of two elements in the same
     * chain
     *
     * @param a
     * @param b
     * @return
     */
    public static String getPositionRelation(int position_x, int position_y) {
        if (position_x > position_y) {
            return ">";
        } else {
            if (position_x < position_y) {
                return "<";
            } else {
                return "=";
            }
        }
    }

    /**
     * Returns =,<,> depending on the tp relation of two elements in different
     * chains
     *
     * @param a
     * @param b
     * @return
     */
    public String getMultiChainRelation(TimePoint x, TimePoint y) {
        String relation = "UNKNOWN";
        HashMap<Integer, String> visited_chains = new HashMap<Integer, String>();
        if (search_timegraph_y_after_x(x, y, visited_chains)) {
            relation = "<";
        } else {
            visited_chains = null;
            visited_chains = new HashMap<Integer, String>();
            if (search_timegraph_y_after_x(y, x, visited_chains)) {
                relation = ">";
            }
        }
        return relation;
    }

    /**
     * Tries to find y after x in the graph (same as x before y)
     *
     * @param x
     * @param y
     * @param visited_chains
     * @return
     */
    public boolean search_timegraph_y_after_x(TimePoint x, TimePoint y, HashMap<Integer, String> visited_chains) {
        boolean found = false;
        Chain c = metagraph.get(x.getChain());
        visited_chains.put(new Integer(x.getChain()), "v");

        //System.err.println("Searching from: " + x);

        // search after connections of x or greater points to other chains

        // check point conections
        HashMap<Integer, TimePoint> conns = c.getBeforeConnections(x.getPosition());
        if (conns != null) {
            // explore direc connexions to y chain and return found if <= (= if it is y)
            if (conns.containsKey(y.getChain())) {
                if (conns.get(y.getChain()).getPosition() <= y.getPosition()) { //review
                    return true;
                } else {
                    return false;
                }
            } // if no direct connections explore all conections of connected points (branch exploration order selected instead of chain by chain)
            // explain this in the paper
            else {
                for (TimePoint dest : conns.values()) {
                    if (!visited_chains.containsKey(dest.getChain())) { // for recursive iterations in next points of the base chain
                        found = search_timegraph_y_after_x(dest, y, visited_chains);
                        visited_chains.put(new Integer(dest.getChain()), "v");
                        if (found) {
                            break;
                        }
                    }
                }
            }
        }

        if (!found) {
            // check connections of next points in the chain (until found)
            //System.err.println(c.getId() + "-------" + x.getChain());
            if (c.hasNext(x)) {
                found = search_timegraph_y_after_x(c.getNextTimePoint(x), y, visited_chains);
                //if (found) {break;}
            }
        }
        return found;
    }

    /**
     * Adds a cross chain relation for two points and check for fusion
     *
     * @param x
     * @param y
     * @return
     */
    public void add_after_crosschain(TimePoint x, TimePoint y) throws Exception {
        // check if previous connection exists and remove it if more informative
        
        TimePoint conflictpoint_x = null;
        // x already conected to y < conflict
        if (metagraph.get(x.getChain()).getAfterConnections(x.getPosition()) != null) {
            conflictpoint_x = metagraph.get(x.getChain()).getAfterConnections(x.getPosition()).get(y.getChain());
        }
        // y already connected to destpoint chain > conflict
        TimePoint conflictpoint_y = null;
        if (metagraph.get(y.getChain()).getBeforeConnections(y.getPosition()) != null) {
            conflictpoint_y = metagraph.get(y.getChain()).getBeforeConnections(y.getPosition()).get(x.getChain());
        }
        if (conflictpoint_x != null || conflictpoint_y != null) {
            // normal conflict
            if (conflictpoint_y == null && metagraph.get(x.getChain()).isMoreInformative(x.getPosition(), y, ">")) {
                metagraph.get(x.getChain()).removeConnection(x.getPosition(), conflictpoint_x, ">");
                metagraph.get(conflictpoint_x.getChain()).removeConnection(conflictpoint_x.getPosition(), x, "<");
                metagraph.get(x.getChain()).addConnection(x.getPosition(), y, ">");
                metagraph.get(y.getChain()).addConnection(y.getPosition(), x, "<");
            }
            // inverse conflict
            if (conflictpoint_x == null && metagraph.get(y.getChain()).isMoreInformative(y.getPosition(), x, "<")) {
                metagraph.get(y.getChain()).removeConnection(y.getPosition(), conflictpoint_y, "<");
                metagraph.get(conflictpoint_y.getChain()).removeConnection(conflictpoint_y.getPosition(), y, ">");
                metagraph.get(x.getChain()).addConnection(x.getPosition(), y, ">");
                metagraph.get(y.getChain()).addConnection(y.getPosition(), x, "<");
            }
            // double conflict
            if (conflictpoint_x != null && conflictpoint_y != null
                    && metagraph.get(x.getChain()).isMoreInformative(x.getPosition(), y, ">")
                    && metagraph.get(y.getChain()).isMoreInformative(y.getPosition(), x, "<")) {
                metagraph.get(x.getChain()).removeConnection(x.getPosition(), conflictpoint_x, ">");
                metagraph.get(conflictpoint_x.getChain()).removeConnection(conflictpoint_x.getPosition(), x, "<");
                metagraph.get(y.getChain()).removeConnection(y.getPosition(), conflictpoint_y, "<");
                metagraph.get(conflictpoint_y.getChain()).removeConnection(conflictpoint_y.getPosition(), y, ">");
                metagraph.get(x.getChain()).addConnection(x.getPosition(), y, ">");
                metagraph.get(y.getChain()).addConnection(y.getPosition(), x, "<");
            }
        } else {
            metagraph.get(x.getChain()).addConnection(x.getPosition(), y, ">");
            metagraph.get(y.getChain()).addConnection(y.getPosition(), x, "<");
        }


        /*if (metagraph.get(x.getChain()).isMoreInformative(x.getPosition(), y, ">")) {
         TimePoint aux = metagraph.get(x.getChain()).getAfterConnections(x.getPosition()).get(y.getChain());
         metagraph.get(x.getChain()).removeConnection(x.getPosition(), aux, ">");
         metagraph.get(aux.getChain()).removeConnection(aux.getPosition(), x, "<");
         } else {
         if (metagraph.get(y.getChain()).isMoreInformative(y.getPosition(), x, "<")) {
         TimePoint aux = metagraph.get(y.getChain()).getBeforeConnections(y.getPosition()).get(x.getChain());
         metagraph.get(y.getChain()).removeConnection(y.getPosition(), aux, "<");
         metagraph.get(aux.getChain()).removeConnection(aux.getPosition(), y, ">");
         }
         }
         metagraph.get(x.getChain()).addConnection(x.getPosition(), y, ">");
         metagraph.get(y.getChain()).addConnection(y.getPosition(), x, "<");
         */

        checkChainFusion(x, y); // TODO SACAR FUERA DE LA FUNCION Y PROVAR OTRA VEZ

    }

    public int createOrSelectNewChain() {
        // Chain selection/creation
        Chain c = null;
        if (metagraph.isEmpty()) {
            c = new Chain(0);
        } else {
            for (int i = 0; i < metagraph.size(); i++) {
                if (metagraph.get(i).isEmptyChain()) {
                    c = new Chain(metagraph.get(i).getId()); // this restarts the arrays
                    break;
                }
            }
            if (c == null) {
                c = new Chain(metagraph.get(metagraph.size() - 1).getId() + 1);
            }
        }
        if (((metagraph.size() - 1)) < c.getId()) {
            metagraph.add(c);
        } else {
            metagraph.set(c.getId(), c);
        }

        return c.getId();
    }

    /**
     * Given a relation name, return the inverse Allen-TimeML relation
     *
     * @param rel
     * @return
     */
    public static String reverseRelationCategory(String rel) {
        try {
            if (rel.equals("BEFORE")) {
                return "AFTER";
            }
            if (rel.equals("AFTER")) {
                return "BEFORE";
            }
            if (rel.equals("IBEFORE")) {
                return "IAFTER";
            }
            if (rel.equals("IAFTER")) {
                return "IBEFORE";
            }
            if (rel.equals("DURING")) {
                return "DURING_INV";
            }
            if (rel.equals("BEGINS")) {
                return "BEGUN_BY";
            }
            if (rel.equals("BEGUN_BY")) {
                return "BEGINS";
            }
            if (rel.equals("ENDS")) {
                return "ENDED_BY";
            }
            if (rel.equals("ENDED_BY")) {
                return "ENDS";
            }
            if (rel.equals("OVERLAPS")) {
                return "OVERLAPPED_BY";
            }
            if (rel.equals("OVERLAPPED_BY")) {
                return "OVERLAPS";
            }
            if (rel.equals("INCLUDES")) {
                return "IS_INCLUDED";
            }
            if (rel.equals("IS_INCLUDED")) {
                return "INCLUDES";
            }
            if (rel.equals("IDENTITY") || rel.equals("SIMULTANEOUS")) {
                return "SIMULTANEOUS";
            }
            throw new Exception("Unknow relation: " + rel);
        } catch (Exception e) {
            System.err.println("Errors found (TimeGraph):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
                System.exit(1);
            }
            return rel;
        }
    }

    public void checkChainFusion(TimePoint p1, TimePoint p2) {

        // fail-safe function --------------------------------------------
        String rel = "UNKNOWN";
        if (rel.equals("UNKNOWN") && metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()) != null && metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()).containsKey(p2.getChain()) && metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()).get(p2.getChain()).equals(p2)) {
            rel = "<";
        }
        if (rel.equals("UNKNOWN") && metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()) != null && metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()).containsKey(p2.getChain()) && metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()).get(p2.getChain()).equals(p2)) {
            rel = ">";
        }
        if (rel.equals("UNKNOWN")) {
            return;
        }
        //----------------------------------------------------------------

        if (rel.equals("<")) {
            // c1-last no next and c2-first no before and c1-last before connexion with c2-first
            if (!metagraph.get(p1.getChain()).hasNext(p1) && !metagraph.get(p2.getChain()).hasPrevious(p2)) {
                collapseChainBefore(p1, p2);
                return;
            }

            // c2 is completely included between c1 two consecutive points
            if (!metagraph.get(p2.getChain()).hasPrevious(p2) && metagraph.get(p1.getChain()).hasNext(p1)
                    && metagraph.get(p1.getChain()).getAfterConnections(metagraph.get(p1.getChain()).getNextPosition(p1)) != null
                    && metagraph.get(p1.getChain()).getAfterConnections(metagraph.get(p1.getChain()).getNextPosition(p1)).get(p2.getChain()) != null
                    && metagraph.get(p1.getChain()).getAfterConnections(metagraph.get(p1.getChain()).getNextPosition(p1)).get(p2.getChain()).getPosition() == metagraph.get(p2.getChain()).getTimePoints().lastKey()) {
                collapseChainBetween(p2.getChain(), p1, metagraph.get(p1.getChain()).getNextTimePoint(p1));
                return;
            }

            // c1 is completely included between c2 two consecutive points
            if (!metagraph.get(p1.getChain()).hasNext(p1) && metagraph.get(p2.getChain()).hasPrevious(p2)
                    && metagraph.get(p2.getChain()).getBeforeConnections(metagraph.get(p2.getChain()).getPreviousPosition(p2)) != null
                    && metagraph.get(p2.getChain()).getBeforeConnections(metagraph.get(p2.getChain()).getPreviousPosition(p2)).get(p1.getChain()) != null
                    && metagraph.get(p2.getChain()).getBeforeConnections(metagraph.get(p2.getChain()).getPreviousPosition(p2)).get(p1.getChain()).getPosition() == metagraph.get(p1.getChain()).getTimePoints().firstKey()) {
                collapseChainBetween(p1.getChain(), metagraph.get(p2.getChain()).getPreviousTimePoint(p2), p2);
                return;
            }

            // COMPLEX CASES
            //(forget for now because these in some cases may introduce more chains in some cases)
            // c1 is partially included between c2 two consecutive points
            // c2 is partially included between c1 two consecutive points
        } else {
            // c1-first no before and c2-last no after and c1-first after connexion with c2-last
            if (!metagraph.get(p1.getChain()).hasPrevious(p1) && !metagraph.get(p2.getChain()).hasNext(p2)) {
                collapseChainBefore(p2, p1);
                return;
            }

            // c2 is completely included between c1 two consecutive points
            if (!metagraph.get(p2.getChain()).hasNext(p2) && metagraph.get(p1.getChain()).hasPrevious(p1)
                    && metagraph.get(p1.getChain()).getBeforeConnections(metagraph.get(p1.getChain()).getPreviousPosition(p1)) != null
                    && metagraph.get(p1.getChain()).getBeforeConnections(metagraph.get(p1.getChain()).getPreviousPosition(p1)).get(p2.getChain()) != null
                    && metagraph.get(p1.getChain()).getBeforeConnections(metagraph.get(p1.getChain()).getPreviousPosition(p1)).get(p2.getChain()).getPosition() == metagraph.get(p2.getChain()).getTimePoints().firstKey()) {
                collapseChainBetween(p2.getChain(), metagraph.get(p1.getChain()).getPreviousTimePoint(p1), p1);
                return;
            }

            // c1 is completely included between c2 two consecutive points
            if (!metagraph.get(p1.getChain()).hasPrevious(p1) && metagraph.get(p2.getChain()).hasNext(p2)
                    && metagraph.get(p2.getChain()).getAfterConnections(metagraph.get(p2.getChain()).getNextPosition(p2)) != null
                    && metagraph.get(p2.getChain()).getAfterConnections(metagraph.get(p2.getChain()).getNextPosition(p2)).get(p1.getChain()) != null
                    && metagraph.get(p2.getChain()).getAfterConnections(metagraph.get(p2.getChain()).getNextPosition(p2)).get(p1.getChain()).getPosition() == metagraph.get(p1.getChain()).getTimePoints().lastKey()) {
                collapseChainBetween(p1.getChain(), p2, metagraph.get(p2.getChain()).getNextTimePoint(p2));
                return;
            }

            // COMPLEX CASES
            //(forget for now because these in some cases may introduce more chains in some cases)
            // c1 is partially included between c2 two consecutive points
            // c2 is partially included between c1 two consecutive points
        }

    }

    public boolean collapseChainBefore(TimePoint p1, TimePoint p2) {
        try {
            if (p1.getChain() == p2.getChain() || metagraph.get(p1.getChain()).hasNext(p1) || metagraph.get(p2.getChain()).hasPrevious(p2)) {
                throw new Exception("Chains can not be collapsed as " + p1 + " before " + p2);
            } else {
                // remove connections
                metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), p2, "<");
                metagraph.get(p2.getChain()).removeConnection(p2.getPosition(), p1, ">");
                // add points in order
                TimePoint insertBeforePoint = p2;

                while (metagraph.get(p1.getChain()).hasPrevious(p1)) {
                    TimePoint nextPoint2Insert = metagraph.get(p1.getChain()).getPreviousTimePoint(p1);
                    // add new point to metagraph
                    TimePoint np = new TimePoint(insertBeforePoint.getChain(), insertBeforePoint.getPositionBefore(), p1.getAssociatedEntities());

                    metagraph.get(insertBeforePoint.getChain()).getTimePoints().put(insertBeforePoint.getPositionBefore(), np);
                    // update connkeys
                    Set<Integer> connkeys = null;
                    if (metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()) != null) {
                        connkeys = new HashSet<Integer>();
                        connkeys.addAll(metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()).keySet());
                    }
                    // IMP: If not separatekeys breaks on update... concurrent modification... of metagraph connkeys.
                    if (connkeys != null) {
                        for (Integer key : connkeys) {
                            TimePoint dest_point = metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()).get(key);
                            metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), p1, "<");
                            metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), dest_point, ">");
                            TimePoint conflictpoint = null;
                            if (metagraph.get(dest_point.getChain()).getBeforeConnections(dest_point.getPosition()) != null) {
                                conflictpoint = metagraph.get(dest_point.getChain()).getBeforeConnections(dest_point.getPosition()).get(p2.getChain());
                            }
                            if (dest_point.getChain() == np.getChain()) {
                                continue;
                            }
                            if (conflictpoint != null) {
                                // tenint en compte dest_point si ja existeix una conexio amb la cadena de p2 hi ha dos opcions:
                                // quedarse en la relació q te o
                                // o llevar la que te i ficar la q está apunt de ficar
                                if (metagraph.get(dest_point.getChain()).isMoreInformative(dest_point.getPosition(), np, "<")) {
                                    metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), conflictpoint, "<");
                                    metagraph.get(conflictpoint.getChain()).removeConnection(conflictpoint.getPosition(), dest_point, ">");
                                    metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, "<");
                                    metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, ">");
                                }
                            } else {
                                metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, "<");
                                metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, ">");
                            }
                        }
                    }
                    connkeys = null;
                    if (metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()) != null) {
                        connkeys = new HashSet<Integer>();
                        connkeys.addAll(metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()).keySet());
                    }
                    if (connkeys != null) {
                        for (Integer key : connkeys) {
                            TimePoint dest_point = metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()).get(key); // connection to move

                            // remove original connection
                            metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), p1, ">");
                            metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), dest_point, "<");
                            TimePoint conflictpoint = null;
                            if (metagraph.get(dest_point.getChain()).getAfterConnections(dest_point.getPosition()) != null) {
                                conflictpoint = metagraph.get(dest_point.getChain()).getAfterConnections(dest_point.getPosition()).get(p2.getChain());
                            }
                            if (dest_point.getChain() == np.getChain()) {
                                continue;
                            }
                            //aquí tb puede haber double conflct con np ya tiene conexion a dest... no lo entiendo debe haber algo mal (debuggin pero no hoy)
                            if (conflictpoint != null) {
                                // tenint en compte dest_point si ja existeix una conexio amb la cadena de p2 hi ha dos opcions:
                                // quedarse en la relació q te o
                                // o llevar la que te i ficar la q está apunt de ficar
                                // si no es més informativa la nova, quedarse en la relació q te
                                // si es més informativa llevar la que te i ficar la q está apunt de ficar
                                if (metagraph.get(dest_point.getChain()).isMoreInformative(dest_point.getPosition(), np, ">")) {
                                    metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), conflictpoint, ">");
                                    metagraph.get(conflictpoint.getChain()).removeConnection(conflictpoint.getPosition(), dest_point, "<");
                                    metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, ">");
                                    metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, "<");
                                }
                            } else {
                                metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, ">");
                                metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, "<");
                            }
                        }
                    }
                    // search and update entity-tp references (change to new point)
                    String[] associated_entities = p1.getAssociatedEntities().split(",");
                    for (int aen = 0; aen < associated_entities.length; aen++) {
                        entity_tp_map.put(associated_entities[aen], np);
                    }

                    //update to next point
                    insertBeforePoint = np;
                    p1 = nextPoint2Insert;
                }
                // add last point and clean chain

                // add new point to metagraph
                TimePoint np = new TimePoint(insertBeforePoint.getChain(), insertBeforePoint.getPositionBefore(), p1.getAssociatedEntities());
                metagraph.get(np.getChain()).getTimePoints().put(np.getPosition(), np);
                // update connkeys
                Set<Integer> connkeys = null;
                if (metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()) != null) {
                    connkeys = new HashSet<Integer>();
                    connkeys.addAll(metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()).keySet());
                }
                if (connkeys != null) {
                    for (Integer key : connkeys) {
                        TimePoint dest_point = metagraph.get(p1.getChain()).getAfterConnections(p1.getPosition()).get(key); // connection to be moved

                        // remove original connection
                        metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), p1, "<");
                        metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), dest_point, ">");

                        TimePoint conflictpoint = null;
                        if (metagraph.get(dest_point.getChain()).getBeforeConnections(dest_point.getPosition()) != null) {
                            conflictpoint = metagraph.get(dest_point.getChain()).getBeforeConnections(dest_point.getPosition()).get(p2.getChain());
                        }
                        if (dest_point.getChain() == np.getChain()) {
                            continue;
                        }
                        if (conflictpoint != null) {
                            // tenint en compte dest_point si ja existeix una conexio amb la cadena de p2 hi ha dos opcions:
                            // quedarse en la relació q te o
                            // o llevar la que te i ficar la q está apunt de ficar
                            if (metagraph.get(dest_point.getChain()).isMoreInformative(dest_point.getPosition(), np, "<")) {
                                metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), conflictpoint, "<");
                                metagraph.get(conflictpoint.getChain()).removeConnection(conflictpoint.getPosition(), dest_point, ">");
                                metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, "<");
                                metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, ">");
                            }
                        } else {
                            metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, "<");
                            metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, ">");
                        }
                    }
                }
                connkeys = null;
                if (metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()) != null) {
                    connkeys = new HashSet<Integer>();
                    connkeys.addAll(metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()).keySet());
                }
                if (connkeys != null) {
                    for (Integer key : connkeys) {
                        TimePoint dest_point = metagraph.get(p1.getChain()).getBeforeConnections(p1.getPosition()).get(key);// connection to be moved

                        //remove original connection
                        metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), p1, ">");
                        metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), dest_point, "<");

                        TimePoint conflictpoint = null;
                        if (metagraph.get(dest_point.getChain()).getAfterConnections(dest_point.getPosition()) != null) {
                            conflictpoint = metagraph.get(dest_point.getChain()).getAfterConnections(dest_point.getPosition()).get(p2.getChain());
                        }
                        if (dest_point.getChain() == np.getChain()) {
                            continue;
                        }
                        if (conflictpoint != null) {
                            // tenint en compte dest_point si ja existeix una conexio amb la cadena de p2 hi ha dos opcions:
                            // quedarse en la relació q te o
                            // o llevar la que te i ficar la q está apunt de ficar
                            // si no es més informativa la nova, quedarse en la relació q te
                            // si es més informativa llevar la que te i ficar la q está apunt de ficar
                            if (metagraph.get(dest_point.getChain()).isMoreInformative(dest_point.getPosition(), np, ">")) {
                                metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), conflictpoint, ">");
                                metagraph.get(conflictpoint.getChain()).removeConnection(conflictpoint.getPosition(), dest_point, "<");
                                metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, ">");
                                metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, "<");
                            }
                        } else {
                            metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, ">");
                            metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, "<");
                        }
                    }
                }
                // search and update entity-tp references (change to new point)
                String[] associated_entities = p1.getAssociatedEntities().split(",");
                for (int aen = 0; aen < associated_entities.length; aen++) {
                    entity_tp_map.put(associated_entities[aen], np);
                }


                // Empty chain (clear)
                metagraph.get(p1.getChain()).clear();

            }
        } catch (Exception e) {
            System.err.println("Errors found (TimeGraph):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return false;
        }
        return true;
    }

    public boolean collapseChainBetween(int chain, TimePoint p1, TimePoint p2) {
        try {
            if (chain == p1.getChain() || p1.getChain() != p2.getChain() || !metagraph.get(p1.getChain()).hasNext(p1) || !metagraph.get(p1.getChain()).isNextFor(p1, p2)) {
                throw new Exception("Chain" + chain + " can not be collapsed between " + p1 + " and " + p2);
            } else {
                // remove connections
                metagraph.get(p1.getChain()).removeConnection(p1.getPosition(), metagraph.get(chain).getTimePoints().firstEntry().getValue(), "<");
                metagraph.get(chain).removeConnection(metagraph.get(chain).getTimePoints().firstKey(), p1, ">");
                metagraph.get(p2.getChain()).removeConnection(p2.getPosition(), metagraph.get(chain).getTimePoints().lastEntry().getValue(), ">");
                metagraph.get(chain).removeConnection(metagraph.get(chain).getTimePoints().lastKey(), p2, "<");

                // obtain value increment
                int increment = (p2.getPosition() - p1.getPosition()) / (metagraph.get(chain).getTimePoints().size() + 1);
                if (increment == 0) {
                    throw new Exception("Graph capacity exceeded, it is not possible to collapse chains. Left uncollapsed.");
                }
                // add points in order
                int incr_counter = 0;
                for (TimePoint tp : metagraph.get(chain).getTimePoints().values()) {
                    incr_counter++;
                    // add new point to metagraph
                    TimePoint np = new TimePoint(p1.getChain(), p1.getPosition() + incr_counter * increment, tp.getAssociatedEntities());
                    metagraph.get(p1.getChain()).getTimePoints().put(np.getPosition(), np);

                    Set<Integer> connkeys = null;
                    if (metagraph.get(chain).getAfterConnections(tp.getPosition()) != null) {
                        connkeys = new HashSet<Integer>();
                        connkeys.addAll(metagraph.get(chain).getAfterConnections(tp.getPosition()).keySet());
                    }
                    if (connkeys != null) {
                        for (Integer key : connkeys) {
                            TimePoint dest_point = metagraph.get(chain).getAfterConnections(tp.getPosition()).get(key);
                            // remove original connection
                            metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), tp, "<");
                            metagraph.get(tp.getChain()).removeConnection(tp.getPosition(), dest_point, ">");
                            TimePoint conflictpoint = null;
                            if (metagraph.get(dest_point.getChain()).getBeforeConnections(dest_point.getPosition()) != null) {
                                conflictpoint = metagraph.get(dest_point.getChain()).getBeforeConnections(dest_point.getPosition()).get(p2.getChain());
                            }
                            if (conflictpoint != null) {
                                // tenint en compte dest_point si ja existeix una conexio amb la cadena de p2 hi ha dos opcions:
                                // quedarse en la relació q te o
                                // o llevar la que te i ficar la q está apunt de ficar
                                if (metagraph.get(dest_point.getChain()).isMoreInformative(dest_point.getPosition(), np, "<")) {
                                    metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), conflictpoint, "<");
                                    metagraph.get(conflictpoint.getChain()).removeConnection(conflictpoint.getPosition(), dest_point, ">");
                                    metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, "<");
                                    metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, ">");
                                }
                            } else {
                                metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, "<");
                                metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, ">");
                            }
                        }
                    }
                    connkeys = null;
                    if (metagraph.get(chain).getBeforeConnections(tp.getPosition()) != null) {
                        connkeys = new HashSet<Integer>();
                        connkeys.addAll(metagraph.get(chain).getBeforeConnections(tp.getPosition()).keySet());
                    }
                    if (connkeys != null) {
                        for (Integer key : connkeys) {
                            TimePoint dest_point = metagraph.get(chain).getBeforeConnections(tp.getPosition()).get(key);
                            metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), tp, ">");
                            metagraph.get(tp.getChain()).removeConnection(tp.getPosition(), dest_point, "<");
                            TimePoint conflictpoint = null;
                            if (metagraph.get(dest_point.getChain()).getAfterConnections(dest_point.getPosition()) != null) {
                                conflictpoint = metagraph.get(dest_point.getChain()).getAfterConnections(dest_point.getPosition()).get(p2.getChain());
                            }
                            if (conflictpoint != null) {
                                // tenint en compte dest_point si ja existeix una conexio amb la cadena de p2 hi ha dos opcions:
                                // si no es més informativa la nova, quedarse en la relació q te
                                // si es més informativa llevar la que te i ficar la q está apunt de ficar
                                if (metagraph.get(dest_point.getChain()).isMoreInformative(dest_point.getPosition(), np, ">")) {
                                    metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), conflictpoint, ">");
                                    metagraph.get(conflictpoint.getChain()).removeConnection(conflictpoint.getPosition(), dest_point, "<");
                                    metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, ">");
                                    metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, "<");
                                }
                            } else {
                                metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, ">");
                                metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, "<");
                            }
                        }
                    }
                    // search and update entity-tp references (change to new point)
                    String[] associated_entities = tp.getAssociatedEntities().split(",");
                    for (int aen = 0; aen < associated_entities.length; aen++) {
                        entity_tp_map.put(associated_entities[aen], np);
                    }
                }

                // Empty chain (clear)
                metagraph.get(chain).clear();

            }
        } catch (Exception e) {
            System.err.println("Errors found (TimeGraph):\n\t" + e.toString() + "\n");
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                e.printStackTrace(System.err);
            }
            return false;
        }
        return true;
    }

    /**
     * Given the implementation model it might happen that a chain or some
     * chains end up empty after the construction of the graph.
     *
     * That happens when chains are collapsed due to new relations between
     * existing entities.
     *
     * This function removes such chains: if it is the last one it is just
     * removed, if not the last one is moved in its place and then the empty
     * remains the last one and it is removed
     *
     */
    public boolean removeEmptyChains() {
        try {
            for (int i = 0; i < metagraph.size(); i++) {
                if (metagraph.get(i).isEmptyChain()) {
                    removeEmptyChain(i);
                }
            }
        } catch (Exception e) {
            System.err.println("Errors found (TimeGraph):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    /**
     *
     * Remove an chain_number chain: if it is the last one it is just removed,
     * if not, if the last one is not chain_number it is moved in its place, if
     * the last one is chain_number it is removed and the last non-chain_number
     * chain is used instead. Then the new last one is chain_number and is
     * removed.
     *
     * @param chain_number chain_number chain number
     */
    public boolean removeEmptyChain(int chain_number) {
        try {
            int lastchain = metagraph.size() - 1;
            if (chain_number != lastchain) {
                while (metagraph.get(lastchain).isEmptyChain()) {
                    metagraph.remove(lastchain);
                    lastchain -= 1;
                }
                // replace with last one
                for (TimePoint tp : metagraph.get(lastchain).getTimePoints().values()) {
                    // add new point to metagraph
                    TimePoint np = new TimePoint(chain_number, tp.getPosition(), tp.getAssociatedEntities());
                    metagraph.get(chain_number).getTimePoints().put(np.getPosition(), np);

                    HashMap<Integer, TimePoint> conns = metagraph.get(lastchain).getAfterConnections(tp.getPosition());
                    if (conns != null) {
                        for (Integer key : conns.keySet()) {
                            TimePoint dest_point = conns.get(key);
                            metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), tp, "<");
                            metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, "<");
                            metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, ">");
                        }
                    }
                    conns = metagraph.get(lastchain).getBeforeConnections(tp.getPosition());
                    if (conns != null) {
                        for (Integer key : conns.keySet()) {
                            TimePoint dest_point = conns.get(key);
                            metagraph.get(dest_point.getChain()).removeConnection(dest_point.getPosition(), tp, ">");
                            metagraph.get(dest_point.getChain()).addConnection(dest_point.getPosition(), np, ">");
                            metagraph.get(np.getChain()).addConnection(np.getPosition(), dest_point, "<");
                        }
                    }
                    // search and update entity-tp references (change to new point)
                    String[] associated_entities = tp.getAssociatedEntities().split(",");
                    for (int aen = 0; aen < associated_entities.length; aen++) {
                        entity_tp_map.put(associated_entities[aen], np);
                    }
                }
                //metagraph.remove(lastchain);
            }
            metagraph.remove(lastchain);

        } catch (Exception e) {
            System.err.println("Errors found (TimeGraph):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String out = "";

        for (int i = metagraph.size() - 1; i >= 0; i--) {
            Chain current_chain = metagraph.get(i);
            boolean firstpoint = true;
            out += "\nChain " + i + ": ";
            for (TimePoint tp : current_chain.getTimePoints().values()) {
                if (firstpoint) {
                    firstpoint = false;
                } else {
                    out += " ---> ";
                }
                HashMap<Integer, TimePoint> before_connections = metagraph.get(tp.getChain()).getBeforeConnections(tp.getPosition());
                HashMap<Integer, TimePoint> after_connections = metagraph.get(tp.getChain()).getAfterConnections(tp.getPosition());
                // before the after because these points are before...
                if (after_connections != null && after_connections.size() > 0) {
                    out += " (>-conn: ";
                    boolean first = true;
                    for (TimePoint conn : after_connections.values()) {
                        if (first) {
                            first = false;
                        } else {
                            out += ",";
                        }
                        out += conn.getAssociatedEntities();
                    }
                    out += ") ";
                }
                if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                    out += tp + "[" + tp.getAssociatedEntities() + "]";
                } else {
                    out += tp.getAssociatedEntities();
                }
                if (before_connections != null && before_connections.size() > 0) {
                    out += " (<-conn: ";
                    boolean first = true;
                    for (TimePoint conn : before_connections.values()) {
                        if (first) {
                            first = false;
                        } else {
                            out += ",";
                        }
                        out += conn.getAssociatedEntities();
                    }
                    out += ") ";
                }

            }
            out += "\n";
        }

        return out;
    }

    /**
     * Return if the TimeGraph contains a relation between two entities (time
     * intervals)
     *
     * @param x
     * @param y
     * @param rel
     * @return
     */
    public String checkRelation(String x, String y, String rel) {

        // Note: entities casing is kept because TimeGraph is not limited to timeml

        rel = rel.toUpperCase();
        // inverse relation if needed
        if (!rel.matches("(BEFORE|IBEFORE|BEGINS|ENDS|OVERLAPS|IS_INCLUDED|SIMULTANEOUS)")) {
            rel = reverseRelationCategory(rel);
            String aux = x;
            x = y;
            y = aux;
        }

        x = replace_e_by_ei_if_needed(x);
        y = replace_e_by_ei_if_needed(y);

        // TimePoints are already known
        String x1 = x + "_s";
        String x2 = x + "_e";
        String y1 = y + "_s";
        String y2 = y + "_e";

        if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
            System.err.println("Entities " + x + "-" + y + " relation " + rel + "\n");
        }

        TimePoint tpx1 = entity_tp_map.get(x1);
        TimePoint tpx2 = entity_tp_map.get(x2);
        TimePoint tpy1 = entity_tp_map.get(y1);
        TimePoint tpy2 = entity_tp_map.get(y2);


        try {
            // if the question is about an entity that is not in the graph check if it is an iso date
            if (tpx1 == null || tpx2 == null) {
                if (DateUtils.is_clean_ISO8601_date_part(x)) {
                    String interval = DateUtils.get_interval_value(x);
                    if (date_entitypoint_map.containsKey(iso.parse(interval.split("\\|")[0])) && date_entitypoint_map.containsKey(iso.parse(interval.split("\\|")[1]))) {
                        x1 = date_entitypoint_map.get(iso.parse(interval.split("\\|")[0]));
                        x2 = date_entitypoint_map.get(iso.parse(interval.split("\\|")[1]));
                        tpx1 = entity_tp_map.get(x1);
                        tpx2 = entity_tp_map.get(x2);
                    } else {
                        throw new Exception(x);
                    }
                } else {
                    throw new Exception(x);
                }
            }
            if (tpy1 == null || tpy2 == null) {
                if (DateUtils.is_clean_ISO8601_date_part(y)) {
                    String interval = DateUtils.get_interval_value(y);
                    if (date_entitypoint_map.containsKey(iso.parse(interval.split("\\|")[0])) && date_entitypoint_map.containsKey(iso.parse(interval.split("\\|")[1]))) {
                        y1 = date_entitypoint_map.get(iso.parse(interval.split("\\|")[0]));
                        y2 = date_entitypoint_map.get(iso.parse(interval.split("\\|")[1]));
                        tpy1 = entity_tp_map.get(y1);
                        tpy2 = entity_tp_map.get(y2);
                    } else {
                        throw new Exception(y);
                    }
                } else {
                    throw new Exception(y);
                }
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "unknown (entity " + e.getMessage() + " not found)";
        }

        if (rel.equals("BEFORE")) { // Allen (<)  x1 < x2 < y1 < y2
            String pointrel = getPointRelation(tpx2, tpy1);
            if (pointrel.equals("<")) {
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
        }

        if (rel.equals("IBEFORE")) { // Allen's meets (m) x1 < (x2 = y1) < y2
            String pointrel = getPointRelation(tpx2, tpy1);
            if (pointrel.equals("=")) { // same as tpx2.equals(tpy1)
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
        }

        if (rel.equals("BEGINS")) { // Allen's starts (s) (x1 = y1) < x2 < y2
            //x1=y1
            String pointrel = getPointRelation(tpx1, tpy1);
            if (!pointrel.equals("=")) {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
            // x2<y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("<")) {
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unkonwn";
                }
            }
        }

        if (rel.equals("ENDS")) { // Allen's Finishes (f) y1 < x1 < (x2 = y2)
            //x2=y2
            String pointrel = getPointRelation(tpx2, tpy2);
            if (!pointrel.equals("=")) {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
            // y1 < x1
            pointrel = getPointRelation(tpy1, tpx1);
            if (pointrel.equals("<")) {
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
        }

        if (rel.equals("OVERLAPS")) { // Allen's Overlaps (o) x1 < y1 < x2 < y2
            // x1 < y1
            String pointrel = getPointRelation(tpx1, tpy1);
            if (!pointrel.equals("<")) {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
            // y1<x2
            pointrel = getPointRelation(tpy1, tpx2);
            if (pointrel.equals("<")) {
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
        }

        if (rel.equals("IS_INCLUDED")) { // Allen's during (d) y1 < x1 < x2 < y2
            // y1 < x1
            String pointrel = getPointRelation(tpy1, tpx1);
            if (!pointrel.equals("<")) {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
            // x2<y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("<")) {
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
        }

        if (rel.equals("SIMULTANEOUS")) { //  # Allen's equal (=) (x1 = y1) < (x2 = y2)
            //x1=y1
            String pointrel = getPointRelation(tpx1, tpy1);
            if (!pointrel.equals("=")) {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
            //x2=y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("=")) {
                return "yes";
            } else {
                if (!pointrel.equals("UNKNOWN")) {
                    return "no";
                } else {
                    return "unknown";
                }
            }
        }
        return "unknown";
    }

    /**
     * Return the relation of 2 entities in the TimeGraph or UNKNOWN if these
     * are not related
     *
     * @param x
     * @param y
     * @param rel
     * @return
     */
    public String getRelation(String x, String y) {
        // TimePoints are already known
        String x1 = x + "_s";
        String x2 = x + "_e";
        String y1 = y + "_s";
        String y2 = y + "_e";

        TimePoint tpx1 = entity_tp_map.get(x1);
        TimePoint tpx2 = entity_tp_map.get(x2);
        TimePoint tpy1 = entity_tp_map.get(y1);
        TimePoint tpy2 = entity_tp_map.get(y2);
        try {
            if (tpx1 == null || tpx2 == null) {
                Date d = iso.parse(x);
                String interval = DateUtils.get_interval_value(x);
                if (date_entitypoint_map.containsKey(iso.parse(interval.split("\\|")[0])) && date_entitypoint_map.containsKey(iso.parse(interval.split("\\|")[1]))) {
                    x1 = date_entitypoint_map.get(iso.parse(interval.split("\\|")[0]));
                    x2 = date_entitypoint_map.get(iso.parse(interval.split("\\|")[1]));
                    tpx1 = entity_tp_map.get(x1);
                    tpx2 = entity_tp_map.get(x2);
                } else {
                    throw new Exception(x);
                }
            }
            if (tpy1 == null || tpy2 == null) {
                Date d = iso.parse(y);
                String interval = DateUtils.get_interval_value(y);
                if (date_entitypoint_map.containsKey(iso.parse(interval.split("\\|")[0])) && date_entitypoint_map.containsKey(iso.parse(interval.split("\\|")[1]))) {
                    y1 = date_entitypoint_map.get(iso.parse(interval.split("\\|")[0]));
                    y2 = date_entitypoint_map.get(iso.parse(interval.split("\\|")[1]));
                    tpy1 = entity_tp_map.get(y1);
                    tpy2 = entity_tp_map.get(y2);
                } else {
                    throw new Exception(y);
                }
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "unknown (entity " + e.getMessage() + " not found)";
        }

        // BEFORE Allen (<)  x1 < x2 < y1 < y2
        String pointrel = getPointRelation(tpx2, tpy1);
        if (pointrel.equals("<")) {
            return "BEFORE";
        }

        // IBEFORE Allen's meets (m) x1 < (x2 = y1) < y2
        if (pointrel.equals("=")) { // same as tpx2.equals(tpy1)
            return "IBEFORE";
        }

        // BEGINS Allen's starts (s) (x1 = y1) < x2 < y2
        //x1=y1
        pointrel = getPointRelation(tpx1, tpy1);
        if (pointrel.equals("=")) {
            // x2<y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("<")) {
                return "BEGINS";
            }
        }


        // ENDS Allen's Finishes (f) y1 < x1 < (x2 = y2)
        //x2=y2
        pointrel = getPointRelation(tpx2, tpy2);
        if (pointrel.equals("=")) {
            // y1 < x1
            pointrel = getPointRelation(tpy1, tpx1);
            if (pointrel.equals("<")) {
                return "ENDS";
            }
        }


        // OVERLAPS Allen's Overlaps (o) x1 < y1 < x2 < y2
        // x1 < y1
        pointrel = getPointRelation(tpx1, tpy1);
        if (pointrel.equals("<")) {
            // y1<x2
            pointrel = getPointRelation(tpy1, tpx2);
            if (pointrel.equals("<")) {
                return "OVERLAPS";
            }
        }

        // IS_INCLUDED Allen's during (d) y1 < x1 < x2 < y2
        // y1 < x1
        pointrel = getPointRelation(tpy1, tpx1);
        if (pointrel.equals("<")) {
            // x2<y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("<")) {
                return "IS_INCLUDED";
            }
        }

        // SIMULTANEOUS Allen's equal (=) (x1 = y1) < (x2 = y2)
        //x1=y1
        pointrel = getPointRelation(tpx1, tpy1);
        if (pointrel.equals("=")) {
            //x2=y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("=")) {
                return "SIMULTANEOUS";
            }
        }

        // inverse relation and check again
        String aux = x;
        x = y;
        y = aux;
        // TimePoints are already known
        x1 = x + "_s";
        x2 = x + "_e";
        y1 = y + "_s";
        y2 = y + "_e";

        tpx1 = entity_tp_map.get(x1);
        tpx2 = entity_tp_map.get(x2);
        tpy1 = entity_tp_map.get(y1);
        tpy2 = entity_tp_map.get(y2);

        // BEFORE Allen (<)  x1 < x2 < y1 < y2
        pointrel = getPointRelation(tpx2, tpy1);
        if (pointrel.equals("<")) {
            return "AFTER";
        }

        // IBEFORE Allen's meets (m) x1 < (x2 = y1) < y2
        if (pointrel.equals("=")) { // same as tpx2.equals(tpy1)
            return "IAFTER";
        }

        // BEGINS Allen's starts (s) (x1 = y1) < x2 < y2
        //x1=y1
        pointrel = getPointRelation(tpx1, tpy1);
        if (pointrel.equals("=")) {
            // x2<y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("<")) {
                return "BEGUN_BY";
            }
        }


        // ENDS Allen's Finishes (f) y1 < x1 < (x2 = y2)
        //x2=y2
        pointrel = getPointRelation(tpx2, tpy2);
        if (pointrel.equals("=")) {
            // y1 < x1
            pointrel = getPointRelation(tpy1, tpx1);
            if (pointrel.equals("<")) {
                return "ENDED_BY";
            }
        }
        // OVERLAPS Allen's Overlaps (o) x1 < y1 < x2 < y2
        // x1 < y1
        pointrel = getPointRelation(tpx1, tpy1);
        if (pointrel.equals("<")) {
            // y1<x2
            pointrel = getPointRelation(tpy1, tpx2);
            if (pointrel.equals("<")) {
                return "OVERLAPPED_BY";
            }
        }

        // IS_INCLUDED Allen's during (d) y1 < x1 < x2 < y2
        // y1 < x1
        pointrel = getPointRelation(tpy1, tpx1);
        if (pointrel.equals("<")) {
            // x2<y2
            pointrel = getPointRelation(tpx2, tpy2);
            if (pointrel.equals("<")) {
                return "INCLUDES";
            }
        }

        // SIMULTANEOUS Allen's equal (=) (x1 = y1) < (x2 = y2)
        // ALREADY CHECKED

        return "unknown";
    }

    public String getEntitiesBeforeEntity(String x) {
        // TimePoints are already known
        String x1 = x + "_s";
        TimePoint tpx1 = entity_tp_map.get(x1);
        try {
            if (tpx1 == null) {
                String interval = DateUtils.get_interval_value(x);
                Date d = iso.parse(interval.split("\\|")[0]);
                if (date_entitypoint_map.containsKey(iso.parse(interval.split("\\|")[0]))) {
                    x1 = date_entitypoint_map.get(iso.parse(interval.split("\\|")[0]));
                    tpx1 = entity_tp_map.get(x1);
                } else {
                    if (date_entitypoint_map.isEmpty()) {
                        return "[] (there are no dates to compare in the graph)";
                    } else {
                        date_entitypoint_map.put(d, "--query--");
                        if (date_entitypoint_map.lowerKey(d) != null) {
                            x1 = date_entitypoint_map.lowerEntry(d).getValue();
                            tpx1 = entity_tp_map.get(x1);
                        } else {
                            return "[] (there are no dates before " + x + ")";
                        }
                        date_entitypoint_map.remove(d);
                    }
                }
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }
        // traverse before
        HashMap<Integer, String> visited_chains = new HashMap<Integer, String>();
        return contract_entities(get_timegraph_before(tpx1, visited_chains));
    }

    public String get_timegraph_before(TimePoint x, HashMap<Integer, String> visited_chains) {
        String output = x.getAssociatedEntities();
        Chain c = metagraph.get(x.getChain());
        visited_chains.put(new Integer(x.getChain()), "v");
        // check point conections
        HashMap<Integer, TimePoint> conns = c.getAfterConnections(x.getPosition());
        if (conns != null) {
            // explore direc connexions 
            for (TimePoint dest : conns.values()) {
                if (!visited_chains.containsKey(dest.getChain())) { // for recursive iterations in next points of the base chain
                    String aux = get_timegraph_before(dest, visited_chains);
                    if (!output.equals("") && !aux.equals("") && !aux.startsWith(",") && !output.endsWith(",")) {
                        output += ",";
                    }
                    output += aux;
                    visited_chains.put(new Integer(dest.getChain()), "v");
                }
            }
        }
        if (c.hasPrevious(x)) {
            String aux = get_timegraph_before(c.getPreviousTimePoint(x), visited_chains);
            if (!output.equals("") && !aux.equals("") && !aux.startsWith(",") && !output.endsWith(",")) {
                output += ",";
            }
            output += aux;
        }

        return output;
    }

    public String getEntitiesAfterEntity(String x) {
        // TimePoints are already known
        String x1 = x + "_e";
        TimePoint tpx1 = entity_tp_map.get(x1);
        try {
            if (tpx1 == null) {
                Date d = iso.parse(x);
                String interval = DateUtils.get_interval_value(x);
                if (date_entitypoint_map.containsKey(iso.parse(interval.split("\\|")[0]))) {
                    x1 = date_entitypoint_map.get(iso.parse(interval.split("\\|")[0]));
                    tpx1 = entity_tp_map.get(x1);
                } else {
                    if (date_entitypoint_map.isEmpty()) {
                        return "[] (there are no dates to compare in the graph)";
                    } else {
                        date_entitypoint_map.put(d, "--query--");
                        if (date_entitypoint_map.higherKey(d) != null) {
                            x1 = date_entitypoint_map.higherEntry(d).getValue();
                            tpx1 = entity_tp_map.get(x1);
                        } else {
                            return "[] (there are no dates after " + x + ")";
                        }
                        date_entitypoint_map.remove(d);
                    }
                }
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }
        // traverse before
        HashMap<Integer, String> visited_chains = new HashMap<Integer, String>();
        return contract_entities(get_timegraph_after(tpx1, visited_chains));
    }

    public String get_timegraph_after(TimePoint x, HashMap<Integer, String> visited_chains) {
        String output = x.getAssociatedEntities();
        Chain c = metagraph.get(x.getChain());
        visited_chains.put(new Integer(x.getChain()), "v");
        // check point conections
        HashMap<Integer, TimePoint> conns = c.getBeforeConnections(x.getPosition());
        if (conns != null) {
            // explore direc connexions
            for (TimePoint dest : conns.values()) {
                if (!visited_chains.containsKey(dest.getChain())) { // for recursive iterations in next points of the base chain
                    String aux = get_timegraph_after(dest, visited_chains);
                    if (!output.equals("") && !aux.equals("") && !aux.startsWith(",") && !output.endsWith(",")) {
                        output += ",";
                    }
                    output += aux;
                    visited_chains.put(new Integer(dest.getChain()), "v");
                }
            }
        }
        if (c.hasNext(x)) {
            String aux = get_timegraph_after(c.getNextTimePoint(x), visited_chains);
            if (!output.equals("") && !aux.equals("") && !aux.startsWith(",") && !output.endsWith(",")) {
                output += ",";
            }
            output += aux;
        }

        return output;
    }

    public String getEntitiesBetween(String x, String y) {
        // TimePoints are already known
        TimePoint tpx1 = null;
        TimePoint tpy2 = null;
        String x1 = x + "_s";
        tpx1 = entity_tp_map.get(x1);
        String y2 = y + "_e";
        tpy2 = entity_tp_map.get(y2);

        try {
            if (tpx1 == null) {
                throw new Exception(x);
            }
            if (tpy2 == null) {
                throw new Exception(y);
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }

        String relation = getPointRelation(tpx1, tpy2);
        if (relation.equals("UNKNOWN")) {
            return "[] (entities not related)";
        }

        if (relation.equals("=") || relation.equals(">")) {
            x1 = y + "_e";
            tpx1 = entity_tp_map.get(x1);
            y2 = x + "_s";
            tpy2 = entity_tp_map.get(y2);
            if (relation.equals("=")) {
                return "[] (same time point)";
            }
        }

        // traverse after
        String output = get_timegraph_after_matching_entity(tpx1, tpy2.getAssociatedEntities());

        return contract_entities(output);
    }

    public String getEntitiesWithinEntity(String x) {
        // TimePoints are already known
        TimePoint tpx1 = null;
        TimePoint tpx2 = null;
        String x1 = x + "_s";
        tpx1 = entity_tp_map.get(x1);
        String x2 = x + "_e";
        tpx2 = entity_tp_map.get(x2);
        try {
            if (tpx1 == null) {
                throw new Exception(x);
            }
            if (tpx2 == null) {
                throw new Exception(x);
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }

        // traverse after
        String output = get_timegraph_after_matching_entity(tpx1, tpx2.getAssociatedEntities());

        return contract_entities(output);
    }

    public String get_timegraph_after_matching_entity(TimePoint x, String e) {
        String output = x.getAssociatedEntities();
        Chain c = metagraph.get(x.getChain());
        //visited_chains.put(new Integer(x.getChain()), "v");
        if (output.contains(e)) {
            return output;
        }
        // check point conections
        HashMap<Integer, TimePoint> conns = c.getBeforeConnections(x.getPosition());
        if (conns != null) {
            // explore direc connexions
            for (TimePoint dest : conns.values()) {
                //if (!visited_chains.containsKey(dest.getChain())) { // for recursive iterations in next points of the base chain
                String aux = get_timegraph_after_matching_entity(dest, e);
                if (aux.contains(e)) {
                    if (!output.equals("") && !aux.equals("") && !aux.startsWith(",") && !output.endsWith(",")) {
                        output += ",";
                    }
                    output += aux;
                }
                //   visited_chains.put(new Integer(dest.getChain()), "v");
                //}
            }
        }
        if (c.hasNext(x)) {
            String aux = get_timegraph_after_matching_entity(c.getNextTimePoint(x), e);
            if (aux.contains(e)) {
                if (!output.equals("") && !aux.equals("") && !aux.startsWith(",") && !output.endsWith(",")) {
                    output += ",";
                }
                output += aux;
            }
        }
        if (!output.contains(e)) {
            output = "";
        }
        return output;
    }

    public String getEntitiesSinceEntity(String x) {
        // TimePoints are already known
        String x1 = x + "_s";
        TimePoint tpx1 = entity_tp_map.get(x1);
        try {
            if (tpx1 == null) {
                throw new Exception(x);
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }
        // traverse before
        HashMap<Integer, String> visited_chains = new HashMap<Integer, String>();
        return contract_entities(tpx1.getAssociatedEntities() + "," + get_timegraph_after(tpx1, visited_chains));
    }

    public String getEntitiesSimultaneous(String x) {
        // TimePoints are already known
        TimePoint tpx1 = null;
        TimePoint tpx2 = null;
        String x1 = x + "_s";
        tpx1 = entity_tp_map.get(x1);
        String x2 = x + "_e";
        tpx2 = entity_tp_map.get(x2);
        try {
            if (tpx1 == null) {
                throw new Exception(x);
            }
            if (tpx2 == null) {
                throw new Exception(x);
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }

        // traverse before
        return tpx1.getAssociatedEntities() + "," + tpx2.getAssociatedEntities();
    }

    public String getEntitiesIncludeEntity(String x) {
        // TimePoints are already known
        TimePoint tpx1 = null;
        TimePoint tpx2 = null;
        String x1 = x + "_s";
        tpx1 = entity_tp_map.get(x1);
        String x2 = x + "_e";
        tpx2 = entity_tp_map.get(x2);
        try {
            if (tpx1 == null) {
                throw new Exception(x);
            }
            if (tpx2 == null) {
                throw new Exception(x);
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return "[] (entity " + e.getMessage() + " not found)";
        }


        // traverse before
        HashMap<Integer, String> visited_chains = new HashMap<Integer, String>();
        String ending_after = get_timegraph_after(tpx2, visited_chains).replaceAll("[eit]+[0-9]+_s(,)?", "");
        visited_chains = new HashMap<Integer, String>();
        String starting_before = get_timegraph_before(tpx1, visited_chains).replaceAll("[eit]+[0-9]+_e(,)?", "");

        String output = starting_before;
        if (!output.equals("") && !output.endsWith(",") && !ending_after.equals("")) {
            output += ",";
        }
        if (!ending_after.equals("")) {
            output += ending_after;
        }
        if (output.equals("")) {
            return "[]";
        } else {
            output = contract_entities(output).replaceAll(x + "[,]\\s*", "").replaceAll(x + "[\\]]*", "]").replaceAll("[eit]+[0-9]+_[se](,\\s*)?", "").replaceAll(",\\s*]", "]");
            if (output.equals("")) {
                return "[]";
            } else {
                return output;
            }
        }
    }

    public static String reversePointRelation(String r) {
        String o = r;
        if (r.equals("<")) {
            return ">";
        }
        if (r.equals(">")) {
            return "<";
        }
        return o;
    }

    public static String reversePointStartEnd(String s) {
        if (s.equals("s")) {
            return "e";
        } else {
            return "s";
        }
    }

    public static String contract_entities(String entity_string) {
        HashSet<String> entities = new HashSet<String>();
        if (entity_string != null && entity_string.trim().length() > 0) {
            String[] outputarr = entity_string.split(",");
            for (int i = 0; i < outputarr.length; i++) {
                String[] e = outputarr[i].split("_");
                if (!entities.contains(e[0])) {
                    if (entities.contains(e[0] + "_" + reversePointStartEnd(e[1]))) {
                        entities.remove(e[0] + "_" + reversePointStartEnd(e[1]));
                        entities.add(e[0]);
                    } else {
                        entities.add(outputarr[i]);
                    }
                }
            }
        }
        return entities.toString();
    }

    /**
     * Get all possible consistent relations between two entities of the graph
     */
    public ArrayList<String> getPossibleConsistentRelations(String x, String y) {
        ArrayList<String> possible_rels = new ArrayList<String>();

        String x1 = x + "_s";
        String x2 = x + "_e";
        String y1 = y + "_s";
        String y2 = y + "_e";
        // TimePoints are already known
        TimePoint tpx1 = entity_tp_map.get(x1);
        TimePoint tpx2 = entity_tp_map.get(x2);
        TimePoint tpy1 = entity_tp_map.get(y1);
        TimePoint tpy2 = entity_tp_map.get(y2);
        try {
            if (tpx1 == null || tpx2 == null) {
                throw new Exception(x);
            }
            if (tpy1 == null || tpy2 == null) {
                throw new Exception(y);
            }
        } catch (Exception e) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.err.println("Entity " + e.getMessage() + " not found in graph. \n");
            }
            return possible_rels;
        }

        String pointrel_x2_y1 = getPointRelation(tpx2, tpy1);
        String pointrel_x1_y1 = getPointRelation(tpx1, tpy1);
        String pointrel_x2_y2 = getPointRelation(tpx2, tpy2);
        String pointrel_y1_x1 = reversePointRelation(pointrel_x1_y1);
        String pointrel_y2_x1 = getPointRelation(tpy2, tpx1);

        if (pointrel_x2_y1.equals("<") || pointrel_x2_y1.equals("UNKNOWN")) {
            possible_rels.add("BEFORE");
        }
        if (pointrel_y2_x1.equals("<") || pointrel_y2_x1.equals("UNKNOWN")) {
            possible_rels.add("AFTER");
        }

        if (pointrel_x2_y1.equals("=") || pointrel_x2_y1.equals("UNKNOWN")) {
            possible_rels.add("IBEFORE");
        }
        if (pointrel_y2_x1.equals("=") || pointrel_y2_x1.equals("UNKNOWN")) {
            possible_rels.add("IAFTER");
        }

        if (pointrel_x1_y1.equals("=") || pointrel_x1_y1.equals("UNKNOWN")) {
            if (pointrel_x2_y2.equals("<") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("BEGINS");
            }
        }

        if (pointrel_x1_y1.equals("=") || pointrel_x1_y1.equals("UNKNOWN")) {
            if (pointrel_x2_y2.equals(">") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("BEGUN_BY");
            }
        }

        if (pointrel_x2_y2.equals("=") || pointrel_x2_y2.equals("UNKNOWN")) {
            if (pointrel_y1_x1.equals("<") || pointrel_y1_x1.equals("UNKNOWN")) {
                possible_rels.add("ENDS");
            }
        }

        if (pointrel_x2_y2.equals("=") || pointrel_x2_y2.equals("UNKNOWN")) {
            if (pointrel_y1_x1.equals(">") || pointrel_y1_x1.equals("UNKNOWN")) {
                possible_rels.add("ENDED_BY");
            }
        }


        if (pointrel_x1_y1.equals("<") || pointrel_x1_y1.equals("UNKNOWN")) {
            if (pointrel_x2_y1.equals(">") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("OVERLAPS");
            }
        }

        if (pointrel_x1_y1.equals(">") || pointrel_x1_y1.equals("UNKNOWN")) {
            if (pointrel_y2_x1.equals(">") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("OVERLAPPED_BY");
            }
        }

        if (pointrel_y1_x1.equals("<") || pointrel_y1_x1.equals("UNKNOWN")) {
            if (pointrel_x2_y2.equals("<") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("IS_INCLUDED");
            }
        }

        if (pointrel_y1_x1.equals(">") || pointrel_y1_x1.equals("UNKNOWN")) {
            if (pointrel_x2_y2.equals(">") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("INCLUDEDS");
            }
        }

        if (pointrel_x1_y1.equals("=") || pointrel_x1_y1.equals("UNKNOWN")) {
            if (pointrel_x2_y2.equals("=") || pointrel_x2_y2.equals("UNKNOWN")) {
                possible_rels.add("SIMULTANEOUS");
            }
        }
        return possible_rels;
    }

    public boolean checkInconsistentConnections() {
        try {
            for (int i = 0; i < metagraph.size(); i++) {

                HashMap<Integer, HashMap<Integer, TimePoint>> conns = metagraph.get(i).getAllAfterConnections();
                // CHECK LOCAL POINT IS FOUND
                for (Integer key : conns.keySet()) {
                    if (!metagraph.get(i).getTimePoints().containsKey(key)) {
                        throw new Exception(key + " after conection but not found in chain " + i);
                    }
                    HashMap<Integer, TimePoint> conns2 = conns.get(key);
                    for (Integer cad : conns2.keySet()) {
                        // CHECK DEST POINT IS FOUND
                        if (!metagraph.get(cad).getTimePoints().containsKey(conns2.get(cad).getPosition())) {
                            throw new Exception(conns2.get(cad) + " after conection but not found in chain " + cad);
                        } else {
                            // CHECK THE CONTRARY BEFORE IS FOUND
                            if (metagraph.get(cad).getBeforeConnections(conns2.get(cad).getPosition()).get(i).getPosition() != key) {
                                throw new Exception(conns2.get(cad) + " after (contrary) conection but not found in chain " + cad);
                            }
                        }
                    }
                }
                conns = metagraph.get(i).getAllBeforeConnections();
                // CHECK LOCAL POINT IS FOUND
                for (Integer key : conns.keySet()) {
                    if (!metagraph.get(i).getTimePoints().containsKey(key)) {
                        throw new Exception(key + " before conection but not found in chain " + i);
                    }
                    HashMap<Integer, TimePoint> conns2 = conns.get(key);
                    for (Integer cad : conns2.keySet()) {
                        // CHECK DEST POINT IS FOUND
                        if (!metagraph.get(cad).getTimePoints().containsKey(conns2.get(cad).getPosition())) {
                            throw new Exception(conns2.get(cad) + " before conection but not found in chain " + cad);
                        } else {
                            // CHECK THE CONTRARY AFTER IS FOUND
                            if (metagraph.get(cad).getAfterConnections(conns2.get(cad).getPosition()).get(i).getPosition() != key) {
                                throw new Exception(conns2.get(cad) + " before (contrary) conection but not found in chain " + cad);
                            }
                        }
                    }
                }


            }
            System.err.println("Connectons checked! (no inconsistencies found)");

        } catch (Exception e) {
            System.err.println("Errors found (TimeGraph):\n\t" + e.toString() + "\n");
            e.printStackTrace(System.err);
            return false;
        }
        return true;

    }

    /**
     * See if user used eXX instead of eiXX (event instead of ev.instance)
     *
     * @param entity
     * @return
     */
    public String replace_e_by_ei_if_needed(String entity) {
        if (entity_tp_map != null && entity != null && entity_tp_map.get(entity + "_s") == null && entity.matches("e[0-9]+")) {
            if (System.getProperty("DEBUG") != null && System.getProperty("DEBUG").equalsIgnoreCase("true")) {
                System.out.println("Replacing e by ei in " + entity);
            }
            entity = entity.replace("e", "ei");
        }
        return entity;
    }
}
