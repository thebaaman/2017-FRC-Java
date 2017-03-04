package org.usfirst.frc.team4192.commands;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import org.usfirst.frc.team4192.Robot;

/**
 * Created by Al on 2/9/2017.
 */
public class Turn extends Command {
  private double gyroTarget;
  
  public Turn(double degrees) {
    gyroTarget = degrees;
  }
  
  public Turn(double degrees, double p, double i, double d) {
    super(5);
    gyroTarget = degrees;
    Robot.turnController.setPID(p, i, d);
    Robot.turnController.reset();
  }
  
  public void initialize() {
    Robot.jankoDrive.prepareForTeleop();
    Robot.zeroSensors();
    Robot.turnController.enable();
  }
  
  public void execute() {
    Robot.turnController.setSetpoint(gyroTarget);
    SmartDashboard.putString("turnOut", ""+Robot.turnController.get());
    SmartDashboard.putBoolean("turnController Enabled", Robot.turnController.isEnabled());
  }
  
  public void end() {
    Robot.frontLeft.set(0);
    Robot.frontRight.set(0);
    Robot.turnController.disable();
  }
  
  public void interrupted() {
    end();
  }
  
  @Override
  public boolean isFinished() {
    return Robot.turnController.onTarget();
  }
}