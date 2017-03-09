package org.usfirst.frc.team4192;

import com.ctre.CANTalon;
import com.kauailabs.navx.frc.AHRS;
import edu.wpi.first.wpilibj.*;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team4192.autonRoutines.*;
import org.usfirst.frc.team4192.utilities.CollisionDetector;
import org.usfirst.frc.team4192.utilities.JaggernautJoystick;

/**
 * Created by Al on 1/22/2017.
 */
public class Robot extends IterativeRobot {
  public static CANTalon leftMaster       = new CANTalon(JankoConstants.leftMasterID);
  public static CANTalon rightMaster      = new CANTalon(JankoConstants.rightMasterID);
  private static CANTalon leftSlave       = new CANTalon(JankoConstants.leftSlaveID);
  private static CANTalon rightSlave      = new CANTalon(JankoConstants.rightSlaveID);
  private static CANTalon flywheel         = new CANTalon(JankoConstants.flywheelID);
  private static CANTalon agitator         = new CANTalon(JankoConstants.agitatorID);
  private static CANTalon lift            = new CANTalon(JankoConstants.liftID);
  private static CANTalon trigger         = new CANTalon(JankoConstants.triggerID);
  private static VictorSP intake           = new VictorSP(JankoConstants.intakeID);
  public static JankoDrive jankoDrive     = new JankoDrive(leftMaster, leftSlave, rightMaster, rightSlave);
  
  private static double driveSensitivity = 0.8;
  
  private JaggernautJoystick joystick = new JaggernautJoystick(JankoConstants.joystick);
  private static AHRS ahrs = new AHRS(SPI.Port.kMXP);          // the NavX board
  
  private static double gyroKp;      // Gyroscope PID constants
  private static double gyroKi;
  private static double gyroKd;
  
  private static double driveKp;     // Drive PID constants
  private static double driveKi;
  private static double drivekd;
  
  private static double flywheelKp;  // Drive PID constants
  private static double flywheelKi;
  private static double flywheelKd;
  private static double flywheelKf;
  private static double flywheelTargetRPM;
  
  public static PIDController turnController = new PIDController(0.01, 0.0, 0, 0.0, ahrs, jankoDrive);
  
  private RedLeftAuton redLeftAuton = new RedLeftAuton();
  private RedMiddleAuton redMiddleAuton = new RedMiddleAuton();
  private RedRightAuton redRightAuton = new RedRightAuton();
  private BlueLeftAuton blueLeftAuton = new BlueLeftAuton();
  private BlueMiddleAuton blueMiddleAuton = new BlueMiddleAuton();
  private BlueRightAuton blueRightAuton = new BlueRightAuton();
  
  private CollisionDetector collisionDetector = new CollisionDetector(ahrs);
  
  ////// End Instance Variables //////
  
  private void setDriveConstants() {
    jankoDrive.setPID(drivekd, driveKi, drivekd);
  }
  
  private void setGyroConstants() {
    turnController.setPID(gyroKp, gyroKi, gyroKd);
  }
  
  private void setFlywheelConstants() {
    flywheel.setP(flywheelKp);
    flywheel.setI(flywheelKi);
    flywheel.setD(flywheelKd);
    flywheel.setF(flywheelKf);
  }
  
  // updates all the drive pid constants to what they are on the dashboard
  private void updateDriveConstants() {
    driveKp = SmartDashboard.getNumber("driveP", JankoConstants.defaultDriveKp);
    driveKi = SmartDashboard.getNumber("driveI", JankoConstants.defaultDriveKi);
    drivekd = SmartDashboard.getNumber("driveD", JankoConstants.defaultDriveKd);
    setDriveConstants();
  }
  
  // updates all the gyro pid constants to what they are on the dashboard
  private void updateGyroConstants() {
    gyroKp = SmartDashboard.getNumber("gyroP", JankoConstants.defaultGyroKp);
    gyroKi = SmartDashboard.getNumber("gyroI", JankoConstants.defaultGyroKi);
    gyroKd = SmartDashboard.getNumber("gyroD", JankoConstants.defaultGyroKd);
    setGyroConstants();
  }
  
  // updates all the flywheelID pid constants to what they are on the dashboard
  private void updateFlywheelConstants() {
    flywheelKp = SmartDashboard.getNumber("flywheelP", JankoConstants.defaultFlywheelKp);
    flywheelKi = SmartDashboard.getNumber("flywheelI", JankoConstants.defaultFlywheelKi);
    flywheelKd = SmartDashboard.getNumber("flywheelD", JankoConstants.defaultFlywheelKd);
    flywheelKf = SmartDashboard.getNumber("flywheelF", JankoConstants.defaultFlywheelKf);
    setFlywheelConstants();
  }
  
  // calls the three constants update functions
  private void updatePIDConstants() {
    updateDriveConstants();
    updateGyroConstants();
    updateFlywheelConstants();
  }
  
  private void setFlywheelTargetRPM() {
    flywheel.setSetpoint(flywheelTargetRPM);
  }
  
  private void updateFlywheelTargetRPM() {
    flywheelTargetRPM = SmartDashboard.getNumber("targetRPMControl", 0.0);
    setFlywheelTargetRPM();
  }
  
  public static void zeroSensors() {
    ahrs.reset();
  }
  
  @Override
  public void robotInit() {
    jankoDrive.setSlewRate(60);
    driveSensitivity = 0.8;
  
    flywheel.changeControlMode(CANTalon.TalonControlMode.Speed);
    flywheel.setFeedbackDevice(CANTalon.FeedbackDevice.CtreMagEncoder_Relative);
    flywheel.setProfile(0);
    
    DriverStation.reportWarning("instantiated navX MXP:  ", false);
    SmartDashboard.putBoolean("gyroPIDExists", true);
    
    turnController.setInputRange(-180.0, 180.0);
    turnController.setOutputRange(-1.0, 1.0);
    turnController.setAbsoluteTolerance(3.0);
    turnController.setContinuous(true);
    turnController.disable();

    updatePIDConstants();

    Thread flywheelControlThread = new Thread(() -> {
      while (!Thread.interrupted()) {
        updateFlywheelConstants();
        updateFlywheelTargetRPM();
      }
    });
    flywheelControlThread.start();
    
    Thread dashboardUpdateThread = new Thread(() -> {
      while (!Thread.interrupted()) {
        SmartDashboard.putNumber("actualHeading", ahrs.getAngle());
        SmartDashboard.putNumber("leftActualRPM", flywheel.getEncVelocity());
        SmartDashboard.putNumber("targetRPM", flywheelTargetRPM);
        SmartDashboard.putNumber("Left Encoder Value", jankoDrive.getLeftValue()/4096);
        SmartDashboard.putNumber("Right Encoder Value", jankoDrive.getRightValue()/4096);
      }
    });
    dashboardUpdateThread.start();
  }
  
  @Override
  public void disabledInit() {
    
    
  }
  
  @Override
  public void disabledPeriodic() {
    super.disabledPeriodic();
  }
  
  @Override
  public void autonomousInit() {
    zeroSensors();
    
    updatePIDConstants();
    
    switch (SmartDashboard.getString("Selected Autonomous", "default")) {
      case "Red Left":
        redLeftAuton.start();
        break;
        
      case "Red Middle":
        redMiddleAuton.start();
        break;
      
      case "Red Right":
        redRightAuton.start();
        break;
      
      case "Blue Left":
        blueLeftAuton.start();
        break;
      
      case "Blue Middle":
        blueMiddleAuton.start();
        break;
        
      case "Blue Right":
        blueRightAuton.start();
        break;
      case "default":
        break;
      default:
        break;
    }
  }
  
  @Override
  public void autonomousPeriodic() {
    Scheduler.getInstance().run();
  }
  
  
  private void sensitivityControl() {
    if (joystick.isHeldDown(JankoConstants.sensitivityButton)) driveSensitivity = 0.5;
    else driveSensitivity = 0.9;
  }
  
  private void triggerControl() {
    if (joystick.getAxis(3) > 0.5)
      trigger.set(1);
    else
      trigger.set(0);
  }
  
  private void driveControl() {
    jankoDrive.arcadeDrive(-joystick.getYaxis()*driveSensitivity, -joystick.getXaxis()*driveSensitivity, true);
  }
  
  private void intakeControl() {
    if (joystick.isHeldDown(JankoConstants.intakeIn))
      intake.set(1);
    else if (joystick.isHeldDown(JankoConstants.intakeOut))
      intake.set(-1);
    else
      intake.set(0);
  }
  
  private void liftControl() {
    if (joystick.isHeldDown(JankoConstants.climberUp))
      lift.set(1);
    else
      lift.set(0);
  }
  
  private void flywheelControl() {
    updateFlywheelConstants();
    if (joystick.buttonPressed(JankoConstants.flywheelToggle)) {
      if (flywheel.isEnabled())
        flywheel.disable();
      else {
        flywheel.enable();
        updateFlywheelTargetRPM();
      }
    }
  }
  
  private void agitatorControl() {
    if (joystick.buttonPressed(JankoConstants.agitatorToggle)) {
      if (agitator.isEnabled())
        agitator.disable();
      else
        agitator.set(1);
    }
  }
  
  private void collisionRumble() {
    if (collisionDetector.isCollisionDetected())
      joystick.rumble();
  }
  
  @Override
  public void teleopInit() {
    zeroSensors();
    
    if (turnController.isEnabled())
      turnController.disable();
    
    jankoDrive.prepareForTeleop();
  }
  
  @Override
  public void teleopPeriodic() {
    joystick.update();
    intakeControl();
    flywheelControl();
    sensitivityControl();
    driveControl();
    liftControl();
    agitatorControl();
    triggerControl();
  }
  
  public static boolean gyroOnTarget() {
    return turnController.onTarget();
  }
}