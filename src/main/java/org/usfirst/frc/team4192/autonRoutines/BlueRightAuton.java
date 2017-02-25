package org.usfirst.frc.team4192.autonRoutines;

import edu.wpi.first.wpilibj.command.CommandGroup;
import org.usfirst.frc.team4192.commands.DriveOn;
import org.usfirst.frc.team4192.commands.Turn;

/**
 * Created by Al on 2/8/2017.
 */
public class BlueRightAuton extends CommandGroup {
  public BlueRightAuton() {
    addSequential(new DriveOn(120));
    addSequential(new Turn(45));
  }
}
