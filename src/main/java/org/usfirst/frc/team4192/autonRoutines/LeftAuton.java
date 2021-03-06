package org.usfirst.frc.team4192.autonRoutines;

import edu.wpi.first.wpilibj.command.CommandGroup;
import org.usfirst.frc.team4192.commands.ActuatorInitialize;
import org.usfirst.frc.team4192.commands.Turn;
import org.usfirst.frc.team4192.commands.Move;

/**
 * Created by Al on 2/8/2017.
 */
public class LeftAuton extends CommandGroup {
  public LeftAuton() {
    addParallel(new ActuatorInitialize(), 10);
    addSequential(new Move(50), 3.5);
    addSequential(new Turn(70), 2);
    addSequential(new Move(66), 5);
  }
}
