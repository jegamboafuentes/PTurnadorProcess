package com.baz.scc.turnadorprocess.support;

import com.baz.scc.turnadorprocess.model.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CjCRPModoProceso {
    
    @Autowired
    private CjCRPAppConfig appConfig;
    private static final Logger LOG = Logger.getLogger(CjCRPModoProceso.class);
    DateFormat ano = new SimpleDateFormat("YYYY");
    DateFormat mes = new SimpleDateFormat("MM");
    DateFormat dia = new SimpleDateFormat("dd");
    Date hoy = new Date();
    Calendar semana = Calendar.getInstance();
    Date haceUnaSemana = new Date(hoy.getTime()-604800000);
    
    public List<CjCRDatosDao> getModoProceso(CjCRSucursales sucursal){
        String procesoModo = appConfig.getProcesoModo();
        List<CjCRDatosDao> datosDaoReturn = new ArrayList<CjCRDatosDao>();
        if(procesoModo.equals("automatico")){
            for(CjCRDatosDao automatico : procesoModoAutomatico(sucursal)){
                CjCRDatosDao auto;
                auto = automatico;
                datosDaoReturn.add(auto);
            }
            return datosDaoReturn;
        }else if(procesoModo.equals("manual")){
            int semManual = Integer.parseInt(appConfig.getProcesoModoManual());
            for(CjCRDatosDao manual : procesoModoManual(semManual, sucursal)){
                CjCRDatosDao man;
                man = manual;
                datosDaoReturn.add(man);
            }
            return datosDaoReturn;
        }else if(procesoModo.equals("rango")){
            String semanaCompleta = appConfig.getProcesoModoRango();
            String semana1S = semanaCompleta.substring(0,6);
            String semana2S = semanaCompleta.substring(7);
            int semana1 = Integer.parseInt(semana1S);
            int semana2 = Integer.parseInt(semana2S);
            for(CjCRDatosDao rango : procesoModoRango(semana1, semana2, sucursal)){
                CjCRDatosDao ran;
                ran = rango;
                datosDaoReturn.add(ran);
            }
            return datosDaoReturn;      
        }else{
            LOG.error("proceso.modo no valido");
            LOG.error("proceso.modo automatico activado");
            for(CjCRDatosDao automatico : procesoModoAutomatico(sucursal)){
                CjCRDatosDao auto;
                auto = automatico;
                datosDaoReturn.add(auto);
            }
            return datosDaoReturn;
        }
    }
    
    public List<CjCRDatosDao> procesoModoAutomatico(CjCRSucursales sucursal){
        CjCRDatosDao datosHaceUnaSemana = new CjCRDatosDao();
        List<CjCRDatosDao> listDatosHaceUnaSemana = new ArrayList<CjCRDatosDao>();
        int fiSemanaAuto = calculaSemanaFormateada(haceUnaSemana);
        int fiSemanSNFormato = calculaSemana(haceUnaSemana);
        String fcSemanaAuto = Integer.toString(fiSemanaAuto);
        String fechaBetweenAuto = calculaFechasSemanaFormateada(fiSemanSNFormato);
        datosHaceUnaSemana.setSemana(fcSemanaAuto);
        datosHaceUnaSemana.setFechaBetween(fechaBetweenAuto);
        datosHaceUnaSemana.setSucursal(sucursal);
        listDatosHaceUnaSemana.add(datosHaceUnaSemana);
        return listDatosHaceUnaSemana;
    } 
    public List<CjCRDatosDao> procesoModoManual (int semana, CjCRSucursales sucursal){ //Entrada de semana Formateada
        CjCRDatosDao datosManual = new CjCRDatosDao();
        List<CjCRDatosDao> listDatosManual = new ArrayList<CjCRDatosDao>();
        String fcSemanaManual = Integer.toString(semana);
        String fechaBetweenManual = calculaFechasSemanaFormateada(desformatearSemana(semana));
        datosManual.setSemana(fcSemanaManual);
        datosManual.setFechaBetween(fechaBetweenManual);
        datosManual.setSucursal(sucursal);
        listDatosManual.add(datosManual);
        return listDatosManual;
    }
    public List<CjCRDatosDao> procesoModoRango(int semanaInicio, int semanaFin, CjCRSucursales sucursal){ // Semana sin 
        List<CjCRDatosDao> listDatosRango = new ArrayList<CjCRDatosDao>(); 
        for(int sem = semanaInicio; sem<=semanaFin; sem ++){
            CjCRDatosDao datosRango;
            datosRango = procesoModoManual(sem, sucursal).get(0);
            listDatosRango.add(datosRango);
        }
        return listDatosRango;
    }
    
    public int calculaSemana(Date fecha){
        semana.setTime(fecha);
        int sem = semana.get(Calendar.WEEK_OF_YEAR);
        return sem;
    } //Calcula la semana
    public int calculaSemanaFormateada(Date fecha){
        semana.setTime(fecha);
        int sem = semana.get(Calendar.WEEK_OF_YEAR);
        int year = semana.get(Calendar.YEAR);
        String semanaFormateadaS =  Integer.toString(year)+Integer.toString(sem);
        int semanaFormateadaI = Integer.parseInt(semanaFormateadaS);         
        return semanaFormateadaI;
    } //Calcula la semana con el año
    public String calculaFechasSemanaFormateada(int semana){ //Devuelve un fifecha between x and y, se ocupa para proceso.modo automatico y proceso.modo.manual
        Calendar inicio = Calendar.getInstance();
        Calendar fin = Calendar.getInstance();
        String fiFechaLabel = "FIFECHA BETWEEN  ";
        inicio.set(Calendar.WEEK_OF_YEAR,semana);
        fin.set(Calendar.WEEK_OF_YEAR,semana);
        inicio.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        fin.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        String inicioFormat = ano.format(inicio.getTime())+mes.format(inicio.getTime())+dia.format(inicio.getTime());
        String finFormat = ano.format(fin.getTime())+mes.format(fin.getTime())+dia.format(fin.getTime());
        return fiFechaLabel+inicioFormat+" AND "+finFormat;
                
    } //Calcula el fifechabetween en el modo semanas
    public String calculaFechasRangoFormateada(int semanaInicio, int semanaFin){// Devuelve un fifecha between x and y, se ocupara para proceso.modo.rango
        Calendar inicio = Calendar.getInstance();
        Calendar fin = Calendar.getInstance();
        String fiFechaLabel = "FIFECHA BETWEEN ";
        inicio.set(Calendar.WEEK_OF_YEAR,semanaInicio);
        fin.set(Calendar.WEEK_OF_YEAR,semanaFin);
        inicio.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        fin.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        
        String inicioFormat = ano.format(inicio.getTime())+mes.format(inicio.getTime())+dia.format(inicio.getTime());
        String finFormat = ano.format(fin.getTime())+mes.format(fin.getTime())+dia.format(fin.getTime());
        return fiFechaLabel+inicioFormat+" AND "+finFormat;
    } //Calcula el fifechabetween en el modo rango
    public String calculaFechasModoSucursalAutomatico(){
        return calculaFechasSemanaFormateada(calculaSemana(haceUnaSemana));
    } //Calcula el fifechabetween en el modo automatico
    public List<String> calculaFechasDiasDetalFormateada(int semana){//Calcula el fifechabetween para la tabla TACJTRDIADETAL
        String FechaLabel = "FIFECHA = ";
        Calendar lunes = Calendar.getInstance();
        Calendar martes = Calendar.getInstance();
        Calendar miercoles = Calendar.getInstance();
        Calendar jueves = Calendar.getInstance();
        Calendar viernes = Calendar.getInstance();
        Calendar sabado = Calendar.getInstance();
        Calendar domingo = Calendar.getInstance();
        List<String> fcfechaBetween = new ArrayList<String>();
        lunes.set(Calendar.WEEK_OF_YEAR, semana);
        lunes.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
        martes.set(Calendar.WEEK_OF_YEAR, semana);
        martes.set(Calendar.DAY_OF_WEEK,Calendar.TUESDAY);
        miercoles.set(Calendar.WEEK_OF_YEAR, semana);
        miercoles.set(Calendar.DAY_OF_WEEK,Calendar.WEDNESDAY);
        jueves.set(Calendar.WEEK_OF_YEAR, semana);
        jueves.set(Calendar.DAY_OF_WEEK,Calendar.THURSDAY);
        viernes.set(Calendar.WEEK_OF_YEAR, semana);
        viernes.set(Calendar.DAY_OF_WEEK,Calendar.FRIDAY);
        sabado.set(Calendar.WEEK_OF_YEAR, semana);
        sabado.set(Calendar.DAY_OF_WEEK,Calendar.SATURDAY);
        domingo.set(Calendar.WEEK_OF_YEAR, semana);
        domingo.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);
        String lunesFormat = FechaLabel+(ano.format(lunes.getTime())+mes.format(lunes.getTime())+dia.format(lunes.getTime()));
        fcfechaBetween.add(lunesFormat);
        String martesFormat = FechaLabel+(ano.format(martes.getTime())+mes.format(martes.getTime())+dia.format(martes.getTime()));
        fcfechaBetween.add(martesFormat);
        String miercolesFormat = FechaLabel+(ano.format(miercoles.getTime())+mes.format(miercoles.getTime())+dia.format(miercoles.getTime()));
        fcfechaBetween.add(miercolesFormat);
        String juevesFormat = FechaLabel+(ano.format(jueves.getTime())+mes.format(jueves.getTime())+dia.format(jueves.getTime()));
        fcfechaBetween.add(juevesFormat);
        String viernesFormat = FechaLabel+(ano.format(viernes.getTime())+mes.format(viernes.getTime())+dia.format(viernes.getTime()));
        fcfechaBetween.add(viernesFormat);
        String sabadoFormat = FechaLabel+(ano.format(sabado.getTime())+mes.format(sabado.getTime())+dia.format(sabado.getTime()));
        fcfechaBetween.add(sabadoFormat);
        String domingoFormat = FechaLabel+(ano.format(domingo.getTime())+mes.format(domingo.getTime())+dia.format(domingo.getTime()));
        fcfechaBetween.add(domingoFormat);
        
        return fcfechaBetween;
    }
   
    public int desformatearSemana(int semanaFormateada){
        int semanaSnFormato;
        String semanaFormateadaString = Integer.toString(semanaFormateada);
        semanaSnFormato = Integer.parseInt(semanaFormateadaString.substring(4));
        return semanaSnFormato;
    }
    
    
    
    
}
