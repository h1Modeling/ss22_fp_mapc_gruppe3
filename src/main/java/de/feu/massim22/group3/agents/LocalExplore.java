 package de.feu.massim22.group3.agents;

    import java.util.Iterator;
    import eis.iilang.*;
    import massim.protocol.data.Thing;


    public class LocalExplore extends Desire {
        
        LocalExplore(BdiAgent agent){
            super("LocalExplore", agent);
        }

        /**
         * The method proves if a certain Desire is possible.
         *
         * @param desire -  the desire that has to be proven 
         * 
         * @return boolean - the desire is possible or not
         */
        public boolean isExecutable(Desire desire) {
             return true;
        }

        /**
         * The method returns the nextAction that is needed.
         * 
         * @return Action - the action that is needed
         * 
         **/
        
        //Norden To Do: richtige Richtung
        public Action getNextAction() {
            Identifier newDirection = new Identifier("n");
            
            return new Action("move", newDirection );
        }
    }


