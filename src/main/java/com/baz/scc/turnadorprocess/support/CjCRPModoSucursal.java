package com.baz.scc.turnadorprocess.support;

import com.baz.scc.turnadorprocess.dao.CjCRTurnadorProcessDao;
import com.baz.scc.turnadorprocess.model.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CjCRPModoSucursal {
    private static final Logger LOG = Logger.getLogger(CjCRPModoSucursal.class);
    @Autowired
    private CjCRPAppConfig sucursalesConfig;
    @Autowired
    private CjCRTurnadorProcessDao sucursalesDao;   
    
    public List<CjCRSucursales> getSucursales(){
        String sucursalesModo = sucursalesConfig.getProcesoSucursalModo();
        List<CjCRSucursales> listaSucursales = new ArrayList<CjCRSucursales>();
        
        if(sucursalesModo.equals("automatico")){
            LOG.info("Inicia calculo de sucursales");
            listaSucursales = sucursalesDao.getSucursales();
            LOG.info("Sucursales encontradas: "+listaSucursales.size());
            return listaSucursales;
        }else if(sucursalesModo.equals("manual")){
            listaSucursales = getSucursalesProperties();
            if(listaSucursales==null){ 
                LOG.info("Sucursal modo automatico, activado");
                listaSucursales = sucursalesDao.getSucursales();
                LOG.info("Sucursales encontradas: "+listaSucursales.size());
                return listaSucursales;
            }else{
                LOG.info("Sucursales encontradas: "+listaSucursales.size());
                return  listaSucursales;
            }
            
        }else{
            LOG.error("sucursal.modo no valido");
            listaSucursales = sucursalesDao.getSucursales();
            LOG.info("Sucursales encontradas: "+listaSucursales.size());
            return listaSucursales;
        }
    }
    
    private List<CjCRSucursales> getSucursalesProperties(){
        List<CjCRSucursales> listSucursales = new ArrayList<CjCRSucursales>();
        String cadenaSucursales = sucursalesConfig.getProcesoSucursal();
        String cadenaPaises = sucursalesConfig.getProcesoPais();
        String cadenaCanales = sucursalesConfig.getProcesoCanal();
        StringTokenizer cadenaSucursalesToken = new StringTokenizer(cadenaSucursales,",");
        StringTokenizer cadenaPaisesToken = new StringTokenizer(cadenaPaises,",");
        StringTokenizer cadenaCanalToken = new StringTokenizer(cadenaCanales,",");
        try{
            while(cadenaSucursalesToken.hasMoreElements()){
            CjCRSucursales oSucursales = new CjCRSucursales();
            String fcSucursal = (String) cadenaSucursalesToken.nextElement();
            String fcPais = (String) cadenaPaisesToken.nextElement();
            String fcCanal = (String) cadenaCanalToken.nextElement();
            int fiSucursal = Integer.parseInt(fcSucursal);
            int fiPais = Integer.parseInt(fcPais);
            int fiCanal = Integer.parseInt(fcCanal);
            oSucursales.setSucursalId(fiSucursal);
            oSucursales.setPaisId(fiPais);
            oSucursales.setCanalId(fiCanal);
            listSucursales.add(oSucursales);
         }
          return listSucursales;  
        }catch(Exception e){
            LOG.error("La sucursal ó Pais ó Canal ingersados son incorrectos");
            listSucursales = null;
            return listSucursales;
        }         
        
    }
    
    
    
}
