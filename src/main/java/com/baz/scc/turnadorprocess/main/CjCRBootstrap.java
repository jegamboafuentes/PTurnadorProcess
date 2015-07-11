
package com.baz.scc.turnadorprocess.main;

import com.baz.scc.commons.util.CjCRSpringContext;
import com.baz.scc.turnadorprocess.logic.CjCRTurnadorProcessLogic;
import org.apache.log4j.Logger;

public class CjCRBootstrap {
    
    private static final Logger LOG = Logger.getLogger(CjCRBootstrap.class);
    
    public static void main(String[] args) {    
        try{
            CjCRSpringContext.init();
            CjCRTurnadorProcessLogic turnadorLogic = CjCRSpringContext.getBean(CjCRTurnadorProcessLogic.class);
        
            turnadorLogic.muestraInfoInicial();
            turnadorLogic.cargaTotal();
            turnadorLogic.pruebas();
            
        
        
        }catch(Exception e){
            LOG.error("Error",e);
        }
    }
    
}
