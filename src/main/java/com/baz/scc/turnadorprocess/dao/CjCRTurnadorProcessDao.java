package com.baz.scc.turnadorprocess.dao;

import com.baz.scc.turnadorprocess.model.*;
import com.baz.scc.turnadorprocess.support.CjCRPAppConfig;
import com.baz.scc.turnadorprocess.support.CjCRPModoProceso;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class CjCRTurnadorProcessDao {

    @Autowired
    private CjCRPAppConfig appConfig;
    @Autowired
    private CjCRPModoProceso mododoProceso;
    //Orcale
    @Autowired
    @Qualifier("usrcajaJdbcTemplate")
    private JdbcTemplate usrcajaJdbcTemplate;

    public CjCRTurnadorProcessDao() {
    }

    private static final Logger LOG = Logger.getLogger(CjCRTurnadorProcessDao.class);
    private final String paisLabel = "FIPAISID = ";
    private final String canalLabel = "FICANALID = ";
    private final String sucursalLabel = "FISUCURSALID = ";
    private final String userInserta = "SCC";

    private String setVariables(CjCRDatosDao datos) {
        return " WHERE " + datos.getFechaBetween() + " AND "
                + paisLabel + Integer.toString(datos.getSucursal().getPaisId())
                + " AND " + canalLabel + Integer.toString(datos.getSucursal().getCanalId())
                + " AND " + sucursalLabel + Integer.toString(datos.getSucursal().getSucursalId());
    }

    //Sucursales *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    public List<CjCRSucursales> getSucursales() {
        String fifecha;
        if (!appConfig.getProcesoModo().equals("automatico")) {
            fifecha = " FIFECHA<>0";
        } else {
            fifecha = mododoProceso.calculaFechasModoSucursalAutomatico();
        }
        String query = "SELECT DISTINCT FISUCURSALID, FIPAISID, FICANALID FROM TACJTRTURNO WHERE " + fifecha;
        return usrcajaJdbcTemplate.query(query, new RowMapper<CjCRSucursales>() {

            @Override
            public CjCRSucursales mapRow(ResultSet rs, int rowNum) throws SQLException {
                CjCRSucursales oSucursales = new CjCRSucursales();
                oSucursales.setSucursalId(rs.getInt(1));
                oSucursales.setPaisId(rs.getInt(2));
                oSucursales.setCanalId(rs.getInt(3));

                return oSucursales;
            }
        });
    }
    // *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    //TACJTRANALISIS *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-

    public List<Integer> getTurnos(CjCRDatosDao datos) {
        String whereClauseTurnos = setVariables(datos);
        //System.out.println("SELECT COUNT(*) FROM TACJTRTURNO" + whereClauseTurnos + " AND FIVIRTUAL = 0");
        return usrcajaJdbcTemplate.query("SELECT COUNT(*) FROM TACJTRTURNO" + whereClauseTurnos + " AND FIVIRTUAL = 0", new RowMapper<Integer>() {

            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                int turnosNormales;
                turnosNormales = rs.getInt(1);
                return turnosNormales;
            }
        });
    }

    public List<Integer> getTurnosVirtuales(CjCRDatosDao datos) {
        String whereClauseTurnos = setVariables(datos);
        return usrcajaJdbcTemplate.query("SELECT COUNT(*) FROM TACJTRTURNO" + whereClauseTurnos + " AND FIVIRTUAL = 1", new RowMapper<Integer>() {

            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                int turnosNormales;
                turnosNormales = rs.getInt(1);
                return turnosNormales;
            }
        });
    }

    public Boolean validaAnalisis(int semana, int canal, int pais, int sucursal) {
        String sqlQuery = "SELECT COUNT(*) FROM TACJTRANALISIS WHERE FISEMANAID = " + semana + " AND FIPAISID = " + pais + " AND FICANALID = " + canal + " AND FISUCURSALID = " + sucursal;
        int resultado = usrcajaJdbcTemplate.query(sqlQuery, new ResultSetExtractor<Integer>() {

            @Override
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                int res = -10;
                if (rs.next()) {
                    res = rs.getInt(1);
                }
                return res;
            }
        });
        if (resultado == 0) {
            return true;
        } else if (resultado == 1) {
            return false;
        } else {
            LOG.error("Error al validad informacion de TACJTRANALISIS");
            return true;
        }

    }

    public void insertaAnalisis(String fiSemana, int fiPais, int fiCanal, int fiSucursal, int fiTurnos, int fiturnosV) {
        String sql = "INSERT INTO TACJTRANALISIS (FISEMANAID,FIPAISID,FICANALID,FISUCURSALID,FITURNOS,FITURNOSV,FDFECHAINSERTA,FCUSERINSERTA)"
                + " VALUES(" + fiSemana + "," + fiPais + "," + fiCanal + "," + fiSucursal + "," + fiTurnos + "," + fiturnosV + ",SYSDATE ,'" + userInserta + "' )";
        try {
            usrcajaJdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            LOG.error("Error al insertar en TACJTRANALISIS", e);
        }
    }

    // *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    //TACJTRDIADETAL *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    public List<Integer> getTurnosDiaDetal(CjCRDatosDao datos) {
        int semana = mododoProceso.desformatearSemana(Integer.parseInt(datos.getSemana()));
        List<Integer> turnos = new ArrayList<Integer>();

        CjCRDatosDao datosLunes = new CjCRDatosDao();
        datosLunes = datos;
        datosLunes.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(0));
        turnos.add(getTurnos(datosLunes).get(0));
        CjCRDatosDao datosMartes = new CjCRDatosDao();
        datosMartes = datos;
        datosMartes.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(1));
        turnos.add(getTurnos(datosMartes).get(0));
        CjCRDatosDao datosMiercoles = new CjCRDatosDao();
        datosMiercoles = datos;
        datosMiercoles.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(2));
        turnos.add(getTurnos(datosMiercoles).get(0));
        CjCRDatosDao datosJueves = new CjCRDatosDao();
        datosJueves = datos;
        datosJueves.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(3));
        turnos.add(getTurnos(datosJueves).get(0));
        CjCRDatosDao datosViernes = new CjCRDatosDao();
        datosViernes = datos;
        datosViernes.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(4));
        turnos.add(getTurnos(datosViernes).get(0));
        CjCRDatosDao datosSabado = new CjCRDatosDao();
        datosSabado = datos;
        datosSabado.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(5));
        turnos.add(getTurnos(datosSabado).get(0));
        CjCRDatosDao datosDomingo = new CjCRDatosDao();
        datosDomingo = datos;
        datosDomingo.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(6));
        turnos.add(getTurnos(datosDomingo).get(0));

        return turnos;
    }

    public List<Integer> getTurnosVirtualesDiaDetal(CjCRDatosDao datos) {
        int semana = mododoProceso.desformatearSemana(Integer.parseInt(datos.getSemana()));
        List<Integer> turnos = new ArrayList<Integer>();

        CjCRDatosDao datosLunes = new CjCRDatosDao();
        datosLunes = datos;
        datosLunes.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(0));
        turnos.add(getTurnosVirtuales(datosLunes).get(0));
        CjCRDatosDao datosMartes = new CjCRDatosDao();
        datosMartes = datos;
        datosMartes.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(1));
        turnos.add(getTurnosVirtuales(datosMartes).get(0));
        CjCRDatosDao datosMiercoles = new CjCRDatosDao();
        datosMiercoles = datos;
        datosMiercoles.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(2));
        turnos.add(getTurnosVirtuales(datosMiercoles).get(0));
        CjCRDatosDao datosJueves = new CjCRDatosDao();
        datosJueves = datos;
        datosJueves.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(3));
        turnos.add(getTurnosVirtuales(datosJueves).get(0));
        CjCRDatosDao datosViernes = new CjCRDatosDao();
        datosViernes = datos;
        datosViernes.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(4));
        turnos.add(getTurnosVirtuales(datosViernes).get(0));
        CjCRDatosDao datosSabado = new CjCRDatosDao();
        datosSabado = datos;
        datosSabado.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(5));
        turnos.add(getTurnosVirtuales(datosSabado).get(0));
        CjCRDatosDao datosDomingo = new CjCRDatosDao();
        datosDomingo = datos;
        datosDomingo.setFechaBetween(mododoProceso.calculaFechasDiasDetalFormateada(semana).get(6));
        turnos.add(getTurnosVirtuales(datosDomingo).get(0));

        return turnos;
    }

    public Boolean validaDiaDetal(int semana, int canal, int pais, int sucursal) {
        String sqlQuery = "SELECT COUNT(*) FROM TACJTRDIADETAL WHERE FISEMANAID = " + semana + " AND FIPAISID = " + pais + " AND FICANALID = " + canal + " AND FISUCURSALID = " + sucursal;
        int resultado = usrcajaJdbcTemplate.query(sqlQuery, new ResultSetExtractor<Integer>() {

            @Override
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                int res = -10;
                if (rs.next()) {
                    res = rs.getInt(1);
                }
                return res;
            }
        });
        if (resultado == 0) {
            return true;
        } else if (resultado == 1) {
            return false;
        } else {
            LOG.error("Error al validad informacion de TACJTRDIADETAL");
            return true;
        }

    }

    public void insertaDiaDetal(String fiSemana, int fiPais, int fiCanal, int fiSucursal, int fiTLunes, int fiVLunes, int fiTMartes, int fiVMartes, int fiTMiercoles, int fiVMiercoles, int fiTJueves, int fiVJueves, int fiTViernes, int fiVViernes, int fiTSabado, int fiVSabado, int fiTDomingo, int fiVDomingo) {
        String sql = "INSERT INTO TACJTRDIADETAL (FISEMANAID,FIPAISID,FICANALID,FISUCURSALID,FITLUNES,FIVLUNES,FITMARTES,FIVMARTES,FITMIERCOLES,FIVMIERCOLES,FITJUEVES,FIVJUEVES,FITVIERNES,FIVVIERNES,FITSABADO,FIVSABADO,FITDOMINGO,FIVDOMINGO,FDFECHAINSERTA,FCUSERINSERTA)"
                + " VALUES(" + fiSemana + "," + fiPais + "," + fiCanal + "," + fiSucursal + "," + fiTLunes + "," + fiVLunes + "," + fiTMartes + "," + fiVMartes + "," + fiTMiercoles + "," + fiVMiercoles + "," + fiTJueves + "," + fiVJueves + "," + fiTViernes + "," + fiVViernes + "," + fiTSabado + "," + fiVSabado + "," + fiTDomingo + "," + fiVDomingo + ",SYSDATE ,'" + userInserta + "' )";
        try {
            usrcajaJdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            LOG.error("Error al insertar en TACJTRDIADETAL", e);
        }
    }

    // *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    //TACJTRDIAS *-*-*-*-*-*-*-*-*-*-Empleados por dia-*-*-*-*-*-*-*-*-*-*-*-*-*-
    private String setVariablesEmpleadosDia(CjCRDatosDao datos) {
        return " WHERE " + datos.getFechaBetween() + " AND "
                + "T." + paisLabel + Integer.toString(datos.getSucursal().getPaisId())
                + " AND " + "T." + canalLabel + Integer.toString(datos.getSucursal().getCanalId())
                + " AND " + "T." + sucursalLabel + Integer.toString(datos.getSucursal().getSucursalId());
    }

    public List<Integer> getEmpleadosAtendiendo(CjCRDatosDao datos) { //Devuelve en un dia en especifico cuantos empleados trabajaron enuna sucursal
        int semana = mododoProceso.desformatearSemana(Integer.parseInt(datos.getSemana()));
        datos.setFechaBetween("T." + mododoProceso.calculaFechasSemanaFormateada(semana));
        String whereClauseTurnos = setVariablesEmpleadosDia(datos);
        String sqlQuery = "SELECT T.FIFECHA,COUNT(DISTINCT T.FCEMPNOID) AS EMPLEADOSDIA FROM TACJTRTURNO T \n"
                + "INNER JOIN TACJTRHISTORICO H ON T.FIFECHA=H.FIFECHA AND T.FITURNOID=H.FITURNOID AND T.FIUNIDADNEGOCIOID = H.FIUNIDADNEGOCIOID AND \n"
                + "T.FIPAISID = H.FIPAISID AND T.FICANALID = H.FICANALID AND T.FISUCURSALID = H.FISUCURSALID \n"
                + whereClauseTurnos + " AND H.FISTATUSTURNOID=3\n"
                + "GROUP BY T.FIFECHA ORDER BY 1";
        //System.out.println(sqlQuery);
        return usrcajaJdbcTemplate.query(sqlQuery, new RowMapper<Integer>() {

            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                int empleadoPorDia;
                empleadoPorDia = rs.getInt(2);
                return empleadoPorDia;
            }
        });

    }

    public List<Integer> getEmpleadosAtendiendoDescompleto(CjCRDatosDao datos) { //Cuando el registro de una semana no es completa este metod devuelve los dias que existieron empleados
        int semana = mododoProceso.desformatearSemana(Integer.parseInt(datos.getSemana()));
        List<String> fechaBetweens = mododoProceso.calculaFechasDiasDetalFormateada(semana);
        List<CjCRDatosDao> datosDao = new ArrayList<CjCRDatosDao>();
        List<Integer> resultados = new ArrayList<Integer>();
        String sql;
        String whereClause;
        int resultado;
        for (int i = 0; i < fechaBetweens.size(); i++) {
            datosDao.add(datos);
            datosDao.get(i).setFechaBetween("T." + fechaBetweens.get(i));
            whereClause = setVariablesEmpleadosDia(datosDao.get(i));
            sql = "SELECT T.FIFECHA,COUNT(DISTINCT T.FCEMPNOID) AS EMPLEADOSDIA FROM TACJTRTURNO T \n"
                    + "INNER JOIN TACJTRHISTORICO H ON T.FIFECHA=H.FIFECHA AND T.FITURNOID=H.FITURNOID AND T.FIUNIDADNEGOCIOID = H.FIUNIDADNEGOCIOID \n"
                    + whereClause + " AND H.FISTATUSTURNOID=3\n"
                    + "GROUP BY T.FIFECHA ORDER BY 1";
            resultado = usrcajaJdbcTemplate.query(sql, new ResultSetExtractor<Integer>() {

                @Override
                public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                    int res = 0;
                    if (rs.next()) {
                        res = rs.getInt(2);
                    }
                    return res;
                }
            });
            resultados.add(resultado);
        }
        return resultados;
    }

    public Boolean validaEmpleadosAtendiendo(int semana, int canal, int pais, int sucursal) {
        String sqlQuery = "SELECT COUNT(*) FROM TACJTRDIAS WHERE FCTIPOANALISIS = '2' AND FISEMANAID = " + semana + " AND FIPAISID = " + pais + " AND FICANALID = " + canal + " AND FISUCURSALID = " + sucursal;
        int resultado = usrcajaJdbcTemplate.query(sqlQuery, new ResultSetExtractor<Integer>() {

            @Override
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                int res = -10;
                if (rs.next()) {
                    res = rs.getInt(1);
                }
                return res;
            }
        });
        if (resultado == 0) {
            return true;
        } else if (resultado == 1) {
            return false;
        } else {
            LOG.error("Error al validad informacion de TACJTRDIAS (Empleados por dia)");
            return true;
        }
    }

    public void insertaEmpleadosAtendiendo(String fiSemana, int fiPais, int fiCanal, int fiSucursal, int fiLunes, int fiMartes, int fiMiercoles, int fiJueves, int fiViernes, int fiSabado, int fiDomingo) {
        String sql = "INSERT INTO TACJTRDIAS (FCTIPOANALISIS, FISEMANAID, FIPAISID, FICANALID, FISUCURSALID, FILUNES, FIMARTES, FIMIERCOLES, FIJUEVES, FIVIERNES, FISABADO, FIDOMINGO,FDFECHAINSERTA, FCUSERINSERTA)"
                + "VALUES('2'," + fiSemana + "," + fiPais + "," + fiCanal + "," + fiSucursal + "," + fiLunes + "," + fiMartes + "," + fiMiercoles + "," + fiJueves + "," + fiViernes + "," + fiSabado + "," + fiDomingo + ",SYSDATE, '" + userInserta + "' )";
        try {
            usrcajaJdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            LOG.error("Error al insertar en TACJTRDIAS (Empleados por dia)", e);
        }

    }

    // *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    //TACJTRDIAS - TACJTRTIEMPOS *-*-*-CLIENTE EN ESPERA-*-*-*-*-*-*-*-*-*-*-*-*-
    private String setVariablesClienteEsperaDias(CjCRDatosDao datos) {
        return " AND TACJTRTURNO." + datos.getFechaBetween() + " AND "
                + "TACJTRTURNO." + paisLabel + Integer.toString(datos.getSucursal().getPaisId())
                + " AND " + "TACJTRTURNO." + canalLabel + Integer.toString(datos.getSucursal().getCanalId())
                + " AND " + "TACJTRTURNO." + sucursalLabel + Integer.toString(datos.getSucursal().getSucursalId());
    }

    public List<Double> getClienteEsperaDias(CjCRDatosDao datos) {
        int semana = mododoProceso.desformatearSemana(Integer.parseInt(datos.getSemana()));
        datos.setFechaBetween(mododoProceso.calculaFechasSemanaFormateada(semana));
        String whereClauseCliente = setVariablesClienteEsperaDias(datos);
//        String sqlQuery = "SELECT E1.FIFECHA, ROUND(AVG(EXTRACT(MINUTE FROM (E2.FDFECHAINSERTA-E1.FDFECHAINSERTA))),2) AS \"MIN PROMEDIO\"--, AVG(DATEDIFF(SECOND,E1.FDFECHAINSERTA,E2.FDFECHAINSERTA))AS PROMEDIO\n"
//                + "FROM TACJTRHISTORICO E1, TACJTRHISTORICO E2, TACJTRTURNO\n"
//                + "WHERE E1.FISTATUSTURNOID = 1 AND E2.FISTATUSTURNOID = 3 AND\n"
//                + "E1.FIFECHA = E2.FIFECHA AND E2.FIFECHA = E1.FIFECHA AND\n"
//                + "E1.FITURNOID = E2.FITURNOID AND E2.FITURNOID = E1.FITURNOID AND \n"
//                + "E1.FIUNIDADNEGOCIOID = E2.FIUNIDADNEGOCIOID\n"
//                + "AND TACJTRTURNO.FIFECHA = E1.FIFECHA AND TACJTRTURNO.FIFECHA = E2.FIFECHA \n"
//                + "AND TACJTRTURNO.FITURNOID = E1.FITURNOID AND TACJTRTURNO.FITURNOID = E2.FITURNOID\n"
//                + "AND TACJTRTURNO.FIVIRTUAL = 0 AND TACJTRTURNO.FIUNIDADNEGOCIOID = 6\n"
//                + whereClauseCliente
//                + "\nGROUP BY (E1.FIFECHA)\n"
//                + "ORDER BY E1.FIFECHA";
        String sqlQuery = "SELECT E1.FIFECHA, ROUND(AVG(EXTRACT(MINUTE FROM (E2.FDFECHAINSERTA-E1.FDFECHAINSERTA))+0.01667*EXTRACT(SECOND FROM (E2.FDFECHAINSERTA-E1.FDFECHAINSERTA))),2) AS \"MINUTOSPROMEDIO\"\n"
                + "FROM TACJTRHISTORICO E1, TACJTRHISTORICO E2, TACJTRTURNO\n"
                + "WHERE E1.FISTATUSTURNOID = 1 AND E2.FISTATUSTURNOID = 3 AND\n"
                + "E1.FIFECHA = E2.FIFECHA AND E2.FIFECHA = E1.FIFECHA AND\n"
                + "E1.FITURNOID = E2.FITURNOID AND E2.FITURNOID = E1.FITURNOID AND \n"
                + "E1.FIPAISID = E2.FIPAISID AND E1.FICANALID = E2.FICANALID AND E1.FISUCURSALID = E2.FISUCURSALID AND\n"
                + "E1.FIUNIDADNEGOCIOID = E2.FIUNIDADNEGOCIOID\n"
                + "AND TACJTRTURNO.FIFECHA = E1.FIFECHA AND TACJTRTURNO.FIFECHA = E2.FIFECHA \n"
                + "AND TACJTRTURNO.FITURNOID = E1.FITURNOID AND TACJTRTURNO.FITURNOID = E2.FITURNOID\n"
                + "AND TACJTRTURNO.FIVIRTUAL = 0 AND TACJTRTURNO.FIUNIDADNEGOCIOID = 6\n"
                + whereClauseCliente
                + "GROUP BY (E1.FIFECHA)\n"
                + "ORDER BY E1.FIFECHA";
        //System.out.println(sqlQuery);
        return usrcajaJdbcTemplate.query(sqlQuery, new RowMapper<Double>() {

            @Override
            public Double mapRow(ResultSet rs, int rowNum) throws SQLException {
                double tiempoAtendiendo;
                tiempoAtendiendo = rs.getDouble(2);
                return tiempoAtendiendo;
            }
        });
    }
    public List<Double> getClienteEsperaDiasDescompleto(CjCRDatosDao datos) { //Cuando el registro de una semana no es completa este metod devuelve los dias que existieron empleados
        int semana = mododoProceso.desformatearSemana(Integer.parseInt(datos.getSemana()));
        List<String> fechaBetweens = mododoProceso.calculaFechasDiasDetalFormateada(semana);
        List<CjCRDatosDao> datosDao = new ArrayList<CjCRDatosDao>();
        List<Double> resultados = new ArrayList<Double>();
        String sql;
        String whereClause;
        double resultado;
        for (int i = 0; i < fechaBetweens.size(); i++) {
            datosDao.add(datos);
            datosDao.get(i).setFechaBetween(fechaBetweens.get(i));
            whereClause = setVariablesClienteEsperaDias(datosDao.get(i));
//            sql = "SELECT E1.FIFECHA, ROUND(AVG(EXTRACT(MINUTE FROM (E2.FDFECHAINSERTA-E1.FDFECHAINSERTA))),2) AS \"MIN PROMEDIO\"--, AVG(DATEDIFF(SECOND,E1.FDFECHAINSERTA,E2.FDFECHAINSERTA))AS PROMEDIO\n"
//                    + "FROM TACJTRHISTORICO E1, TACJTRHISTORICO E2, TACJTRTURNO\n"
//                    + "WHERE E1.FISTATUSTURNOID = 1 AND E2.FISTATUSTURNOID = 3 AND\n"
//                    + "E1.FIFECHA = E2.FIFECHA AND E2.FIFECHA = E1.FIFECHA AND\n"
//                    + "E1.FITURNOID = E2.FITURNOID AND E2.FITURNOID = E1.FITURNOID AND \n"
//                    + "E1.FIUNIDADNEGOCIOID = E2.FIUNIDADNEGOCIOID\n"
//                    + "AND TACJTRTURNO.FIFECHA = E1.FIFECHA AND TACJTRTURNO.FIFECHA = E2.FIFECHA \n"
//                    + "AND TACJTRTURNO.FITURNOID = E1.FITURNOID AND TACJTRTURNO.FITURNOID = E2.FITURNOID\n"
//                    + "AND TACJTRTURNO.FIVIRTUAL = 0 AND TACJTRTURNO.FIUNIDADNEGOCIOID = 6\n"
//                    + whereClause
//                    + "\nGROUP BY (E1.FIFECHA)\n"
//                    + "ORDER BY E1.FIFECHA";
            sql = "SELECT E1.FIFECHA, ROUND(AVG(EXTRACT(MINUTE FROM (E2.FDFECHAINSERTA-E1.FDFECHAINSERTA))+0.01667*EXTRACT(SECOND FROM (E2.FDFECHAINSERTA-E1.FDFECHAINSERTA))),2) AS \"MINUTOSPROMEDIO\"\n"
                    + "FROM TACJTRHISTORICO E1, TACJTRHISTORICO E2, TACJTRTURNO\n"
                    + "WHERE E1.FISTATUSTURNOID = 1 AND E2.FISTATUSTURNOID = 3 AND\n"
                    + "E1.FIFECHA = E2.FIFECHA AND E2.FIFECHA = E1.FIFECHA AND\n"
                    + "E1.FITURNOID = E2.FITURNOID AND E2.FITURNOID = E1.FITURNOID AND \n"
                    + "E1.FIPAISID = E2.FIPAISID AND E1.FICANALID = E2.FICANALID AND E1.FISUCURSALID = E2.FISUCURSALID AND\n"
                    + "E1.FIUNIDADNEGOCIOID = E2.FIUNIDADNEGOCIOID\n"
                    + "AND TACJTRTURNO.FIFECHA = E1.FIFECHA AND TACJTRTURNO.FIFECHA = E2.FIFECHA \n"
                    + "AND TACJTRTURNO.FITURNOID = E1.FITURNOID AND TACJTRTURNO.FITURNOID = E2.FITURNOID\n"
                    + "AND TACJTRTURNO.FIVIRTUAL = 0 AND TACJTRTURNO.FIUNIDADNEGOCIOID = 6\n"
                    + whereClause
                    + "GROUP BY (E1.FIFECHA)\n"
                    + "ORDER BY E1.FIFECHA";
            resultado = usrcajaJdbcTemplate.query(sql, new ResultSetExtractor<Double>() {

                @Override
                public Double extractData(ResultSet rs) throws SQLException, DataAccessException {
                    double res = 0;
                    if (rs.next()) {
                        res = rs.getDouble(2);
                    }
                    return res;
                }
            });
            resultados.add(resultado);
        }
        return resultados;
    }

    public Boolean validaClienteEsperaDias(int semana, int canal, int pais, int sucursal) {
        String sqlQuery = "SELECT COUNT(*) FROM TACJTRDIAS WHERE FCTIPOANALISIS = '1' AND FISEMANAID = " + semana + " AND FIPAISID = " + pais + " AND FICANALID = " + canal + " AND FISUCURSALID = " + sucursal;
        int resultado = usrcajaJdbcTemplate.query(sqlQuery, new ResultSetExtractor<Integer>() {

            @Override
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                int res = -10;
                if (rs.next()) {
                    res = rs.getInt(1);
                }
                return res;
            }
        });
        if (resultado == 0) {
            return true;
        } else if (resultado == 1) {
            return false;
        } else {
            LOG.error("Error al validad informacion de TACJTRDIAS (CLIENTE EN ESPERA)");
            return true;
        }
    }
    public void instertaClienteEsperaDias(String fiSemana, int fiPais, int fiCanal, int fiSucursal, double fiLunes, double fiMartes, double fiMiercoles, double fiJueves, double fiViernes, double fiSabado, double fiDomingo) {
        String sql = "INSERT INTO TACJTRDIAS (FCTIPOANALISIS, FISEMANAID, FIPAISID, FICANALID, FISUCURSALID, FILUNES, FIMARTES, FIMIERCOLES, FIJUEVES, FIVIERNES, FISABADO, FIDOMINGO,FDFECHAINSERTA, FCUSERINSERTA)"
                + "VALUES('1'," + fiSemana + "," + fiPais + "," + fiCanal + "," + fiSucursal + "," + fiLunes + "," + fiMartes + "," + fiMiercoles + "," + fiJueves + "," + fiViernes + "," + fiSabado + "," + fiDomingo + ",SYSDATE, '" + userInserta + "' )";
        try {
            usrcajaJdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            LOG.error("Error al insertar en TACJTRDIAS (CLIENTE EN ESPERA)", e);
        }
    }
    
    public List<CjCRTiempos> getClienteDiasTiempo (CjCRDatosDao datos){
        final CjCRDatosDao datos2 = datos;
        int semana = mododoProceso.desformatearSemana(Integer.parseInt(datos.getSemana()));
        datos.setFechaBetween(mododoProceso.calculaFechasSemanaFormateada(semana));
        String whereClauseCliente = setVariablesClienteEsperaDias(datos);
        String sqlQuery = "SELECT AVG(EXTRACT(MINUTE FROM DIF)+0.01667*EXTRACT(SECOND FROM DIF)) AS \"MINUTOS PROMEDIO\",\n"
                + "       EXTRACT(MINUTE FROM MAX(DIF))+0.01667*EXTRACT(SECOND FROM MAX(DIF)) AS \"MINUTOS MAXIMOS\",\n"
                + "       EXTRACT(MINUTE FROM MIN(DIF))+0.01667*EXTRACT(SECOND FROM MIN(DIF)) AS \"MINUTOS MINIMOS\"\n"
                + "	   		FROM(\n"
                + "	   		    SELECT TACJTRTURNO.FIFECHA, TACJTRTURNO.FITURNOID,  E2.FDFECHAINSERTA-E1.FDFECHAINSERTA AS DIF\n"
                + "	   		    FROM TACJTRHISTORICO E1, TACJTRHISTORICO E2, TACJTRTURNO\n"
                + "	   		    WHERE E1.FISTATUSTURNOID = 1 AND E2.FISTATUSTURNOID = 3 AND\n"
                + "	   		    E1.FIFECHA = E2.FIFECHA AND E2.FIFECHA = E1.FIFECHA AND\n"
                + "	   		    E1.FITURNOID = E2.FITURNOID AND E2.FITURNOID = E1.FITURNOID AND\n"
                + "	   		    E1.FIPAISID = E2.FIPAISID AND E1.FICANALID = E2.FICANALID AND E1.FISUCURSALID = E2.FISUCURSALID AND\n"
                + "	   		    E1.FIUNIDADNEGOCIOID = E2.FIUNIDADNEGOCIOID\n"
                + "	   		    AND TACJTRTURNO.FIFECHA = E1.FIFECHA AND TACJTRTURNO.FIFECHA = E2.FIFECHA \n"
                + "	   		    AND TACJTRTURNO.FITURNOID = E1.FITURNOID AND TACJTRTURNO.FITURNOID = E2.FITURNOID\n"
                + "	   		    AND TACJTRTURNO.FICANALID = E1.FICANALID AND TACJTRTURNO.FICANALID = E2.FICANALID\n"
                + "	   		    AND TACJTRTURNO.FIPAISID = E1.FIPAISID AND TACJTRTURNO.FIPAISID = E2.FIPAISID\n"
                + "	   		    AND TACJTRTURNO.FISUCURSALID = E1.FISUCURSALID AND TACJTRTURNO.FISUCURSALID = E2.FISUCURSALID\n"
                + "	   		    AND TACJTRTURNO.FIVIRTUAL = 0 AND TACJTRTURNO.FIUNIDADNEGOCIOID = 6\n"
                + whereClauseCliente
                + "	   		    )";
        return usrcajaJdbcTemplate.query(sqlQuery, new RowMapper<CjCRTiempos>() {
            @Override
            public CjCRTiempos mapRow(ResultSet rs, int rowNum) throws SQLException {
                CjCRTiempos tiempos = new CjCRTiempos();
                tiempos.setFcTipoAnalisis("1");
                tiempos.setSucursal(datos2.getSucursal());
                tiempos.setFiAvg(rs.getDouble(1));
                tiempos.setFiMax(rs.getDouble(2));
                tiempos.setFiMin(rs.getDouble(3));   
                return tiempos;
            }
        });
    }
    
    public Boolean validaClienteEsperaTiempo(int semana, int canal, int pais, int sucursal) {
        String sqlQuery = "SELECT COUNT(*) FROM TACJTRTIEMPOS WHERE FCTIPOANALISIS = '1' AND FISEMANAID = " + semana + " AND FIPAISID = " + pais + " AND FICANALID = " + canal + " AND FISUCURSALID = " + sucursal;
        int resultado = usrcajaJdbcTemplate.query(sqlQuery, new ResultSetExtractor<Integer>() {

            @Override
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                int res = -10;
                if (rs.next()) {
                    res = rs.getInt(1);
                }
                return res;
            }
        });
        if (resultado == 0) {
            return true;
        } else if (resultado == 1) {
            return false;
        } else {
            LOG.error("Error al validad informacion de TACJTRDIAS (CLIENTE EN ESPERA)");
            return true;
        }
    }
    public void instertaClienteEsperaTiempo(String fiSemana, int fiPais, int fiCanal, int fiSucursal, double fiAvg, double fiMin, double fiMax, double desvest ) {
        String sql = "INSERT INTO TACJTRTIEMPOS (FCTIPOANALISIS, FISEMANAID, FIPAISID, FICANALID, FISUCURSALID, FIAVG, FIMIN, FIMAX, FIDESVEST,FDFECHAINSERTA, FCUSERINSERTA)"
                + "VALUES('1'," + fiSemana + "," + fiPais + "," + fiCanal + "," + fiSucursal + "," + fiAvg + "," + fiMin + "," + fiMax + ","+desvest+" ,SYSDATE, '" + userInserta + "' )";
        try {
            usrcajaJdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            LOG.error("Error al insertar en TACJTRTIEMPOS (CLIENTE EN ESPERA)", e);
        }
    }
    
    // *-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-
    //TACJTRDIAS - TACJTRTIEMPOS *-*-*-TIEMPO SIN ATENDER-*-*-*-*-*-*-*-*-*-*-*-*-
    private String setVariablesUnoTiempoSnAtenderDias (CjCRDatosDao datos){
        return  " AND T1."+datos.getFechaBetween()+
                " AND T1."+paisLabel+Integer.toString(datos.getSucursal().getPaisId())+
                " AND T1."+ canalLabel + Integer.toString(datos.getSucursal().getCanalId())+
                " AND T1." + sucursalLabel + Integer.toString(datos.getSucursal().getSucursalId());
    }
    private String setvariablesDosTiempoSnAtenderTiempo (CjCRDatosDao datos){
        return " AND TACJTRTURNO."+datos.getFechaBetween()+
                " AND TACJTRTURNO." + paisLabel + Integer.toString(datos.getSucursal().getPaisId())
                + " AND TACJTRTURNO." + canalLabel + Integer.toString(datos.getSucursal().getCanalId())
                + " AND TACJTRTURNO." + sucursalLabel + Integer.toString(datos.getSucursal().getSucursalId());
    }
    private String makeQueryTiempoSnAtenderDias(String variablesDias, String variablesTiempo){
        return 
       "SELECT TAB1.FECHA,TAB1.HORASPROMEDIO,TAB1.NO_EMPLEADOS_TRABAJARON,TAB2.TIEMPOTOTAL,\n" +
"    	TAB1.HORASPROMEDIO-(TAB1.NO_EMPLEADOS_TRABAJARON*2) AS HORAS_TRABAJADAS_MENOS_COMIDA,\n" +
"    	(TAB1.HORASPROMEDIO-(TAB1.NO_EMPLEADOS_TRABAJARON*2))-TAB2.TIEMPOTOTAL AS HORASTRABAJADASMENOSCOMIDA,\n" +
"    	((TAB1.HORASPROMEDIO-(TAB1.NO_EMPLEADOS_TRABAJARON*2))-TAB2.TIEMPOTOTAL)/TAB1.NO_EMPLEADOS_TRABAJARON AS TIEMPSNATENDRTODOSLOSEMP,\n" +
"    	(((TAB1.HORASPROMEDIO-(TAB1.NO_EMPLEADOS_TRABAJARON*2))-TAB2.TIEMPOTOTAL))/TAB2.TIEMPOTOTAL AS CAJEROSSINATENDERHORAS,\n" +
"    	((((TAB1.HORASPROMEDIO-(TAB1.NO_EMPLEADOS_TRABAJARON*2))-TAB2.TIEMPOTOTAL))/TAB2.TIEMPOTOTAL)*60 AS CAJEROSSINATENDERMINUTOS\n" +
"    	FROM(\n" +
"    	    SELECT T1.FIFECHA FECHA,\n" +
"    	            ROUND(SUM(EXTRACT(HOUR FROM (T2.FDFECHAINSERTA-T1.FDFECHAINSERTA))+0.01667*EXTRACT(MINUTE FROM (T2.FDFECHAINSERTA-T1.FDFECHAINSERTA))),2) AS \"HORASPROMEDIO\",\n" +
"    	            COUNT (T1.FIFECHA)AS NO_EMPLEADOS_TRABAJARON\n" +
"    	    FROM TACJTRTURNO T1 , TACJTRTURNO T2\n" +
"    	    WHERE T1.FCEMPNOID IS NOT NULL \n" +
"    	            AND T1.FDFECHAINSERTA IN (SELECT MIN(I1.FDFECHAINSERTA) FROM TACJTRTURNO I1 WHERE \n" +
"    	                                      I1.FCEMPNOID = T1.FCEMPNOID AND I1.FIFECHA = T1.FIFECHA AND I1.FIUNIDADNEGOCIOID = T1.FIUNIDADNEGOCIOID \n" +
"    	                                      AND I1.FISUCURSALID = T1.FISUCURSALID AND I1.FIPAISID = T1.FIPAISID AND I1.FICANALID = T1.FICANALID)\n" +
"    	            AND T2.FDFECHAINSERTA IN (SELECT MAX(I2.FDFECHAINSERTA) FROM TACJTRTURNO I2 WHERE \n" +
"    	                                      I2.FCEMPNOID = T1.FCEMPNOID AND I2.FIFECHA = T1.FIFECHA AND I2.FIUNIDADNEGOCIOID = T1.FIUNIDADNEGOCIOID \n" +
"    	                                      AND I2.FISUCURSALID = T1.FISUCURSALID AND I2.FIPAISID = T1.FIPAISID AND I2.FICANALID = T1.FICANALID)\n" +
                    variablesDias+
"    	    GROUP BY T1.FIFECHA\n" +
"    	) TAB1\n" +
"    	INNER JOIN \n" +
"    	(\n" +
"    	    SELECT  E1.FIFECHA FECHA,    \n" +
"    	    ROUND(SUM(EXTRACT(HOUR FROM (E2.FDFECHAINSERTA-E1.FDFECHAINSERTA))+0.01667*EXTRACT(MINUTE FROM (E2.FDFECHAINSERTA-E1.FDFECHAINSERTA))),2) AS \"TIEMPOTOTAL\"\n" +
"    	    FROM TACJTRHISTORICO E1, TACJTRHISTORICO E2, TACJTRTURNO\n" +
"    	    WHERE \n" +
"    	            E1.FIFECHA = E2.FIFECHA AND\n" +
"    	            E1.FITURNOID = E2.FITURNOID AND \n" +
"    	            E1.FISUCURSALID = E2.FISUCURSALID AND\n" +
"    	            E1.FIPAISID = E2.FIPAISID AND\n" +
"    	            E1.FICANALID = E2.FICANALID AND\n" +
"    	            E1.FIUNIDADNEGOCIOID = E2.FIUNIDADNEGOCIOID \n" +
"    	            AND TACJTRTURNO.FIFECHA = E1.FIFECHA\n" +
"    	            AND TACJTRTURNO.FITURNOID = E1.FITURNOID\n" +
"    	            AND TACJTRTURNO.FISUCURSALID = E1.FISUCURSALID\n" +
"    	            AND TACJTRTURNO.FICANALID = E1.FICANALID\n" +
"    	            AND TACJTRTURNO.FIPAISID = E1.FIPAISID\n" +
"    	            AND TACJTRTURNO.FIUNIDADNEGOCIOID = E1.FIUNIDADNEGOCIOID \n" +
"    	            AND E1.FISTATUSTURNOID = 3 AND E2.FISTATUSTURNOID = 4 \n" +
"    	            AND TACJTRTURNO.FIUNIDADNEGOCIOID = 6 \n" +
                    variablesTiempo+
"    	    GROUP BY E1.FIFECHA\n" +
"    	) TAB2\n" +
"    	ON TAB1.FECHA = TAB2.FECHA";
    }
    private String makeQueryTiemposSnAtenderTiempo(String variableDias, String variableTiempo){
        return 
                "SELECT ROUND(AVG(CAJEROSSINATENDERMINUTOS),2) PROM, ROUND(MIN(CAJEROSSINATENDERMINUTOS),2) MINI, ROUND(MAX(CAJEROSSINATENDERMINUTOS),2) MAXI\n" +
"		FROM (\n"+makeQueryTiempoSnAtenderDias(variableDias, variableTiempo)+
                "\n)";
    }
    
    public List<Double> getTiempoSnAtenderDias (CjCRDatosDao datos){
        int semana = mododoProceso.desformatearSemana(Integer.parseInt(datos.getSemana()));
        datos.setFechaBetween(mododoProceso.calculaFechasSemanaFormateada(semana));
        String query = makeQueryTiempoSnAtenderDias(setVariablesUnoTiempoSnAtenderDias(datos), setvariablesDosTiempoSnAtenderTiempo(datos));
        return usrcajaJdbcTemplate.query(query, new RowMapper<Double>() {

            @Override
            public Double mapRow(ResultSet rs, int rowNum) throws SQLException {
                double tiempoSnAtender;
                tiempoSnAtender = rs.getDouble(8);
                return tiempoSnAtender;
            }
        });
    }
    public List<Double> getTiempoSnAtenderDiasDescompleto(CjCRDatosDao datos) { //Cuando el registro de una semana no es completa este metod devuelve los dias que existieron empleados
        int semana = mododoProceso.desformatearSemana(Integer.parseInt(datos.getSemana()));
        List<String> fechaBetweens = mododoProceso.calculaFechasDiasDetalFormateada(semana);
        List<CjCRDatosDao> datosDao = new ArrayList<CjCRDatosDao>();
        List<Double> resultados = new ArrayList<Double>();
        String sql;
        String whereClause1;
        String whereClause2;
        double resultado;
        for (int i = 0; i < fechaBetweens.size(); i++) {
            datosDao.add(datos);
            datosDao.get(i).setFechaBetween(fechaBetweens.get(i));
            whereClause1 = setVariablesUnoTiempoSnAtenderDias(datosDao.get(i));
            whereClause2 = setvariablesDosTiempoSnAtenderTiempo(datosDao.get(i));
            sql = makeQueryTiempoSnAtenderDias(whereClause1,whereClause2);
            resultado = usrcajaJdbcTemplate.query(sql, new ResultSetExtractor<Double>() {

                @Override
                public Double extractData(ResultSet rs) throws SQLException, DataAccessException {
                    double res = 0;
                    if (rs.next()) {
                        res = rs.getDouble(2);
                    }
                    return res;
                }
            });
            resultados.add(resultado);
        }
        return resultados;
    }
    
    public Boolean validaTiempoSnAtenderDias(int semana, int canal, int pais, int sucursal) {
        String sqlQuery = "SELECT COUNT(*) FROM TACJTRDIAS WHERE FCTIPOANALISIS = '3' AND FISEMANAID = " + semana + " AND FIPAISID = " + pais + " AND FICANALID = " + canal + " AND FISUCURSALID = " + sucursal;
        int resultado = usrcajaJdbcTemplate.query(sqlQuery, new ResultSetExtractor<Integer>() {

            @Override
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                int res = -10;
                if (rs.next()) {
                    res = rs.getInt(1);
                }
                return res;
            }
        });
        if (resultado == 0) {
            return true;
        } else if (resultado == 1) {
            return false;
        } else {
            LOG.error("Error al validad informacion de TACJTRDIAS (TIEMPO SIN ATENDER)");
            return true;
        }
    }
    public void instertaTiempoSnAtenderDias(String fiSemana, int fiPais, int fiCanal, int fiSucursal, double fiLunes, double fiMartes, double fiMiercoles, double fiJueves, double fiViernes, double fiSabado, double fiDomingo) {
        String sql = "INSERT INTO TACJTRDIAS (FCTIPOANALISIS, FISEMANAID, FIPAISID, FICANALID, FISUCURSALID, FILUNES, FIMARTES, FIMIERCOLES, FIJUEVES, FIVIERNES, FISABADO, FIDOMINGO,FDFECHAINSERTA, FCUSERINSERTA)"
                + "VALUES('3'," + fiSemana + "," + fiPais + "," + fiCanal + "," + fiSucursal + "," + fiLunes + "," + fiMartes + "," + fiMiercoles + "," + fiJueves + "," + fiViernes + "," + fiSabado + "," + fiDomingo + ",SYSDATE, '" + userInserta + "' )";
        try {
            usrcajaJdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            LOG.error("Error al insertar en TACJTRDIAS (CLIENTE EN ESPERA)", e);
        }
    }
    
    public List<CjCRTiempos> getTiemposSnAtenderTiempo(CjCRDatosDao datos){
        final CjCRDatosDao datos2 = datos;
        int semana = mododoProceso.desformatearSemana(Integer.parseInt(datos.getSemana()));
        datos.setFechaBetween(mododoProceso.calculaFechasSemanaFormateada(semana));
        String sql = makeQueryTiemposSnAtenderTiempo(setVariablesUnoTiempoSnAtenderDias(datos), setvariablesDosTiempoSnAtenderTiempo(datos));
        return usrcajaJdbcTemplate.query(sql, new RowMapper<CjCRTiempos>() {

            @Override
            public CjCRTiempos mapRow(ResultSet rs, int rowNum) throws SQLException {
                CjCRTiempos tiempos = new CjCRTiempos();
                tiempos.setFcTipoAnalisis("3");
                tiempos.setSucursal(datos2.getSucursal());
                tiempos.setFiAvg(rs.getDouble(1));
                tiempos.setFiMin(rs.getDouble(2));
                tiempos.setFiMax(rs.getDouble(3));
                return tiempos;
            }
        });
        
    }
    
    public Boolean validaTiemposSnAtenderTiempo(int semana, int canal, int pais, int sucursal) {
        String sqlQuery = "SELECT COUNT(*) FROM TACJTRTIEMPOS WHERE FCTIPOANALISIS = '3' AND FISEMANAID = " + semana + " AND FIPAISID = " + pais + " AND FICANALID = " + canal + " AND FISUCURSALID = " + sucursal;
        int resultado = usrcajaJdbcTemplate.query(sqlQuery, new ResultSetExtractor<Integer>() {

            @Override
            public Integer extractData(ResultSet rs) throws SQLException, DataAccessException {
                int res = -10;
                if (rs.next()) {
                    res = rs.getInt(1);
                }
                return res;
            }
        });
        if (resultado == 0) {
            return true;
        } else if (resultado == 1) {
            return false;
        } else {
            LOG.error("Error al validad informacion de TACJTRDIAS (CLIENTE EN ESPERA)");
            return true;
        }
    }
    public void instertaTiemposSnAtenderTiempo(String fiSemana, int fiPais, int fiCanal, int fiSucursal, double fiAvg, double fiMin, double fiMax, double desvest ) {
        String sql = "INSERT INTO TACJTRTIEMPOS (FCTIPOANALISIS, FISEMANAID, FIPAISID, FICANALID, FISUCURSALID, FIAVG, FIMIN, FIMAX, FIDESVEST,FDFECHAINSERTA, FCUSERINSERTA)"
                + "VALUES('3'," + fiSemana + "," + fiPais + "," + fiCanal + "," + fiSucursal + "," + fiAvg + "," + fiMin + "," + fiMax + ","+desvest+" ,SYSDATE, '" + userInserta + "' )";
        try {
            usrcajaJdbcTemplate.execute(sql);
        } catch (DataAccessException e) {
            LOG.error("Error al insertar en TACJTRTIEMPOS (CLIENTE EN ESPERA)", e);
        }
    }

}
