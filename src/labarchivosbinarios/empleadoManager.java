/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package labarchivosbinarios;

/**
 *
 * @author andre
 */
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import javax.swing.JOptionPane;
public class empleadoManager {
    
    private RandomAccessFile rcods, remps;
    public static ArrayList<String>array=new ArrayList<>();
    public static String print;
    public empleadoManager(){
    try{
        //1-Asegurar que el folder company exista.
        File f= new File("company");
        f.mkdir();
        //2- Instanciar los RAFs dentro del folder company.
        rcods= new RandomAccessFile("company/codigos.emp","rw");
        remps= new RandomAccessFile("company/empleado.emp","rw");
        //3- Inicializar el archivo de codigos si es nuevo.
        initCode(); 
    }catch(IOException e){
        System.out.println("No deberia de pasar esto!");
    }
    
    }
    
    private void initCode()throws IOException{
        if(rcods.length()==0){
            rcods.writeInt(1);
        }
    }
    
    private int getCode()throws IOException{
        //seek(int);
        //getFilePoiner();
        
        rcods.seek(0);
        int code=rcods.readInt();
        rcods.seek(0);
        rcods.writeInt(code+1);
        return code;
    }
    
    public void addEmployee(String name,double salary)throws IOException{
        /*
        
        Formato de Empleados.emp
        
        codigo- int
        nombre- String
        salario- double
        fecha de Contratacion- Fecha de momento - long.
        fecha de Despido- Fecha del momento de despido- long.
        
        */
        remps.seek(remps.length());
        //Codigo.
        int codigo=getCode();
        remps.writeInt(codigo);
        //Nombre.
        remps.writeUTF(name);
        //Salario.
        remps.writeDouble(salary);
        //Fechade contratacion.
        long fecha=Calendar.getInstance().getTimeInMillis();
        remps.writeLong(fecha);
        //Fecha de despedida.
        remps.writeLong(0);
        //Asegurar sus archivos individuales.
        createEmployeeFolder(codigo);
    }
    private String employeeFolder(int code){
        return "company/empleado"+code;
    }
    
    private void createEmployeeFolder(int code)throws IOException{
        //Crear folder del Empleado + code.
        File folder= new File(employeeFolder(code));
        folder.mkdir();
        //Crear los archivos de venta del empleado.
        createYearSaleFileFor(code);
    }
    
    private RandomAccessFile salesFileFor(int code)throws IOException{
        String dirPadre=employeeFolder(code);
        int yearActual=Calendar.getInstance().get(Calendar.YEAR);
        String path=dirPadre+"/ventas"+yearActual+".emp";
        return new RandomAccessFile(path,"rw");
    }
    
    private void createYearSaleFileFor(int code)throws IOException{
        RandomAccessFile ryear= salesFileFor(code);
        /*
        Formato VentasYear.emp
        
        ventas - Double.
        estado - Boolean
        
        */
        if(ryear.length()==0){
            for(int mes=0;mes<12;mes++){ 
               ryear.writeBoolean(false);
               ryear.writeDouble(0);
            }
        }
    }
    
    public void employeeList()throws IOException{
        array.clear();
        remps.seek(0);
        while(remps.getFilePointer()<remps.length()){
            int codigo=remps.readInt();
            String nombre=remps.readUTF();
            double Salario=remps.readDouble();
            Date fechaContratacion = new Date(remps.readLong());
            
            if(remps.readLong()==0){
                array.add("Code : "+codigo+" Nombre : "+nombre+" Salario : L."+Salario+" Fecha de Contratacion: "+fechaContratacion+"\n");
            }
          
        }
    }
    
    private boolean fired(int code) throws IOException{
        remps.seek(0);
        while (remps.getFilePointer()<remps.length()){
            int codigo=remps.readInt();
            long posicionPuntero=remps.getFilePointer();
            remps.readUTF();
            remps.skipBytes(16);
            if (remps.readLong()==0 && codigo==code) {
                remps.seek(posicionPuntero);
                return true;
            }
        }
        return false;
    }

    public void fireEmployee(int code) throws IOException{
        if(fired(code)){
            String name=remps.readUTF();
            remps.skipBytes(16);
            remps.writeLong(new Date().getTime());
            System.out.println("Empleado despedido : " + name);
            JOptionPane.showMessageDialog(null, "Empleado despedido correctamente T_T");
        }else{
            System.out.println("Este empleado ya fue despedido antes");
            JOptionPane.showMessageDialog(null, "no se puede despedir a un empleado despedido!");
        }
    }
    
    public void addSalesToEmployee(int code,double sales)throws IOException{
        if(fired(code)){
        RandomAccessFile sYear=salesFileFor(code);
        sYear.seek(0);
        
        int pos=0;
        for(int i=0;i<Calendar.getInstance().get(Calendar.MONTH);i++){
            sYear.skipBytes(9);
            pos+=9;
        }
        if(sYear.readBoolean()==false){
            sYear.seek(pos);
            sYear.readBoolean();
            long pointer = sYear.getFilePointer();
            double acumulador = sYear.readDouble();
            sYear.seek(pointer);
            sYear.writeDouble(sales+acumulador);
            JOptionPane.showMessageDialog(null, "Se ha agregado la venta correctamente!");
        }else{
            JOptionPane.showMessageDialog(null, "no se le pueden agregar mas ventas a este empleado porque ya fue pagado!");
        }
        
        }else{
         JOptionPane.showMessageDialog(null, "Este empleado fue despedido, no se puede ejecuctar esta accion con este usuario");   
        }
   }
     
    public void payEmployee(int code) throws IOException {
        if(fired(code)){
        Date fecha=new Date();
        RandomAccessFile PFiles=billsFilefor(code);
        RandomAccessFile rYear=salesFileFor(code);
        rYear.seek(0);
        int posicion=0;
        for (int i=0;i<Calendar.getInstance().get(Calendar.MONTH);i++){
            rYear.skipBytes(9);
            posicion+=9;
        }
        if(isEmployedpayed(code)==false){
            rYear.seek(posicion);
            rYear.writeBoolean(true);
            double amountVenta=rYear.readDouble();
            double salary=salary(code);
            PFiles.seek(PFiles.length());
            PFiles.writeLong(fecha.getTime());
            long pointer = PFiles.getFilePointer();
            PFiles.writeDouble(salary+(amountVenta*0.10));
            PFiles.seek(pointer);
            double sueldo = PFiles.readDouble();
            long point = PFiles.getFilePointer();
            PFiles.writeDouble(salary*0.35);
            PFiles.seek(point);
            double deducc = PFiles.readDouble();
            
            remps.seek(0);
            String name="";
            while (remps.getFilePointer()<remps.length()){
            int codigo=remps.readInt();
            String nombreactual=remps.readUTF();
            remps.skipBytes(24);
            if (codigo==code) {
                name=nombreactual;
            }
            }
            
            PFiles.writeInt(Calendar.getInstance().get(Calendar.YEAR));
            PFiles.writeInt(Calendar.getInstance().get(Calendar.MONTH));
            JOptionPane.showMessageDialog(null, "El empleado "+name+" se le ha pagado "+(sueldo-deducc));
            JOptionPane.showMessageDialog(null, "Pago efectuado correctamente!");
        }else{
            JOptionPane.showMessageDialog(null, "Ya se le habia pagado antes");
        }
        }else{
        JOptionPane.showMessageDialog(null, "Este empleado fue despedido, no se puede ejecuctar esta accion con este usuario");           
                }
    }

    private RandomAccessFile billsFilefor(int code) throws IOException {
        String EmployeeFolder=employeeFolder(code);
        String path=EmployeeFolder+"/recibo.emp";
        return new RandomAccessFile(path, "rw");
    }
    
    private double salary(int code) throws IOException {
        if(fired(code)){
        remps.seek(0);
        double salario=0;
        while(remps.getFilePointer()<remps.length()){
            int codigo=remps.readInt();
            remps.readUTF();
            double salary=remps.readDouble();
            remps.skipBytes(16);
            if(codigo==code){
                salario=salary;
            }
        }
        return salario;
        }else{
        JOptionPane.showMessageDialog(null, "Este empleado fue despedido, no se puede ejecuctar esta accion con este usuario"); 
        return 0;
        }
    }
    public boolean isEmployedpayed(int code) throws IOException{
        RandomAccessFile rYear=salesFileFor(code);
        rYear.seek(0);
        for (int i=0;i<Calendar.getInstance().get(Calendar.MONTH);i++){
            rYear.skipBytes(9);
        }
        return rYear.readBoolean();
    }
}
