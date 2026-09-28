/*:-----------------------------------------------------------------------------
 *:                       INSTITUTO TECNOLOGICO DE LA LAGUNA
 *:                     INGENIERIA EN SISTEMAS COMPUTACIONALES
 *:                         LENGUAJES Y AUTOMATAS II           
 *: 
 *:                  SEMESTRE: ___________    HORA: ___________ HRS
 *:                                   
 *:               
 *:         Clase con la funcionalidad del Analizador Sintactico
 *                 
 *:                           
 *: Archivo       : SintacticoSemantico.java
 *: Autor         : Fernando Gil  ( Estructura general de la clase  )
 *:                 Grupo de Lenguajes y Automatas II ( Procedures  )
 *: Fecha         : 03/SEP/2014
 *: Compilador    : Java JDK 7
 *: Descripción   : Esta clase implementa un parser descendente del tipo 
 *:                 Predictivo Recursivo. Se forma por un metodo por cada simbolo
 *:                 No-Terminal de la gramatica mas el metodo emparejar ().
 *:                 El analisis empieza invocando al metodo del simbolo inicial.
 *: Ult.Modif.    :
 *:  Fecha      Modificó            Modificacion
 *:=============================================================================
 *: 22/Feb/2015 FGil                -Se mejoro errorEmparejar () para mostrar el
 *:                                 numero de linea en el codigo fuente donde 
 *:                                 ocurrio el error.
 *: 08/Sep/2015 FGil                -Se dejo lista para iniciar un nuevo analizador
 *:                                 sintactico.
 *: 23/FEB/2026 F.Gil, T.Swift,     -Se implementaron los procedures del parser
 *:             S.Johanson,         predictivo recursivo de leng PaytonTk.
 *:             K.Perry
 *:-----------------------------------------------------------------------------
 */
package compilador;

import javax.swing.JOptionPane;

public class SintacticoSemantico {

    private Compilador cmp;
    private boolean    analizarSemantica = false;
    private String     preAnalisis;
    
    //--------------------------------------------------------------------------
    // Constructor de la clase, recibe la referencia de la clase principal del 
    // compilador.
    //

    public SintacticoSemantico(Compilador c) {
        cmp = c;
    }

    //--------------------------------------------------------------------------
    //--------------------------------------------------------------------------
    // Metodo que inicia la ejecucion del analisis sintactico predictivo.
    // analizarSemantica : true = realiza el analisis semantico a la par del sintactico
    //                     false= realiza solo el analisis sintactico sin comprobacion semantica

    public void analizar(boolean analizarSemantica) {
        this.analizarSemantica = analizarSemantica;
        preAnalisis = cmp.be.preAnalisis.complex;

        // * * *   INVOCAR AQUI EL PROCEDURE DEL SIMBOLO INICIAL   * * *    
        Programa();
    }

    //--------------------------------------------------------------------------

    private void emparejar(String t) {
        if (cmp.be.preAnalisis.complex.equals(t)) {
            cmp.be.siguiente();
            preAnalisis = cmp.be.preAnalisis.complex;            
        } else {
            errorEmparejar( t, cmp.be.preAnalisis.lexema, cmp.be.preAnalisis.numLinea );
        }
    }
    
    //--------------------------------------------------------------------------
    // Metodo para devolver un error al emparejar
    //--------------------------------------------------------------------------
 
    private void errorEmparejar(String _token, String _lexema, int numLinea ) {
        String msjError = "";

        if (_token.equals("id")) {
            msjError += "Se esperaba un identificador";
        } else if (_token.equals("num")) {
            msjError += "Se esperaba una constante entera";
        } else if (_token.equals("num.num")) {
            msjError += "Se esperaba una constante real";
        } else if (_token.equals("literal")) {
            msjError += "Se esperaba una literal";
        } else if (_token.equals("oparit")) {
            msjError += "Se esperaba un operador aritmetico";
        } else if (_token.equals("oprel")) {
            msjError += "Se esperaba un operador relacional";
        } else if (_token.equals("opasig")) {
            msjError += "Se esperaba operador de asignacion";
        } else {
            msjError += "Se esperaba " + _token;
        }
        msjError += " se encontró " + ( _lexema.equals ( "$" )? "fin de archivo" : _lexema ) + 
                    ". Linea " + numLinea;        // FGil: Se agregó el numero de linea

        cmp.me.error(Compilador.ERR_SINTACTICO, msjError);
    }

    // Fin de ErrorEmparejar
    //--------------------------------------------------------------------------
    // Metodo para mostrar un error sintactico

    private void error(String _descripError) {
        cmp.me.error(cmp.ERR_SINTACTICO, _descripError);
    }

    // Fin de error
    //--------------------------------------------------------------------------
    //  *  *   *   *    PEGAR AQUI EL CODIGO DE LOS PROCEDURES  *  *  *  *
    //--------------------------------------------------------------------------

    private void Programa(){
        if (preAnalisis.equals("class")){
            /*Programa → class id { ListaMetodos } */
            emparejar("class");
            emparejar("id");
            emparejar("{");
            ListaMetodos();
            emparejar("}");
            // para que no haya nada despues del fin del archivo
            emparejar("$");
        }
        else{
            error("[Programa] Se esperaba class en la línea\n"+cmp.be.preAnalisis.numLinea);
        }
    }
    
    private void ListaMetodos(){
        if (preAnalisis.equals("fun")){
            // → Metodo ListaMetodos'
            Metodo();
            ListaMetodosP();
        }
        else
            error("[ListaMetodos] Se esperaba fun (inicio de una funcion) en la línea\n" + cmp.be.preAnalisis.numLinea);
    }
    
    private void ListaMetodosP(){
        if (preAnalisis.equals("fun")){
             /*→ Metodo ListaMetodos' | ε   */
            Metodo();
            ListaMetodosP();
        }
        else
            return;
    }
    
    private void Metodo(){
        if (preAnalisis.equals("fun")){
            /*Metodo → fun id ( Parametros ) Retorno { ListaSentencias }*/
            emparejar("fun");
            emparejar("id");
            emparejar("(");
            Parametros();
            emparejar(")");
            Retorno();
            emparejar("{");
            ListaSentencias();
            emparejar("}");
            
        }
        else
            error("[Metodo] Se esperaba fun (inicio de una funcion) en la íinea\n" +cmp.be.preAnalisis.numLinea);
    }
    
    private void Retorno(){
        if (preAnalisis.equals(":")){
            // Retorno -> : Tipo |  ε
            emparejar(":");
            Tipo();
        }
        else 
            return;
    }
    
    private void Parametros(){
        if (preAnalisis.equals("id")){
        /*    → Parametro ListaParametros' | ε  */
            Parametro();
            ListaParametrosP();
        }
        else
            return;
    }
    
    private void ListaParametrosP(){
        if (preAnalisis.equals(",")){
        /*    → , Parametro ListaParametros' | ε  */
            emparejar(",");
            Parametro();
            ListaParametrosP();
        }
        else
            return;
    }
    
    private void Parametro(){
        if (preAnalisis.equals("id")){
            // Parametro -> id : Tipo
            emparejar("id");
            emparejar(":");
            Tipo();
        }
        else
            error("[Parametro] se esperaba un id en la línea \n" +cmp.be.preAnalisis.numLinea);
    }
    
    private void Tipo(){
        if (preAnalisis.equals("Int"))
            //Tipo -> Int | Double | String
            emparejar("Int");
        else if (preAnalisis.equals("Double"))
            emparejar("Double");
        else if (preAnalisis.equals("String"))
            emparejar("String");
        else
            error("[Tipo] se esperaba Int Double o String en la línea"+cmp.be.preAnalisis.numLinea);
    }
    
    private void ListaSentencias(){
        // ListaSentencias -> Sentencia ListaSentencias'
        //Primeros(Sentencia) = {var, val, let, id, if, while, return }
        //Declaracion -> Primeros(Modificador)
        //Modificador -> var | val
          if (preAnalisis.equals("var")
          ||  preAnalisis.equals("val")
          ||  preAnalisis.equals("let")
          ||  preAnalisis.equals("id")
          ||  preAnalisis.equals("if")
          ||  preAnalisis.equals("while")
          ||  preAnalisis.equals("return")){
              Sentencia();
              ListaSentenciasP();
          }
        else
            error("[ListaSentencias] Se esperaba una sentencia var || val || let || id || if || while || return en la línea " + cmp.be.preAnalisis.numLinea);
        
    }
    
    private void ListaSentenciasP(){
        // ListaSentencias -> Sentencia ListaSentencias' | ε 
        //Primeros(Sentencia) = {var, val, let, id, if, while, return }
        //Declaracion -> Primeros(Modificador)
        //Modificador -> var | val
         if (preAnalisis.equals("var")
         ||  preAnalisis.equals("val")
         ||  preAnalisis.equals("let")
         ||  preAnalisis.equals("id")
         ||  preAnalisis.equals("if")
         ||  preAnalisis.equals("while")
         ||  preAnalisis.equals("return")){
             Sentencia();
             ListaSentenciasP();
        }
        else
        return;
    }
    
    private void Sentencia(){
        /*    → Declaracion ;
                | Asignacion ;
                | Invocacion ;
                | If
                | While
                | Return
*/          
        if (preAnalisis.equals("var") || preAnalisis.equals("val")){
            Declaracion();
            emparejar(";");
        }   // Sentencia -> Primeros(Asignacion)
            //Asignacion -> let
            //Asignacion ;
            else if (preAnalisis.equals("let")){
                    Asignacion();
                    emparejar(";");
                    }
            //Sentencia -> Primeros(Invocacion)
            //Invocacion -> id
            // Invocacion ;
            else if (preAnalisis.equals("id")){
                Invocacion();
                emparejar(";");
            }
            //Sentencia -> Primeros(If)
            //If -> if
            else if (preAnalisis.equals("if")){
                If();
            }
            //Sentencia -> Primeros(While)
            //While -> while
            else if (preAnalisis.equals("while")){
                While();
            }
            //Sentencia -> Primeros(Return)
            //Return -> return
            else if (preAnalisis.equals("return")){
                Return();
            }    
        else
            error("[Sentencia] se esperaba var || val || let || id || if || while || return en la línea"+cmp.be.preAnalisis.numLinea);
    
    }
    
    private void Declaracion(){
        if (preAnalisis.equals("var") || preAnalisis.equals("val")){
            // Declaracion -> Modificador id : Tipo Declaracion'
            Modificador();
            emparejar("id");
            emparejar(":");
            Tipo();
            DeclaracionP();
        }
        else 
            error("[Declaracion] Se esperaba var o val en la línea \n"+cmp.be.preAnalisis.numLinea);
    }
    
    private void Modificador(){
        // Modificador -> var | val
        if (preAnalisis.equals("var"))
            //Modificador -> var
            emparejar("var");
        else if (preAnalisis.equals("val"))
            //Modificador -> val
            emparejar("val");
        else 
            error("[Modificador] Se esperaba var o val en la línea\n"+cmp.be.preAnalisis.numLinea);
    }
    
    private void DeclaracionP(){
        if (preAnalisis.equals("=")){
            // Declaracion' -> = Expresion || ε 
            emparejar("=");
            Expresion();
        }
        else
            return;
    }
    
    private void Asignacion(){
        if (preAnalisis.equals("let")){
            // Asignacion -> let id = Expresion
            emparejar("let");
            emparejar("id");
            emparejar("=");
            Expresion();
        }
        else
            error("[Asignacion] Se esperaba letn id o = en la línea\n"+cmp.be.preAnalisis.numLinea);
    }
    
    private void Invocacion(){
        if (preAnalisis.equals("id")){
            // Invocacion -> id (Argumentos)
            emparejar("id");
            emparejar("(");
            Argumentos();
            emparejar(")");
        }
        else
         error("[Invocacion] Se esperaba un id en la línea " + cmp.be.preAnalisis.numLinea);
    }
    
    private void Argumentos(){
        /*Argumentos -> Expresion ListaArgumentos' || ε 
        
        Primeros(Expresion)
        Expresion -> Termino Expresion'
        Primeros(Temino)
        Termino -> Factor Termino'
        Primeros(Factor)
        Factor ->( Expresion ) | id  Factor’| num | num.num | literal
        */
        
         if (preAnalisis.equals("(")
        || preAnalisis.equals("id")
        || preAnalisis.equals("num")
        || preAnalisis.equals("num.num")
        || preAnalisis.equals("literal")){

        Expresion();
        ListaArgumentosP();
    }
    else{
        return;
    }
            
    }
    
    private void ListaArgumentosP(){
        if (preAnalisis.equals(",")){
            //ListaArgumentos' -> , Expresion ListaArgumentos' | ε
            emparejar(",");
            Expresion();
            ListaArgumentosP();
        }
        else
            return;               
    }
    
    private void If(){
        if (preAnalisis.equals("if")){
            //If -> if ( Condicion ) { ListaSentencias } If'
            emparejar("if");
            emparejar("(");
            Condicion();
            emparejar(")");
            emparejar("{");
            ListaSentencias();
            emparejar("}");
            IfP();
        }
        else
            error("[If] Sentencia if incompleta"+cmp.be.preAnalisis.numLinea);
    }
    
    private  void IfP(){
        if (preAnalisis.equals("else")){
            // If' -> else { ListaSentencias } || ε
            emparejar("else");
            emparejar("{");
            ListaSentencias();
            emparejar("}");
        }
        else
            return;
    }
    
    private void While(){
        if (preAnalisis.equals("while")){
            // While -> while ( Condicion ) { ListaSentencias }
            emparejar("while");
            emparejar("(");
            Condicion();
            emparejar(")");
            emparejar("{");
            ListaSentencias();
            emparejar("}");
        }
        else
            error("Sentencia while incompleta"+cmp.be.preAnalisis.numLinea);
    }
    
    private void Return(){
        if (preAnalisis.equals("return")){
            //Return -> return Return'
            emparejar("return");
            ReturnP();
        }
        else
            error("[Return] Se esperaba return"+cmp.be.preAnalisis.numLinea);
    }
    //Corregir este a como está implementado sentencias
    private void ReturnP(){
        /*Return' -> Expresion ; | ;
          
          Primeros(Expresion)
          Expresion -> Termino Expresion'
          Primeros(Termino)
          Termino -> Factor Termino'
          Primeros(Factor)
          Factor -> ( Expresion ) | id  Factor' | num | num.num | literal
*/
        if (preAnalisis.equals("(")
         || preAnalisis.equals("id")
         || preAnalisis.equals("num")
         || preAnalisis.equals("num.num")
         || preAnalisis.equals("literal")){

            Expresion();
            emparejar(";");
        }
        else if (preAnalisis.equals(";"))
        emparejar(";");
      else
            error("[Return'] Se esperaba una expresión o ; en la línea\n"+cmp.be.preAnalisis.numLinea);
    }
    
    private void Condicion(){
        //Condicion -> Expresion oprel Expresion
        //Primeros(Expresion)
        //Expresion -> Termino
        //Primeros(Termino)
        //Termino -> Factor Termino'
        //Primeros(Factor)
        /*Factor -> ( Expresion ) | id  Factor’ | num | num.num | literal
        */   
        if (preAnalisis.equals("(")
           || preAnalisis.equals("id")
           || preAnalisis.equals("num")
           || preAnalisis.equals("num.num")
           || preAnalisis.equals("literal")){
            Expresion();
            emparejar("oprel");
            Expresion();
        }
        else
            error("[Condicion] Se esperaba ( | id | num | num.num | literal en la línea\n"+cmp.be.preAnalisis.numLinea);
        
    }
    private void Expresion(){
        //Expresion -> Termino Expresion'
        //Primeros(Termino)
        //Termino -> Factor Termino'
        //Primeros(Factor)
        //*Factor -> ( Expresion ) | id  Factor’ | num | num.num | literal
        if (preAnalisis.equals("(")
           || preAnalisis.equals("id")
           || preAnalisis.equals("num")
           || preAnalisis.equals("num.num")
           || preAnalisis.equals("literal")){
            Termino();
            ExpresionP();
        }
        else
            error("[Condicion] Se esperaba ( | id | num | num.num | literal) en la línea\n"+cmp.be.preAnalisis.numLinea);
    }
    
    private void ExpresionP(){
        if (preAnalisis.equals("opsuma")){
            //Expresion' -> opsuma Termino Expresion' | ε
            emparejar("opsuma");
            Termino();
            ExpresionP();
        }
        else 
            return;
    }
    
    private void Termino(){
        /*Termino -> Factor Termino'
          Primeros(Factor)
          Factor -> ( Expresion ) | id  Factor’ | num | num.num | literal*/
        if (preAnalisis.equals("(")
           || preAnalisis.equals("id")
           || preAnalisis.equals("num")
           || preAnalisis.equals("num.num")
           || preAnalisis.equals("literal")){
            Factor();
            TerminoP();
        }
        else
            error("[Termino] Se esperaba ( | id | num | num.num | literal en la línea\n"+cmp.be.preAnalisis.numLinea);
    }
    
    private void TerminoP(){
        if (preAnalisis.equals("opmult")){
            //Termino -> opmult Factor Termino' | ε
            emparejar("opmult");
            Factor();
            TerminoP();
        }
        else
            return;
    }
    
    private void Factor(){
        /*   Factor → ( Expresion ) | id  Factor’ | num | num.num | literal  */
        if (//Factor -> (Expresion)
            preAnalisis.equals("(")){
            emparejar("(");
            Expresion();
            emparejar(")");
        }
        //Factor -> id Factor'
        else if (preAnalisis.equals("id")){
            emparejar("id");
            FactorP();
        }
        //Factor -> num
        else if (preAnalisis.equals("num"))
            emparejar("num");
        //Factor -> num.num
        else if (preAnalisis.equals("num.num"))
            emparejar("num.num");
        //Factor -> literal
        else if (preAnalisis.equals("literal"))
            emparejar("literal");
        else
            error("[Factor] Se esperaba ( | id | num | num.num | literal en la línea\n"+cmp.be.preAnalisis.numLinea);
    }
    
    private void FactorP(){
        //Factor' -> (Argumentos) | ε
        if (preAnalisis.equals("(")){
            emparejar("(");
            Argumentos();
            emparejar(")");
        }
        else
            return;
    }
}
//------------------------------------------------------------------------------
//::