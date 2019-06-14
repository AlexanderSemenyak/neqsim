/*
 * Copyright 2018 ESOL.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package neqsim.thermodynamicOperations.phaseEnvelopeOps.multicomponentEnvelopeOps;

import Jama.*;
import neqsim.MathLib.nonLinearSolver.newtonRhapson;
import neqsim.thermo.system.SystemInterface;
import static neqsim.thermodynamicOperations.phaseEnvelopeOps.multicomponentEnvelopeOps.pTphaseEnvelope.logger;
import org.apache.log4j.Logger;

public class sysNewtonRhapsonPhaseEnvelope extends Object implements java.io.Serializable {

    private static final long serialVersionUID = 1000;
    static Logger logger = Logger.getLogger(sysNewtonRhapsonPhaseEnvelope.class);

    double sumx = 0, sumy = 0;
    int neq = 0, iter = 0, iter2 = 0  ;
    int ic02p = -100, ic03p = -100, testcrit = 0, npCrit = 0;
    double beta = 0, ds = 0, dTmax = 10, dPmax =  10, TC1 = 0, TC2 = 0, PC1 = 0, PC2 = 0, specVal = 0.0;
    int  lc=0, hc=0;
    double sumlnKvals;
    Matrix Jac;
    Matrix fvec;
    Matrix u;
    Matrix uold;
    Matrix uolder;
    Matrix ucrit;
    Matrix ucritold;
    Matrix Xgij;
    SystemInterface system;
    int numberOfComponents;
    int speceq = 0;
    Matrix a = new Matrix(4, 4);
    Matrix dxds = null;
    Matrix s = new Matrix(1, 4);
    Matrix xg;
    Matrix xcoef;
    newtonRhapson solver;
    boolean etterCP = false;
    boolean etterCP2 = false;
    boolean calcCP = false;
    boolean ettercricoT = false;
    Matrix xcoefOld;
    double sign = 1.0;
    double dt; 
    double dp;
    double norm;
    double vol= Math.pow(10, 5);
    double volold= Math.pow(10, 5);
    double volold2= Math.pow(10, 5);
                        
    public sysNewtonRhapsonPhaseEnvelope() {
    }


    public sysNewtonRhapsonPhaseEnvelope(SystemInterface system, int numberOfPhases, int numberOfComponents) {
        this.system = system;
        this.numberOfComponents = numberOfComponents;
        neq = numberOfComponents + 2;
        Jac = new Matrix(neq, neq); //this is the jacobian of the system of equations
        fvec = new Matrix(neq, 1);  //this is the system of equations
        u = new Matrix(neq, 1);     //this is the vector of variables 
        Xgij = new Matrix(neq, 4);
        setu();
        uold = u.copy();
        findSpecEqInit();
        solver = new newtonRhapson();
        solver.setOrder(3);
    }
  
    public void setfvec2() {
        for (int i = 0; i < numberOfComponents; i++) {
            fvec.set(i, 0,
                    Math.log(
                    system.getPhase(0).getComponents()[i].getFugasityCoeffisient() * system.getPhase(0).getComponents()[i].getx())
                    - Math.log(
                    system.getPhase(1).getComponents()[i].getFugasityCoeffisient() * system.getPhase(1).getComponents()[i].getx()));
        }
        double fsum = 0.0;
        for (int i = 0; i < numberOfComponents; i++) {
            fsum += system.getPhase(0).getComponents()[i].getx() - system.getPhase(1).getComponents()[i].getx();
        }
        fvec.set(numberOfComponents, 0, fsum);
        fvec.set(numberOfComponents, 0, sumy - sumx);
        fvec.set(numberOfComponents + 1, 0, u.get(speceq, 0) - specVal);

    }

    public void setfvec() {
        for (int i = 0; i < numberOfComponents; i++) {
            fvec.set(i, 0, u.get(i, 0) + system.getPhase(0).getComponents()[i].getLogFugasityCoeffisient() - system.getPhase(1).getComponents()[i].getLogFugasityCoeffisient());
        }
        fvec.set(numberOfComponents, 0, sumy - sumx);
        fvec.set(numberOfComponents + 1, 0, u.get(speceq, 0) - specVal);
    }

    public void findSpecEqInit() {
        speceq = 0;
        int speceqmin = 0;
        
        system.getPhase(0).getComponents()[numberOfComponents-1].getPC();
        system.getPhase(0).getComponents()[numberOfComponents-1].getTC();
        system.getPhase(0).getComponents()[numberOfComponents-1].getAcentricFactor();
        
        for (int i = 0; i < numberOfComponents; i++) {
            if (system.getPhase(0).getComponents()[i].getTC() > system.getPhase(0).getComponents()[speceq].getTC()) {
                speceq = system.getPhase(0).getComponents()[i].getComponentNumber();
                specVal = u.get(i, 0);
                hc=i ;
            }
            if (system.getPhase(0).getComponents()[i].getTC() < system.getPhase(0).getComponents()[speceqmin].getTC()) {
                speceqmin = system.getPhase(0).getComponents()[i].getComponentNumber();
                lc=i ;
            }
                        
        }
        
    }

    public void findSpecEq() {
        double max = 0;
        double max2=0. ;        
        int speceq2=0 ;
         
        
        for (int i = 0; i < numberOfComponents + 2; i++) {
            double testVal = Math.abs((Math.exp(u.get(i, 0)) - Math.exp(uold.get(i, 0))) / Math.exp(uold.get(i, 0)));
            
            // the most sensitive variable is calculated not by the difference, 
            // but from the sensibility vector in the original Michelsen code
            
            if (testVal > max) {
                speceq = i;
                specVal = u.get(i, 0);
                max = testVal;
            }
            
            testVal = Math.abs(dxds.get(i, 0));

            if (testVal > max2) {
                speceq2 = i;
                double specVal2 = u.get(i, 0);
                max2 = testVal;
            }            
        }
           
        if ( Double.isNaN(dxds.get(1,0))) {
            speceq2 =  numberOfComponents + 3 ; 
        }
        
        if (speceq != speceq2 ) {
            speceq = speceq2 ;
        }        

    }

    public void useAsSpecEq(int i) {
        speceq = i;
        specVal = u.get(i, 0);
        System.out.println("Enforced Scec Variable" + speceq + "  " + specVal);
        
    }

    public final void calc_x_y() {
        sumx = 0;
        sumy = 0;
        for (int j = 0; j < system.getNumberOfPhases(); j++) {
            for (int i = 0; i < system.getPhase(0).getNumberOfComponents(); i++) {
                if (j == 0) {
                    sumy += system.getPhase(j).getComponents()[i].getK() * system.getPhase(j).getComponents()[i].getz() / (1.0 - system.getBeta(0) + system.getBeta(0) * system.getPhase(0).getComponents()[i].getK());
                }
                if (j == 1) {
                    sumx += system.getPhase(0).getComponents()[i].getz() / (1.0 - system.getBeta(0) + system.getBeta(0) * system.getPhase(0).getComponents()[i].getK());
                }
            }
        }
    }

    public void setJac2() {
        Jac.timesEquals(0.0);
        double dij = 0.0;
        double[] dxidlnk = new double[numberOfComponents];
        double[] dyidlnk = new double[numberOfComponents];
        double tempJ = 0.0;
        for (int i = 0; i < numberOfComponents; i++) {
            dxidlnk[i] = -system.getBeta() * system.getPhase(1).getComponents()[i].getx() * system.getPhase(0).getComponents()[i].getx() / system.getPhase(0).getComponents()[i].getz();
            dyidlnk[i] = system.getPhase(0).getComponents()[i].getx() + system.getPhase(1).getComponents()[i].getK() * dxidlnk[i];
        }
        for (int i = 0; i < numberOfComponents; i++) {
            double dlnxdlnK = -1.0 / (1.0 + system.getBeta() * system.getPhase(0).getComponents()[i].getK() - system.getBeta()) * system.getBeta() * system.getPhase(0).getComponents()[i].getK();
            double dlnydlnK = 1.0 - 1.0 / (system.getPhase(0).getComponents()[i].getK() * system.getBeta() + 1 - system.getBeta()) * system.getBeta() * system.getPhase(0).getComponents()[i].getK();
            for (int j = 0; j < numberOfComponents; j++) {
                dij = i == j ? 1.0 : 0.0;//Kroneckers deltaget
                tempJ = -dij + dij * dlnydlnK - dij * dlnxdlnK;
                Jac.set(i, j, tempJ);
            }
            tempJ = system.getTemperature() * (system.getPhase(0).getComponents()[i].getdfugdt() - system.getPhase(1).getComponents()[i].getdfugdt());
            Jac.set(i, numberOfComponents, tempJ);
            tempJ = system.getPressure() * (system.getPhase(0).getComponents()[i].getdfugdp() - system.getPhase(1).getComponents()[i].getdfugdp());
            Jac.set(i, numberOfComponents + 1, tempJ);
            Jac.set(numberOfComponents, i, dyidlnk[i] - dxidlnk[i]);
        }
        Jac.set(numberOfComponents + 1, speceq, 1.0);
    }

    public void setJac() {
        Jac.timesEquals(0.0);
        double dij = 0.0;
        double[] dxidlnk = new double[numberOfComponents];
        double[] dyidlnk = new double[numberOfComponents];
        double tempJ = 0.0;
        for (int i = 0; i < numberOfComponents; i++) {

            dxidlnk[i] = -system.getPhase(1).getComponents()[i].getz() * Math.pow(system.getPhase(0).getComponents()[i].getK() * system.getBeta() + 1.0 - system.getBeta(), -2.0) * system.getBeta() * system.getPhase(1).getComponents()[i].getK();
            dyidlnk[i] = system.getPhase(1).getComponents()[i].getz() / (system.getPhase(0).getComponents()[i].getK() * system.getBeta() + 1.0 - system.getBeta()) * system.getPhase(1).getComponents()[i].getK()
                    - system.getPhase(0).getComponents()[i].getK() * system.getPhase(1).getComponents()[i].getz() / Math.pow(1.0 - system.getBeta() + system.getBeta() * system.getPhase(0).getComponents()[i].getK(), 2.0) * system.getBeta() * system.getPhase(0).getComponents()[i].getK();

        }
        for (int i = 0; i < numberOfComponents; i++) {
            for (int j = 0; j < numberOfComponents; j++) {
                dij = i == j ? 1.0 : 0.0;//Kroneckers delta
                tempJ = dij + system.getPhase(0).getComponents()[i].getdfugdx(j) * dyidlnk[j] - system.getPhase(1).getComponents()[i].getdfugdx(j) * dxidlnk[j];
                Jac.set(i, j, tempJ);
            }
            tempJ = system.getTemperature() * (system.getPhase(0).getComponents()[i].getdfugdt() - system.getPhase(1).getComponents()[i].getdfugdt());
            Jac.set(i, numberOfComponents, tempJ);
            tempJ = system.getPressure() * (system.getPhase(0).getComponents()[i].getdfugdp() - system.getPhase(1).getComponents()[i].getdfugdp());
            Jac.set(i, numberOfComponents + 1, tempJ);
            Jac.set(numberOfComponents, i, dyidlnk[i] - dxidlnk[i]);
            Jac.set(numberOfComponents + 1, i, 0.0);
        }
        Jac.set(numberOfComponents + 1, speceq, 1.0);
    }

    public void setu() {
        for (int i = 0; i < numberOfComponents; i++) {
            u.set(i, 0, Math.log(system.getPhase(0).getComponents()[i].getK()));
        }
        u.set(numberOfComponents, 0, Math.log(system.getTemperature()));
        u.set(numberOfComponents + 1, 0, Math.log(system.getPressure()));
    }

    public void init() {
        for (int i = 0; i < numberOfComponents; i++) {
            system.getPhase(0).getComponents()[i].setK(Math.exp(u.get(i, 0)));
            system.getPhase(1).getComponents()[i].setK(Math.exp(u.get(i, 0)));
        }
        system.setTemperature(Math.exp(u.get(numberOfComponents, 0)));
        system.setPressure(Math.exp(u.get(numberOfComponents + 1, 0)));

        calc_x_y();
        system.calc_x_y();
        system.init(3);
        
    }

    public void calcInc(int np) {
        // First we need the sensitivity vector dX/dS
        // calculates the sensitivity vector and stores the xgij matrix
        
        init();

        if (np < 5) {
            //for the first five points use as spec eq the nc+1 
            //which is pressure
            
            setu();      
            speceq = numberOfComponents + 1;
            specVal = u.get(speceq, 0);
            setJac();
            fvec.timesEquals(0.0);
            fvec.set(speceq, 0, 1.0);
            dxds = Jac.solve(fvec);
            double dp = 0.1;
            ds = dp / dxds.get(numberOfComponents + 1, 0);
            
            Xgij.setMatrix(0, numberOfComponents + 1, np - 1, np - 1, u.copy());
            u.plusEquals(dxds.times(ds));
            specVal = u.get(speceq, 0);

        } else {
            //for the rest of the points use as spec eq the most sensitive variable
            int speceqOld= speceq ;
            findSpecEq(); 
            if (speceq ==  numberOfComponents + 3){
                speceq=speceqOld;
            }  
            
            int intsign = (int) Math.round(Math.round(dxds.get(speceq, 0)*100000000)); 
            int sign1 = Integer.signum(intsign);    
            ds = sign1 * ds;
            
            setfvec();
            setJac();
            fvec.timesEquals(0.0);
            fvec.set(numberOfComponents + 1, 0, 1.0);
            
            //calculate the dxds of the system
            dxds = Jac.solve(fvec);
            
            //check for critical point
            
            //check density
            double densV = system.getPhase(0).getDensity();
            double densL = system.getPhase(1).getDensity(); 
            //check the proximity to the critical point by addind the lnKs and finding the highest                 
            double Kvallc = system.getPhase(0).getComponent(lc).getx()/system.getPhase(1).getComponent(lc).getx();
            double Kvalhc = system.getPhase(0).getComponent(hc).getx()/system.getPhase(1).getComponent(hc).getx();             
            
            if ( (etterCP == false) ) {
                if (Kvallc < 1.05 && Kvalhc > 0.95){   
                    calcCP = true;
                    etterCP = true;
                    npCrit=np;       
                    system.invertPhaseTypes();
                    System.out.println("critical point");
                }
            } 
           
            //manipulate stepsize according to the number of iterations of the previous point
            if (iter > 6) {
                ds *= 0.5;      
            } else {
                if (iter < 3) {
                    ds *= 1.1;              
                }
                if (iter == 3) {
                    ds *= 1.0;
                }
                if (iter == 4) {
                    ds *= 0.9;
                }
                if (iter > 4) {
                    ds *= 0.7;
                }
            }

            // Now we check wheater this ds is greater than dTmax and dPmax.
            intsign = (int) Math.round(Math.round(ds*100000000)); 
            int sign2 = Integer.signum(intsign);    
                        
            if ((1 + dTmax / system.getTemperature()) < Math.exp(dxds.get(numberOfComponents, 0) * ds)) {
                //logger.info("too high dT");
                ds = Math.log(1 + dTmax / system.getTemperature()) / dxds.get(numberOfComponents, 0);
            }else if ((1 - dTmax / system.getTemperature()) > Math.exp(dxds.get(numberOfComponents, 0) * ds)) {
                //logger.info("too low dT");
                ds = Math.log(1 - dTmax / system.getTemperature()) / dxds.get(numberOfComponents, 0);
            } else if ((1 + dPmax / system.getPressure()) < Math.exp(dxds.get(numberOfComponents + 1, 0) * ds)) {
                //logger.info("too low dP");
                ds = Math.log(1 + dPmax / system.getPressure()) / dxds.get(numberOfComponents +1, 0);
            } else if ((1 - dPmax / system.getPressure()) > Math.exp(dxds.get(numberOfComponents + 1, 0) * ds)) {
                //logger.info("too low dP");
                ds = Math.log(1 - dPmax / system.getPressure()) / dxds.get(numberOfComponents +1, 0);
            }
            ds = sign2 * Math.abs(ds);
           
                  
            //store the values of the solution for the last 5 iterations 
            Xgij.setMatrix(0, numberOfComponents + 1, 0, 2, Xgij.getMatrix(0, numberOfComponents + 1, 1, 3));
            Xgij.setMatrix(0, numberOfComponents + 1, 3, 3, u.copy());
            
            s.setMatrix(0, 0, 0, 3, Xgij.getMatrix(speceq, speceq, 0, 3));
            
            //calculate next u
            calcInc2(np);
                    
        }
    //since you are calculating the next point the previous iterations should be zero
    iter = 0;  
    iter2= 0;  
    }    
    
    public void calcInc2(int np) {
        
        // Here we calcualte the estimate of the next point from the polynomial.
        for (int i = 0; i < 4; i++) {
            a.set(i, 0, 1.0);
            a.set(i, 1, s.get(0, i));
            a.set(i, 2, s.get(0, i) * s.get(0, i));
            a.set(i, 3, a.get(i, 2) * s.get(0, i));
        }

        // finds the estimate of the next point of the envelope that corresponds 
        // to the most specified equation
        double sny;
        sny = ds * dxds.get(speceq, 0) + s.get(0, 3);
        specVal = sny;
        
        // finds the estimate of the next point of the envelope that corresponds 
        // to all the equations 
        for (int j = 0; j < neq; j++) {
            xg = Xgij.getMatrix(j, j, 0, 3);
            try {
                xcoef = a.solve(xg.transpose());
            } catch (Exception e) {
                xcoef = xcoefOld.copy();
            }
            u.set(j, 0, xcoef.get(0, 0) + sny * (xcoef.get(1, 0) + sny * (xcoef.get(2, 0) + sny * xcoef.get(3, 0))));
        }
        xcoefOld = xcoef.copy();     
    }

    public void calcCrit() {
        //calculates the critical point based on interpolation polynomials
        Matrix aa=a.copy();
        Matrix ss=s.copy();
        Matrix xx=Xgij.copy();
        Matrix uu=u.copy();
                
        // Here we calcualte the estimate of the next point from the polynomial.
        for (int i = 0; i < 4; i++) {
            a.set(i, 0, 1.0);
            a.set(i, 1, s.get(0, i));
            a.set(i, 2, s.get(0, i) * s.get(0, i));
            a.set(i, 3, a.get(i, 2) * s.get(0, i));
        }

        double sny;
        sny = 0.;
        try{        
        for (int j = 0; j < neq; j++) {
            xg = Xgij.getMatrix(j, j, 0, 3);
            try {
                xcoef = a.solve(xg.transpose());
            } catch (Exception e) {
                xcoef = xcoefOld.copy();
            }
            u.set(j, 0, xcoef.get(0, 0) + sny * (xcoef.get(1, 0) + sny * (xcoef.get(2, 0) + sny * xcoef.get(3, 0))));
            
        }
        }catch (Exception e) {
            logger.error("error",e);
        }
        
        system.setTC(Math.exp(u.get(numberOfComponents, 0)));
        system.setPC(Math.exp(u.get(numberOfComponents+1, 0)));

        a=aa.copy();
        s=ss.copy();
        Xgij=xx.copy();
        u=uu.copy();        
        
    }
    
    public int getNpCrit() {
        return npCrit;
    }

    public double sign(double a, double b) {
        a = Math.abs(a);
        b = b >= 0 ? 1.0 : -1.0;
        return a * b;
    }

    public void solve(int np) {
        //this method actually solves the phase evnelope point
        Matrix dx;
        double dxOldNorm = 1e10;

        do {
            iter++;
            init();
            setfvec();
            setJac();

            dx = Jac.solve(fvec);
            u.minusEquals(dx);
            
            if (Double.isNaN(dx.norm2()) || Double.isInfinite(dx.norm2())) {
                if (iter2 >= 15 ){
                    //deliberate crush
                    ds =0./0.;
                    u.set(numberOfComponents, 0, ds);
                    u.set(numberOfComponents+1, 0, ds);
                }                
                //if the norm is NAN reduce step and try again
                //logger.info("Double.isNaN(dx.norm2())........");
                iter2 ++ ;
                u = uold.copy();
                ds *= 0.5;
                calcInc2(np);
                solve(np);  
            }else if (dxOldNorm < dx.norm2() ) {
                if (iter2 ==0){
                    uolder = uold.copy();
                }
                if (iter2 >= 15 ){
                    //deliberate crush                    
                    ds =0./0.;
                    u.set(numberOfComponents, 0, ds);
                    u.set(numberOfComponents+1, 0, ds);
                } 
                //if the norm does not reduce there is a danger of entering trivial solution
                // reduce step and try again to avoid it
                //logger.info("dxOldNorm < dx.norm2()");
                iter2 ++ ;
                u = uold.copy();
                ds *= 0.5;
                calcInc2(np);
                solve(np);     
            } 
         
            if (Double.isNaN(dx.norm2())){
                norm = 1e10;
            }else{
                norm = dx.norm2(); 
                dxOldNorm = norm;           
            }
           
        } while ( norm > 1.e-5  ) ;
    
        init();
        findSpecEq();
        
        //check density for direction
        volold2=volold;
        volold=vol;
        vol = system.getPhase(0).getMolarVolume();
        uold = u.copy();

        
        if (volold < vol){
            /*
            volold=volold2;
            ds=-ds;
            u = uold.copy();
            calcInc2(np);
            solve(np);               
            */
        }
        
        
        try{
            Matrix utest = u.copy();  
        }catch (Exception e0){
            double nef=0.;
        }
        
    }
    public static void main(String args[]) {
        /*
         * sysNewtonRhapson test=new sysNewtonRhapson(); double[] constants =
         * new double[]{0.4,0.4}; test.setx(constants); while
         * (test.nonsol()>1.0e-8) { constants=test.getx();
         * logger.info(constants[0]+" "+constants[1]); } test.nonsol();
         * constants=test.getf(); logger.info(constants[0]+"
         * "+constants[1]); System.exit(0);
         */ }

}
