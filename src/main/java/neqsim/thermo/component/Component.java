/*
 * Component.java
 *
 * Created on 8. april 2000, 23:28
 */
package neqsim.thermo.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import neqsim.thermo.ThermodynamicConstantsInterface;
import neqsim.thermo.atomElement.Element;
import neqsim.thermo.component.atractiveEosTerm.AtractiveTermInterface;
import neqsim.thermo.phase.PhaseInterface;
import neqsim.util.database.NeqSimDataBase;

abstract class Component implements ComponentInterface {
    private static final long serialVersionUID = 1000;

    double[] surfTensInfluenceParam = {0.28367, -0.05164, -0.81594, 1.06810, -1.1147};
    protected int index, componentNumber, atractiveTermNumber = 0, numberOfAssociationSites = 0;
    protected double logFugasityCoeffisient = 0.0, associationVolume = 0.0, associationEnergy = 0.0,
            aCPA = 0.0, bCPA = 0.0, mCPA = 0.0, srkacentricFactor = 0.0;
    protected String componentName = "default", referenceStateType = "solvent",
            associationScheme = "0", antoineLiqVapPresType = null;
    private String formulae = "", CASnumber = "";
    protected Element elements = null;
    protected boolean isTBPfraction = false, isPlusFraction = false, isNormalComponent = true,
            isIon = false;
    private boolean isHydrateFormer = false;
    private boolean waxFormer = false;
    private String componentType = "Component";
    protected double qPure = 0, voli = 1.0;
    protected int calcActivity = 1;
    protected boolean solidCheck = false;
    protected double dqPuredT = 0, dqPuredTdT = 0;
    private double racketZCPA = 0;
    private double criticalCompressibilityFactor = 0.0;
    private double volumeCorrectionConst = 0.0, volumeCorrectionT = 0.0,
            volumeCorrectionT_CPA = 0.0;
    protected double criticalPressure, criticalTemperature, molarMass, acentricFactor,
            numberOfMoles = 0.0, numberOfMolesInPhase = 0.0, normalLiquidDensity = 0;
    protected double reducedPressure, reducedTemperature, fugasityCoeffisient,
            debyeDipoleMoment = 0, viscosityCorrectionFactor = 0, criticalVolume = 0, racketZ = 0;
    protected double gibbsEnergyOfFormation = 0, criticalViscosity = 0.0;
    protected double referencePotential = 0, viscosityFrictionK = 1.0;
    protected int liquidViscosityModel = 0;
    protected int ionicCharge = 0;
    private double referenceEnthalpy = 0.0;
    protected double parachorParameter = 0.0, normalBoilingPoint = 0, sphericalCoreRadius = 0.384,
            standardDensity = 0, AntoineASolid = 0.0, AntoineBSolid = 0.0, AntoineCSolid = 0.0;
    protected double[] liquidViscosityParameter = new double[4];
    protected double[] liquidConductivityParameter = new double[3];
    protected double[] henryCoefParameter = new double[4];
    protected double[] dielectricParameter = new double[5];
    protected double[] schwartzentruberParams = new double[3], matiascopemanParams = new double[3],
            matiascopemanParamsPR = new double[3], TwuCoonParams = new double[3],
            matiascopemanSolidParams = new double[3];
    protected double lennardJonesMolecularDiameter = 0, lennardJonesEnergyParameter = 0,
            stokesCationicDiameter = 0, paulingAnionicDiameter = 0;
    protected double K, z;
    protected double x = 0;
    private int orginalNumberOfAssociationSites = 0;
    protected double dfugdt = 0.1, dfugdp = 0.1;
    protected double[] dfugdn = new double[MAX_NUMBER_OF_COMPONENTS];
    public double[] dfugdx = new double[MAX_NUMBER_OF_COMPONENTS];
    double AntoineA = 0, AntoineB = 0, AntoineC = 0, AntoineD = 0, AntoineE = 0;
    private double CpA = 100.0;
    private double CpB = 0;
    private double CpC = 0;
    private double CpD = 0;
    private double CpE = 0;
    private double[] CpSolid = new double[5];
    private double[] CpLiquid = new double[5];
    private double heatOfFusion = 0.0;
    double triplePointDensity = 10.0, triplePointPressure = 0.0;
    private double triplePointTemperature = 1000.0;
    double meltingPointTemperature = 110.0;
    private double idealGasEnthalpyOfFormation = 0.0;
    double idealGasGibsEnergyOfFormation = 0.0, idealGasAbsoluteEntropy = 0.0;
    double Hsub = 0.0;
    double[] solidDensityCoefs = new double[5];
    double[] liquidDensityCoefs = new double[5];
    double[] heatOfVaporizationCoefs = new double[5];
    protected double mSAFTi = 0;
    protected double sigmaSAFTi = 0;
    protected double epsikSAFT = 0;
    private double associationVolumeSAFT;
    private double associationEnergySAFT = 0;
    static Logger logger = LogManager.getLogger(Component.class);

    /**
     * <p>
     * Constructor for Component.
     * </p>
     */
    // Class methods
    public Component() {}

    /**
     * <p>
     * Constructor for Component.
     * </p>
     *
     * @param number a int
     * @param moles a double
     */
    public Component(int number, double moles) {
        numberOfMoles = moles;
    }

    /**
     * <p>
     * Constructor for Component.
     * </p>
     *
     * @param moles a double
     */
    public Component(double moles) {
        numberOfMoles = moles;
    }

    /**
     * <p>
     * Constructor for Component.
     * </p>
     *
     * @param number a int
     * @param TC a double
     * @param PC a double
     * @param M a double
     * @param a a double
     * @param moles a double
     */
    public Component(int number, double TC, double PC, double M, double a, double moles) {
        criticalPressure = PC;
        criticalTemperature = TC;
        molarMass = M;
        acentricFactor = a;
        numberOfMoles = moles;
    }

    /**
     * <p>
     * Constructor for Component.
     * </p>
     *
     * @param component_name a {@link java.lang.String} object
     * @param moles a double
     * @param molesInPhase a double
     * @param compnumber a int
     */
    public Component(String component_name, double moles, double molesInPhase, int compnumber) {
        createComponent(component_name, moles, molesInPhase, compnumber);
    }

    /** {@inheritDoc} */
    @Override
    public void insertComponentIntoDatabase(String databaseName) {
        databaseName = "comptemp";
        neqsim.util.database.NeqSimDataBase database = new neqsim.util.database.NeqSimDataBase();
        try {
            int isW = 0;
            if (isWaxFormer()) {
                isW = 1;
            }
            if (NeqSimDataBase.createTemporaryTables()) {
                database.execute("insert into comptemp VALUES (" + (1000 + componentNumber) + ", '"
                        + componentName + "', '00-00-0','" + getComponentType() + "', "
                        + (1000 + componentNumber) + ", 'HC', " + (molarMass * 1000.0) + ", "
                        + normalLiquidDensity + ", " + (getTC() - 273.15) + ", " + getPC() + ", "
                        + getAcentricFactor() + "," + (getNormalBoilingPoint() - 273.15)
                        + ", 39.948, 74.9, 'Classic', 0, " + getCpA() + ", " + getCpB() + ", "
                        + getCpC() + ", " + getCpD() + ", " + getCpE()
                        + ", 'log', 5.2012, 1936.281, -20.143, -1.23303, 1000, 1.8, 0.076, 0.0, 0.0, 2.52, 809.1, 0, 3, -24.71, 4210, 0.0453, -3.38e-005, -229000, -19.2905, 29814.5, -0.019678, 0.000132, -3.11e-007, 0, 'solvent', 0, 0, 0, 0, 0.0789, -1.16, 0, -0.384, 0.00525, -6.37e-006, 207, "
                        + getHeatOfFusion() + ", 1000, 0.00611, " + getTriplePointTemperature()
                        + ", " + getMeltingPointTemperature()
                        + ", -242000, 189, 53, -0.00784, 0, 0, 0, 5.46, 0.305, 647, 0.081, 0, 52100000, 0.32, -0.212, 0.258, 0, 0.999, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '0', 0, 0, 0, 0,0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'no', "
                        + getmSAFTi() + ", " + (getSigmaSAFTi() * 1e10) + ", " + getEpsikSAFT()
                        + ", 0, 0,0,0,0,0," + isW
                        + ",0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0)");
            }
            index = 1000 + componentNumber;
            CASnumber = "00-00-0";
        } catch (Exception e) {
            logger.error("error in inserting to database", e);
        } finally {
            try {
                if (database.getStatement() != null) {
                    database.getStatement().close();
                }
                if (database.getConnection() != null) {
                    database.getConnection().close();
                }
            } catch (Exception e) {
                logger.error("error closing database.....", e);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void createComponent(String component_name, double moles, double molesInPhase,
            int compnumber) {
        componentName = component_name;
        numberOfMoles = moles;
        numberOfMolesInPhase = molesInPhase;
        neqsim.util.database.NeqSimDataBase database = new neqsim.util.database.NeqSimDataBase();
        java.sql.ResultSet dataSet = null;
        try {
            if (!component_name.equals("default")) {
                try {
                    if (NeqSimDataBase.createTemporaryTables()) {
                        dataSet = database.getResultSet(
                                ("SELECT * FROM comptemp WHERE name='" + component_name + "'"));
                    } else {
                        dataSet = database.getResultSet(
                                ("SELECT * FROM comp WHERE name='" + component_name + "'"));
                    }
                    dataSet.next();
                    dataSet.getString("ID");
                    // if(dataSet.isAfterLast()) dataSet.next();
                } catch (Exception e) {
                    try {
                        dataSet.close();
                        // logger.info("no parameters in tempcomp -- trying comp.. " +
                        // component_name);
                        dataSet = database.getResultSet(
                                ("SELECT * FROM comp WHERE name='" + component_name + "'"));
                        dataSet.next();
                    } catch (Exception e2) {
                        throw new RuntimeException(e2);
                    }
                }

                setComponentType(dataSet.getString("comptype"));
                setCASnumber(dataSet.getString("CASnumber"));
                index = Integer.parseInt(dataSet.getString("compindex"));
                setFormulae(dataSet.getString("FORMULA").trim()); // C
                molarMass = Double.parseDouble(dataSet.getString("molarmass")) / 1000.0; // D
                normalLiquidDensity = Double.parseDouble(dataSet.getString("liqdens")); // E
                criticalTemperature = (Double.parseDouble(dataSet.getString("TC")) + 273.15); // F
                criticalPressure = Double.parseDouble(dataSet.getString("PC")); // G
                acentricFactor = Double.parseDouble(dataSet.getString("acsfact")); // J
                criticalVolume = Double.parseDouble(dataSet.getString("critvol"));
                criticalCompressibilityFactor = criticalPressure * criticalVolume
                        / ThermodynamicConstantsInterface.R / criticalTemperature / 10.0;
                referenceEnthalpy = Double.parseDouble(dataSet.getString("Href"));
                setCpA(dataSet.getDouble("CPA")); // R //S
                setCpB(dataSet.getDouble("CPB")); // S
                setCpC(dataSet.getDouble("CPC")); // T
                setCpD(dataSet.getDouble("CPD"));
                setCpE(dataSet.getDouble("CPE"));

                CpSolid[0] = Double.parseDouble(dataSet.getString("CPsolid1"));
                CpSolid[1] = Double.parseDouble(dataSet.getString("CPsolid2"));
                CpSolid[2] = Double.parseDouble(dataSet.getString("CPsolid3"));
                CpSolid[3] = Double.parseDouble(dataSet.getString("CPsolid4"));
                CpSolid[4] = Double.parseDouble(dataSet.getString("CPsolid5"));

                CpLiquid[0] = Double.parseDouble(dataSet.getString("CPliquid1"));
                CpLiquid[1] = Double.parseDouble(dataSet.getString("CPliquid2"));
                CpLiquid[2] = Double.parseDouble(dataSet.getString("CPliquid3"));
                CpLiquid[3] = Double.parseDouble(dataSet.getString("CPliquid4"));
                CpLiquid[4] = Double.parseDouble(dataSet.getString("CPliquid5"));

                antoineLiqVapPresType = dataSet.getString("AntoineVapPresLiqType");
                AntoineA = Double.parseDouble(dataSet.getString("ANTOINEA")); // AY
                AntoineB = Double.parseDouble(dataSet.getString("ANTOINEB")); // AZ
                AntoineC = Double.parseDouble(dataSet.getString("ANTOINEC")); // AX
                AntoineD = Double.parseDouble(dataSet.getString("ANTOINED"));
                AntoineE = Double.parseDouble(dataSet.getString("ANTOINEE"));

                if (AntoineA == 0) {
                    AntoineA = 1.0;
                    AntoineB = getNormalBoilingPoint();
                }

                AntoineASolid = Double.parseDouble(dataSet.getString("ANTOINESolidA"));
                AntoineBSolid = Double.parseDouble(dataSet.getString("ANTOINESolidB"));
                AntoineCSolid = Double.parseDouble(dataSet.getString("ANTOINESolidC"));

                debyeDipoleMoment = Double.parseDouble(dataSet.getString("dipolemoment"));
                normalBoilingPoint = Double.parseDouble(dataSet.getString("normboil"));
                standardDensity = Double.parseDouble(dataSet.getString("stddens"));
                viscosityCorrectionFactor = Double.parseDouble(dataSet.getString("viscfact")); // BC
                racketZ = Double.parseDouble(dataSet.getString("racketZ")); // BE
                lennardJonesMolecularDiameter = Double.parseDouble(dataSet.getString("LJdiameter")); // BF
                lennardJonesEnergyParameter = Double.parseDouble(dataSet.getString("LJeps"));
                sphericalCoreRadius = Double.parseDouble(dataSet.getString("SphericalCoreRadius"));
                liquidViscosityModel = Integer.parseInt(dataSet.getString("liqviscmodel"));
                liquidViscosityParameter[0] = Double.parseDouble(dataSet.getString("liqvisc1"));
                liquidViscosityParameter[1] = Double.parseDouble(dataSet.getString("liqvisc2"));
                liquidViscosityParameter[2] = Double.parseDouble(dataSet.getString("liqvisc3"));
                liquidViscosityParameter[3] = Double.parseDouble(dataSet.getString("liqvisc4"));

                gibbsEnergyOfFormation =
                        Double.parseDouble(dataSet.getString("gibbsEnergyOfFormation"));
                dielectricParameter[0] =
                        Double.parseDouble(dataSet.getString("dielectricParameter1"));
                dielectricParameter[1] =
                        Double.parseDouble(dataSet.getString("dielectricParameter2"));
                dielectricParameter[2] =
                        Double.parseDouble(dataSet.getString("dielectricParameter3"));
                dielectricParameter[3] =
                        Double.parseDouble(dataSet.getString("dielectricParameter4"));
                dielectricParameter[4] =
                        Double.parseDouble(dataSet.getString("dielectricParameter5"));

                ionicCharge = Integer.parseInt(dataSet.getString("ionicCharge"));

                referenceStateType = dataSet.getString("referenceStateType").trim();
                henryCoefParameter[0] = Double.parseDouble(dataSet.getString("HenryCoef1"));
                henryCoefParameter[1] = Double.parseDouble(dataSet.getString("HenryCoef2"));
                henryCoefParameter[2] = Double.parseDouble(dataSet.getString("HenryCoef3"));
                henryCoefParameter[3] = Double.parseDouble(dataSet.getString("HenryCoef4"));

                schwartzentruberParams[0] =
                        Double.parseDouble(dataSet.getString("schwartzentruber1"));
                schwartzentruberParams[1] =
                        Double.parseDouble(dataSet.getString("schwartzentruber2"));
                schwartzentruberParams[2] =
                        Double.parseDouble(dataSet.getString("schwartzentruber3"));

                matiascopemanParams[0] = Double.parseDouble(dataSet.getString("MC1"));
                matiascopemanParams[1] = Double.parseDouble(dataSet.getString("MC2"));
                matiascopemanParams[2] = Double.parseDouble(dataSet.getString("MC3"));

                matiascopemanParamsPR[0] = Double.parseDouble(dataSet.getString("MCPR1"));
                matiascopemanParamsPR[1] = Double.parseDouble(dataSet.getString("MCPR2"));
                matiascopemanParamsPR[2] = Double.parseDouble(dataSet.getString("MCPR3"));

                matiascopemanSolidParams[0] = Double.parseDouble(dataSet.getString("MC1Solid"));
                matiascopemanSolidParams[1] = Double.parseDouble(dataSet.getString("MC2Solid"));
                matiascopemanSolidParams[2] = Double.parseDouble(dataSet.getString("MC3Solid"));

                TwuCoonParams[0] = Double.parseDouble(dataSet.getString("TwuCoon1"));
                TwuCoonParams[1] = Double.parseDouble(dataSet.getString("TwuCoon2"));
                TwuCoonParams[2] = Double.parseDouble(dataSet.getString("TwuCoon3"));

                liquidConductivityParameter[0] =
                        Double.parseDouble(dataSet.getString("liquidConductivity1"));
                liquidConductivityParameter[1] =
                        Double.parseDouble(dataSet.getString("liquidConductivity2"));
                liquidConductivityParameter[2] =
                        Double.parseDouble(dataSet.getString("liquidConductivity3"));

                if (this.getClass().getName().equals("neqsim.thermo.component.ComponentSrkCPA")
                        || this.getClass().getName()
                                .equals("neqsim.thermo.component.ComponentSrkCPAs")) {
                    parachorParameter = Double.parseDouble(dataSet.getString("PARACHOR_CPA"));
                } else {
                    parachorParameter = Double.parseDouble(dataSet.getString("parachor"));
                }

                setHeatOfFusion(Double.parseDouble(dataSet.getString("heatOfFusion")));

                triplePointDensity = Double.parseDouble(dataSet.getString("triplePointDensity"));
                triplePointPressure = Double.parseDouble(dataSet.getString("triplePointPressure"));
                setTriplePointTemperature(
                        Double.parseDouble(dataSet.getString("triplePointTemperature")));
                meltingPointTemperature =
                        Double.parseDouble(dataSet.getString("meltingPointTemperature"));

                Hsub = Double.parseDouble(dataSet.getString("Hsub"));

                setIdealGasEnthalpyOfFormation(
                        Double.parseDouble(dataSet.getString("EnthalpyOfFormation")));
                idealGasGibsEnergyOfFormation = gibbsEnergyOfFormation;
                idealGasAbsoluteEntropy = Double.parseDouble(dataSet.getString("AbsoluteEntropy"));

                for (int i = 0; i < 5; i++) {
                    solidDensityCoefs[i] =
                            Double.parseDouble((dataSet.getString("solidDensityCoefs" + (i + 1))));
                }
                for (int i = 0; i < 5; i++) {
                    liquidDensityCoefs[i] =
                            Double.parseDouble((dataSet.getString("liquidDensityCoefs" + (i + 1))));
                }
                for (int i = 0; i < 5; i++) {
                    heatOfVaporizationCoefs[i] = Double
                            .parseDouble((dataSet.getString("heatOfVaporizationCoefs" + (i + 1))));
                }
                // disse maa settes inn fra database ssociationsites
                numberOfAssociationSites = Integer.parseInt(dataSet.getString("associationsites"));
                orginalNumberOfAssociationSites = numberOfAssociationSites;
                associationScheme = dataSet.getString("associationscheme");
                associationEnergy = Double.parseDouble(dataSet.getString("associationenergy"));

                calcActivity = Integer.parseInt(dataSet.getString("calcActivity"));
                setRacketZCPA(Double.parseDouble(dataSet.getString("racketZCPA")));

                setVolumeCorrectionT_CPA(Double.parseDouble(dataSet.getString("volcorrCPA_T")));
                volumeCorrectionT = Double.parseDouble(dataSet.getString("volcorrSRK_T"));

                if (this.getClass().getName().equals("neqsim.thermo.component.ComponentPrCPA")) {
                    // System.out.println("pr-cpa");
                    associationVolume =
                            Double.parseDouble(dataSet.getString("associationboundingvolume_PR"));
                    aCPA = Double.parseDouble(dataSet.getString("aCPA_PR"));
                    bCPA = Double.parseDouble(dataSet.getString("bCPA_PR"));
                    mCPA = Double.parseDouble(dataSet.getString("mCPA_PR"));
                } else {
                    // System.out.println("srk-cpa");
                    associationVolume =
                            Double.parseDouble(dataSet.getString("associationboundingvolume_SRK"));
                    aCPA = Double.parseDouble(dataSet.getString("aCPA_SRK"));
                    bCPA = Double.parseDouble(dataSet.getString("bCPA_SRK"));
                    mCPA = Double.parseDouble(dataSet.getString("mCPA_SRK"));
                }

                criticalViscosity = Double.parseDouble(dataSet.getString("criticalViscosity"));
                if (criticalViscosity < 1e-20) {
                    criticalViscosity = 7.94830 * Math.sqrt(1e3 * molarMass)
                            * Math.pow(criticalPressure, 2.0 / 3.0)
                            / Math.pow(criticalTemperature, 1.0 / 6.0) * 1e-7;
                }
                mSAFTi = Double.parseDouble(dataSet.getString("mSAFT"));
                sigmaSAFTi = Double.parseDouble(dataSet.getString("sigmaSAFT")) / 1.0e10;
                epsikSAFT = Double.parseDouble(dataSet.getString("epsikSAFT"));
                setAssociationVolumeSAFT(
                        Double.parseDouble(dataSet.getString("associationboundingvolume_PCSAFT")));
                setAssociationEnergySAFT(
                        Double.parseDouble(dataSet.getString("associationenergy_PCSAFT")));
                if (Math.abs(criticalViscosity) < 1e-12) {
                    criticalViscosity = 7.94830 * Math.sqrt(molarMass * 1e3)
                            * Math.pow(criticalPressure, 2.0 / 3.0)
                            / Math.pow(criticalTemperature, 1.0 / 6.0) * 1e-7;
                }
                // System.out.println("crit visc " + criticalViscosity);
                if (normalLiquidDensity == 0) {
                    normalLiquidDensity =
                            molarMass / (0.285 * Math.pow(criticalVolume, 1.048)) * 1000.0;
                }
                if (dataSet.getString("HydrateFormer").equals("yes")) {
                    setIsHydrateFormer(true);
                } else {
                    setIsHydrateFormer(false);
                }

                waxFormer = Integer.parseInt(dataSet.getString("waxformer")) == 1;
                // System.out.println(componentName + " pure component parameters: ok...");
            }
            componentNumber = compnumber;
        } catch (Exception e) {
            logger.error("error in comp", e);
        } finally {
            try {
                if (dataSet != null) {
                    dataSet.close();
                }
                if (database.getStatement() != null) {
                    database.getStatement().close();
                }
                if (database.getConnection() != null) {
                    database.getConnection().close();
                }
            } catch (Exception e) {
                logger.error("error closing database.....", e);
            }
        }

        srkacentricFactor = acentricFactor;
        stokesCationicDiameter = lennardJonesMolecularDiameter;
        paulingAnionicDiameter = lennardJonesMolecularDiameter;
    }

    /** {@inheritDoc} */
    @Override
    public Component clone() {
        Component clonedComponent = null;
        try {
            clonedComponent = (Component) super.clone();
        } catch (Exception e) {
            logger.error("Cloning failed.", e);
        }

        return clonedComponent;
    }

    /** {@inheritDoc} */
    @Override
    public void addMolesChemReac(double dn) {
        numberOfMoles += dn;
        numberOfMolesInPhase += dn;
        if (numberOfMoles < 0) {
            numberOfMoles = 0;
        }
        if (numberOfMolesInPhase < 0) {
            numberOfMolesInPhase = 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addMolesChemReac(double dn, double totdn) {
        numberOfMoles += totdn;
        numberOfMolesInPhase += dn;

        if (numberOfMoles < 0) {
            numberOfMoles = 0;
        }
        if (numberOfMolesInPhase < 0) {
            numberOfMolesInPhase = 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void addMoles(double dn) {
        numberOfMolesInPhase += dn;
        if (numberOfMolesInPhase < 0) {
            numberOfMolesInPhase = 0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setProperties(ComponentInterface component) {
        x = component.getx();
        z = component.getz();
        numberOfMolesInPhase = component.getNumberOfMolesInPhase();
        numberOfMoles = component.getNumberOfmoles();
        K = component.getK();
    }

    /** {@inheritDoc} */
    @Override
    public void init(double temperature, double pressure, double totalNumberOfMoles, double beta,
            int type) {
        if (type == 0) {
            z = numberOfMoles / totalNumberOfMoles;
            K = Math.exp(Math.log(criticalPressure / pressure) + 5.373 * (1.0 + srkacentricFactor)
                    * (1.0 - criticalTemperature / temperature));
            x = z;
            // System.out.println("K " + K);
        }
        numberOfMolesInPhase = totalNumberOfMoles * x * beta;
        numberOfMoles = totalNumberOfMoles * z; // added late by Even 22/10-06
        z = numberOfMoles / totalNumberOfMoles;
    }

    /** {@inheritDoc} */
    @Override
    public Element getElements() {
        if (elements == null) {
            elements = new Element(componentName);
        }
        return elements;
    }

    /** {@inheritDoc} */
    @Override
    public void Finit(PhaseInterface phase, double temp, double pres, double totMoles, double beta,
            int numberOfComponents, int type) {}

    /** {@inheritDoc} */
    @Override
    public final double getx() {
        return x;
    }

    /** {@inheritDoc} */
    @Override
    public final double getz() {
        return z;
    }

    /** {@inheritDoc} */
    @Override
    public final void setz(double z) {
        this.z = z;
    }

    /** {@inheritDoc} */
    @Override
    public final double getReferencePotential() {
        return referencePotential;
    }

    /** {@inheritDoc} */
    @Override
    public final void setReferencePotential(double ref) {
        this.referencePotential = ref;
    }

    /** {@inheritDoc} */
    @Override
    public final double getK() {
        return K;
    }

    /** {@inheritDoc} */
    @Override
    public final double getHeatOfFusion() {
        return heatOfFusion;
    }

    /** {@inheritDoc} */
    @Override
    public double getHeatOfVapourization(double temp) {
        return heatOfVaporizationCoefs[0] + heatOfVaporizationCoefs[1] * temp
                + heatOfVaporizationCoefs[2] * temp * temp + heatOfVaporizationCoefs[3] * temp
                        * temp * temp * heatOfVaporizationCoefs[4] * temp * temp * temp * temp; // maa
                                                                                                // settes
                                                                                                // paa
                                                                                                // rett
                                                                                                // form
    }

    /** {@inheritDoc} */
    @Override
    public final double getTripplePointDensity() {
        return triplePointDensity;
    }

    /** {@inheritDoc} */
    @Override
    public final double getTriplePointPressure() {
        return triplePointPressure;
    }

    /** {@inheritDoc} */
    @Override
    public final double getTriplePointTemperature() {
        return triplePointTemperature;
    }

    /** {@inheritDoc} */
    @Override
    public final double getMeltingPointTemperature() {
        return meltingPointTemperature;
    }

    /** {@inheritDoc} */
    @Override
    public final double getIdealGasEnthalpyOfFormation() {
        return idealGasEnthalpyOfFormation;
    }

    /** {@inheritDoc} */
    @Override
    public final double getIdealGasGibsEnergyOfFormation() {
        return idealGasGibsEnergyOfFormation;
    }

    /** {@inheritDoc} */
    @Override
    public final double getIdealGasAbsoluteEntropy() {
        return idealGasAbsoluteEntropy;
    }

    /** {@inheritDoc} */
    @Override
    public final double getTC() {
        return criticalTemperature;
    }

    /** {@inheritDoc} */
    @Override
    public final void setTC(double val) {
        criticalTemperature = val;
    }

    /** {@inheritDoc} */
    @Override
    public final void setPC(double val) {
        criticalPressure = val;
    }

    /** {@inheritDoc} */
    @Override
    public final String getComponentName() {
        return componentName;
    }

    /** {@inheritDoc} */
    @Override
    public final String getReferenceStateType() {
        return referenceStateType;
    }

    /** {@inheritDoc} */
    @Override
    public final double getPC() {
        return criticalPressure;
    }

    /** {@inheritDoc} */
    @Override
    public double getGibbsEnergyOfFormation() {
        return gibbsEnergyOfFormation;
    }

    /** {@inheritDoc} */
    @Override
    public double getDiElectricConstant(double temperature) {
        return dielectricParameter[0] + dielectricParameter[1] / temperature
                + dielectricParameter[2] * temperature
                + dielectricParameter[3] * temperature * temperature
                + dielectricParameter[4] * Math.pow(temperature, 3.0);
    }

    /** {@inheritDoc} */
    @Override
    public double getDiElectricConstantdT(double temperature) {
        return -dielectricParameter[1] / Math.pow(temperature, 2.0) + dielectricParameter[2]
                + 2.0 * dielectricParameter[3] * temperature
                + 3.0 * dielectricParameter[4] * Math.pow(temperature, 2.0);
    }

    /** {@inheritDoc} */
    @Override
    public double getDiElectricConstantdTdT(double temperature) {
        return 2.0 * dielectricParameter[1] / Math.pow(temperature, 3.0)
                + 2.0 * dielectricParameter[3]
                + 6.0 * dielectricParameter[4] * Math.pow(temperature, 1.0);
    }

    /** {@inheritDoc} */
    @Override
    public double getDebyeDipoleMoment() {
        return debyeDipoleMoment;
    }

    /** {@inheritDoc} */
    @Override
    public final double getIonicCharge() {
        return ionicCharge;
    }

    /** {@inheritDoc} */
    @Override
    public final void setViscosityAssociationFactor(double val) {
        viscosityCorrectionFactor = val;
    }

    /** {@inheritDoc} */
    @Override
    public final double getRacketZ() {
        return racketZ;
    }

    /** {@inheritDoc} */
    @Override
    public double getVolumeCorrectionConst() {
        return volumeCorrectionConst;
    }

    /** {@inheritDoc} */
    @Override
    public double getNormalLiquidDensity() {
        return normalLiquidDensity;
    }

    /** {@inheritDoc} */
    @Override
    public double getViscosityCorrectionFactor() {
        return viscosityCorrectionFactor;
    }

    /** {@inheritDoc} */
    @Override
    public double getCriticalVolume() {
        return criticalVolume;
    }

    /** {@inheritDoc} */
    @Override
    public final int getLiquidViscosityModel() {
        return liquidViscosityModel;
    }

    /** {@inheritDoc} */
    @Override
    public final double getParachorParameter() {
        return parachorParameter;
    }

    /** {@inheritDoc} */
    @Override
    public final void setParachorParameter(double parachorParameter) {
        this.parachorParameter = parachorParameter;
    }

    /** {@inheritDoc} */
    @Override
    public final void setLiquidViscosityModel(int modelNumber) {
        liquidViscosityModel = modelNumber;
    }

    /** {@inheritDoc} */
    @Override
    public final void setLiquidViscosityParameter(double number, int i) {
        liquidViscosityParameter[i] = number;
    }

    /** {@inheritDoc} */
    @Override
    public final double getLiquidViscosityParameter(int i) {
        return liquidViscosityParameter[i];
    }

    /** {@inheritDoc} */
    @Override
    public void setLiquidConductivityParameter(double number, int i) {
        liquidConductivityParameter[i] = number;
    }

    /** {@inheritDoc} */
    @Override
    public double getLiquidConductivityParameter(int i) {
        return liquidConductivityParameter[i];
    }

    /**
     * {@inheritDoc}
     *
     * Units in m*e10
     */
    @Override
    public double getLennardJonesMolecularDiameter() {
        return lennardJonesMolecularDiameter;
    }

    /** {@inheritDoc} */
    @Override
    public double getLennardJonesEnergyParameter() {
        return lennardJonesEnergyParameter;
    }

    /** {@inheritDoc} */
    @Override
    public double getHsub() {
        return Hsub;
    }

    /**
     * {@inheritDoc}
     *
     * Calculates the pure comonent solid vapor pressure (bar) with the C-C equation, based on Hsub
     * Should only be used in the valid temperature range below the triple point (specified in
     * component database).
     */
    @Override
    public double getCCsolidVaporPressure(double temperature) {
        return triplePointPressure
                * (Math.exp(Hsub / R * (1.0 / getTriplePointTemperature() - 1.0 / temperature)));
    }

    /**
     * {@inheritDoc}
     *
     * Calculates the DT of pure comonent solid vapor pressure (bar) with the C-C equation, based on
     * Hsub Should only be used in the valid temperature range below the triple point (specified in
     * component database).
     */
    @Override
    public double getCCsolidVaporPressuredT(double temperature) {
        return triplePointPressure * Hsub / R * (1.0 / (temperature * temperature))
                * (Math.exp(Hsub / R * (1.0 / getTriplePointTemperature() - 1.0 / temperature)));
    }

    /**
     * {@inheritDoc}
     *
     * Calculates the pure component solid density in kg/liter Should only be used in the valid
     * temperature range (specified in component database).
     */
    @Override
    public double getPureComponentSolidDensity(double temperature) {
        return molarMass * 1000.0
                * (solidDensityCoefs[0] + solidDensityCoefs[1] * Math.pow(temperature, 1.0)
                        + solidDensityCoefs[2] * Math.pow(temperature, 2.0)
                        + solidDensityCoefs[3] * Math.pow(temperature, 3.0)
                        + solidDensityCoefs[4] * Math.pow(temperature, 4.0));
    }

    /**
     * {@inheritDoc}
     *
     * Calculates the pure component liquid density in kg/liter Should only be used in the valid
     * temperature range (specified in component database). This method seems to give bad results at
     * the moment
     */
    @Override
    public double getPureComponentLiquidDensity(double temperature) {
        return molarMass * 1000.0
                * (liquidDensityCoefs[0] + liquidDensityCoefs[1] * Math.pow(temperature, 1.0)
                        + liquidDensityCoefs[2] * Math.pow(temperature, 2.0)
                        + liquidDensityCoefs[3] * Math.pow(temperature, 3.0)
                        + liquidDensityCoefs[4] * Math.pow(temperature, 4.0));
        // return Math.pow(liquidDensityCoefs[0] / liquidDensityCoefs[1], 1.0 +
        // Math.pow(1.0 - temperature / liquidDensityCoefs[2], liquidDensityCoefs[3]));
    }

    /**
     * {@inheritDoc}
     *
     * Calculates the pure comonent heat of vaporization in J/mol
     */
    @Override
    public double getPureComponentHeatOfVaporization(double temperature) {
        return 1.0e-3 * heatOfVaporizationCoefs[0]
                * Math.pow((1.0 - temperature / criticalTemperature),
                        heatOfVaporizationCoefs[1]
                                + heatOfVaporizationCoefs[2] * temperature / criticalTemperature
                                + heatOfVaporizationCoefs[3]
                                        * Math.pow(temperature / criticalTemperature, 2.0));
    }

    /** {@inheritDoc} */
    @Override
    public final void setx(double newx) {
        if (Double.isNaN(newx) || Double.isInfinite(newx)) {
            return;
        }
        if (newx < 0) {
            x = 1.0e-50;
        }
        if (newx > 0) {
            x = newx;
        }
        if (newx > 5) {
            x = 5;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void setNumberOfmoles(double newmoles) {
        numberOfMoles = newmoles;
    }

    /** {@inheritDoc} */
    @Override
    public final void setNumberOfMolesInPhase(double totmoles) {
        numberOfMolesInPhase = totmoles * x;
    }

    /** {@inheritDoc} */
    @Override
    public final double getNumberOfmoles() {
        return this.numberOfMoles;
    }

    /** {@inheritDoc} */
    @Override
    public final double getMolarMass() {
        return this.molarMass;
    }

    /** {@inheritDoc} */
    @Override
    public final double getNumberOfMolesInPhase() {
        return this.numberOfMolesInPhase;
    }

    /** {@inheritDoc} */
    @Override
    public double getRate(String unitName) {
        neqsim.util.unit.Unit unit = new neqsim.util.unit.RateUnit(numberOfMolesInPhase, "mol/sec",
                molarMass, normalLiquidDensity, normalBoilingPoint);
        double val = unit.getValue(unitName);
        return val;
    }

    /** {@inheritDoc} */
    @Override
    public final void setK(double newK) {
        K = newK;
    }

    /** {@inheritDoc} */
    @Override
    public final double getFugasityCoeffisient() {
        return fugasityCoeffisient;
    }

    /** {@inheritDoc} */
    @Override
    public final double getFugasityCoefficient() {
        return getFugasityCoeffisient();
    }

    /** {@inheritDoc} */
    @Override
    public double fugcoef(PhaseInterface phase) {
        fugasityCoeffisient = 1.0;// this.fugcoef(phase, phase.getNumberOfComponents(),
                                  // phase.getTemperature(),
                                  // phase.getPressure());
        logFugasityCoeffisient = Math.log(fugasityCoeffisient);
        return fugasityCoeffisient;
    }

    /** {@inheritDoc} */
    @Override
    public double logfugcoefdT(PhaseInterface phase) {
        dfugdt = 0.0;// this.fugcoefDiffTemp(phase, phase.getNumberOfComponents(),
                     // phase.getTemperature(), phase.getPressure());
        return dfugdt;
    }

    /** {@inheritDoc} */
    @Override
    public double logfugcoefdP(PhaseInterface phase) {
        dfugdp = 0.0;// this.fugcoefDiffPres(phase, phase.getNumberOfComponents(),
                     // phase.getTemperature(), phase.getPressure());
        return dfugdp;
    }

    /** {@inheritDoc} */
    @Override
    public double[] logfugcoefdN(PhaseInterface phase) {
        // dfugdn = this.fugcoefDiffN(phase, phase.getNumberOfComponents(),
        // phase.getTemperature(), phase.getPressure());
        return new double[2];
    }

    /** {@inheritDoc} */
    @Override
    public double logfugcoefdNi(PhaseInterface phase, int k) {
        return 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public double getdfugdt() {
        return dfugdt;
    }

    /** {@inheritDoc} */
    @Override
    public double getdfugdp() {
        return dfugdp;
    }

    /** {@inheritDoc} */
    @Override
    public void setdfugdt(double val) {
        dfugdt = val;
    }

    /** {@inheritDoc} */
    @Override
    public void setdfugdp(double val) {
        dfugdp = val;
    }

    /** {@inheritDoc} */
    @Override
    public void setdfugdn(int i, double val) {
        dfugdn[i] = val;
    }

    /** {@inheritDoc} */
    @Override
    public void setdfugdx(int i, double val) {
        dfugdx[i] = val;
    }

    /** {@inheritDoc} */
    @Override
    public double getAcentricFactor() {
        return acentricFactor;
    }

    /** {@inheritDoc} */
    @Override
    public double getdfugdx(int i) {
        return dfugdx[i];
    }

    /** {@inheritDoc} */
    @Override
    public double getdfugdn(int i) {
        return dfugdn[i];
    }

    /** {@inheritDoc} */
    @Override
    public int getIndex() {
        return index;
    }

    /** {@inheritDoc} */
    @Override
    public int getComponentNumber() {
        return componentNumber;
    }

    /** {@inheritDoc} */
    @Override
    public final double getGibbsEnergy(double temperature, double pressure) {
        return getEnthalpy(temperature) - temperature * getEntropy(temperature, pressure);
    }

    /** {@inheritDoc} */
    @Override
    public final double getChemicalPotentialIdealReference(PhaseInterface phase) {
        return (getHID(phase.getTemperature())
                - phase.getTemperature() * getIdEntropy(phase.getTemperature()));
    }

    /** {@inheritDoc} */
    @Override
    public final double getChemicalPotential(double temperature, double pressure) {
        return getGibbsEnergy(temperature, pressure) / numberOfMolesInPhase;
    }

    /** {@inheritDoc} */
    @Override
    public double getChemicalPotential(PhaseInterface phase) {
        return getGibbsEnergy(phase.getTemperature(), phase.getPressure()) / numberOfMolesInPhase;
        // return getGresTV;
    }

    /**
     * <p>
     * getFugacitydN.
     * </p>
     *
     * @param i a int
     * @param phase a {@link neqsim.thermo.phase.PhaseInterface} object
     * @return a double
     */
    public final double getFugacitydN(int i, PhaseInterface phase) {
        double tempFug = 0.0;
        if (i == componentNumber) {
            tempFug = 1.0 / getNumberOfMolesInPhase();
        }
        return getdfugdn(i) + tempFug - 1.0 / phase.getNumberOfMolesInPhase();
    }

    /** {@inheritDoc} */
    @Override
    public final double getChemicalPotentialdNTV(int i, PhaseInterface phase) {
        return getChemicalPotentialdN(i, phase)
                - getVoli() * phase.getComponent(i).getVoli() * phase.getdPdVTn();
    }

    /** {@inheritDoc} */
    @Override
    public final double getChemicalPotentialdN(int i, PhaseInterface phase) {
        return R * phase.getTemperature() * getFugacitydN(i, phase);
    }

    /** {@inheritDoc} */
    @Override
    public final double getChemicalPotentialdP() {
        return voli;
    }

    /** {@inheritDoc} */
    @Override
    public final double getChemicalPotentialdT(PhaseInterface phase) {
        return -getEntropy(phase.getTemperature(), phase.getPressure()) / numberOfMolesInPhase;
    }

    /** {@inheritDoc} */
    @Override
    public final double getChemicalPotentialdV(PhaseInterface phase) {
        return getChemicalPotentialdP() * phase.getdPdVTn();
    }

    /**
     * <p>
     * getChemicalPotentialdP.
     * </p>
     *
     * @param i a int
     * @param phase a {@link neqsim.thermo.phase.PhaseInterface} object
     * @return a double
     */
    public final double getChemicalPotentialdP(int i, PhaseInterface phase) {
        return R * phase.getTemperature() * getFugacitydN(i, phase);
    }

    /** {@inheritDoc} */
    @Override
    public void setComponentNumber(int numb) {
        componentNumber = numb;
    }

    /** {@inheritDoc} */
    @Override
    public double getAntoineVaporPressure(double temp) {
        if (antoineLiqVapPresType.equals("pow10")) {
            return Math.pow(10.0, AntoineA - (AntoineB / (temp + AntoineC - 273.15))); // equation
                                                                                       // and
                                                                                       // parameter
                                                                                       // from
                                                                                       // properties
                                                                                       // o liquids
                                                                                       // and
                                                                                       // gases
                                                                                       // (poling
                                                                                       // 5th ed)
        } else if (antoineLiqVapPresType.equals("exp") || antoineLiqVapPresType.equals("log")) {
            return Math.exp(AntoineA - (AntoineB / (temp + AntoineC))); // equation and parameter
                                                                        // from properties o
                                                                        // liquids and gases (poling
                                                                        // 5th ed)
        } else if (Math.abs(AntoineE) > 1e-12) {
            return Math.exp(AntoineA + AntoineB / temp + AntoineC * Math.log(temp)
                    + AntoineD * Math.pow(temp, AntoineE)) / 100000;
        } else {
            double x = 1 - (temp / criticalTemperature);
            return (Math
                    .exp(Math.pow((1 - x), -1) * (AntoineA * x + AntoineB * Math.pow(x, 1.5)
                            + AntoineC * Math.pow(x, 3) + AntoineD * Math.pow(x, 6)))
                    * criticalPressure);
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getAntoineVaporPressuredT(double temp) {
        if (antoineLiqVapPresType.equals("pow10")) {
            // (10^ (A - B/(C + x - 5463/20)) *B*log(10))/(C + x - 5463/20)^2
            double ans = (Math.pow(AntoineA - AntoineB / (AntoineC + temp - 273.15), 10.0)
                    * AntoineB * Math.log(10.0)) / Math.pow((AntoineC + temp - 273.15), 2.0);
            return ans;
        } else if (antoineLiqVapPresType.equals("exp") || antoineLiqVapPresType.equals("log")) {
            // (B*exp(A - B/(C + x)))/(C + x)^2
            double ans = AntoineB * (Math.exp(AntoineA - AntoineB / (AntoineC + temp)))
                    / Math.pow((AntoineC + temp), 2.0);
            return ans;
        } else {
            return 0.0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public double getAntoineVaporTemperature(double pres) {
        double nyPres = 0, nyTemp = criticalTemperature * 0.7;
        int iter = 0;
        do {
            iter++;

            nyPres = getAntoineVaporPressure(nyTemp);
            nyTemp -= (nyPres - pres);
            // nyTemp = nyTemp-(nyPres - pres)/getAntoineVaporPressuredT(nyTemp);
            // System.out.println("temp Antoine " +nyTemp + " error "+Math.abs((nyPres -
            // pres) / pres));
        } while (Math.abs((nyPres - pres) / pres) > 0.00001 && iter < 1000);
        return nyTemp;
    }

    /** {@inheritDoc} */
    @Override
    public final double getHresTP(double temperature) {
        return R * temperature * (-temperature * getdfugdt());
    }

    /** {@inheritDoc} */
    @Override
    public final double getGresTP(double temperature) {
        return R * temperature * (Math.log(getFugasityCoeffisient()));
    }

    /** {@inheritDoc} */
    @Override
    public final double getSresTP(double temperature) {
        return (getHresTP(temperature) - getGresTP(temperature)) / temperature;
    }

    /** {@inheritDoc} */
    @Override
    public final double getCp0(double temperature) {
        return getCpA() + getCpB() * temperature + getCpC() * Math.pow(temperature, 2)
                + getCpD() * Math.pow(temperature, 3) + getCpE() * Math.pow(temperature, 4);
    }

    /** {@inheritDoc} */
    @Override
    public final double getCv0(double temperature) {
        return getCpA() + getCpB() * temperature + getCpC() * Math.pow(temperature, 2)
                + getCpD() * Math.pow(temperature, 3) + getCpE() * Math.pow(temperature, 4) - R;
    }

    // integralet av Cp0 mhp T
    /** {@inheritDoc} */
    @Override
    public final double getHID(double T) {
        return 0 * getIdealGasEnthalpyOfFormation()
                + (getCpA() * T + 1.0 / 2.0 * getCpB() * T * T + 1.0 / 3.0 * getCpC() * T * T * T
                        + 1.0 / 4.0 * getCpD() * T * T * T * T)
                + 1.0 / 5.0 * getCpE() * T * T * T * T * T
                - (getCpA() * referenceTemperature
                        + 1.0 / 2.0 * getCpB() * referenceTemperature * referenceTemperature
                        + 1.0 / 3.0 * getCpC() * referenceTemperature * referenceTemperature
                                * referenceTemperature
                        + 1.0 / 4.0 * getCpD() * referenceTemperature * referenceTemperature
                                * referenceTemperature * referenceTemperature
                        + 1.0 / 5.0 * getCpE() * referenceTemperature * referenceTemperature
                                * referenceTemperature * referenceTemperature
                                * referenceTemperature);
    }

    /** {@inheritDoc} */
    @Override
    public final double getEnthalpy(double temperature) {
        return getHID(temperature) * numberOfMolesInPhase
                + getHresTP(temperature) * numberOfMolesInPhase;
    }

    /** {@inheritDoc} */
    @Override
    public double getIdEntropy(double temperature) {
        return (getCpE() * temperature * temperature * temperature * temperature / 4.0
                + getCpD() * temperature * temperature * temperature / 3.0
                + getCpC() * temperature * temperature / 2.0 + getCpB() * temperature
                + getCpA() * Math.log(temperature)
                - getCpE() * referenceTemperature * referenceTemperature * referenceTemperature
                        * referenceTemperature / 4.0
                - getCpD() * referenceTemperature * referenceTemperature * referenceTemperature
                        / 3.0
                - getCpC() * referenceTemperature * referenceTemperature / 2.0
                - getCpB() * referenceTemperature - getCpA() * Math.log(referenceTemperature));
    }

    /** {@inheritDoc} */
    @Override
    public double getEntropy(double temperature, double pressure) {
        if (x < 1e-100) {
            return 0.0;
        }
        return numberOfMolesInPhase * (getIdEntropy(temperature)
                - (R * Math.log(pressure / referencePressure)) - R * Math.log(x))
                + getSresTP(temperature) * numberOfMolesInPhase; // 1 bor vaere Z
    }

    /** {@inheritDoc} */
    @Override
    public final String getName() {
        return componentName;
    }

    /** {@inheritDoc} */
    @Override
    public void setAcentricFactor(double val) {
        acentricFactor = val;
        getAtractiveTerm().init();
    }

    /** {@inheritDoc} */
    @Override
    public void setRacketZ(double val) {
        racketZ = val;
    }

    /** {@inheritDoc} */
    @Override
    public void setAtractiveTerm(int i) {
        atractiveTermNumber = i;
    }

    /** {@inheritDoc} */
    @Override
    public AtractiveTermInterface getAtractiveTerm() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final double[] getSchwartzentruberParams() {
        return schwartzentruberParams;
    }

    /** {@inheritDoc} */
    @Override
    public final void setSchwartzentruberParams(int i, double param) {
        schwartzentruberParams[i] = param;
    }

    /** {@inheritDoc} */
    @Override
    public final double[] getTwuCoonParams() {
        return TwuCoonParams;
    }

    /** {@inheritDoc} */
    @Override
    public final void setTwuCoonParams(int i, double param) {
        TwuCoonParams[i] = param;
    }

    /** {@inheritDoc} */
    @Override
    public double fugcoefDiffPresNumeric(PhaseInterface phase, int numberOfComponents,
            double temperature, double pressure) {
        double temp1 = 0.0, temp2 = 0.0;
        double dp = phase.getPressure() / 1.0e5;
        temp1 = phase.getComponents()[componentNumber].getFugasityCoeffisient();
        phase.setPressure(phase.getPressure() - dp);
        phase.init(numberOfMolesInPhase, numberOfComponents, 1, phase.getPhaseType(),
                phase.getBeta());
        phase.getComponents()[componentNumber].fugcoef(phase);
        temp2 = phase.getComponents()[componentNumber].getFugasityCoeffisient();
        phase.setPressure(phase.getPressure() + dp);
        phase.init(numberOfMolesInPhase, numberOfComponents, 1, phase.getPhaseType(),
                phase.getBeta());
        phase.getComponents()[componentNumber].fugcoef(phase);
        dfugdp = (Math.log(temp1) - Math.log(temp2)) / dp;
        return dfugdp;
    }

    /** {@inheritDoc} */
    @Override
    public double fugcoefDiffTempNumeric(PhaseInterface phase, int numberOfComponents,
            double temperature, double pressure) {
        double temp1 = 0.0, temp2 = 0.0;
        double dt = phase.getTemperature() / 1.0e6;
        temp1 = phase.getComponents()[componentNumber].getFugasityCoeffisient();
        phase.setTemperature(phase.getTemperature() - dt);
        phase.init(numberOfMolesInPhase, numberOfComponents, 1, phase.getPhaseType(),
                phase.getBeta());
        phase.getComponents()[componentNumber].fugcoef(phase);
        temp2 = phase.getComponents()[componentNumber].getFugasityCoeffisient();
        // phase.setTemperature(phase.getTemperature()+dt);
        // System.out.println("temp " + phase.getTemperature());
        // phase.init(numberOfMolesInPhase, numberOfComponents, 1,phase.getPhaseType(),
        // phase.getBeta());
        // phase.getComponents()[componentNumber].fugcoef(phase, numberOfComponents,
        // phase.getTemperature(), phase.getPressure());
        dfugdt = (Math.log(temp1) - Math.log(temp2)) / dt;
        return dfugdt;
    }

    /**
     * <p>
     * getIonicDiameter.
     * </p>
     *
     * @return a double
     */
    public final double getIonicDiameter() {
        if (ionicCharge < 0) {
            return paulingAnionicDiameter;
        } else if (ionicCharge > 0) {
            return stokesCationicDiameter;
        } else {
            return lennardJonesMolecularDiameter;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property stokesCationicDiameter.
     */
    @Override
    public double getStokesCationicDiameter() {
        return stokesCationicDiameter;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property stokesCationicDiameter.
     */
    @Override
    public void setStokesCationicDiameter(double stokesCationicDiameter) {
        this.stokesCationicDiameter = stokesCationicDiameter;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property paulingAnionicDiameter.
     */
    @Override
    public final double getPaulingAnionicDiameter() {
        return paulingAnionicDiameter;
    }

    /**
     * Setter for property paulingAnionicDiameter.
     *
     * @param paulingAnionicDiameter New value of property paulingAnionicDiameter.
     */
    public void setPaulingAnionicDiameter(double paulingAnionicDiameter) {
        this.paulingAnionicDiameter = paulingAnionicDiameter;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property logFugasityCoeffisient.
     */
    @Override
    public final double getLogFugasityCoeffisient() {
        return logFugasityCoeffisient;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property atractiveTermNumber.
     */
    @Override
    public final int getAtractiveTermNumber() {
        return atractiveTermNumber;
    }

    /** {@inheritDoc} */
    @Override
    public double getVoli() {
        return voli;
    }

    /**
     * <p>
     * Setter for the field <code>voli</code>.
     * </p>
     *
     * @param molarVol a double
     */
    public void setVoli(double molarVol) {
        voli = molarVol;
    }

    /**
     * Indexed getter for property matiascopemanParams.
     *
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
     */
    public final double getMatiascopemanParams(int index) {
        return matiascopemanParams[index];
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property matiascopemanParams.
     */
    @Override
    public final double[] getMatiascopemanParams() {
        return matiascopemanParams;
    }

    /**
     * <p>
     * Getter for the field <code>matiascopemanParamsPR</code>.
     * </p>
     *
     * @return an array of {@link double} objects
     */
    public final double[] getMatiascopemanParamsPR() {
        return matiascopemanParamsPR;
    }

    /**
     * <p>
     * Setter for the field <code>matiascopemanParamsPR</code>.
     * </p>
     *
     * @param index a int
     * @param matiascopemanParams a double
     */
    public void setMatiascopemanParamsPR(int index, double matiascopemanParams) {
        this.matiascopemanParamsPR[index] = matiascopemanParams;
    }

    /**
     * {@inheritDoc}
     *
     * Indexed setter for property matiascopemanParams.
     */
    @Override
    public void setMatiascopemanParams(int index, double matiascopemanParams) {
        this.matiascopemanParams[index] = matiascopemanParams;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property matiascopemanParams.
     */
    @Override
    public void setMatiascopemanParams(double[] matiascopemanParams) {
        this.matiascopemanParams = matiascopemanParams;
    }

    /** {@inheritDoc} */
    @Override
    public void setFugacityCoefficient(double val) {
        fugasityCoeffisient = val;
        logFugasityCoeffisient = Math.log(fugasityCoeffisient);
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property numberOfAssociationSites.
     */
    @Override
    public final int getNumberOfAssociationSites() {
        return numberOfAssociationSites;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property numberOfAssociationSites.
     */
    @Override
    public void setNumberOfAssociationSites(int numberOfAssociationSites) {
        this.numberOfAssociationSites = numberOfAssociationSites;
    }

    /** {@inheritDoc} */
    @Override
    public void seta(double a) {
        logger.error("no method set a");
    }

    /** {@inheritDoc} */
    @Override
    public void setb(double b) {
        logger.error("no method set b");
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property associationVolume.
     */
    @Override
    public final double getAssociationVolume() {
        return associationVolume;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property associationVolume.
     */
    @Override
    public void setAssociationVolume(double associationVolume) {
        this.associationVolume = associationVolume;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property associationEnergy.
     */
    @Override
    public final double getAssociationEnergy() {
        return associationEnergy;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property associationEnergy.
     */
    @Override
    public void setAssociationEnergy(double associationEnergy) {
        this.associationEnergy = associationEnergy;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property normalBoilingPoint.
     */
    @Override
    public double getNormalBoilingPoint() {
        return normalBoilingPoint;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property normalBoilingPoint.
     */
    @Override
    public void setNormalBoilingPoint(double normalBoilingPoint) {
        this.normalBoilingPoint = normalBoilingPoint;
    }

    /**
     * Getter for property standardDensity.
     *
     * @return Value of property standardDensity.
     */
    public double getStandardDensity() {
        return standardDensity;
    }

    /**
     * Setter for property standardDensity.
     *
     * @param standardDensity New value of property standardDensity.
     */
    public void setStandardDensity(double standardDensity) {
        this.standardDensity = standardDensity;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property AntoineASolid.
     */
    @Override
    public double getAntoineASolid() {
        return AntoineASolid;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property AntoineASolid.
     */
    @Override
    public void setAntoineASolid(double AntoineASolid) {
        this.AntoineASolid = AntoineASolid;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property AntoineBSolid.
     */
    @Override
    public double getAntoineBSolid() {
        return AntoineBSolid;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property AntoineBSolid.
     */
    @Override
    public void setAntoineBSolid(double AntoineBSolid) {
        this.AntoineBSolid = AntoineBSolid;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property AntoineBSolid.
     */
    @Override
    public double getAntoineCSolid() {
        return AntoineBSolid;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property AntoineBSolid.
     */
    @Override
    public void setAntoineCSolid(double AntoineCSolid) {
        this.AntoineCSolid = AntoineCSolid;
    }

    /** {@inheritDoc} */
    @Override
    public final double getSolidVaporPressure(double temperature) {
        if (Math.abs(AntoineCSolid) < 1e-10) {
            return Math.exp(AntoineASolid + AntoineBSolid / temperature);
        } else {
            return Math.pow(10.0, AntoineASolid - AntoineBSolid / (temperature + AntoineCSolid));
        }
    }

    /** {@inheritDoc} */
    @Override
    public final double getSolidVaporPressuredT(double temperature) {
        if (Math.abs(AntoineCSolid) < 1e-10) {
            return -AntoineBSolid / (temperature * temperature)
                    * Math.exp(AntoineASolid + AntoineBSolid / temperature);
        } else {
            return AntoineBSolid * Math.log(10)
                    * Math.pow((1 / 10),
                            (AntoineASolid - AntoineBSolid / (temperature + AntoineCSolid)))
                    * Math.pow(10, AntoineASolid) / Math.pow((temperature + AntoineCSolid), 2);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final double getSphericalCoreRadius() {
        return sphericalCoreRadius;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property componentName.
     */
    @Override
    public void setComponentName(java.lang.String componentName) {
        this.componentName = componentName;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property lennardJonesEnergyParameter.
     */
    @Override
    public void setLennardJonesEnergyParameter(double lennardJonesEnergyParameter) {
        this.lennardJonesEnergyParameter = lennardJonesEnergyParameter;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property lennardJonesMolecularDiameter.
     */
    @Override
    public void setLennardJonesMolecularDiameter(double lennardJonesMolecularDiameter) {
        this.lennardJonesMolecularDiameter = lennardJonesMolecularDiameter;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property sphericalCoreRadius.
     */
    @Override
    public void setSphericalCoreRadius(double sphericalCoreRadius) {
        this.sphericalCoreRadius = sphericalCoreRadius;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property calcActivity.
     */
    @Override
    public boolean calcActivity() {
        return calcActivity != 0;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property isTBPfraction.
     */
    @Override
    public boolean isIsTBPfraction() {
        return isTBPfraction;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isHydrocarbon() {
        return isIsTBPfraction() || isPlusFraction || componentType.equals("HC");
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property isTBPfraction.
     */
    @Override
    public void setIsTBPfraction(boolean isTBPfraction) {
        setIsAllTypesFalse();
        this.isTBPfraction = isTBPfraction;
    }

    /**
     * <p>
     * setIsAllTypesFalse.
     * </p>
     */
    protected void setIsAllTypesFalse() {
        this.isTBPfraction = false;
        this.isPlusFraction = false;
        this.isNormalComponent = false;
        this.isIon = false;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property isPlusFraction.
     */
    @Override
    public boolean isIsPlusFraction() {
        return isPlusFraction;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property isPlusFraction.
     */
    @Override
    public void setIsPlusFraction(boolean isPlusFraction) {
        setIsAllTypesFalse();
        this.isPlusFraction = isPlusFraction;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property isNormalComponent.
     */
    @Override
    public boolean isIsNormalComponent() {
        return isNormalComponent;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInert() {
        return componentType.equals("inert");
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property isNormalComponent.
     */
    @Override
    public void setIsNormalComponent(boolean isNormalComponent) {
        setIsAllTypesFalse();
        this.isNormalComponent = isNormalComponent;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property isIon.
     */
    @Override
    public boolean isIsIon() {
        if (componentType.equals("ion"))
            setIsIon(true);
        return isIon;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property isIon.
     */
    @Override
    public void setIsIon(boolean isIon) {
        setIsAllTypesFalse();
        this.isIon = isIon;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property normalLiquidDensity.
     */
    @Override
    public void setNormalLiquidDensity(double normalLiquidDensity) {
        this.normalLiquidDensity = normalLiquidDensity;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property molarMass.
     */
    @Override
    public void setMolarMass(double molarMass) {
        this.molarMass = molarMass;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property solidCheck.
     */
    @Override
    public final boolean doSolidCheck() {
        return solidCheck;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property solidCheck.
     */
    @Override
    public void setSolidCheck(boolean solidCheck) {
        this.solidCheck = solidCheck;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property associationScheme.
     */
    @Override
    public java.lang.String getAssociationScheme() {
        return associationScheme;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property associationScheme.
     */
    @Override
    public void setAssociationScheme(java.lang.String associationScheme) {
        this.associationScheme = associationScheme;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property componentType.
     */
    @Override
    public java.lang.String getComponentType() {
        if (isTBPfraction) {
            componentType = "TBP";
        } else if (isPlusFraction) {
            componentType = "plus";
        } else if (isNormalComponent) {
            componentType = "normal";
        } else if (isIon) {
            componentType = "ion";
        }
        return componentType;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property Henrys Coeffisient. Unit is bar. ln H = C1 + C2/T + C3lnT + C4*T
     */
    @Override
    public double getHenryCoef(double temperature) {
        // System.out.println("henry " +
        // Math.exp(henryCoefParameter[0]+henryCoefParameter[1]/temperature+henryCoefParameter[2]*Math.log(temperature)+henryCoefParameter[3]*temperature)*100*0.01802);
        return Math.exp(henryCoefParameter[0] + henryCoefParameter[1] / temperature
                + henryCoefParameter[2] * Math.log(temperature)
                + henryCoefParameter[3] * temperature) * 0.01802 * 100;
    }

    /** {@inheritDoc} */
    @Override
    public double getHenryCoefdT(double temperature) {
        return getHenryCoef(temperature) * (-henryCoefParameter[1] / (temperature * temperature)
                + henryCoefParameter[2] / temperature + henryCoefParameter[3]);
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property henryCoefParameter.
     */
    @Override
    public double[] getHenryCoefParameter() {
        return this.henryCoefParameter;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property henryCoefParameter.
     */
    @Override
    public void setHenryCoefParameter(double[] henryCoefParameter) {
        this.henryCoefParameter = henryCoefParameter;
    }

    /**
     * {@inheritDoc}
     *
     * Getter for property matiascopemanSolidParams.
     */
    @Override
    public double[] getMatiascopemanSolidParams() {
        return this.matiascopemanSolidParams;
    }

    /**
     * Setter for property matiascopemanSolidParams.
     *
     * @param matiascopemanSolidParams New value of property matiascopemanSolidParams.
     */
    public void setMatiascopemanSolidParams(double[] matiascopemanSolidParams) {
        this.matiascopemanSolidParams = matiascopemanSolidParams;
    }

    /** {@inheritDoc} */
    @Override
    public double getPureComponentCpSolid(double temperature) {
        // unit J/mol*K DIPPR function
        return 1. / 1000.0
                * (CpSolid[0] + CpSolid[1] * temperature + CpSolid[2] * Math.pow(temperature, 2.0)
                        + CpSolid[3] * Math.pow(temperature, 3.0)
                        + CpSolid[4] * Math.pow(temperature, 4.0));
    }

    // A^2/(1-Tr)+B-2*A*C*(1-Tr)-A*D*(1-Tr)^2-C^2*(1-Tr)^3/3-C*D*(1-Tr)^4/2-D^2*(1-Tr)^5/5
    /** {@inheritDoc} */
    @Override
    public double getPureComponentCpLiquid(double temperature) {
        // unit J/mol*K DIPPR function
        return 1. / 1000.0
                * (CpLiquid[0] + CpLiquid[1] * temperature
                        + CpLiquid[2] * Math.pow(temperature, 2.0)
                        + CpLiquid[3] * Math.pow(temperature, 3.0)
                        + CpLiquid[4] * Math.pow(temperature, 4.0));
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property criticalVolume.
     */
    @Override
    public void setCriticalVolume(double criticalVolume) {
        this.criticalVolume = criticalVolume;
    }

    /** {@inheritDoc} */
    @Override
    public double getCriticalViscosity() {
        return criticalViscosity;
    }

    /**
     * {@inheritDoc}
     *
     * Setter for property criticalViscosity.
     */
    @Override
    public void setCriticalViscosity(double criticalViscosity) {
        this.criticalViscosity = criticalViscosity;
    }

    // return mol/litre
    /** {@inheritDoc} */
    @Override
    public double getMolarity(PhaseInterface phase) {
        return x * 1.0 / (phase.getMolarVolume() * 1e-5) / 1e3;
    }

    // return mol/kg
    /** {@inheritDoc} */
    @Override
    public double getMolality(PhaseInterface phase) {
        return getMolarity(phase) / (phase.getDensity() / 1.0e3);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isHydrateFormer() {
        return isIsHydrateFormer();
    }

    /** {@inheritDoc} */
    @Override
    public void setIsHydrateFormer(boolean isHydrateFormer) {
        this.isHydrateFormer = isHydrateFormer;
    }

    /** {@inheritDoc} */
    @Override
    public double getmSAFTi() {
        return mSAFTi;
    }

    /** {@inheritDoc} */
    @Override
    public void setmSAFTi(double mSAFTi) {
        this.mSAFTi = mSAFTi;
    }

    /** {@inheritDoc} */
    @Override
    public double getSigmaSAFTi() {
        return sigmaSAFTi;
    }

    /** {@inheritDoc} */
    @Override
    public void setSigmaSAFTi(double sigmaSAFTi) {
        this.sigmaSAFTi = sigmaSAFTi;
    }

    /** {@inheritDoc} */
    @Override
    public double getEpsikSAFT() {
        return epsikSAFT;
    }

    /** {@inheritDoc} */
    @Override
    public void setEpsikSAFT(double epsikSAFT) {
        this.epsikSAFT = epsikSAFT;
    }

    /** {@inheritDoc} */
    @Override
    public double getAssociationVolumeSAFT() {
        return associationVolumeSAFT;
    }

    /** {@inheritDoc} */
    @Override
    public void setAssociationVolumeSAFT(double associationVolumeSAFT) {
        this.associationVolumeSAFT = associationVolumeSAFT;
    }

    /** {@inheritDoc} */
    @Override
    public double getAssociationEnergySAFT() {
        return associationEnergySAFT;
    }

    /** {@inheritDoc} */
    @Override
    public void setAssociationEnergySAFT(double associationEnergySAFT) {
        this.associationEnergySAFT = associationEnergySAFT;
    }

    /** {@inheritDoc} */
    @Override
    public double getCriticalCompressibilityFactor() {
        return criticalCompressibilityFactor;
    }

    /** {@inheritDoc} */
    @Override
    public void setCriticalCompressibilityFactor(double criticalCompressibilityFactor) {
        this.criticalCompressibilityFactor = criticalCompressibilityFactor;
    }

    /** {@inheritDoc} */
    @Override
    public double getSurfaceTenisionInfluenceParameter(double temperature) {
        return 1.0;
    }

    /** {@inheritDoc} */
    @Override
    public void setSurfTensInfluenceParam(int factNum, double val) {
        surfTensInfluenceParam[factNum] = val;
    }

    /** {@inheritDoc} */
    @Override
    public double getSurfTensInfluenceParam(int factNum) {
        return surfTensInfluenceParam[factNum];
    }

    /** {@inheritDoc} */
    @Override
    public boolean isWaxFormer() {
        return waxFormer;
    }

    /** {@inheritDoc} */
    @Override
    public void setWaxFormer(boolean waxFormer) {
        this.waxFormer = waxFormer;
    }

    /** {@inheritDoc} */
    @Override
    public void setHeatOfFusion(double heatOfFusion) {
        this.heatOfFusion = heatOfFusion;
    }

    /** {@inheritDoc} */
    @Override
    public void setTriplePointTemperature(double triplePointTemperature) {
        this.triplePointTemperature = triplePointTemperature;
    }

    /** {@inheritDoc} */
    @Override
    public void setComponentType(String componentType) {
        this.componentType = componentType;
        if (componentType.equals("TBP")) {
            setIsTBPfraction(true);
        }
    }

    /**
     * <p>
     * isIsHydrateFormer.
     * </p>
     *
     * @return the isHydrateFormer
     */
    public boolean isIsHydrateFormer() {
        return isHydrateFormer;
    }

    /**
     * <p>
     * Getter for the field <code>referenceEnthalpy</code>.
     * </p>
     *
     * @return the referenceEnthalpy
     */
    public double getReferenceEnthalpy() {
        return referenceEnthalpy;
    }

    /**
     * <p>
     * Setter for the field <code>referenceEnthalpy</code>.
     * </p>
     *
     * @param referenceEnthalpy the referenceEnthalpy to set
     */
    public void setReferenceEnthalpy(double referenceEnthalpy) {
        this.referenceEnthalpy = referenceEnthalpy;
    }

    /** {@inheritDoc} */
    @Override
    public double getCpA() {
        return CpA;
    }

    /** {@inheritDoc} */
    @Override
    public void setCpA(double CpA) {
        this.CpA = CpA;
    }

    /** {@inheritDoc} */
    @Override
    public double getCpB() {
        return CpB;
    }

    /** {@inheritDoc} */
    @Override
    public void setCpB(double CpB) {
        this.CpB = CpB;
    }

    /** {@inheritDoc} */
    @Override
    public double getCpC() {
        return CpC;
    }

    /** {@inheritDoc} */
    @Override
    public void setCpC(double CpC) {
        this.CpC = CpC;
    }

    /** {@inheritDoc} */
    @Override
    public double getCpD() {
        return CpD + 1e-10;
    }

    /** {@inheritDoc} */
    @Override
    public void setCpD(double CpD) {
        this.CpD = CpD;
    }

    /** {@inheritDoc} */
    @Override
    public String getFormulae() {
        return formulae;
    }

    /** {@inheritDoc} */
    @Override
    public void setFormulae(String formulae) {
        this.formulae = formulae;
    }

    /** {@inheritDoc} */
    @Override
    public String getCASnumber() {
        return CASnumber;
    }

    /** {@inheritDoc} */
    @Override
    public void setCASnumber(String CASnumber) {
        this.CASnumber = CASnumber;
    }

    /** {@inheritDoc} */
    @Override
    public int getOrginalNumberOfAssociationSites() {
        return orginalNumberOfAssociationSites;
    }

    /** {@inheritDoc} */
    @Override
    public double getdrhodN() {
        return molarMass;
    }

    /** {@inheritDoc} */
    @Override
    public double getCpE() {
        return CpE;
    }

    /** {@inheritDoc} */
    @Override
    public void setCpE(double CpE) {
        this.CpE = CpE;
    }

    /** {@inheritDoc} */
    @Override
    public double getRacketZCPA() {
        return racketZCPA;
    }

    /** {@inheritDoc} */
    @Override
    public void setRacketZCPA(double racketZCPA) {
        this.racketZCPA = racketZCPA;
    }

    /** {@inheritDoc} */
    @Override
    public double getVolumeCorrectionT() {
        return volumeCorrectionT;
    }

    /**
     * <p>
     * getVolumeCorrection.
     * </p>
     *
     * @return a double
     */
    public double getVolumeCorrection() {
        return 0.0;
    }

    /** {@inheritDoc} */
    @Override
    public void setVolumeCorrectionConst(double getVolumeCorrectionConst) {
        this.volumeCorrectionConst = getVolumeCorrectionConst;
    }

    /** {@inheritDoc} */
    @Override
    public void setVolumeCorrectionT(double volumeCorrectionT) {
        this.volumeCorrectionT = volumeCorrectionT;
    }

    /** {@inheritDoc} */
    @Override
    public double getVolumeCorrectionT_CPA() {
        return volumeCorrectionT_CPA;
    }

    /** {@inheritDoc} */
    @Override
    public void setVolumeCorrectionT_CPA(double volumeCorrectionT_CPA) {
        this.volumeCorrectionT_CPA = volumeCorrectionT_CPA;
    }

    /** {@inheritDoc} */
    @Override
    public void setIdealGasEnthalpyOfFormation(double idealGasEnthalpyOfFormation) {
        this.idealGasEnthalpyOfFormation = idealGasEnthalpyOfFormation;
    }

    /**
     * {@inheritDoc}
     *
     * method to return flow rate of component
     */
    @Override
    public double getFlowRate(String flowunit) {
        if (flowunit.equals("kg/sec")) {
            return numberOfMolesInPhase * getMolarMass();
        } else if (flowunit.equals("kg/min")) {
            return numberOfMolesInPhase * getMolarMass() * 60.0;
        } else if (flowunit.equals("kg/hr")) {
            return numberOfMolesInPhase * getMolarMass() * 3600.0;
        } else if (flowunit.equals("m3/hr")) {
            return getVoli() / 1.0e5 * 3600.0;
        } else if (flowunit.equals("m3/min")) {
            return getVoli() / 1.0e5 * 60.0;
        } else if (flowunit.equals("m3/sec")) {
            return getVoli() / 1.0e5;
        } else if (flowunit.equals("mole/sec")) {
            return numberOfMolesInPhase;
        } else if (flowunit.equals("mole/min")) {
            return numberOfMolesInPhase * 60.0;
        } else if (flowunit.equals("mole/hr")) {
            return numberOfMolesInPhase * 3600.0;
        } else {
            throw new RuntimeException("failed.. unit: " + flowunit + " not suported");
        }
    }

    /**
     * {@inheritDoc}
     *
     * method to return total flow rate of component
     */
    @Override
    public double getTotalFlowRate(String flowunit) {
        if (flowunit.equals("kg/sec")) {
            return numberOfMoles * getMolarMass();
        } else if (flowunit.equals("kg/min")) {
            return numberOfMoles * getMolarMass() * 60.0;
        } else if (flowunit.equals("kg/hr")) {
            return numberOfMoles * getMolarMass() * 3600.0;
        } else if (flowunit.equals("mole/sec")) {
            return numberOfMoles;
        } else if (flowunit.equals("mole/min")) {
            return numberOfMoles * 60.0;
        } else if (flowunit.equals("mole/hr")) {
            return numberOfMoles * 3600.0;
        } else {
            throw new RuntimeException("failed.. unit: " + flowunit + " not suported");
        }
    }
}
