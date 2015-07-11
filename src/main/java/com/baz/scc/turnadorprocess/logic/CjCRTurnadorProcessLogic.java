package com.baz.scc.turnadorprocess.logic;

import com.baz.scc.turnadorprocess.dao.CjCRTurnadorProcessDao;
import com.baz.scc.turnadorprocess.model.*;
import com.baz.scc.turnadorprocess.model.CjCRSucursales;
import com.baz.scc.turnadorprocess.support.*;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CjCRTurnadorProcessLogic {

    @Autowired
    private CjCRTurnadorProcessDao turnadorProcessDao;
    @Autowired
    private CjCRPModoProceso modoProceso;
    @Autowired
    private CjCRPModoSucursal modoSucursal;
    @Autowired
    private CjCRPAppConfig appConfig;

    private static final Logger LOG = Logger.getLogger(CjCRTurnadorProcessLogic.class);

    public void muestraInfoInicial() {
        String procesoModo = appConfig.getProcesoModo();
        String sucursalModo = appConfig.getProcesoSucursalModo();
        LOG.info("-------------------------------------------------------------");
        LOG.info("--------------------Turnador Process-------------------------");
        LOG.info("Proceso Modo: " + procesoModo);
        if (procesoModo.equals("manual")) {
            LOG.info("Semana manual: " + appConfig.getProcesoModoManual());
        } else if (procesoModo.equals("rango")) {
            LOG.info("Semanas rango: " + appConfig.getProcesoModoRango());
        }
        LOG.info("Sucursal Modo: " + sucursalModo);
        if (sucursalModo.equals("manual")) {
            LOG.info("Sucursal(es): " + appConfig.getProcesoSucursal());
            LOG.info("Canal(es): " + appConfig.getProcesoCanal());
            LOG.info("Pais(es): " + appConfig.getProcesoPais());
        }

    }

    public void cargaTotal() {
        cargas();
    }

    public void cargas() {
        cargaAnalisis();
        cargaDiaDetal();
        cargaEmpleadosAtendiendo();
        cargaClienteEsperaDias();
        cargaClienteEsperaTiempo();
        cargaTiempoSnAtenderDias();
        cargaTiempoSnAtenderTiempo();
    }

    public void cargaAnalisis() {
        LOG.info("Inicia proceso de carga a TACJTRANALISIS");
        String semana;
        int fiPais;
        int fiCanal;
        int fiSucursal;
        int fiTurnos;
        int fiTurnosV;
        int contador = 0;
        Boolean existencia;
        for (CjCRSucursales sucursal : modoSucursal.getSucursales()) {
            for (CjCRDatosDao datos : modoProceso.getModoProceso(sucursal)) {
                if (turnadorProcessDao.getTurnos(datos).get(0) != 0) {
                    semana = datos.getSemana();
                    fiPais = datos.getSucursal().getPaisId();
                    fiCanal = datos.getSucursal().getCanalId();
                    fiSucursal = datos.getSucursal().getSucursalId();
                    fiTurnos = turnadorProcessDao.getTurnos(datos).get(0);
                    fiTurnosV = turnadorProcessDao.getTurnosVirtuales(datos).get(0);
                    existencia = turnadorProcessDao.validaAnalisis(Integer.parseInt(semana),fiCanal,fiPais,fiSucursal);
                    if(existencia){
                        turnadorProcessDao.insertaAnalisis(semana, fiPais, fiCanal, fiSucursal, fiTurnos, fiTurnosV);
                        contador++;
                    }
                }
            }
        }
        LOG.info("Se agregaron con exito: "+contador+" registros");
    }
    public void cargaDiaDetal(){
        LOG.info("Inicia proceso de carga a TACJTRDIADETAL");
        String semana;
        int fiPais;
        int fiCanal;
        int fiSucursal;
        int fiTLunes;
        int fiVLunes;
        int fiTMartes;
        int fiVMartes;
        int fiTMiercoles;
        int fiVMiercoles;
        int fiTJueves;
        int fiVJueves;
        int fiTViernes;
        int fiVViernes;
        int fiTSabado;
        int fiVSabado;
        int fiTDomingo;
        int fiVDomingo;
        int contador = 0;
        Boolean existencia;
        for (CjCRSucursales sucursal : modoSucursal.getSucursales()) {
            for (CjCRDatosDao datos : modoProceso.getModoProceso(sucursal)) {
                if (turnadorProcessDao.getTurnos(datos).get(0) != 0) {
                    List<Integer> turnosDiasSemana;
                    List<Integer> virtualesDiasSemana;
                    turnosDiasSemana = turnadorProcessDao.getTurnosDiaDetal(datos);
                    virtualesDiasSemana = turnadorProcessDao.getTurnosVirtualesDiaDetal(datos);
                    semana = datos.getSemana();
                    fiPais = datos.getSucursal().getPaisId();
                    fiCanal = datos.getSucursal().getCanalId();
                    fiSucursal = datos.getSucursal().getSucursalId();
                    fiTLunes = turnosDiasSemana.get(0);
                    fiVLunes = virtualesDiasSemana.get(0);
                    fiTMartes = turnosDiasSemana.get(1);
                    fiVMartes = virtualesDiasSemana.get(1);
                    fiTMiercoles = turnosDiasSemana.get(2);
                    fiVMiercoles = virtualesDiasSemana.get(2);
                    fiTJueves = turnosDiasSemana.get(3);
                    fiVJueves = virtualesDiasSemana.get(3);
                    fiTViernes = turnosDiasSemana.get(4);
                    fiVViernes = virtualesDiasSemana.get(4);
                    fiTSabado = turnosDiasSemana.get(5);
                    fiVSabado = virtualesDiasSemana.get(5);
                    fiTDomingo = turnosDiasSemana.get(6);
                    fiVDomingo = virtualesDiasSemana.get(6);
                    existencia = turnadorProcessDao.validaDiaDetal(Integer.parseInt(semana), fiCanal, fiPais, fiSucursal);
                    if(existencia){
                        turnadorProcessDao.insertaDiaDetal(semana, fiPais, fiCanal, fiSucursal, fiTLunes, fiVLunes, fiTMartes, fiVMartes, fiTMiercoles, fiVMiercoles, fiTJueves, fiVJueves, fiTViernes, fiVViernes, fiTSabado, fiVSabado, fiTDomingo, fiVDomingo);
                        contador++;
                    }
                }
            }
        }
        LOG.info("Se agregaron con exito: "+contador+" registros");
    }
    public void cargaEmpleadosAtendiendo(){
        LOG.info("Inicia proceso de carga a TACJTRDIAS(Empleados Atendiendo)");
        String semana;
        int fiPais;
        int fiCanal;
        int fiSucursal;
        int fiDiasSemana [] = new int[7];
        int contador = 0;
        Boolean existencia;
        for(CjCRSucursales sucursal : modoSucursal.getSucursales()){
            for(CjCRDatosDao datos : modoProceso.getModoProceso(sucursal)){
                if(turnadorProcessDao.getTurnos(datos).get(0) != 0){
                    List<Integer> empleadosAtendiendo;
                    empleadosAtendiendo = turnadorProcessDao.getEmpleadosAtendiendo(datos);
                    semana = datos.getSemana();
                    fiPais = datos.getSucursal().getPaisId();
                    fiCanal = datos.getSucursal().getCanalId();
                    fiSucursal = datos.getSucursal().getSucursalId();
                    if(empleadosAtendiendo.size()<7){
                        empleadosAtendiendo = turnadorProcessDao.getEmpleadosAtendiendoDescompleto(datos);
                    }
                    for(int i = 0; i<empleadosAtendiendo.size();i++){
                        fiDiasSemana[i] = empleadosAtendiendo.get(i);
                    }
                    existencia = turnadorProcessDao.validaEmpleadosAtendiendo(Integer.parseInt(semana), fiCanal, fiPais, fiSucursal);
                    if(existencia){
                        turnadorProcessDao.insertaEmpleadosAtendiendo(semana, fiPais, fiCanal, fiSucursal, fiDiasSemana[0], fiDiasSemana[1], fiDiasSemana[2], fiDiasSemana[3], fiDiasSemana[4], fiDiasSemana[5], fiDiasSemana[6]);
                        contador++;
                    }
                }
                
            }
        }
        LOG.info("Se agregaron con exito: "+contador+" registros");
    }
    public void cargaClienteEsperaDias(){
        LOG.info("Inicia proceso de carga a TACJTRDIAS(Cliente en espera)");
        String semana;
        int fiPais;
        int fiCanal;
        int fiSucursal;
        Double fiDiasSemana[] = new Double[7];
        int contador = 0;
        Boolean existencia;
        for(CjCRSucursales sucursal : modoSucursal.getSucursales()){
            for(CjCRDatosDao datos : modoProceso.getModoProceso(sucursal)){
                if(turnadorProcessDao.getTurnos(datos).get(0) != 0){
                    List<Double> promTiempoEsperaDias;
                    promTiempoEsperaDias = turnadorProcessDao.getClienteEsperaDias(datos);
                    semana = datos.getSemana();
                    fiPais = datos.getSucursal().getPaisId();
                    fiCanal = datos.getSucursal().getCanalId();
                    fiSucursal = datos.getSucursal().getSucursalId();
                    if(promTiempoEsperaDias.size()<7){
                        promTiempoEsperaDias = turnadorProcessDao.getClienteEsperaDiasDescompleto(datos);
                    }                
                    for(int i = 0; i<promTiempoEsperaDias.size();i++){
                        fiDiasSemana[i] = promTiempoEsperaDias.get(i);
                    }                
                    existencia = turnadorProcessDao.validaClienteEsperaDias(Integer.parseInt(semana), fiCanal, fiPais, fiSucursal);
                    if(existencia){
                        turnadorProcessDao.instertaClienteEsperaDias(semana, fiPais, fiCanal, fiSucursal, fiDiasSemana[0], fiDiasSemana[1], fiDiasSemana[2], fiDiasSemana[3], fiDiasSemana[4], fiDiasSemana[5], fiDiasSemana[6]);
                        contador++;
                    }
//                    System.out.println("------------------------");
//                    System.out.println("Semana: "+semana);
//                    System.out.println("Sucursal: "+fiSucursal);
//                    System.out.println(fiDiasSemana[0]+" | "+fiDiasSemana[1]+" | "+fiDiasSemana[2]+" | "+fiDiasSemana[3]+" | "+fiDiasSemana[4]+" | "+fiDiasSemana[5]+" | "+fiDiasSemana[6]);
//                    System.out.println(promTiempoEsperaDias.size());
//                    System.out.println("------------------------");
                    
                    
                }      
            }
        }
        LOG.info("Se agregaron con exito: "+contador+" registros");
    }
    public void cargaClienteEsperaTiempo(){
        LOG.info("Inicia proceso de carga a TACJTRTIEMPOS (Cliente en espera)");
        String semana;
        int fiPais;
        int fiCanal;
        int fiSucursal;
        int contador = 0;
        CjCRTiempos tiempos;
        Boolean existencia;
        for (CjCRSucursales sucursal : modoSucursal.getSucursales()) {
            for (CjCRDatosDao datos : modoProceso.getModoProceso(sucursal)) {
                if (turnadorProcessDao.getTurnos(datos).get(0) != 0) {
                    semana = datos.getSemana();
                    fiPais = datos.getSucursal().getPaisId();
                    fiCanal = datos.getSucursal().getCanalId();
                    fiSucursal = datos.getSucursal().getSucursalId();
                    tiempos = turnadorProcessDao.getClienteDiasTiempo(datos).get(0);
                    existencia = turnadorProcessDao.validaClienteEsperaTiempo(Integer.parseInt(semana), fiCanal, fiPais, fiSucursal);
                    if(existencia){
                        turnadorProcessDao.instertaClienteEsperaTiempo(semana, fiPais, fiCanal, fiSucursal, tiempos.getFiAvg(), tiempos.getFiMin(), tiempos.getFiMax(), 66.99);
                        contador++;
                    }
//                    System.out.println("Semana: "+semana);
//                    System.out.println("Sucursal: "+tiempos.getSucursal().getSucursalId());
//                    System.out.println("AVG: "+tiempos.getFiAvg());
//                    System.out.println("Min: "+tiempos.getFiMin());
//                    System.out.println("Max: "+tiempos.getFiMax());
                }
            }
        }
        LOG.info("Se agregaron con exito: "+contador+" registros");
        
    }
    public void cargaTiempoSnAtenderDias(){
        LOG.info("Inicia proceso de carga a TACJTRDIAS(Tiempo sin atender)");
        String semana;
        int fiPais;
        int fiCanal;
        int fiSucursal;
        Double fiDiasSemana[] = new Double[7];
        int contador = 0;
        Boolean existencia;
        for(CjCRSucursales sucursal: modoSucursal.getSucursales()){
            for(CjCRDatosDao datos: modoProceso.getModoProceso(sucursal)){
                if(turnadorProcessDao.getTurnos(datos).get(0) != 0){
                    List<Double> promTiempoSnAtenderDias;
                    promTiempoSnAtenderDias = turnadorProcessDao.getTiempoSnAtenderDias(datos);
                    semana = datos.getSemana();
                    fiPais = datos.getSucursal().getPaisId();
                    fiCanal = datos.getSucursal().getCanalId();
                    fiSucursal = datos.getSucursal().getSucursalId();
                    if(promTiempoSnAtenderDias.size()<7){
                        promTiempoSnAtenderDias = turnadorProcessDao.getTiempoSnAtenderDiasDescompleto(datos);
                    }
                    for(int i = 0; i<promTiempoSnAtenderDias.size();i++){
                        fiDiasSemana[i] = promTiempoSnAtenderDias.get(i);
                    }
                    existencia = turnadorProcessDao.validaTiempoSnAtenderDias(Integer.parseInt(semana), fiCanal, fiPais, fiSucursal);
                    if(existencia){
                        turnadorProcessDao.instertaTiempoSnAtenderDias(semana, fiPais, fiCanal, fiSucursal, fiDiasSemana[0], fiDiasSemana[1], fiDiasSemana[2], fiDiasSemana[3], fiDiasSemana[4], fiDiasSemana[5], fiDiasSemana[6]);
                        contador++;
                    }
                }
            }
        }
        LOG.info("Se agregaron con exito: "+contador+" registros");
    }
    public void cargaTiempoSnAtenderTiempo(){
        LOG.info("Inicia proceso de carga a TACJTRTIEMPOS (Tiempo sin atender)");
        String semana;
        int fiPais;
        int fiCanal;
        int fiSucursal;
        int contador = 0;
        CjCRTiempos tiempos;
        Boolean existencia;
        for (CjCRSucursales sucursal : modoSucursal.getSucursales()) {
            for (CjCRDatosDao datos : modoProceso.getModoProceso(sucursal)) {
                if (turnadorProcessDao.getTurnos(datos).get(0) != 0) {
                    semana = datos.getSemana();
                    fiPais = datos.getSucursal().getPaisId();
                    fiCanal = datos.getSucursal().getCanalId();
                    fiSucursal = datos.getSucursal().getSucursalId();
                    tiempos = turnadorProcessDao.getTiemposSnAtenderTiempo(datos).get(0);
                    existencia = turnadorProcessDao.validaTiemposSnAtenderTiempo(Integer.parseInt(semana), fiCanal, fiPais, fiSucursal);
                    if(existencia){
                        turnadorProcessDao.instertaTiemposSnAtenderTiempo(semana, fiPais, fiCanal, fiSucursal, tiempos.getFiAvg(), tiempos.getFiMin(), tiempos.getFiMax(), 66.99);
                        contador++;
                    }
                }
            }
        }
        LOG.info("Se agregaron con exito: "+contador+" registros");
    }
    
    
    public void imprimeTurnos() {
        for (CjCRSucursales sucursal : modoSucursal.getSucursales()) {
            for (CjCRDatosDao datos : modoProceso.getModoProceso(sucursal)) {
                for (Integer i : turnadorProcessDao.getTurnos(datos)) {
                    System.out.println("--Sucursal: " + sucursal.getSucursalId());
                    System.out.println("\tCantidad de turnos " + turnadorProcessDao.getTurnos(datos));
                    System.out.println("\tCantidad de turnos virtuales " + turnadorProcessDao.getTurnosVirtuales(datos));
                    System.out.println("\tTotal de turnos " + (turnadorProcessDao.getTurnos(datos).get(0) + turnadorProcessDao.getTurnosVirtuales(datos).get(0)));
                }
            }

        }

    }
    public void imprimeSucursales() {
        for (CjCRSucursales i : modoSucursal.getSucursales()) {
            System.out.println("Sucursal " + i.getSucursalId());
            System.out.println("Canal " + i.getCanalId());
            System.out.println("Pais " + i.getPaisId());

        }
    }
    public void imprmeDiaSemana() {
        Date hoy = new Date();
        Date haceUnaSemana = new Date(hoy.getTime() - 604800000);
        Calendar c1 = GregorianCalendar.getInstance();
        c1.set(2014, 10, 28);
        Date ciertoDia = c1.getTime();

        System.out.println("La semana del cierto dia es: " + modoProceso.calculaSemana(ciertoDia) + " ó " + modoProceso.calculaSemanaFormateada(ciertoDia));
        System.out.println("Semana de hoy: " + modoProceso.calculaSemanaFormateada(hoy));
        System.out.println(modoProceso.calculaFechasSemanaFormateada(modoProceso.calculaSemana(hoy)));
        System.out.println("Semana pasada: " + modoProceso.calculaSemanaFormateada(haceUnaSemana));
        System.out.println(modoProceso.calculaFechasSemanaFormateada(modoProceso.calculaSemana(haceUnaSemana)));
        System.out.println("Rango: 201405, 201425");
        System.out.println(modoProceso.calculaFechasRangoFormateada(5, 25));

    }
    public void imprimeProcesoModo() {
        System.out.println("--------------------------------------------------");
        System.out.println("Proceso Modo Automatico");
        for (CjCRSucursales sucursalesAutomatico : modoSucursal.getSucursales()) {
            for (CjCRDatosDao automatico : modoProceso.procesoModoAutomatico(sucursalesAutomatico)) {
                System.out.println("\tSemana: " + automatico.getSemana());
                System.out.println("\tfecha between: " + automatico.getFechaBetween());
                System.out.println("\tsucursal: ");
                System.out.println("\t\tpais: " + automatico.getSucursal().getPaisId());
                System.out.println("\t\tcanal: " + automatico.getSucursal().getCanalId());
                System.out.println("\t\tsucursal: " + automatico.getSucursal().getSucursalId());
                System.out.println("++++++++++++++++++++++++++++++++++++++++++");
            }
        }
        System.out.println("--------------------------------------------------");
        System.out.println("Proceso Modo Manual");
        for (CjCRSucursales sucursaleManual : modoSucursal.getSucursales()) {
            for (CjCRDatosDao manual : modoProceso.procesoModoManual(201444, sucursaleManual)) {
                System.out.println("\tSemana: " + manual.getSemana());
                System.out.println("\tfecha between: " + manual.getFechaBetween());
                System.out.println("\tsucursal: ");
                System.out.println("\t\tpais: " + manual.getSucursal().getPaisId());
                System.out.println("\t\tcanal: " + manual.getSucursal().getCanalId());
                System.out.println("\t\tsucursal: " + manual.getSucursal().getSucursalId());
                System.out.println("++++++++++++++++++++++++++++++++++++++++++");
            }
        }
        System.out.println("--------------------------------------------------");
        System.out.println("Proceso Modo Rango");
        for (CjCRSucursales sucursalesRango : modoSucursal.getSucursales()) {
            for (CjCRDatosDao rango : modoProceso.procesoModoRango(201350, 201402, sucursalesRango)) {
                System.out.println("\tSemana: " + rango.getSemana());
                System.out.println("\tfecha between: " + rango.getFechaBetween());
                System.out.println("\tsucursal: ");
                System.out.println("\t\tpais: " + rango.getSucursal().getPaisId());
                System.out.println("\t\tcanal: " + rango.getSucursal().getCanalId());
                System.out.println("\t\tsucursal: " + rango.getSucursal().getSucursalId());
                System.out.println("++++++++++++++++++++++++++++++++++++++++++");
            }
        }
    }
    public void imprimeEmpleadoPorDia(){
        CjCRDatosDao data = new CjCRDatosDao();
        CjCRSucursales suc = new CjCRSucursales();
        suc.setSucursalId(4624);
        suc.setPaisId(1);
        suc.setCanalId(1);
        data.setSucursal(suc);
        data.setSemana("201438");
        data.setFechaBetween(" T.FIFECHA = 20140912");
        System.out.println("------------->"+turnadorProcessDao.getEmpleadosAtendiendo(data));
    }

    public void pruebas() {
//        imprimeSucursales();
//        imprimeTurnos();
//        imprmeDiaSemana();
//        imprimeProcesoModo();
//        imprimeEmpleadoPorDia();

    }

}
