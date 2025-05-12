package gov.mil.otc._3dvis.vmf;

import java.util.Calendar;

/**
 * The VMF K05.19 Entity Data Class
 */
public class K0519 extends VmfMessage {

    /**
     * {@inheritDoc}
     */
    protected K0519(Header header, VmfDataBuffer data, Calendar collectTime, String collector) {
        super(header, data, collectTime, collector);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final boolean parse(VmfDataBuffer data) {

        int graphicalReferenceEntityType = data.getInt(4);

        //R1
        boolean griR1;
        do {
            griR1 = data.getGri();
            int urn = data.getInt(24);
            int year = data.getInt(7) + 2000;
            int month = data.getInt(4);
            int dayOfMonth = data.getInt(5);
            int hour = data.getInt(5);
            int minute = data.getInt(6);
            int second = data.getInt(6);
            int entityIdSerialNumber = data.getFpiInt(32);
            int actionDesignator = data.getInt(2);
            int securityClassification = data.getInt(4);

            //G1
            if (data.getGpi()) {
                int symbolCode = data.getFpiInt(105);

                //G2
                if (data.getGpi()) {
                    int identity = data.getInt(4);
                    int dimension = data.getInt(6);
                    int entityType = data.getInt(6);
                    //FPI code here
                    int entitySubtype = data.getFpiInt(6);
                    //FPI Code here
                    int entitySizeMobility = data.getFpiInt(8);
                    //FPI Code here
                    int entityStatus = data.getFpiInt(1);
                    int nationality = data.getFpiInt(9);

                    if (data.getFpi()) {
                        //R2
                        boolean griR2;
                        do {
                            griR2 = data.getGri();
                            int additionalinfomation = data.getInt(140);
                        } while (griR2);
                    }

                    if (data.getFpi()) {
                        //R3
                        boolean griR3;
                        do {
                            griR3 = data.getGri();
                            int uniquedesignation = data.getInt(245);
                        } while (griR3);
                    }

                    //STAFFCOMMENT CODES
                    String staffComments = data.getFpiString(140);
                }
            }

            //G3
            if (data.getGpi()) {
                int fireMissionType = data.getFpiInt(4);
                int targetNumber = data.getFpiInt(28);

                //G4
                if (data.getGpi()) {
                    int engagementType = data.getInt(2);
                    int fireMissionUrn = data.getFpiInt(24);

                    //G5
                    if (data.getGpi()) {

                        //R4
                        boolean griR4;
                        do {
                            griR4 = data.getGri();
                            int targetLatitude = data.getInt(25);
                            int targetLongitude = data.getInt(26);
                            int targetNumberExtention = data.getFpiInt(4);
                        } while (griR4);
                    }

                    //G6
                    if (data.getGpi()) {
                        int dayOnTarget = data.getInt(5);
                        int hourOnTarget = data.getInt(5);
                        int minuteOnTarget = data.getInt(6);
                    }

                    //G7
                    if (data.getGpi()) {
                        //G8
                        if (data.getGpi()) {
                            int length = data.getInt(14);
                            int width = data.getInt(14);
                            int attitude = data.getInt(13);
                        }
                        int radius = data.getFpiInt(14);
                    }
                }
            }

            //G9
            if (data.getGpi()) {
                int entityAxisOrientation = data.getFpiInt(9);

                //R5
                boolean griR5;
                do {
                    griR5 = data.getGri();
                    int obstacleTimeFunction = data.getInt(3);
                    int obstacleYear = data.getInt(7);
                    int obstacleMonth = data.getInt(4);
                    int obstacleDayOfMonth = data.getInt(5);
                    int obstacleHour = data.getInt(5);
                    int obstacleMinute = data.getInt(6);
                    int obstacleSecond = data.getFpiInt(6);
                } while (griR5);

                int impactOnMovement = data.getFpiInt(3);
                int obstacleHeight = data.getFpiInt(10);
                int obstacleDepth = data.getFpiInt(5);
                int obstacleLength = data.getFpiInt(10);
                int obstacleWidth = data.getFpiInt(10);

                //R6
                boolean griR6;
                do {
                    griR6 = data.getGri();
                    int obstacleLatitude = data.getInt(25);
                    int obstacleLongitude = data.getInt(26);
                } while (griR6);

                int enemyActivity = data.getFpiInt(6);
                int zoneMarking = data.getFpiInt(4);
                int obstacleUrn = data.getFpiInt(24);
                int obstacleType = data.getFpiInt(7);
                int obstacleStatus = data.getFpiInt(5);

                //G10
                if (data.getGpi()) {
                    int safeLaneStatus = data.getInt(1);

                    //R7
                    boolean griR7;
                    do {
                        griR7 = data.getGri();
                        int safeLandLattitude = data.getInt(31);
                        int safeLaneLongitude = data.getInt(32);
                        int safeLandWidth = data.getInt(7);
                    } while (griR7);

                    String symbolCode = data.getFpiString(105);
                }
            }

            //G11
            if (data.getGpi()) {
                int warningIndicator = data.getInt(1);
                int strikeYear = data.getInt(7);
                int strikeMonth = data.getInt(4);
                int strikeDay = data.getInt(5);
                int strikeHour = data.getInt(5);
                int strikeMinute = data.getInt(6);

                //G12
                if (data.getGpi()) {
                    int minimumSafeDistance1Radius = data.getFpiInt(10);
                    int minimumSafeDistance2Radius = data.getFpiInt(10);

                    //G13
                    if (data.getGpi()) {

                        //R8
                        boolean griR8;
                        do {
                            griR8 = data.getGri();
                            int groundZeroLatitude = data.getInt(25);
                            int groundZeroLongitude = data.getInt(26);
                        } while (griR8);
                    }
                }
            }

            //G14
            if (data.getGpi()) {
                int threatType = data.getFpiInt(4);
                int threatPostureVmf = data.getInt(3);

                //G15
                if (data.getGpi()) {
                    int threadWarningYear = data.getInt(7) + 2000;
                    int threadWarningMonth = data.getInt(4);
                    int threadWarningDayOfMonth = data.getInt(5);
                    int threadWarningHour = data.getInt(5);
                    int threadWarningMinute = data.getInt(6);
                    int threadWarningSecond = data.getFpiInt(6);
                }

                //G16
                if (data.getGpi()) {
                    int threatLatitude0051Minute = data.getInt(21);
                    int threatLongitude0051Minute = data.getInt(22);
                    int threatAltitude25Ft = data.getFpiInt(13);
                    int getFpiIntStrength = data.getFpiInt(4);
                    int getFpiIntCourse = data.getFpiInt(9);
                    int getFpiIntSpeed = data.getFpiInt(11);
                }

                //G17
                if (data.getGpi()) {

                    //R9
                    boolean griR9;
                    do {
                        griR9 = data.getGri();
                        int impactLatitude0103Minute = data.getInt(20);
                        int impactLongitude0103Minute = data.getInt(21);
                    } while (griR9);

                    //G18
                    if (data.getGpi()) {
                        int impactSquareCircleSwitch = data.getInt(2);
                        int impactAxisOrientation = data.getInt(8);
                        int impactAreaMajorAxis4 = data.getInt(7);
                        int impactAreaMinorAxis4 = data.getInt(7);
                    }
                }
            }

            //G19
            if (data.getGpi()) {
                int nbcEventType = data.getInt(4);
                int TypeOfNuclearBurst = data.getFpiInt(3);

                //R10
                boolean griR10;
                do {
                    griR10 = data.getGri();
                    int nbcTimeFunctionNbc = data.getInt(4);
                    int nbcDayOfMonth = data.getInt(5);
                    int nbcHour = data.getInt(5);
                    int nbcMinute = data.getInt(6);
                } while (griR10);

                //G20
                if (data.getGpi()) {
                    int nbcLocationQualifier = data.getInt(1);
                    int AttackLocationLatitude = data.getInt(25);
                    int AttackLocationLongitude = data.getInt(26);

                    //G21
                    if (data.getGpi()) {
                        int TerrainDescription = data.getFpiInt(4);
                        int VegetationType = data.getFpiInt(3);

                        //R11
                        boolean griR11;
                        do {
                            griR11 = data.getGri();
                            int AgentType = data.getFpiInt(5);
                            int AgentName = data.getFpiInt(6);
                            int AgentPersistencyType = data.getFpiInt(2);

                            if (data.getFpi()) {

                                //R12
                                boolean griR12;
                                do {
                                    griR12 = data.getGri();
                                    int TypeOfDetection = data.getInt(4);
                                } while (griR12);
                            }
                        } while (griR11);
                    }
                }
            }

            //G22
            if (data.getGpi()) {
                int bridgeType = data.getFpiInt(3);

                //R13
                boolean griR13;
                do {
                    griR13 = data.getGri();
                    int bridgeLatitude = data.getInt(23);
                    int bridgeLongitude = data.getInt(24);
                } while (griR13);

                int controllingForce = data.getInt(2);
                int widthOfBridge = data.getFpiInt(10);
                int bridgeWeightClassification = data.getFpiInt(10);
            }

            //G23
            if (data.getGpi()) {
                int supplyPointReference = data.getInt(24);
                int supplyPointType = data.getInt(5);

                //G24
                if (data.getGpi()) {

                    //R14
                    boolean griR14;
                    do {
                        griR14 = data.getGri();
                        int supplyPointLatitude = data.getInt(25);
                        int supplyPointLongitude = data.getInt(26);
                    } while (griR14);
                }

                //G25
                if (data.getGpi()) {

                    //R15
                    boolean griR15;
                    do {
                        griR15 = data.getGri();
                        int supplyPointMonth = data.getInt(4);
                        int supplyPointDayOfMonth = data.getInt(5);
                        int supplyPointHour = data.getInt(5);
                        int supplyPointMinute = data.getInt(6);
                    } while (griR15);
                }
            }

            //G26
            if (data.getGpi()) {
                int locationDerivation = data.getInt(4);
                int EplrsRsId = data.getFpiInt(14);
                int observedLatitude = data.getInt(23);
                int observedLongitude = data.getInt(24);
                int observedLocationQuality = data.getFpiInt(4);

                //G27
                if (data.getGpi()) {
                    int course = data.getInt(9);
                    int unitSpeed = data.getInt(11);
                }

                int activity = data.getFpiInt(6);
                int elevation = data.getFpiInt(17);
                int altitude = data.getFpiInt(13);
                int quantityOfEquipmentWeaponsObserved = data.getFpiInt(14);
                int actionTaken = data.getFpiInt(6);
            }

            //G28
            if (data.getGpi()) {
                int entityCombatStatusUrn = data.getFpiInt(24);
                int entityCombatStatus = data.getInt(2);
            }

            //G29
            if (data.getGpi()) {
                int MedevacMissionType = data.getInt(2);
                int MedevacRequestNumber = data.getFpiInt(35);

                //G30
                if (data.getGpi()) {
                    int casualtyPickUpLatitude = data.getInt(21);
                    int casualtyPickUpLongitude = data.getInt(22);
                }

                //G31
                if (data.getGpi()) {
                    int pickUpDay = data.getInt(5);
                    int pickUpHour = data.getInt(5);
                    int pickUpMinute = data.getInt(6);
                }

                int zoneMarking = data.getFpiInt(4);
                int zoneHot = data.getInt(1);
            }

            //G32
            if (data.getGpi()) {
                int Dimension = data.getInt(6);
                int EntityType = data.getInt(6);
                int EntitySubtype = data.getFpiInt(6);
                int EntityLatitude = data.getInt(25);
                int EntityLongitude = data.getInt(26);
                int IedStatus = data.getInt(2);
                int StaffComments = data.getFpiInt(140);
            }

            //G33
            if (data.getGpi()) {

                //R16
                boolean griR16;
                do {
                    griR16 = data.getGri();
                    int bypassLatitude = data.getInt(25);
                    int bypassLongitude = data.getInt(26);
                } while (griR16);

                int bypassPotential = data.getFpiInt(2);
                int bypassSymbolCode = data.getFpiInt(105);
            }

            //G34
            if (data.getGpi()) {
                int maydayLatitude = data.getInt(23);
                int maydayLongitude = data.getInt(24);
                int maydayElevation = data.getFpiInt(17);
                int maydayPointLocationElevation = data.getFpiInt(22);
                int maydayAltitude = data.getFpiInt(13);
                int maydayUrn = data.getFpiInt(24);
                int maydayEmergencyType = data.getFpiInt(4);

                //G35
                if (data.getGpi()) {

                    //R17
                    boolean griR17;
                    do {
                        griR17 = data.getGri();
                        int IdentityIsolatedPersonnel = data.getInt(3);
                        int IsolatedPersonnelClassification = data.getFpiInt(4);
                        int NationalityOfIsolatedPersons = data.getFpiInt(9);
                    } while (griR17);
                }
            }
        } while (griR1);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return header.toString() + System.lineSeparator() + "Entity Data";
    }
}
